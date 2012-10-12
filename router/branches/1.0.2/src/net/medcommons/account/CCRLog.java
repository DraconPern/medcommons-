package net.medcommons.account;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import net.medcommons.account.AccountConnection;
import net.medcommons.account.AccountConnectionPool;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.configuration.ConfigurationService;

public class CCRLog {
	static AccountConnectionPool dbPool = null;
	//private AccountConnectionPool pool;
	private static Logger log = Logger.getLogger(CCRLog.class);

	protected static ConfigurationService configService = null;
	static{
		configService = new ConfigurationService();
		try{
			configService.start();
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Failed to initialize configuation", e);
		}
		try{
			
			String driver = Configuration.getProperty("accountdb_driver");
			String database = Configuration.getProperty("accountdb_database");
			String user = Configuration.getProperty("accountdb_user");
			String password = Configuration.getProperty("accountdb_password");
			
			Properties properties = new Properties();
			properties.put("user", user);
			properties.put("password", password);
			
			dbPool =  AccountConnectionPool.NewInstance(driver, database, properties);
			log.info("Created dbPool with driver =" + driver + ", database = " + database);
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Failed to initialize database", e);
		}
	}
	
	public CCRLog() throws ConfigurationException, ClassNotFoundException, InstantiationException,
    IllegalAccessException, SQLException {
		log.info("Created CCRLog");
	}
	   /**
     * Get an old AccountConnection or create a new one.
     *
     * @see AccountConnectionPool
     */
    protected AccountConnection getConnection() throws SQLException, InterruptedException {
    	log.info("Attempting to get connection..");
    	AccountConnection c = dbPool.getConnection();
    	log.info("Obtained connection " + c);
	return c;
    }
    /**
     * Return an AccountConnection to the pool, closing it if the pool is full.
     *
     * @see AccountConnectionPool
     */
    protected void releaseConnection(AccountConnection c) {
    	log.info("Released connection " + c);
    	dbPool.releaseConnection(c);
    }
    
	public CCRInfo[] queryAcctid(String accid){
		log.info("queryAcctid:" + accid);
		AccountConnection c = null;
		CCRInfo ccrs[] = null;
		try{
		c = getConnection();
		
		ccrs = c.getCCRs(accid);
		}
		catch(Exception e){
			log.info("queryAcctid:", e);
		}
		return(ccrs);
		
		
	}
}
