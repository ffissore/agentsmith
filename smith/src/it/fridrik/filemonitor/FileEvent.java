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
import java.util.EventObject;

/**
 * Raised every time a file is added, deleted or modified
 * 
 * @author Federico Fissore (federico@fsfe.org)
 */
public class FileEvent extends EventObject {

	private static final long serialVersionUID = 4696923746078504205L;

	/**
	 * Creates a new FileEvent, removing the absolute folder path supplied when
	 * creating this instance. Therefore the event will contain the relative path
	 * to the file
	 * 
	 * @param path
	 *          the path of the file
	 * @param basePath
	 *          the basepath to erase from the event source
	 */
	public FileEvent(String path, String basePath) {
		super(path.replace(basePath + File.separator, ""));
	}

	/**
	 * The relative path to the changed file.
	 */
	@Override
	public String getSource() {
		return (String) super.getSource();
	}

}
