/**
 * Copyright 2010, MedCommons Inc.
 */
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

import net.oauth.*;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;

import org.json.JSONObject;

/**
 * A simple example program to show how to get authorization to 
 * access a patient account by redirecting to the MedCommons 
 * Authorization page.
 * <p>
 * For more information on the MedCommons OAuth API, see the 
 * <a href='http://healthurl.medcommons.net/api/doc'>Online Documentation</a>.
 * 
 * @author ssadedin@medcommons.net
 */
public class AuthorizeAccess {
    
    static { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e) { } }    
    
    // The consumer token details will be provided to you by MedCommons
    // or the operator of the appliance you wish to use.  You must
    // get these details as a one time operation via manual or 
    // out-of-band process in which you register your details with the
    // appliance operator.   The following are example tokens that will
    // work with demo accounts.
    public static final String consumerToken = "270ebdf10b9bb9dd957a4a14833367183a196da7"; 
    public static final String consumerSecret = "72c25b142d4dd453213b586fc1278afc446b1d89"; 
    
    // The patient id of the account that we will access
    public static final String accountId = "1013062431111407";
    
    // The base URL of the MedCommons Appliance you wish to make service 
    // calls to
    public static final String applianceBaseUrl = "https://healthurl.medcommons.net/";
    
    public static void main(String[] args)  throws Exception {
        
        // First create the OAuth credentials
        OAuthServiceProvider provider = 
            new OAuthServiceProvider(
                    applianceBaseUrl+"/api/request_token.php",
                    applianceBaseUrl+"/api/authorize.php",
                    applianceBaseUrl+"/api/access_token.php");
        
        OAuthConsumer consumer = new OAuthConsumer(null, consumerToken, consumerSecret, provider);
        OAuthAccessor accessor = new OAuthAccessor(consumer);
        
        // The OAuthClient does the actual work of making calls for us
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        
        client.getRequestToken(accessor);
        
        String authorizationURL = OAuth.addParameters(applianceBaseUrl + "/api/authorize.php", 
                "oauth_token", accessor.requestToken,
                "accid", accountId,
                "realm", "Sample Application");
        
        // Now we launch the browser, showing the authorization
        // URL so that the patient (or anyone who has appropriate access
        // to their account) can authorize it.  If this was a web server
        // program it could also add the oauth_callback parameter to 
        // send the user back to our web site after they approve access
        Desktop.getDesktop().browse(new URI(authorizationURL));
        
        showMessageDialog(null, "Click OK After Authorizing the Access Request\n\n"+
                "If you are prompted to login in, use the following credentials:\n\n" + "" +
                "User name:    jhernandez@medcommons.net\nPassword:   tester\n\n");         
        
        // Now get an access token that was assigned when the request was authorized
        client.getAccessToken(accessor, null, null);
        
        // Note: at this point the example is the same as the GetCCR example
        // where we had the access token and secret up front.  It would be normal
        // at this point to save the token and secret from the accessor in a secure 
        // database, file or other means to enable access at a later time.
        
        // First find the storage location (gateway) by passing
        // the account id to the find_storage service
        Map<String, String> params = new HashMap<String, String>();
        params.put("accid",accountId);
        OAuthMessage response = client.invoke(accessor, applianceBaseUrl+"api/find_storage.php", params.entrySet());
        String text = response.readBodyAsString();
        
        System.out.println("Got response to find_storage call : " + text);
        JSONObject obj = new JSONObject(text);
        String gwUrl = obj.getString("result");
        
        System.out.println("Storage URL for account " + accountId + " is " + gwUrl);

        // Now get the CCR from the gateway from the ccrs service on the gateway we found
        params.clear();
        params.put("fmt","xml"); // ask for it in XML format.  We could also ask for "json"
        System.out.println(gwUrl+"/ccrs/"+accountId);
        response = client.invoke(accessor, gwUrl+"/ccrs/"+accountId, params.entrySet());
        
        String ccrXml = response.readBodyAsString();
        System.out.println("Got CCR");
        
        showMessageDialog(null, "Successfully Retrieved Patient CCR - Click OK to Display it.");         
        
        new FileWriter("ccr.xml").write(ccrXml);
        
        Desktop.getDesktop().browse(new File("ccr.xml").toURI());
    }
}
