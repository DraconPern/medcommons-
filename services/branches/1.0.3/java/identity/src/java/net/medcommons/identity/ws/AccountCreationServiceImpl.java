/*
 * $Id$
 * Created on 12/06/2006
 */
package net.medcommons.identity.ws;

import static net.medcommons.identity.util.StringUtil.blank;
import static org.junit.Assert.assertEquals;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

import net.medcommons.identity.IdentityServlet;
import net.medcommons.identity.MCIDGenerator;
import net.medcommons.identity.Password;
import net.medcommons.identity.model.*;
import net.medcommons.identity.util.HibernateUtil;
import net.medcommons.modules.services.interfaces.*;
import net.medcommons.rest.RESTUtil;

import org.apache.log4j.Logger;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.service.invoker.AbstractInvoker;
import org.codehaus.xfire.soap.SoapSerializer;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONObject;
import org.junit.Test;

import com.amazonaws.ls.AmazonLS;
import com.amazonaws.ls.AmazonLSConfig;
import com.amazonaws.ls.AmazonLSException;
import com.amazonaws.ls.http.AmazonLSQuery;
import com.amazonaws.ls.model.ActivateHostedProduct;
import com.amazonaws.ls.model.ActivateHostedProductResponse;
import com.amazonaws.ls.model.ActivateHostedProductResult;

/**
 * SOAP Implementation of AccountCreationService.
 * 
 * @author ssadedin
 */
public class AccountCreationServiceImpl implements AccountCreationService {
    
