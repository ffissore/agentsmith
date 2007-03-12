/*
 * File Monitor - Watches a folder and notify files changes
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
package it.fridrik.filemonitor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * FileMonitor (the name says it all) monitors a folder and its subfolders for
 * file changes (added, removed and modified). Only one file extension can be
 * monitored for each instance of FileMonitor. For each change found, an event
 * is raised. File renames are notified as a file removal and a file addition,
 * in this order. FileMonitor implements Runnable and expects you to start it
 * through a ScheduledExecutorService
 * 
 * @author Federico Fissore (federico@fsfe.org)
 */
public class FileMonitor implements Runnable {

	private final File folder;
	private final ExtFilenameFilter filenameFilter;
	private Map<String, Long> fileMap;
	final String fileExtension;
	private List<FileAddedListener> fileAddedListeners;
	private List<FileDeletedListener> fileDeletedListeners;
	private List<FileModifiedListener> fileModifiedListeners;

	class ExtFilenameFilter implements FilenameFilter {

		public boolean accept(File folder, String name) {
			return name.endsWith(fileExtension)
					|| new File(folder.getAbsolutePath() + File.separator + name)
							.isDirectory();
		}

	}

	/**
	 * Creates a new instance of FileMonitor
	 * 
	 * @param absoluteFolderPath
	 *          the absolute folder path to monitor
	 * @param fileExtension
	 *          the file extension to monitor
	 */
	public FileMonitor(String absoluteFolderPath, String fileExtension) {
		this.fileExtension = fileExtension;
		this.filenameFilter = new ExtFilenameFilter();
		this.fileMap = new HashMap<String, Long>();
		this.fileAddedListeners = new LinkedList<FileAddedListener>();
		this.fileDeletedListeners = new LinkedList<FileDeletedListener>();
		this.fileModifiedListeners = new LinkedList<FileModifiedListener>();
		this.folder = new File(absoluteFolderPath);

		if (!folder.isAbsolute() || !folder.isDirectory()) {
			throw new IllegalArgumentException("The parameter with value "
					+ absoluteFolderPath + " MUST be a folder");
		}

		checkAddAndModify(folder);
	}

	public void run() {
		checkDeletion();
		checkAddAndModify(folder);
	}

	/**
	 * Checks for files deletion
	 */
	protected void checkDeletion() {
		List<String> pathsToDelete = new LinkedList<String>();
		for (String path : fileMap.keySet()) {
			if (!new File(path).exists()) {
				pathsToDelete.add(path);
				notifyDeletedListeners(newFileEvent(path));
			}
		}
		for (String path : pathsToDelete) {
			fileMap.remove(path);
		}
	}

	/**
	 * Checks for file addition and modification
	 * 
	 * @param folder
	 *          the folder to monitor
	 */
	protected void checkAddAndModify(File folder) {
		for (File file : folder.listFiles(filenameFilter)) {
			if (file.isDirectory()) {
				checkAddAndModify(file);
			} else {
				if (fileMap.containsKey(file.getAbsolutePath())) {
					if (fileMap.get(file.getAbsolutePath()).longValue() != file
							.lastModified()) {
						fileMap.put(file.getAbsolutePath(), Long.valueOf(file
								.lastModified()));
						notifyModifiedListeners(newFileEvent(file.getAbsolutePath()));
					}
				} else {
					fileMap
							.put(file.getAbsolutePath(), Long.valueOf(file.lastModified()));
					notifyAddedListeners(newFileEvent(file.getAbsolutePath()));
				}
			}
		}

	}

	/**
	 * Creates a new FileEvent, removing the absolute folder path supplied when
	 * creating this instance. Therefore the event will contain the relative path
	 * to the file
	 * 
	 * @param path
	 *          the path of the file
	 * @return a new {@link FileEvent}
	 */
	private FileEvent newFileEvent(String path) {
		return new FileEvent(path.replace(
				folder.getAbsolutePath() + File.separator, ""));
	}

	/**
	 * Adds a file modified listener
	 * 
	 * @param listener
	 *          the listener
	 */
	public void addModifiedListener(FileModifiedListener listener) {
		fileModifiedListeners.add(listener);
	}

	/**
	 * Adds a file deleted listener
	 * 
	 * @param listener
	 *          the listener
	 */
	public void addDeletedListener(FileDeletedListener listener) {
		fileDeletedListeners.add(listener);
	}

	/**
	 * Adds a file added listener
	 * 
	 * @param listener
	 *          the listener
	 */
	public void addAddedListener(FileAddedListener listener) {
		fileAddedListeners.add(listener);
	}

	private void notifyModifiedListeners(FileEvent event) {
		for (FileModifiedListener listener : fileModifiedListeners) {
			listener.fileModified(event);
		}
	}

	private void notifyAddedListeners(FileEvent event) {
		for (FileAddedListener listener : fileAddedListeners) {
			listener.fileAdded(event);
		}
	}

	private void notifyDeletedListeners(FileEvent event) {
		for (FileDeletedListener listener : fileDeletedListeners) {
			listener.fileDeleted(event);
		}
	}

}
