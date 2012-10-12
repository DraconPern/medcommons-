package net.medcommons.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import net.medcommons.account.CCRInfo;
import net.medcommons.account.PatientCCRServer;
import net.medcommons.router.services.dicom.util.MCSeries;

/**
 * CREATE TABLE `ccrlog` (
  `id` int(11) NOT NULL auto_increment,
  `accid` decimal(16,0) NOT NULL default '0',
  `idp` varchar(255) NOT NULL default '',
  `guid` varchar(64) NOT NULL default '0',
  `status` varchar(12) NOT NULL default '',
  `date` timestamp(14) NOT NULL,
  `src` varchar(255) NOT NULL default '',
  `dest` varchar(255) NOT NULL default '',
  `subject` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`id`),
  KEY `accid` (`accid`)
) TYPE=MyISAM COMMENT='every touch of a document' AUTO_INCREMENT=100038 ;

-- 
-- Dumping data for table `ccrlog`
-- 

INSERT INTO `ccrlog` VALUES (100013, 0240223147727995, 'ImageExpress', '0696dd860cb189f991cce24a5dcb6834e8cbcb0c', 'RED', '20060131182517', 'UNKNOWN', 'sdoyle@gmail.com', 'HIV Sample 2');
 */
/**
 * This class maintains a JDBC connection to an identity
 * database, linking SAML-provided users to their MCIDs
 * and medcommons servers.
 */
public class AccountConnection {
	private static Logger log = Logger.getLogger(AccountConnection.class);
	private static final String CCRLOG_QUERY_BY_ACCID =
		"SELECT ccrlog.accid, ccrlog.idp, ccrlog.guid, ccrlog.status, ccrlog.date, ccrlog.src, ccrlog.dest, ccrlog.subject " +
		" FROM ccrlog WHERE ccrlog.accid = ?";
	private static final String CCRLOG_QUERY_BY_ACCID_IPD =
		"SELECT ccrlog.accid, ccrlog.idp, ccrlog.guid, ccrlog.status, ccrlog.date, ccrlog.src, ccrlog.dest, ccrlog.subject " +
		" FROM ccrlog WHERE ccrlog.accid = ? AND ccrlog.ipd= ?";
	private static final String CCRLOG_QUERY_BY_ACCID_AND_STATUS =
		"SELECT ccrlog.accid, ccrlog.idp, ccrlog.guid, ccrlog.status, ccrlog.date, ccrlog.src, ccrlog.dest, ccrlog.subject " +
		" FROM ccrlog WHERE ccrlog.accid = ? AND ccrlog.status= ?";
	private static final String CCRLOG_UPDATE_STATUS_BY_GUID =
		"UPDATE ccrlog  " +
		" SET status = ? " +
		" WHERE guid = ? AND " +
		" accid = ? ";

   /* private static final String INCOMING_USER_QUERY =
	"SELECT servers.url, users.mcid, users.email, identity_providers.name, users.first_name, users.last_name " +
	" FROM external_users, users, identity_providers, servers " +
	" WHERE identity_providers.source_id = ? AND " + 
	" external_users.username = ? AND " +
	" external_users.provider_id = identity_providers.id AND " +
	" external_users.mcid = users.mcid AND " +
	" users.server_id = servers.id";

    private static final String LOGIN_QUERY =
	"SELECT servers.url, users.mcid, users.email, users.first_name, users.last_name " +
	" FROM users, servers " +
	" WHERE users.mcid = ? AND " +
	" users.sha1 = ? AND " +
	" users.server_id = servers.id";

    private static final String SOURCE_QUERY =
	"SELECT identity_providers.name " +
	" FROM identity_providers " +
	" WHERE identity_providers.source_id = ?";

    private static final String MCID_QUERY =
	"SELECT users.mcid FROM users WHERE users.email = ?";

    private static final String NEW_USER_UPDATE =
	"INSERT INTO users(mcid, email, sha1, server_id, " +
	"                  first_name, last_name, mobile) " +
	" VALUES(?, ?, ?, ?, ?, ?, ?)";

    private static final String LINK_USER_UPDATE =
	"INSERT INTO external_users(mcid, username, provider_id) " +
	" SELECT ?, ?, identity_providers.id " +
	" FROM identity_providers WHERE source_id = ?";

    private static final String PASSWORD_UPDATE =
	"UPDATE users " +
	" SET sha1 = ? " +
	" WHERE users.mcid = ? AND " +
	" users.sha1 = ? ";

    private static final String ADD_ADDRESS_UPDATE =
	"INSERT INTO addresses(mcid, comment, address1, address2, " +
	"                      city, state, postcode, country, telephone)" +
	" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
*/
    Connection connection;
    PreparedStatement ccrlogQueryByAccid;
    PreparedStatement ccrLogQueryByAccidAndStatus;
    PreparedStatement ccrUpdateStatusByGuid;
   

