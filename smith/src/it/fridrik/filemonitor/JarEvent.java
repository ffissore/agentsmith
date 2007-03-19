package it.fridrik.filemonitor;

import java.util.EventObject;
import java.util.jar.JarFile;

public class JarEvent extends EventObject {

	private static final long serialVersionUID = -7809367345460212417L;

	private final String entryName;

	public JarEvent(JarFile file, String entryName) {
		super(file);
		this.entryName = entryName;
	}

	@Override
	public JarFile getSource() {
		return (JarFile) super.getSource();
	}

	public String getEntryName() {
		return entryName;
	}

}
