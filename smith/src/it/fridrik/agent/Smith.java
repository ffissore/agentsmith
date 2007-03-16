/*
 * Agent Smith - A java hot class redefinition implementation
 * Copyright (C) 2007  Federico Fissore
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.fridrik.agent;

import it.fridrik.filemonitor.FileEvent;
import it.fridrik.filemonitor.FileModifiedListener;
import it.fridrik.filemonitor.FileMonitor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
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
 * are changed by the developer, therefore making java look more like a
 * scripting language than a compiled one. The Smith class bundles together
 * Instrumentation and FileMonitor
 * 
 * @author Federico Fissore (federico@fsfe.org)
 * @see FileMonitor
 */
public class Smith implements FileModifiedListener {

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

		scheduleMonitor(classFolder, "class", monitorPeriod);

		if (jarFolder != null) {
			scheduleMonitor(jarFolder, "jar", monitorPeriod);
		}

		log.info("Smith: watching class folder: " + classFolder);
		log.info("Smith: watching jars folder: " + jarFolder);
		log.info("Smith: period between checks (ms): " + monitorPeriod);
	}

	private void scheduleMonitor(String classFolder, String ext, int monitorPeriod) {
		FileMonitor fileMonitor = new FileMonitor(classFolder, ext);
		fileMonitor.addModifiedListener(this);
		service.scheduleWithFixedDelay(fileMonitor, 0, monitorPeriod,
				TimeUnit.MILLISECONDS);
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

		if (fileName.endsWith(".class")) {
			redefineClassFile(fileName);
		} else if (fileName.endsWith(".jar")) {
			redefineJarFile(fileName);
		}
	}

	private void redefineJarFile(String fileName) {
		String jarName = jarFolder + fileName;
		try {
			JarFile jar = new JarFile(jarName);
			Class[] loadedClasses = inst.getAllLoadedClasses();
			JarEntry jarEntry = null;
			for (Enumeration<JarEntry> entries = jar.entries(); entries
					.hasMoreElements();) {
				jarEntry = entries.nextElement();
				if (jarEntry.getName().endsWith(".class")) {
					for (Class<?> clazz : loadedClasses) {
						if (clazz.getName().equals(
								jarEntry.getName().replace(".class", "").replace(
										File.separatorChar, '.'))) {
							try {
								InputStream inputStream = jar.getInputStream(jarEntry);
								ClassDefinition definition = new ClassDefinition(clazz,
										toByteArray(inputStream));
								inst.redefineClasses(new ClassDefinition[] { definition });
							} catch (Exception e) {
								log.log(Level.SEVERE, "error", e);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "error", e);
		}

	}

	private void redefineClassFile(String fileName) {
		String className = fileName.replace(".class", "").replace(
				File.separatorChar, '.');

		Class[] loadedClasses = inst.getAllLoadedClasses();

		for (Class<?> clazz : loadedClasses) {
			if (clazz.getName().equals(className)) {
				try {
					ClassDefinition definition = new ClassDefinition(
							clazz,
							toByteArray(new FileInputStream(new File(classFolder + fileName))));
					inst.redefineClasses(new ClassDefinition[] { definition });
				} catch (Exception e) {
					log.log(Level.SEVERE, "error", e);
				}
			}
		}
	}

	/**
	 * Loads .class files as byte[]
	 * 
	 * @param fileName
	 *          the filename to load
	 * @return a byte[]
	 * @throws IOException
	 *           if an error occurs while reading file
	 */
	// private byte[] loadBytes(String fileName) throws IOException {
	// File file = new File(classFolder + fileName);
	//
	// // InputStream fis = new FileInputStream(file);
	// // byte[] result = new byte[(int) file.length()];
	// // fis.read(result);
	// // fis.close();
	// //
	// // return result;
	// return toByteArray(new FileInputStream(file));
	// }
	private byte[] toByteArray(InputStream is) throws IOException {
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