    /**
     * Prepares a common set of SQL queries.
     */
    public AccountConnection(Connection c) throws SQLException {
    	log.info("AccountConnection:" + c);
    	if (c == null){
    		throw new NullPointerException("Null connection:" + c);
    	}
		this.connection = c;
		this.ccrlogQueryByAccid = c.prepareStatement(CCRLOG_QUERY_BY_ACCID);
		this.ccrLogQueryByAccidAndStatus = c.prepareStatement(CCRLOG_QUERY_BY_ACCID_AND_STATUS);
		this.ccrUpdateStatusByGuid = c.prepareStatement(CCRLOG_UPDATE_STATUS_BY_GUID);
	
    }

    private static final String INIT_MCID_QUERY =
	"SELECT mcid " +
	" FROM users " +
	" WHERE ? <= mcid AND mcid < ? " +
	" ORDER BY since DESC " +
	" LIMIT 1";

    /**
     * Retrieves the last MCID of a given type.  This can then be used as a
     * seed to retrieve the next batch of MCIDs.
     *
     * <pre>pre: 0 <= type < 10</pre>
     */
    /*
    public long getMCIDSeed(int type) throws SQLException {
	String fr_mcid = MCIDGenerator.ToString(   type    * 1000000000000000L);
	String to_mcid = MCIDGenerator.ToString((type + 1) * 1000000000000000L);

	PreparedStatement stmt = connection.prepareStatement(INIT_MCID_QUERY);
	stmt.setLong(1,    type    * 1000000000000000L);
	stmt.setLong(2, (type + 1) * 1000000000000000L);

	try {
	    ResultSet r = stmt.executeQuery();

	    try {
		if (r.next())
		    return r.getLong(1);
		else
		    return 0L;
	    } finally {
		r.close();
	    }
	} finally {
	    stmt.close();
	}

    }
    */
    
    public CCRInfo[] getCCRs(String acct) throws SQLException {
    	this.ccrlogQueryByAccid.clearParameters();
    	this.ccrlogQueryByAccid.setString(1, acct);

    	ResultSet r = this.ccrlogQueryByAccid.executeQuery();
//ccrlog.date, ccrlog.src, ccrlog.dest, ccrlog.subject
    	ArrayList<CCRInfo> ccrList = new ArrayList<CCRInfo>();
    	CCRInfo ccrs[] = null;
    	//ArrayList v = new ArrayList(); // probably should be list..
    	try {
    	    while(r.next()){
    	    	CCRInfo i = new CCRInfo();
    	    	i.setAcctId(r.getString(1));
    	    	i.setIdp(r.getString(2));
    	    	i.setGuid(r.getString(3));
    	    	i.setStatus(r.getString(4));
    	    	i.setDate(r.getString(5));
    	    	i.setSrc(r.getString(6));
    	    	i.setDest(r.getString(7));
    	    	i.setSubject(r.getString(8));
    	    	
    	    	ccrList.add(i);
    	    	log.info("Retrieved " + i);
    	    	
    	    }
    	    
    	    	
    		if(ccrList.size()==0)
    			return(null);
    	    else{
    	    	ccrs = new CCRInfo[ccrList.size()];
    	    	for (int i=0;i<ccrs.length;i++){
    	    		ccrs[i] = (CCRInfo) ccrList.get(i);
    	    	}
    	    	
    	    	log.info("CCR array has " + ccrs.length + " elements");
    	    	
    	    }
    		
    	} finally {
    	    r.close();
    	}
    	return(ccrs);
        }

