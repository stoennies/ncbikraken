package de.l3s.dlg.ncbikraken.cmd;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * A enumeration holding all program options.
 * 
 * @author toennies
 *
 */
public enum StandardCommandLineOptions implements ICommandLineOptions {
	HELP("help", "print this message", null),
	VERSION("version", "print the version information and exit", null),
	QUIET("quiet", "be extra quiet", null),
	VERBOSE("verbose", "be extra verbose", null),
	DEBUG("debug", "print debugging information", null);

	private String name;
	private String description;
	private String argument;

	private StandardCommandLineOptions(String name, String description, String argument) {
		this.name = name;
		this.description = description;
		this.argument = argument;
	}

	/* (non-Javadoc)
	 * @see de.l3s.dlg.nlm.utils.CommandLineOptions#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.l3s.dlg.nlm.utils.CommandLineOptions#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see de.l3s.dlg.nlm.utils.CommandLineOptions#getOption()
	 */
	@SuppressWarnings("static-access")
	public Option getOption() {
		if (this.argument == null) {
			return new Option(name, description);
		} else {
			return OptionBuilder.withArgName(argument).hasArg()
					.withDescription(description).create(name);
		}
	}

}
