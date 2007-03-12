/*
 * SmithLoader - Loads an agent at runtime (after jvm start up)
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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * SmithLoader loads a Smith agent after the jvm start up. This class is
 * experimental. See {@link #hotStart(String, String)} for more details.
 * 
 * @author Federico Fissore (federico@fsfe.org)
 */
public class SmithLoader {

	/**
	 * Asks the jvm to load the Smith agent specified by the absolute path
	 * parameter and telling the agent to monitor the specified folder. This class
	 * is experimental as it needs a sort of low-level interaction with the
	 * operating system. The following assumptions are done:
	 * <ul>
	 * <li>The Process Identifier (PID of the target jvm) is a integer </li>
	 * <li>The latest started jvm is the one we want to plug the agent into and
	 * it has the greatest PID</li>
	 * </ul>
	 * If any of these assumptions are NOT valid for your operating system, please
	 * report is ASAP and contribute to the project
	 * 
	 * @param pathToSmithJar
	 *          the absolute path to the Smith jar
	 * @param folderToWatch
	 *          the absolute path of the folder to watch (mind to put a trailing
	 *          slash at the end)
	 * @throws Exception
	 *           if something goes wrong
	 */
	public static void hotStart(String pathToSmithJar, String folderToWatch)
			throws Exception {
		List<VirtualMachineDescriptor> vmds = new LinkedList<VirtualMachineDescriptor>(
				VirtualMachine.list());
		Collections.sort(vmds, new Comparator<VirtualMachineDescriptor>() {

			public int compare(VirtualMachineDescriptor arg0,
					VirtualMachineDescriptor arg1) {
				return Integer.valueOf(arg1.id()).compareTo(Integer.valueOf(arg0.id()));
			}

		});

		VirtualMachine vm = VirtualMachine.attach(vmds.get(0));
		vm.loadAgent(pathToSmithJar, folderToWatch);
	}

}
