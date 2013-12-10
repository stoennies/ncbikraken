package de.l3s.dlg.ncbikraken.storage.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import eu.toennies.snippets.db.DbPoolingDriver;
import eu.toennies.snippets.db.IPoolingDriverConfig;
import eu.toennies.snippets.db.PoolingException;

/**
 * A helper class for database handling.
 * 
 * @author toennies
 * 
 */
public final class DBUtil {

	private static Logger log = LoggerFactory.getLogger(DBUtil.class);

	/**
	 * Hidden constructor for utility class.
	 */
	private DBUtil() {
		
	}
	
	/**
	 * Close the (@see Connection) safely.
	 * @param con - the connection to be closed.
	 */
	public static void closeSilent(Connection con) {
		log.trace("Start closing connection.");
		try {
			con.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.trace("DB connection closed.");
	}

	/**
	 * Close the (@see Statement) safely.
	 * @param stm - the statement to be closed.
	 */
	public static void closeSilent(Statement stm) {
		log.trace("Start closing statement.");
		try {
			stm.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.trace("Statement closed.");
	}

	/**
	 * Close the (@see ResultSet) safely.
	 *  
	 * @param rs - the ResultSet to be closed.
	 */
	public static void closeSilent(ResultSet rs) {
		log.trace("Start closing result set.");
		try {
			rs.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.trace("ResultSet closed.");
	}

	/**
	 * This method returns a new database connection.
	 * 
	 * @return a new database connection
	 * @throws SQLException - if the connection could not be opened
	 * @throws PoolingException - if no pool for the given pool name was found
	 */
	public static Connection getConnection() throws SQLException, PoolingException {
		log.trace("Getting DB connection");
		return DriverManager.getConnection(DbPoolingDriver.getInstance()
				.getPoolName());
	}
}
