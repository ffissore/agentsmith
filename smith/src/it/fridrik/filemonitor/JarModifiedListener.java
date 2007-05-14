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

/**
 * Signals the modification of a class contained into a jar
 * 
 * @author Federico Fissore (federico@fissore.org)
 * @since 1.0
 */
public interface JarModifiedListener {

	void jarModified(JarEvent event);

}