    /**
     * Retrieves a URL to the MedCommons server that hosts a particular user's
     * desktop.
     *
     * <p>
     * The input is the SAML-provided source_id and username.  These two fields
     * uniquely identify a user.  Since the username is provided by an identity
     * provider, there may be duplicates: 'jim' may come in from St. Mungo's,
     * and be different from the 'jim' coming in from Boston General.
     *
     * The output is a string URL of the form:
     * 'http://something.somewhere/desktop?mcid=XXXXXXXXXXXXXXXX'
     * Tack additional query parameters at the end, like hashpw or whatever.
     *
     * <p>
     * If the user/source_id combination is not found, returns null.
     */
    /*
    String getServerUrl(String source_id, String user_name,
			Map userInfo) throws SQLException {
	this.incomingUserQuery.clearParameters();
	this.incomingUserQuery.setString(1, source_id);
	this.incomingUserQuery.setString(2, user_name);

	ResultSet r = this.incomingUserQuery.executeQuery();

	try {
	    if (r.next()) {
		userInfo.put("mcid", r.getString(2));
		userInfo.put("email", r.getString(3));
		userInfo.put("source_name", r.getString(4));
		userInfo.put("first_name", r.getString(5));
		userInfo.put("last_name", r.getString(6));

		return Expand.expand(r.getString(1), userInfo);
	    }
	    else
		return null;
	} finally {
	    r.close();
	}
    }
*/
    /**
     * Retrieves a URL to the MedCommons server that hosts a particular user's
     * desktop.
     *
     * <p>
     * The input is a form-provided MCID and raw password.  The MCID should be
     * a 16-digit number, stripped of any user-provided spaces or dashes.
     *
     * <p>
     * The output is a string URL of the form:
     * 'http://something.somewhere/desktop?mcid=XXXXXXXXXXXXXXXX'
     * Tack additional query parameters at the end, like hashpw or whatever.
     *
     * <pre>pre: mcid.length() == 16</pre>
     *
     * <p>
     * If the result is null, the login failed.  No information about whether
     * the MCID was bad or the password was bad should be given to the user.
     */
    /*
    public String login(String mcid, String password,
			Map userInfo) throws SQLException {
	this.loginQuery.clearParameters();
	this.loginQuery.setString(1, mcid);
	this.loginQuery.setString(2, Password.hash(mcid, password));

	ResultSet r = this.loginQuery.executeQuery();

	try {
	    if (r.next()) {
		userInfo.put("mcid", r.getString(2));
		userInfo.put("email", r.getString(3));
		userInfo.put("first_name", r.getString(4));
		userInfo.put("last_name", r.getString(5));

		return Expand.expand(r.getString(1), userInfo);
	    }
	    else
		return null;
	} finally {
	    r.close();
	}
    }
*/
    /**
     * Given a source_id, passed in by cookies from our SAML Relying Party
     * server (ex: 'StMungos') return the display string (ex: "Saint Mungo's")
     */
    /*
    public String getSourceName(String source) throws SQLException {
	if (source != null) {
	    this.sourceQuery.clearParameters();
	    this.sourceQuery.setString(1, source);

	    ResultSet r = this.sourceQuery.executeQuery();

	    try {
		if (r.next())
		    return r.getString(1);
	    } finally {
		r.close();
	    }
	}

	return null;
    }
*/
    /**
     * Retrieves the MCID given an email address.
     */
    /*
    public String getMCID(String email) throws SQLException {
	this.mcidQuery.clearParameters();
	this.mcidQuery.setString(1, email);

	ResultSet r = this.mcidQuery.executeQuery();

	try {
	    if (r.next())
		return r.getString(1);
	    else
		return null;
	} finally {
	    r.close();
	}
    }
*/
    /**
     * Creates a new user in the database, given an MCID, an email address, and
     * the un-hashed raw password.
     *
     * @param mcid  16-character raw MCID, stripped of user-provided spaces or dashes
     * @param email E-mail address of user
     * @param password raw user-provided password
     */
    /*
    public String newUser(String mcid, String email, String password, Map info) throws SQLException {
	this.newUserUpdate.clearParameters();
	this.newUserUpdate.setString(1, mcid);
	this.newUserUpdate.setString(2, email);
	this.newUserUpdate.setString(3, Password.hash(mcid, password));
	this.newUserUpdate.setInt(4, 1);
	this.newUserUpdate.setString(5, (String) info.get("first_name"));
	this.newUserUpdate.setString(6, (String) info.get("last_name"));
	this.newUserUpdate.setString(7, (String) info.get("mobile"));

	this.newUserUpdate.executeUpdate();

	return login(mcid, password, info);
    }

    public void addAddress(String mcid, Map info) throws SQLException {
	this.addAddressUpdate.clearParameters();
	this.addAddressUpdate.setString(1, mcid);
	this.addAddressUpdate.setString(2, (String) info.get("comment"));
	this.addAddressUpdate.setString(3, (String) info.get("address1"));
	this.addAddressUpdate.setString(4, (String) info.get("address2"));
	this.addAddressUpdate.setString(5, (String) info.get("city"));
	this.addAddressUpdate.setString(6, (String) info.get("state"));
	this.addAddressUpdate.setString(7, (String) info.get("postcode"));
	this.addAddressUpdate.setString(8, (String) info.get("country"));
	this.addAddressUpdate.setString(9, (String) info.get("telephone"));

	this.addAddressUpdate.executeUpdate();
    }
*/
    /**
     * Link a particular MCID to a SAML-provided incoming user.
     *
     * @param mcid       MCID of user
     * @param source_id  identity provider as given via cookies by PingFederate
     * @param username   external user as given via cookies by PingFederate
     */
    /*
    public void linkUser(String mcid, String source_id, String username) throws SQLException {
	this.linkUserUpdate.clearParameters();
	this.linkUserUpdate.setString(1, mcid);
	this.linkUserUpdate.setString(2, username);
	this.linkUserUpdate.setString(3, source_id);

	this.linkUserUpdate.executeUpdate();
    }

    public boolean changePassword(String mcid, String oldPassword, String newPassword)
	throws SQLException {
	this.passwordUpdate.clearParameters();
	this.passwordUpdate.setString(1, Password.hash(mcid, newPassword));
	this.passwordUpdate.setString(2, mcid);
	this.passwordUpdate.setString(3, Password.hash(mcid, oldPassword));

	return this.passwordUpdate.executeUpdate() > 0;
    }
*/
    /**
     * Close the JDBC database connection and all associated prepared
     * queries.
     */
    
    public void close() {
	closeStatement(this.ccrlogQueryByAccid);
	closeStatement(this.ccrLogQueryByAccidAndStatus);
	closeStatement(this.ccrUpdateStatusByGuid);
	
	
	try {
	    this.connection.close();
	} catch (SQLException ex) {
	    ex.printStackTrace(System.err);
	}
    }

    private void closeStatement(PreparedStatement stmt) {
	try {
	    stmt.close();
	} catch (SQLException ex) {
	    ex.printStackTrace(System.err);
	}
    }

}