    /**
     * Default number of days accounts will be left in expired state
     * before they are physically deleted.
     */
    private static final Long DEFAULT_ACCOUNT_EXPIRY_WINDOW = new Long(7);
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AccountCreationServiceImpl.class);

    /**
     * Creates the requested account, including entry in the users table.
     * @param email - email address (REQUIRED)
     * @param password - desired password (REQUIRED)
     * @param firstName - first name
     * @param lastName  - last name
     * @param telephoneNumber - telephone number
     * @param notificationRecipient - recipient of account creation notification (if any)
     * @param rlsUrl - an optional rls URL
     * @return - account id of newly created account
     */
    public String [] register(AccountType accountType, 
                               String email, 
                               String password, 
                               String firstName, 
                               String lastName, 
                               String telephoneNumber, 
                               String notificationRecipient, 
                               String rlsUrl, 
                               String photoUrl, 
                               String auth,
                               ActivationDetails activationDetails,
                               AccountOptions options) throws ServiceException 
    {
        Session s = HibernateUtil.getSession();
        try {
            String mcid = MCIDGenerator.getInstance().nextMCIDString();
            if(activationDetails != null) {
                log.info("Creating account " + mcid + " with activation details " +  activationDetails);
            }
               
            s.beginTransaction();
            User u = new User();
            u.setAccountType(accountType.name());
            u.setMcid(new Long(mcid));
            u.setFirstName(firstName);
            u.setLastName(lastName);
            if(!blank(password))
                u.setSha1(Password.hash(mcid,password));
            u.setPhotoUrl(photoUrl);
            u.server_id = 1L; 
            u.updatetime = (int)((new Date()).getTime() / 1000);
            u.since = new Timestamp(System.currentTimeMillis());
            u.ccrlogupdatetime = 0;
            u.setEmail(email);
            
            if(!blank(notificationRecipient)) 
                this.saveNotification(u, notificationRecipient);
            
             if(rlsUrl != null)  {
                AccountRLS rls = this.saveRls(s, mcid, rlsUrl);
                u.getRlsSet().add(rls);
             }
             
             if(options != null) {
                 u.setExpirationDate(options.getExpiryDate());
                 u.setEnableSimtrak(options.getEnableSimtrak() ? 1 : 0);
             }
             else 
                 u.setEnableSimtrak(0);
            
             s.save(u);
             
            // Create an authentication token for the new account
            String [] params = (auth != null) ? new String[]{ "accountIds", mcid, "auth", auth}  : new String[] { "accountIds", mcid };
            JSONObject result = RESTUtil.callJSON("", "DocumentService.createAuthenticationToken", params);
            String token = result.getString("result");
            String secret = result.getString("secret");            
            
            // If activation details were supplied, activate the account
            if(activationDetails != null) 
                this.activateAccount(s, u, activationDetails);
            
            s.getTransaction().commit();
            return new String[] { mcid, token, secret };
        }
        catch(ServiceException e) {
            log.error("Unable to register user",e);
            if(s.getTransaction()!=null && s.getTransaction().isActive())
                s.getTransaction().rollback();
            throw e;
        }
        catch(Exception e){
            log.error("Unable to register user",e);
            if(s.getTransaction()!=null && s.getTransaction().isActive())
                s.getTransaction().rollback();
            throw new ServiceException(e);
        }
        finally {
            s.close();
        }
    }
    
    /**
     * Activate the requested product for the requested user using the given 
     * activation key.
     * @throws ServiceException 
     */
    public void activate(Long mcid, String activationKey, String activationProductCode) throws ServiceException {
        Session s = HibernateUtil.getSession();
        try {
            s.beginTransaction();
                
            ActivationDetails details = new ActivationDetails();
            details.setActivationKey(activationKey);
            details.setActivationProductCode(activationProductCode);
            
            User u = (User) s.load(User.class, mcid);
            this.activateAccount(s, u, details);
            
            s.getTransaction().commit();
        }
        catch (HibernateException e) {
            throw new ServiceException("Unable to activate account " + mcid + " using key " + activationKey, e);
        }
        finally {
            s.close();
        }
    }
    
    public void activateAccount(User u, ActivationDetails activationDetails) throws ServiceException {
        this.activateAccount(null,u,activationDetails);
    }
    
    /**
     * Attempt to activate the product specified using the given activation key
     * on Amazon Payment services.
     * 
     * @param activationDetails
     * @throws ServiceException 
     */
    @SuppressWarnings("unchecked")
    public void activateAccount(Session s, User u, ActivationDetails activationDetails) throws ServiceException {
        
        if(s == null)
            s = HibernateUtil.getSession();
        
        try {
            S3Key key = this.getS3Key();
            
            if(key == null) {
                log.warn("Account activation details provided but no Amazon S3 Key could be found in configuration");
                return;
            }
            
            if(blank(activationDetails.getActivationKey()) && !blank(activationDetails.getAccountId())) { // Use amazon details from sponsor account
                log.info("Inheriting amazon details for user " + u.getMcid() + " from sponsor account " + activationDetails.getAccountId());
                User sponsor = (User)s.load(User.class, Long.parseLong(activationDetails.getAccountId()));
                if(!blank(sponsor.getAmazonPID())) {
	                u.setAmazonPID(sponsor.getAmazonPID());
	                u.setAmazonProductToken(sponsor.getAmazonProductToken());
	                u.setAmazonUserToken(sponsor.getAmazonUserToken());
                }
            }
            else { // Use new activation key to activate account
                
                AmazonLSConfig config = new AmazonLSConfig();
                config.setSignatureVersion("0");
                AmazonLS service = new AmazonLSQuery(key.getKey(), key.getSecret(), config);
                
                // String testProductToken = "{ProductToken}AAMGQXBwVGtuJF0c9kO06itDd87I7ag3M6tg6pHu42g2LRbpVPwUvaTGu4Il0pKIV+RRqeSexL5LsvRND0vdVxTR7XkPQnz4Xbc/LUXs2DOKDtnxBvvANJ8UvZ0IWb6gRgHo3al2Yrci/SD137lt4HsFvYsOObWh1PJLkseXHVTs/BvY6irL/4AVN3K3Q+5v/c1Bw7jayPPV/4pu0IF6gPAGmrIUmBBfQs2+hz1J1k2dwWjJw6IUAbDaqOUDUvHXcNRvg6U9GJf3+XpAy1usNeV0fRBlqgCqvavyRUMGFX4hShcEukxRpgM=";
                
                String defaultProductToken = "{ProductToken}AAEGQXBwVGtuSHYWBBhV3kEg54FD2/ImP8lKpXX1tX6hpdLWAsccBnJ9FY/xdCAFm5NkFB+AU7LiW7ybSv3SOOUeT0Va7Wu+RPxNULKCvQoriDRW3xZnSpnTsqIu47obdpoQArwg51T3GgV0SdEudbMWl9v+8S+12+p3Z3KRh5HAWeeYMl5T3F3qzU3+oYj2rpxOVphKrQkRai+INpr8WCbQ9G4RVJvskeWNTNdaql+vWsI+Bv14AXf90cVmjuO3rQ4G++JVRzvJV7izBUDvQm2sylwHicBljRfusyaFkm1+8kIqxO2bUyQ=";
                String productToken = IdentityServlet.config.getProperty("productToken", defaultProductToken);                        
                
                // Look in mcproperties to see if product token specified there
                List<MCProperty> props = s.createQuery("from MCProperty where property = 'acProductToken'").list();
                if(!props.isEmpty()) {
                    log.info("Found product token configured as mcproperty");
                    productToken = props.get(0).getValue();
                }
                
                log.info("Activating account " + u.getMcid() + " with key " + activationDetails.getActivationKey() + " and  product token " + productToken);
                
                // Activate the hosted product
                ActivateHostedProduct p = new ActivateHostedProduct();
                p.setActivationKey(activationDetails.getActivationKey());
                p.setProductToken(productToken);
                
                ActivateHostedProductResponse response = service.activateHostedProduct(p);
                ActivateHostedProductResult result = response.getActivateHostedProductResult();
                String userToken = result.getUserToken();
                String pid = result.getPersistentIdentifier();
                
                log.info("Successfully activated using key " + activationDetails.getActivationKey() + " received {userToken:"+userToken+" pid:" + pid + "} in response");
                u.setAmazonUserToken(userToken);
                u.setAmazonProductToken(productToken);
                u.setAmazonPID(pid);
            }
            s.update(u);
        }
        catch (AmazonLSException e) {
            throw new ServiceException("Failed to activate account " + u.getMcid() + " using activation key " + activationDetails.getActivationKey() + ": "+e.getMessage(),e);
        }
    }


    private S3Key getS3Key() {
        String keyId = IdentityServlet.config.getProperty("s3AccessKeyId");
        String keySecret = IdentityServlet.config.getProperty("s3SecretAccessKey");
        
        // Default to system properties if not found
        if(keyId == null) {
            keyId = System.getProperty("s3AccessKeyId");
            keySecret = System.getProperty("s3SecretAccessKey");
        }
        
        if(keyId != null) {
            return new S3Key(keyId, keySecret);
        }
        
        // Try reading the file
        return loadS3KeyFromFile("/opt/mc_backups/mc_backups.rc");
    }


    public S3Key loadS3KeyFromFile(String fileName) {
        try {
            File f = new File(fileName);
            InputStream s = new FileInputStream(f);
            byte buffer[] = new byte[(int) f.length()];
            s.read(buffer);
            String contents = new String(buffer, "UTF-8");
            StringTokenizer t = new StringTokenizer(contents);
            t.nextToken();
            String keyId = t.nextToken();
            t.nextToken();
            String secret = t.nextToken();
            
            return new S3Key(keyId, secret);
        }
        catch (FileNotFoundException e) {
            log.warn("Unable to load s3 keys from file " + fileName + ": " + e.getMessage());
        }
        catch (UnsupportedEncodingException e) {
            log.warn("Unable to load s3 keys from file " + fileName + ": " + e.getMessage());
        }
        catch (IOException e) {
            log.warn("Unable to load s3 keys from file " + fileName + ": " + e.getMessage());
        }
        return null;
    }
    
    @Test
    public void testLoadS3Key() throws Exception {
        AccountCreationServiceImpl acs = new AccountCreationServiceImpl();
        S3Key k = acs.loadS3KeyFromFile("test-src/test_mcbackup.rc");
        
        assertEquals("0ZF092W7X1V2MQ3HT6R2", k.getKey());
        assertEquals("yOe7u8hCdydo+A2TILQ01e29k0SezIE9KgidaC+3", k.getSecret());
    }

    private void setPhotoUrl(String mcid, String photoUrl) throws ServiceException {
        
        Session s = HibernateUtil.getSession();
        try {
            s.beginTransaction();
            User u = (User) s.load(User.class, new Long(mcid));
            u.setPhotoUrl(photoUrl);
            s.update(u);
            s.getTransaction().commit();
        }
        catch(HibernateException ex) {
            s.getTransaction().rollback();
            log.error("Unable to save notification: ", ex);
        }
        finally {
            s.close();
        }
         
    }

    private AccountRLS saveRls(Session s, String mcid, String rlsUrl) throws ServiceException {
        log.info("Setting rls url " + rlsUrl + " for new user " + mcid);
        AccountRLS acctRls = new AccountRLS();
        acctRls.setAccountId(mcid);
        acctRls.setRlsUrl(rlsUrl);
        s.save(acctRls);
        return acctRls;
    }
    
    private void saveNotification(User u, String notificationRecipient) throws ServiceException {
        Session s = HibernateUtil.getSession();
        AccountNotification n = new AccountNotification();
        n.recipient = notificationRecipient;
        n.status = "Active";
        n.user = u;
        s.save(n);
    }
    
    /**
     * Translates the given emails to account ids or null
     * 
     * @param emails - array of email addresses
     * @return - array with one member for each email in the input array.
     * @throws ServiceException 
     */
    public String [] translateAccounts(String [] emails) throws ServiceException {
        Session s = HibernateUtil.getSession();
        ArrayList<String> results = new ArrayList<String>();
        log.debug("Translating " + emails.length + " emails");
        try {
            s.beginTransaction();
            Query q = s.createQuery("from User u where email = :email");
            for (String email : emails) {
                q.setString("email", email);
                List<User> l = q.list();
                if(!l.isEmpty()) {
                    results.add(String.valueOf(l.get(0).mcid));
                }
                else
                    results.add(null);
            }
            return results.toArray(new String[results.size()]);
        }
        catch(HibernateException ex) {
            log.error("Unable to translate accounts: ",ex);
            throw new ServiceException("Unable to translate accounts", ex);
        }
        finally {
            s.close();
        }
    }

    /**
     * Queries for accounts created since a specified date.
     * 
     * @param since
     * @return - XML feed of new accounts created since the specified date
     * @throws ServiceException 
     */
    public String queryCreated(String recipient, Long since, Boolean delete) throws ServiceException {        
        Session s = HibernateUtil.getSession();
        try {
            s.beginTransaction();
            List notifications = 
                s.createQuery("from AccountNotification n where n.recipient = :recipient and n.user.since > :since")
                .setString("recipient", recipient)
                .setTimestamp("since", new Timestamp(since*1000))
                .list();            
            
            StringBuilder result = new StringBuilder("<result>");
            result.append("<serverTime>"+(System.currentTimeMillis()/1000)+"</serverTime>\n<accounts>\n");
            for (Iterator iter = notifications.iterator(); iter.hasNext();) {
                AccountNotification n = (AccountNotification) iter.next();
                result.append("<user><mcid>").append(n.user.mcid).append("</mcid>").append("</user>\n");
                if(Boolean.TRUE.equals(delete)) {
                    n.status = "Deleted";
                    s.update(n);
                }
            }
            result.append("</accounts></result>");
            return result.toString();
        }
        catch (HibernateException e) {
            log.error("Unable to query for created accounts",e);
            if(s.getTransaction()!=null)
                s.getTransaction().rollback();            
            throw new ServiceException(e);
        }
        finally {
            s.close();
        }
    }

    public long next_mcid() throws ServiceException {
        String mcid;
        try {
            MessageContext ctx = AbstractInvoker.getContext();
            ctx.setProperty(SoapSerializer.SERIALIZE_PROLOG, true);
            
            mcid = MCIDGenerator.getInstance().nextMCIDString();
            return Long.parseLong(mcid);
        }
        catch (IOException e) {
            throw new ServiceException(e);
        }
    }

    public void confirmAccount(String accountId) throws ServiceException {
        Session s = HibernateUtil.getSession();
        try {
            s.beginTransaction();
            User u = (User) s.load(User.class, new Long(accountId));
            if(AccountType.PROVISIONAL.name().equals(u.getAccountType())) {
               u.setAccountType(AccountType.USER.name()); 
            }
            s.update(u);
            s.getTransaction().commit();
        }
        catch(HibernateException ex) {
            s.getTransaction().rollback();
            log.error("Unable to confirm account " + accountId,ex);
            throw new ServiceException("Unable to confirm account", ex);
        }
        finally {
            s.close();
        }
    }


    public String [] translate(String[] accounts) throws ServiceException {
        
        Session s = HibernateUtil.getSession();
        ArrayList<String> results = new ArrayList<String>();
        log.debug("Translating " + accounts.length + " emails");
        try {
            s.beginTransaction();
            for (String a : accounts) {
                User u = (User) s.get(User.class, Long.parseLong(a));
                results.add(u != null ? u.getEmail() : null);
            }
            return results.toArray(new String[results.size()]);
        }
        catch(HibernateException ex) {
            log.error("Unable to translate accounts",ex);
            throw new ServiceException("Unable to translate accounts", ex);
        }
        finally {
            s.close();
        }
    }
    
    public Long accountExpiryWindow = null;

    /**
     * Query for all accounts that are past their 'expired' date but have not been set to 'DELETED'
     * 
     * @return
     */
    public String[] queryExpiredAccounts() {
        Session s = HibernateUtil.getThreadSession();
        try {
            s.beginTransaction();
            
            Long expiryDays = getAccountExpiryDays();
            
            if(expiryDays == null || expiryDays <= 0)
                throw new IllegalArgumentException("Invalid value " + expiryDays + " configured for account expiry window");
            
            log.info("Querying for accounts that expired more than " + expiryDays + " ago");
            Date expiryDate = new Date(System.currentTimeMillis() - expiryDays * 24 * 3600 * 1000);
            List<User> expired = s.createQuery("from User u where " +
                                              "(u.expirationDate < :date and u.accountType != 'DELETED') or u.accountType = 'EXPIRE_IMMEDIATE'")
                                  .setDate("date",expiryDate)
                                  .list();
            
            String [] result = new String[expired.size()];
            int i=0;
            for(User u : expired) {
                result[i++] = u.getMcid().toString();
            }
            return result;
        }
        finally {
            HibernateUtil.closeSession();
        }
    }
    
    private Long getAccountExpiryDays() {
        Session s = HibernateUtil.getSession();
        if(accountExpiryWindow == null) {
            MCProperty expiryValue = 
                (MCProperty) s.createQuery("from MCProperty p where p.property = 'acAccountExpiryWindow'")
                              .uniqueResult();
            if(expiryValue != null)
                accountExpiryWindow = Long.parseLong(expiryValue.getValue());
            else
                accountExpiryWindow = DEFAULT_ACCOUNT_EXPIRY_WINDOW; 
        }
        return accountExpiryWindow;
    }

    static final Set<String> ALLOWED_DELETE_STATES = new HashSet<String>() {{
       add(AccountType.USER.name());
       add(AccountType.SPONSORED.name());
       add(AccountType.VOUCHER.name());
       add(AccountType.EXPIRE_IMMEDIATE.name());
    }};
    
    static final Set<AccountType> VALID_DELETED_STATES = new HashSet<AccountType>() {{
       add(AccountType.DELETE_FAIL);
       add(AccountType.DELETED);
       add(AccountType.DELETE_WARN);
    }};
    
    @Override
    public void deleteExpiredAccount(String accid, String deleteStatus) throws ServiceException {
        Session s = HibernateUtil.getThreadSession();
        try {
            s.beginTransaction();
            
            User u = (User) s.load(User.class, Long.valueOf(accid));
            if(!ALLOWED_DELETE_STATES.contains(u.getAccountType())) 
                throw new IllegalStateException("Attempt to delete account " + accid + " not in valid deletable state (" + u.getAccountType() + ")");
            
            AccountType status = AccountType.DELETED;
            if(!blank(deleteStatus)) {
                status = AccountType.valueOf(deleteStatus);
                if(!VALID_DELETED_STATES.contains(status))
                    throw new IllegalArgumentException("Invalid deleted account status provided: " + deleteStatus);
            }
            
            u.setAccountType(status.name());
            s.update(u);
            s.getTransaction().commit();
        }
        catch(Exception e) {
            log.error("Failed to set user account " + accid + " to deleted status "+ deleteStatus,e);
            if(s.getTransaction() != null && s.getTransaction().isActive())
                s.getTransaction().rollback();
        }
        finally {
            HibernateUtil.closeSession();
        }
    }
}
