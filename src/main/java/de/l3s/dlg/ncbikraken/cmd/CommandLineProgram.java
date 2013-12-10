package de.l3s.dlg.ncbikraken.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public abstract class CommandLineProgram {

	private Options options;
	private ICommandLineOptions[] cmdValues;
	private String cmdName;
	
	public CommandLineProgram(ICommandLineOptions[] cmdValues, String cmdName) {
		this.cmdValues = cmdValues;
		this.cmdName = cmdName;
		
		init();
	}
	
	
	private void init() {
		options = new Options();
		for(ICommandLineOptions option : StandardCommandLineOptions.values()) {
			options.addOption(option.getOption());
		}

		for (ICommandLineOptions option : cmdValues) {
			options.addOption(option.getOption());
		}
		
	}
	
	public void printVersion() {
		System.out.println(Version.getVersion());
	}

	public void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(cmdName, options);
	}
	
	public Options getOptions() {
		return this.options;
	}
	
	public void checkForStandardOptions(CommandLine line) {
		if (line.hasOption(StandardCommandLineOptions.HELP.getName())) {
			printHelp();
			System.exit(0);
		} else if (line.hasOption(StandardCommandLineOptions.VERSION.getName())) {
			printVersion();
			System.exit(0);
		}
		
		if (line.hasOption(StandardCommandLineOptions.QUIET.getName())) {
			Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			root.setLevel(Level.ERROR);
		} else if (line.hasOption(StandardCommandLineOptions.VERBOSE.getName())) {
			Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			root.setLevel(Level.TRACE);
		} else if (line.hasOption(StandardCommandLineOptions.DEBUG.getName())) {
			Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			root.setLevel(Level.DEBUG);
		}
	}
	

}
