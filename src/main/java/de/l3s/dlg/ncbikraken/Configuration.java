/*******************************************************************************
 * Copyright (c) 2012 Sascha Tönnies (toennies@l3s.de).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Sascha Tönnies (toennies@l3s.de) - initial API and implementation
 ******************************************************************************/
package de.l3s.dlg.ncbikraken;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import de.l3s.dlg.ncbikraken.ftp.MedlineFileType;

import eu.toennies.snippets.db.IPoolingDriverConfig;

/**
 * A utility class for handling the properties.
 * 
 * @author toennies
 *
 */
/**
 * @author toennies
 *
 */
public enum Configuration implements IPoolingDriverConfig {
	
	INSTANCE;

	/**
	 * The apache commons configuration object
	 */
	private PropertiesConfiguration config;

	/**
	 * Hidden defualt constructor 
	 */
	private Configuration() {
		
	}
	
	/**
	 * The hidden constructor for this utility class.
	 * @throws ConfigurationException 
	 */
	public void init(String configFile) throws ConfigurationException {
		this.config = new PropertiesConfiguration(configFile);
	}

	private String getWorkingDir() {
		return this.config.getString("WorkingDir", "~/nlmbox");
	}

	private String getDataDir() {
		return this.config.getString("DataDir");
	}

	public String getDBDriver() {
		return this.config.getString("DBDriver", "com.mysql.jdbc.Driver");
	}

	public String getDBURL() {
		return this.config.getString("DBUrl");
	}

	public String getDBUsername() {
		return this.config.getString("DBUser");
	}

	public String getDBPassword() {
		return this.config.getString("DBPassword");
	}

	public String getFileindex() {
		return getWorkingDir() + File.separator + "file.dat";
	}

	public String getDownloadDir() {
		return getWorkingDir() + File.separator + "download";
	}

	public String getOASDataDir() {
		return getWorkingDir() + File.separator + "pmc";
	}

	public String getMeshDir() {
		return getWorkingDir() + File.separator + "mesh";
	}
	
	public String getMedlineDir() {
		return getDataDir() + File.separator + "medline";
	}

	public String getDBPoolSize() {
		return this.config.getString("PoolSize", "50");
	}

	public String getDBPoolName() {
		return this.config.getString("PoolName", "ncbikraken");
	}

	/**
	 * Property: MedlineDbTableName

	 * Used for the insertion and update of medline data in the database
	 * @return the table name used for medline citations
	 */
	public String getMedlineDbTableName() {
		return this.config.getString("MedlineDbTableName", "medline");
    }

	/**
	 * Property: MedlineArchiveFormat
	 * 
	 * Retrieves the wanted archive format for the medline corpus.
	 * @return the archive format to use (ZIP, GZ)
	 */
	public MedlineFileType getMedlineFileType() {
		if(this.config.getString("MedlineArchiveFormat", "ZIP").equalsIgnoreCase("ZIP")) {
			return MedlineFileType.ZIP;
		} else {
			return MedlineFileType.GZ;
		}
    }

	
}
