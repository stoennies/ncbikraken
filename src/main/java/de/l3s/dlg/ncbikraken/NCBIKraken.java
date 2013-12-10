package de.l3s.dlg.ncbikraken;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.LoggerFactory;

import de.l3s.dlg.ncbikraken.cmd.CommandLineProgram;
import de.l3s.dlg.ncbikraken.cmd.ICommandLineOptions;
import de.l3s.dlg.ncbikraken.cmd.StandardCommandLineOptions;
import de.l3s.dlg.ncbikraken.ftp.NcbiFTPClient;

public class NCBIKraken extends CommandLineProgram {

	private final static String CMD_PREFIX = NCBIKraken.class.getSimpleName()
			+ " [options] configurationFile";
	
	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NCBIKraken.class);

	public NCBIKraken(ICommandLineOptions[] cmdValues) {
	    super(cmdValues, CMD_PREFIX);
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		// assume SLF4J is bound to logback in the current environment
//	    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//	    // print logback's internal status
//	    StatusPrinter.print(lc);

	    
		NCBIKraken cmd = new NCBIKraken(StandardCommandLineOptions.values());
		if (args.length == 0) {
			cmd.interrupt();
		}

		CommandLineParser parser = new GnuParser();
		String configFile = "";

		try {
			CommandLine line = parser.parse(cmd.getOptions(), args);
			cmd.checkForStandardOptions(line);

			if (line.getArgList().size() != 1) {
				cmd.interrupt();
			}
			
			configFile = (String) line.getArgList().get(0);
			Configuration.INSTANCE.init(configFile);
		} catch (ConfigurationException ce) {
			LOGGER.error(String.format("Could not load configuration file %s\n", configFile));
			return;
		} catch (ParseException e) {
	        e.printStackTrace();
        }

		NcbiFTPClient.getMedline(Configuration.INSTANCE.getMedlineFileType());

	}

	private void interrupt() {
		printHelp();
		System.exit(1);
	}
}
