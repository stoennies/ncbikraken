package de.l3s.dlg.ncbikraken.cmd;

import org.apache.commons.cli.Option;

public interface ICommandLineOptions {
	
	/**
	 * Retrieves the option's name.
	 * @return the name of this option
	 */
	public String getName();
	
	/**
	 * Retrieves the description for this option.
	 * @return the description of this option
	 */
	public String getDescription();
	
	/**
	 * Retrieves the option a an (@see Option.class)
	 * @return the option as Option object
	 */
	public Option getOption();
	
}
