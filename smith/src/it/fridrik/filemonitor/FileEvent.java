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

import java.util.EventObject;

/**
 * Raised every time a file is added, deleted or modified
 * 
 * @author Federico Fissore (federico@fsfe.org)
 */
public class FileEvent extends EventObject {

	private static final long serialVersionUID = 4696923746078504205L;

	public FileEvent(String path) {
		super(path);
	}

	@Override
	public String getSource() {
		return (String) super.getSource();
	}

}
