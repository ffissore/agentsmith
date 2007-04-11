/*
 * SmithArgs: the arguments parser of Smith
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
package it.fridrik.agent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * SmithArgs takes care about the parameters you use to start Smith, parsing and
 * making them available with some getters
 * 
 * @author Federico Fissore (federico@fsfe.org)
 */
public class SmithArgs {

	private static final String KEY_CLASSES = "classes";
	private static final String KEY_JARS = "jars";
	private static final String KEY_PERIOD = "period";
	private static final String KEY_LOG_LEVEL = "loglevel";
	
	private String classFolder;
	private String jarFolder;
	private int period;
	private Level logLevel;

	private SmithArgs() {
		this.classFolder = null;
		this.jarFolder = null;
		this.period = -1;
		this.logLevel = Level.WARNING;
	}

	public SmithArgs(String agentArgs) {
		this();

		if (agentArgs != null && agentArgs.length() > 0) {
			if (agentArgs.indexOf("=") != -1) {
				initWithNamedArgs(agentArgs);
			} else {
				initOldArgs(agentArgs);
			}
		}
	}

	public SmithArgs(String classFolder, String jarFolder, int period,
			String logLevel) {
		this();

		setClassFolder(classFolder);
		setJarFolder(jarFolder);
		setLogLevel(logLevel);
		this.period = period;
	}

	public String getClassFolder() {
		return classFolder;
	}

	public String getJarFolder() {
		return jarFolder;
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public int getPeriod() {
		return period;
	}

	private void initOldArgs(String agentArgs) {
		String[] args = agentArgs.split(",");
		setClassFolder(args[0]);

		if (args.length > 1) {
			setJarFolder(args[1]);
		}

		if (args.length > 2) {
			setPeriod(args[2]);
		}

		if (args.length > 3) {
			setLogLevel(args[3]);
		}
	}

	private void initWithNamedArgs(String agentArgs) {
		String[] args = agentArgs.split(",");
		Map<String, String> argsMap = new HashMap<String, String>();
		for (String s : args) {
			String[] param = s.split("=");
			argsMap.put(param[0].trim(), param[1]);
		}

		if (argsMap.containsKey(KEY_CLASSES)) {
			setClassFolder(argsMap.get(KEY_CLASSES));
		}

		if (argsMap.containsKey(KEY_JARS)) {
			setJarFolder(argsMap.get(KEY_JARS));
		}

		if (argsMap.containsKey(KEY_PERIOD)) {
			setPeriod(argsMap.get(KEY_PERIOD));
		}

		if (argsMap.containsKey(KEY_LOG_LEVEL)) {
			setLogLevel(argsMap.get(KEY_LOG_LEVEL));
		}

	}

	public boolean isValid() {
		return classFolder != null;
	}

	private void setClassFolder(String classFolder) {
		this.classFolder = parseFolderPath(classFolder);
	}

	private void setJarFolder(String jarFolder) {
		this.jarFolder = parseFolderPath(jarFolder);
	}

	private void setLogLevel(String logLevel) {
		try {
			this.logLevel = Level.parse(logLevel.trim());
		} catch (Exception e) {
			this.logLevel = Level.WARNING;
		}
	}

	private void setPeriod(String period) {
		try {
			this.period = Integer.parseInt(period.trim());
		} catch (NumberFormatException e) {
			this.period = -1;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(KEY_CLASSES).append("=").append(classFolder);

		if (jarFolder != null) {
			sb.append(",").append(KEY_JARS).append("=").append(jarFolder);
		}

		sb.append(",").append(KEY_PERIOD).append("=").append(period);
		sb.append(",").append(KEY_LOG_LEVEL).append("=")
				.append(logLevel.toString());

		return sb.toString();
	}

	private static String parseFolderPath(String folder) {
		if (folder != null) {
			String trimmed = folder.trim();
			return trimmed.endsWith(File.separator) ? trimmed : trimmed
					+ File.separator;
		}
		return null;
	}

}