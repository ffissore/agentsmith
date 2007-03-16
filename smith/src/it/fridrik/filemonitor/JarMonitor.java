package it.fridrik.filemonitor;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JarMonitor implements FileModifiedListener, FileAddedListener,
		FileDeletedListener, Runnable {

	private final static Logger log = Logger.getLogger("JarMonitor");

	private final FileMonitor fileMonitor;
	private final String fileExtension;
	private final String absoluteFolderPath;
	private final Map<String, Map<String, Long>> jarsMap;

	public JarMonitor(String absoluteFolderPath, String fileExtension) {
		this.absoluteFolderPath = absoluteFolderPath;
		this.fileExtension = fileExtension;
		this.jarsMap = new HashMap<String, Map<String, Long>>();

		fileMonitor = new FileMonitor(absoluteFolderPath, "jar");
		fileMonitor.addModifiedListener(this);
		fileMonitor.addAddedListener(this);
		fileMonitor.addDeletedListener(this);
	}

	public void run() {
		fileMonitor.run();
	}

	public void fileModified(FileEvent event) {
		// TODO Auto-generated method stub

	}

	public void fileAdded(FileEvent event) {
		JarFile file = null;
		try {
			file = new JarFile(absoluteFolderPath + event.getSource());
		} catch (IOException e) {
			log.log(Level.SEVERE, "error", e);
		}

		if (file != null) {
			Map<String, Long> jarEntries = new HashMap<String, Long>();
			jarsMap.put(event.getSource(), jarEntries);

			for (Enumeration<JarEntry> entries = file.entries(); entries
					.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				if (entry.getName().endsWith(fileExtension)) {
					jarEntries.put(entry.getName(), Long.valueOf(entry.getTime()));
				}
			}
		}
	}

	public void fileDeleted(FileEvent event) {
		jarsMap.remove(event.getSource());
	}

}
