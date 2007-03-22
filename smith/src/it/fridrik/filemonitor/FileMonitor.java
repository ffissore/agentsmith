/*
 * File Monitor - Watches a folder and notify files changes
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
	private final String fileExtension;
	private final Map<String, Long> fileMap;
	private final List<FileAddedListener> fileAddedListeners;
	private final List<FileDeletedListener> fileDeletedListeners;
	private final List<FileModifiedListener> fileModifiedListeners;

	class ExtFilenameFilter implements FilenameFilter {

		@SuppressWarnings("synthetic-access")
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
				notifyDeletedListeners(new FileEvent(path, folder.getAbsolutePath()));
			}
		}
		for (String path : pathsToDelete) {
			fileMap.remove(path);
		}
	}

	/**
	 * Checks for file addition and modification
	 * 
	 * @param currentFolder
	 *          the folder to monitor
	 */
	protected void checkAddAndModify(File currentFolder) {
		for (File file : getFiles(currentFolder)) {
			if (file.isDirectory()) {
				checkAddAndModify(file);
			} else {
				if (fileMap.containsKey(file.getAbsolutePath())) {
					if (fileMap.get(file.getAbsolutePath()).longValue() != file
							.lastModified()) {
						fileMap.put(file.getAbsolutePath(), Long.valueOf(file
								.lastModified()));
						notifyModifiedListeners(new FileEvent(file.getAbsolutePath(),
								folder.getAbsolutePath()));
					}
				} else {
					fileMap
							.put(file.getAbsolutePath(), Long.valueOf(file.lastModified()));
					notifyAddedListeners(new FileEvent(file.getAbsolutePath(), folder
							.getAbsolutePath()));
				}
			}
		}
	}

	public File[] getFiles(File folder) {
		return folder.listFiles(filenameFilter);
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
