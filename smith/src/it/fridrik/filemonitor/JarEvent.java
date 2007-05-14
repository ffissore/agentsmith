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

import java.util.EventObject;
import java.util.jar.JarFile;

/**
 * Raised every time a class contained into a jar is modified
 * 
 * @author Federico Fissore (federico@fissore.org)
 * @since 1.0
 */
public class JarEvent extends EventObject {

	private static final long serialVersionUID = -7809367345460212417L;

	private final String entryName;

	public JarEvent(JarFile file, String entryName) {
		super(file);
		this.entryName = entryName;
	}

	/**
	 * The changed jar file
	 * 
	 * @return a JarFile
	 */
	@Override
	public JarFile getSource() {
		return (JarFile) super.getSource();
	}

	/**
	 * The changed class file
	 * 
	 * @return the changed class file
	 */
	public String getEntryName() {
		return entryName;
	}

}
