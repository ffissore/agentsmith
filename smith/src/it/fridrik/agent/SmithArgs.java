/**
 * 
 */
package it.fridrik.agent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SmithArgs {

	private static final String KEY_CLASSES = "classes";
	private static final String KEY_JARS = "jars";
	private static final String KEY_PERIOD = "period";

	private String classFolder;
	private String jarFolder;
	private int period;

	private SmithArgs() {
		this.classFolder = null;
		this.jarFolder = null;
		this.period = -1;

	}

	public SmithArgs(String classFolder, String jarFolder, int period) {
		this();
		
		setClassFolder(classFolder);
		setJarFolder(jarFolder);
		this.period = period;
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
	}

	public String getClassFolder() {
		return classFolder;
	}

	public String getJarFolder() {
		return jarFolder;
	}

	public int getPeriod() {
		return period;
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