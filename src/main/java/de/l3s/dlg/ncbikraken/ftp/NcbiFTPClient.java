/*******************************************************************************
 * Copyright (c) 2013 Sascha Tönnies (toennies@l3s.de). All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the GNU Public License v3.0 which accompanies this distribution, and
 * is available at http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Sascha Tönnies (toennies@l3s.de) - initial API and
 * implementation
 ******************************************************************************/
package de.l3s.dlg.ncbikraken.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.l3s.dlg.ncbikraken.Configuration;

/**
 * A static helper class for retrieving the open access archives from the NCBI
 * ftp server.
 * 
 * @author toennies
 * 
 */
public final class NcbiFTPClient {

	private static final int BUFFER = 32 * 1024;

	/**
	 * The class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(NcbiFTPClient.class);

	/**
	 * http://www.nlm.nih.gov/bsd/licensee/access/medline_pubmed.html
	 */
	private static final String SERVER_NAME = "ftp.nlm.nih.gov";
	private static final String NCBI_SERVER_NAME = "ftp.ncbi.nlm.nih.gov/";
	
	private static final String BASELINE_PATH = "/nlmdata/.medleasebaseline";
	private static final String UPDATE_PATH = "/nlmdata/.medlease/";
	

	private static final String PMC_SERVER_NAME = "nlmpubs.nlm.nih.gov";
	private static final String PMC_SERVER_PATH = "/online/mesh/.xmlmesh";

	/**
	 * Hidden constructor for this utility class.
	 */
	private NcbiFTPClient() {
		super();
	}

	public static void getMedline(MedlineFileType type) {
		FileOutputStream out = null;
		FTPClient ftp = new FTPClient();
		try {
			// Connection String
			LOGGER.info("Connecting to FTP server " + SERVER_NAME);
			ftp.connect(SERVER_NAME);
			ftp.login("anonymous", "");
			ftp.cwd(BASELINE_PATH);
			ftp.cwd(type.getServerPath());

			try {
				ftp.pasv();
			} catch (IOException e) {
				LOGGER.error("Can not access the passive mode. Maybe a problem with your (Windows) firewall. Just try to run as administrator: \nnetsh advfirewall set global StatefulFTP disable");
				return;
			}

			for (FTPFile file : ftp.listFiles()) {
				if (file.isFile()) {
					File meshF = new File(file.getName());
					LOGGER.debug("Downloading file: " + SERVER_NAME + ":" + BASELINE_PATH + "/"
					        + type.getServerPath() + "/" + meshF.getName());
					out = new FileOutputStream(Configuration.INSTANCE.getMedlineDir() + File.separator + meshF);
					ftp.retrieveFile(meshF.getName(), out);
					out.flush();
					out.close();
				}
			}

		} catch (IOException ioe) {
			LOGGER.error(ioe.getMessage());

		} finally {
			IOUtils.closeQuietly(out);
			try {
				ftp.disconnect();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}

	}

}
