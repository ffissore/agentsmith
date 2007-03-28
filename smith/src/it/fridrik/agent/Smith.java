/*
 * Agent Smith - A java hot class redefinition implementation
 * Copyright (C) 2007 Federico Fissore
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.fridrik.agent;

import it.fridrik.filemonitor.FileEvent;
import it.fridrik.filemonitor.FileModifiedListener;
import it.fridrik.filemonitor.FileMonitor;
import it.fridrik.filemonitor.JarEvent;
import it.fridrik.filemonitor.JarModifiedListener;
import it.fridrik.filemonitor.JarMonitor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Agent Smith is an agent with just one aim: redefining classes as soon as they
 * are changed. Smith bundles together Instrumentation, FileMonitor and
 * JarMonitor
 * 
 * @author Federico Fissore (federico@fsfe.org)
 * @see FileMonitor
 * @see JarMonitor
 */
public class Smith implements FileModifiedListener, JarModifiedListener {

	private static Logger log = Logger.getLogger("Smith");

	/** Lists of active Smith agents */
	private static Vector<Smith> smiths = new Vector<Smith>();

	/** Called when the agent is initialized via command line */
	public static void premain(String agentArgs, Instrumentation inst) {
		initialize(agentArgs, inst);
	}

	/** Called when the agent is initialized after the jvm startup */
	public static void agentmain(String agentArgs, Instrumentation inst) {
		initialize(agentArgs, inst);
	}

	private static void initialize(String agentArgs, Instrumentation inst) {
		SmithArgs args = new SmithArgs(agentArgs);

		if (!args.isValid()) {
			throw new RuntimeException(
					"Your parameters are invalid! Check the documentation for the correct syntax");
		}

		Smith smith = new Smith(inst, args);
		smiths.add(smith);
	}

	/** Stops all active Smith agents */
	public static void stopAll() {
		for (Smith smith : smiths) {
			smith.stop();
		}
	}

	private final Instrumentation inst;
	private final String classFolder;
	private final String jarFolder;
	private final ScheduledExecutorService service;

	/**
	 * Creates and starts a new Smith agent. Please note that periods smaller than
	 * 500 (milliseconds) won't be considered.
	 * 
	 * @param inst
	 *          the instrumentation implementation
	 * @param args
	 *          the {@link SmithArgs} instance
	 */
	public Smith(Instrumentation inst, SmithArgs args) {
		this.inst = inst;
		this.classFolder = args.getClassFolder();
		this.jarFolder = args.getJarFolder();
		int monitorPeriod = 500;
		if (args.getPeriod() > monitorPeriod) {
			monitorPeriod = args.getPeriod();
		}

		service = Executors.newScheduledThreadPool(2);

		FileMonitor fileMonitor = new FileMonitor(classFolder, "class");
		fileMonitor.addModifiedListener(this);
		service.scheduleWithFixedDelay(fileMonitor, 0, monitorPeriod,
				TimeUnit.MILLISECONDS);

		if (jarFolder != null) {
			JarMonitor jarMonitor = new JarMonitor(jarFolder);
			jarMonitor.addJarModifiedListener(this);
			service.scheduleWithFixedDelay(jarMonitor, 0, monitorPeriod,
					TimeUnit.MILLISECONDS);
		}

		log.info("Smith: watching class folder: " + classFolder);
		log.info("Smith: watching jars folder: " + jarFolder);
		log.info("Smith: period between checks (ms): " + monitorPeriod);
	}

	/**
	 * Stops this Smith agent
	 */
	public void stop() {
		service.shutdown();
	}

	/**
	 * When the monitor notifies of a changed class file, Smith will redefine it
	 */
	public void fileModified(FileEvent event) {
		String fileName = event.getSource();
		try {
			redefineClass(toClassName(fileName), new FileInputStream(new File(
					classFolder + fileName)));
		} catch (Exception e) {
			log.log(Level.SEVERE, "error", e);
		}
	}

	/**
	 * When the monitor notifies of a changed jar file, Smith will redefine the
	 * changed class file the jar contains
	 */
	public void jarModified(JarEvent event) {
		JarFile jar = event.getSource();
		String entryName = event.getEntryName();
		try {
			redefineClass(toClassName(entryName), jar.getInputStream(getJarEntry(jar,
					entryName)));
		} catch (Exception e) {
			log.log(Level.SEVERE, "error", e);
		}
	}

	/**
	 * Redefines the specified class
	 * 
	 * @param className
	 *          the class name to redefine
	 * @param classStream
	 *          the inputstream to the class file
	 * @throws IOException
	 *           if the inputstream is someway unreadable
	 * @throws ClassNotFoundException
	 *           if the class name cannot be found
	 * @throws UnmodifiableClassException
	 *           if the class is unmodifiable
	 */
	protected void redefineClass(String className, InputStream classStream)
			throws IOException, ClassNotFoundException, UnmodifiableClassException {
		Class[] loadedClasses = inst.getAllLoadedClasses();
		for (Class<?> clazz : loadedClasses) {
			if (clazz.getName().equals(className)) {
				ClassDefinition definition = new ClassDefinition(clazz,
						toByteArray(classStream));
				inst.redefineClasses(new ClassDefinition[] { definition });
			}
		}
	}

	/**
	 * Converts an absolute path to a file to a fully qualified class name
	 * 
	 * @param fileName
	 *          the absolute path of the class file
	 * @return a fully qualified class name
	 */
	private static String toClassName(String fileName) {
		return fileName.replace(".class", "").replace(File.separatorChar, '.');
	}

	/**
	 * Gets the specified jar entry from the specified jar file
	 * 
	 * @param jar
	 *          the jar file that contains the jar entry
	 * @param entryName
	 *          the name of the entry contained in the jar file
	 * @return a JarEntry
	 * @throws IllegalArgumentException
	 *           if the specified entryname is not contained in the specified jar
	 *           file
	 */
	private static JarEntry getJarEntry(JarFile jar, String entryName) {
		JarEntry entry = null;
		for (Enumeration<JarEntry> entries = jar.entries(); entries
				.hasMoreElements();) {
			entry = entries.nextElement();
			if (entry.getName().equals(entryName)) {
				return entry;
			}
		}
		throw new IllegalArgumentException("EntryName " + entryName
				+ " does not exist in jar " + jar);
	}

	/**
	 * Loads .class files as byte[]
	 * 
	 * @param is
	 *          the inputstream of the bytes to load
	 * @return a byte[]
	 * @throws IOException
	 *           if an error occurs while reading file
	 */
	private static byte[] toByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		while ((bytesRead = is.read(buffer)) != -1) {
			byte[] tmp = new byte[bytesRead];
			System.arraycopy(buffer, 0, tmp, 0, bytesRead);
			baos.write(tmp);
		}

		byte[] result = baos.toByteArray();

		baos.close();
		is.close();

		return result;
	}
}
