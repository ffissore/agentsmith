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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
		String folder = null;
		int period = 0;
		if (agentArgs != null) {
			String[] sa = agentArgs.split(",");
			folder = sa[0];
			if (sa.length > 1) {
				try {
					period = Integer.parseInt(sa[1]);
				} catch (NumberFormatException e) {
					period = -1;
				}
			}
		}

		Smith smith = new Smith(inst, folder, period);
		smiths.add(smith);
	}

	/** Stops all active Smith agents */
	public static void stopAll() {
		for (Smith smith : smiths) {
			smith.stop();
		}
	}

	private final Instrumentation inst;
	private final String folder;
	private final ScheduledExecutorService service;

	/**
	 * Creates and starts a new Smith agent
	 * 
	 * @param inst
	 *          the instrumentation implementation
	 * @param folder
	 *          the folder to monitor
	 * @param period
	 *          the period between a class folder check and one another. Must be
	 *          greter than 500 to be accepted
	 */
	public Smith(Instrumentation inst, String folder, int period) {
		this.inst = inst;
		this.folder = folder.endsWith(File.separator) ? folder : folder
				+ File.separator;
		int monitorPeriod = 500;
		if (period > monitorPeriod) {
			monitorPeriod = period;
		}

		FileMonitor fileMonitor = new FileMonitor(folder, "class");
		fileMonitor.addModifiedListener(this);

		service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(fileMonitor, 0, monitorPeriod,
				TimeUnit.MILLISECONDS);

		log.info("Smith: watching folder: " + folder);
		log.info("Smith: period between checks (ms): " + period);
	}

	/**
	 * Stops this Smith agent
	 */
	public void stop() {
		service.shutdown();
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
	private byte[] loadBytes(String fileName) throws IOException {
		File file = new File(folder + fileName);

		FileInputStream fis = new FileInputStream(file);
		byte[] result = new byte[(int) file.length()];
		fis.read(result);

		return result;
	}

	/**
	 * When the monitor notifies of a changed class file, Smith will redefine it
	 */
	public void fileModified(FileEvent event) {
		String className = event.getSource().replace(".class", "").replace(
				File.separatorChar, '.');
		
		Class[] loadedClasses = inst.getAllLoadedClasses();
		
		for (Class<?> clazz : loadedClasses) {
			if (clazz.getName().equals(className)) {
				try {
					ClassDefinition definition = new ClassDefinition(clazz,
							loadBytes(event.getSource()));
					inst.redefineClasses(new ClassDefinition[] { definition });
				} catch (Exception e) {
					log.log(Level.SEVERE, "error", e);
				}
			}
		}

	}

}
