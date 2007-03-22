/*
 * Jar Monitor - Watches a jar folder and notify jar classes changes
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
package it.fridrik.filemonitor;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JarMonitor uses FileMonitor to receive notifications about jar changes, then
 * looks into the changed jar for the changed classes and then tells its
 * listeners about the changed classes in the changed jars
 * 
 * @author Federico Fissore (federico@fsfe.org)
 */
public class JarMonitor implements FileModifiedListener, FileAddedListener,
		FileDeletedListener, Runnable {

	private final static Logger log = Logger.getLogger("JarMonitor");

	private final FileMonitor fileMonitor;
	private final String absoluteFolderPath;
	private final Map<String, Map<String, Long>> jarsMap;
	private final List<JarModifiedListener> jarModifiedListeners;

	public JarMonitor(String absoluteFolderPath) {
		this.absoluteFolderPath = absoluteFolderPath;
		this.jarsMap = new HashMap<String, Map<String, Long>>();
		this.jarModifiedListeners = new LinkedList<JarModifiedListener>();

		fileMonitor = new FileMonitor(absoluteFolderPath, "jar");
		fileMonitor.addModifiedListener(this);
		fileMonitor.addAddedListener(this);
		fileMonitor.addDeletedListener(this);
	}

	public void run() {
		fileMonitor.run();
	}

	public void fileModified(FileEvent event) {
		JarFile file = getJarFile(event);

		if (file != null) {
			Map<String, Long> jarEntries = jarsMap.get(event.getSource());
			for (Enumeration<JarEntry> entries = file.entries(); entries
					.hasMoreElements();) {
				JarEntry entry = entries.nextElement();

				if (!jarEntries.containsKey(entry.getName())) {
					jarEntries.put(entry.getName(), Long.valueOf(entry.getTime()));
				}

				if (entry.getTime() != jarEntries.get(entry.getName()).longValue()) {
					jarEntries.put(entry.getName(), Long.valueOf(entry.getTime()));
					notifyJarModifiedListeners(new JarEvent(file, entry.getName()));
				}

			}

		}
	}

	public void fileAdded(FileEvent event) {
		JarFile file = getJarFile(event);

		if (file != null) {
			Map<String, Long> jarEntries = new HashMap<String, Long>();
			jarsMap.put(event.getSource(), jarEntries);

			for (Enumeration<JarEntry> entries = file.entries(); entries
					.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				if (entry.getName().endsWith("jar")) {
					jarEntries.put(entry.getName(), Long.valueOf(entry.getTime()));
				}
			}
		}
	}

	public void fileDeleted(FileEvent event) {
		jarsMap.remove(event.getSource());
	}

	public void addJarModifiedListener(JarModifiedListener listener) {
		jarModifiedListeners.add(listener);
	}

	private void notifyJarModifiedListeners(JarEvent event) {
		for (JarModifiedListener listener : jarModifiedListeners) {
			listener.jarModified(event);
		}
	}

	private JarFile getJarFile(FileEvent event) {
		try {
			return new JarFile(absoluteFolderPath + event.getSource());
		} catch (IOException e) {
			log.log(Level.SEVERE, "error", e);
			return null;
		}
	}

}
