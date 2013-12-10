package de.l3s.dlg.ncbikraken.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Version {

	final static String UNKOWN_VERSION = "UNKOWN";

	/**
	 * This method returns the current project version. Therefore it reads the
	 * version.prop file which is filtered by Maven during the code generation
	 * phase.
	 * 
	 * @return the current project version out of the pom.xml
	 */
	public static String getVersion() {
		String path = "/version.prop";
		InputStream stream = Version.class.getResourceAsStream(path);
		if (stream == null)
			return UNKOWN_VERSION;
		Properties props = new Properties();
		try {
			props.load(stream);
			stream.close();
			return (String) props.get("version");
		} catch (IOException e) {
			return UNKOWN_VERSION;
		}
	}

}
