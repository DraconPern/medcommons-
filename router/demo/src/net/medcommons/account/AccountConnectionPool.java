package net.medcommons.account;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.sun.tools.xjc.generator.validator.StringOutputStream;

public class AccountConnectionPool {
	private static Logger log = Logger.getLogger(AccountConnectionPool.class);

    private static final String ATTRIBUTE_NAME =
	"net.medcommons.identity-connection-pool";

    private static final String ATTRIBUTE_PREFIX = "db.";

    Pool pool = new Pool(10);

    Driver driver;
    String url;
    Properties properties;

    /**
     * Create a new AccountConnection pool.
     *
     * @param driver     - JDBC driver to use
     * @param url        - JDBC URL to database
     * @param properties - set of properties to use when connecting to DB
     */
    public AccountConnectionPool(Driver driver, String url, Properties properties) {
	this.driver = driver;
	this.url = url;
	this.properties = properties;
    }

    /**
     * A bunch of different servlets in the same servlet context can all share
     * the same AccountConnectionPool.  Pass the ServletContext to
     * GetInstance(), and it either finds an AccountConnectionPool previously
     * stored as an attribute, or it uses the context init parameters to build
     * a new one.
     *
     * @see #NewInstance
     *
     * @param ctx   ServletContext shared between a group of servlets
     * @exception ClassNotFoundException if we can't find the JDBC driver
     *            class
     * @exception InstantiationException if the JDBC driver does not have
     *            an appropriate constructor (probably got the wrong driver
     *            name)
     * @exception IllegalAccessException if the JDBC driver isn't a public
     *            class (probably got the wrong driver name)
     */
    
  /*
    public static synchronized AccountConnectionPool GetInstance
	(ServletContext ctx) throws ClassNotFoundException,
				    InstantiationException,
				    IllegalAccessException, SQLException {
	Object o = ctx.getAttribute(ATTRIBUTE_NAME);
	AccountConnectionPool cp;

	if (o instanceof AccountConnectionPool) {
	    cp =  (AccountConnectionPool) o;
	}
	else {
	    cp = NewInstance(ctx);
	    ctx.setAttribute(ATTRIBUTE_NAME, cp);
	}

	return cp;
    }
    */

    /**
     * Create a new AccountConnection pool based on ServletContext
     * initialization parameters.
     *
     * The context parameters must include:
     *   driver   - Java class name for JDBC driver,
     *              ex. com.mysql.jdbc.Driver
     *   database - JDBC url of database to connect to,
     *              ex. jdbc:mysql://localhost/mcidentity
     *
     *   db.*     - all parameters starting with 'db.' are placed in a set of
     *              properties and passed to JDBC on connection,
     *              ex. db.user=web, db.password=password
     *
     * @param ctx   ServletContext shared between a group of servlets
     * @exception ClassNotFoundException if we can't find the JDBC driver
     *            class
     * @exception InstantiationException if the JDBC driver does not have
     *            an appropriate constructor (probably got the wrong driver
     *            name)
     * @exception IllegalAccessException if the JDBC driver isn't a public
     *            class (probably got the wrong driver name)
     */
    
    public static AccountConnectionPool NewInstance(String driverName, String database, Properties properties) 
	throws ClassNotFoundException, InstantiationException,
	       IllegalAccessException, SQLException {
    	Enumeration e = properties.propertyNames();
    	while(e.hasMoreElements()){
    		log.info("property: " + e.nextElement());
    	}
	/* Load appropriate JDBC driver */
	Class cl = Class.forName(driverName);
	Driver driver = (Driver) cl.newInstance();
	

	DriverManager.registerDriver(driver);

	//Enumeration e = ctx.getInitParameterNames();
/*
	while (e.hasMoreElements()) {
	    String param = (String) e.nextElement();

	    if (param.startsWith(ATTRIBUTE_PREFIX))
		properties.put(param.substring(ATTRIBUTE_PREFIX.length()),
			       ctx.getInitParameter(param));
	}
*/
	return new AccountConnectionPool(driver,
					  database,
					  properties);
    }

    /**
     * Get an AccountConnection, either by pulling it out of the pool, or by
     * making a new one.
     *
     * @exception SQLException on any error creating the connection,
     *            or preparing the Identity SQL statements.
     * @exception InterruptedException if another thread interrupts us while waiting
     *            on the pool
     */
    public AccountConnection getConnection() throws SQLException, InterruptedException {
	AccountConnection c;

	if ((c = (AccountConnection) this.pool.getWait(0L)) == null)
	    c = newConnection();

	return c;
    }

    /**
     * Release a connection back to the pool.  If the pool is full, close
     * the connection.
     */
    public void releaseConnection(AccountConnection c) {
	if (!this.pool.put(c))
	    c.close();
    }

    /**
     * Create a new connection.
     *
     * @exception SQLException on any error creating the connection,
     *            or preparing the Identity SQL statements.
     */
    public AccountConnection newConnection() throws SQLException {
    	
    		
	Connection c = this.driver.connect(this.url, this.properties);
		log.info("New connection generated:" + c);
	return new AccountConnection(c);
    }

    
}
