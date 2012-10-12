/**
 * Copyright 2009, MedCommons Inc.
 */
import static java.lang.String.format;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.client.OAuthClient;
import net.oauth.client.URLConnectionClient;
import net.oauth.client.OAuthClient.ParameterStyle;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * A simple example program to show how to launch
 * a DICOM on Demand upload and then fetch the images
 * for the uploaded patient using the OAuth API.
 * <p>
 * This example works by using the MedCommons DOD service which allows you 
 * to upload image data directly into a group's patient list.
 * To do so it uses two separate authentication mechanisms:
 * <ul><li>OAuth - for applications to identify themselves (this application)
 *     <li>Login Service - for people to identify themselves (your user account)
 * </ul>
 * <p>
 * The example proceeds roughly in four separate phases:
 * <ol>
 *     <li>Create a unique order reference and launch the browser
 *         to allow upload of DICOM
 *     <li>Login to the MedCommons Appliance and use the login
 *         authorization to poll the group patient list
 *         until the new patient for the order appears.
 *     <li>Download the Current CCR for the patient and parse 
 *         it to find references to DICOM series
 *     <li>Create an HTML file with links to the uploaded images
 *         and then show that file in the browser
 * </ol>
 * <p>
 * <i><b>Please Note:</b></i>  This example requires Java 1.6 or greater.
 * <p>
 * For more information on the MedCommons OAuth API, see the 
 * <a href='http://healthurl.medcommons.net/api/doc'>Online Documentation</a>.
 * 
 * @author ssadedin@medcommons.net
 */
public class GetImageURLs {
    
    // The consumer token details will be provided to you by MedCommons
    // or the operator of the appliance you wish to use.  You must
    // get these details as a one time operation via manual or 
    // out-of-band process in which you register your details with the
    // appliance operator.
    public static final String consumerToken = "270ebdf10b9bb9dd957a4a14833367183a196da7"; 
    public static final String consumerSecret = "72c25b142d4dd453213b586fc1278afc446b1d89"; 
    
    // For this example we are using the account id of the default demo
    // group that is publicly accessible inside MedCommons.  You would
    // need to register a group and change it to be the account id of your
    // group (find it on the Settings page after you log in).
    public static final String groupAccountId = "1172619833385984";
    
    // We need the login details for a user who is a member of the above 
    // group.  In this case we are using a fixed demo account that
    // always has these credentials.
    public static final String LOGIN_EMAIL_ADDRESS = "demodoctor@medcommons.net";
    public static final String LOGIN_PASSWORD = "tester";
    
    // The base URL of the MedCommons Appliance you wish to make service 
    // calls to
    public static final String applianceBaseUrl = "https://healthurl.medcommons.net/";
    
    // For convenience we make an xpath instance to help pull
    // data out of the documents we will retrieve
    public static XPath xpath = XPathFactory.newInstance().newXPath();
    
    public static void main(String[] args)  throws Exception {
        
        // Start by inventing a random order reference - this could be 
        // anything, as long as it is unique
        String ref = String.valueOf(System.currentTimeMillis());
        
        // Start by opening the browser on the upload page so the data can get uploaded
        Desktop.getDesktop().browse(new URI(applianceBaseUrl+groupAccountId+"/upload?callers_order_reference="+ref+"&oauth_consumer_key="+consumerToken));
        
        // Create the OAuth credentials will use to make our calls
        OAuthConsumer consumer = new OAuthConsumer(null, consumerToken, consumerSecret, null);
        OAuthAccessor accessor = new OAuthAccessor(consumer);
        
        // The OAuthClient does the actual work of making calls for us
        OAuthClient client = new OAuthClient(new URLConnectionClient());
        
        // Login to MedCommons as the group user and get the auth token that
        // lets us perform operations as that user.  We need this 
        // to query the patient list of the group and see when our order has
        // arrived
        String auth = login(accessor, client);
        System.out.println("Auth token from login is " + auth);
        
        //////////////////  Poll Patient List to Wait for Order to Arrive /////////////
        Map<String, String> params = new HashMap<String, String>();
        params.put("auth",auth);
        String accountId;
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        while(true) {
            // Get the atom feed for our group
            InputStream atomStream = 
                client.invoke(accessor, applianceBaseUrl+"acct/patient_list_atom.php", params.entrySet())
                      .getBodyAsStream();
            
            // Parse the atom feed and look for our order in there
            Document dom = parser.parse(atomStream);
            
            // new XMLSerializer(System.out, new OutputFormat(dom)).serialize(dom);
            
            accountId = xpath.evaluate(format("/feed/entry[order-reference='%s' and status='DDL_ORDER_UPLOAD_COMPLETE']/id",ref),dom);
            if(accountId != null && accountId != "") { // Found our order entry in the patient list!
                accessor.accessToken = 
                    xpath.evaluate(format("/feed/entry[order-reference='%s' and status='DDL_ORDER_UPLOAD_COMPLETE']/auth-token",ref),dom);
                accessor.tokenSecret = 
                    xpath.evaluate(format("/feed/entry[order-reference='%s' and status='DDL_ORDER_UPLOAD_COMPLETE']/auth-secret",ref),dom);
                break;
            }
            else
                System.out.println("Order " + ref + " not complete yet!");
            
            // Wait some time before requesting the patient list again
            Thread.sleep(5000);
        }
        System.out.println("Found patient id " + accountId + " with auth token " + accessor.accessToken);
        
        
        //////////////////  Now that we found patient, get their CCR /////////////
        
        // First find the storage location (gateway) by passing
        // the account id to the find_storage service
        params.clear();
        params.put("accid",accountId);
        JSONObject obj = new JSONObject(
                client.invoke(accessor, applianceBaseUrl+"api/find_storage.php",params.entrySet()).readBodyAsString());
        
        String gwUrl = obj.getString("result");
        
        System.out.println("Storage URL for account " + accountId + " is " + gwUrl);
        
        String ccrUrl = gwUrl+"/ccrs/"+accountId;
        System.out.println("Current CCR URL for account " + accountId + " is " + ccrUrl);

        params.clear();
        params.put("fmt", "xml");
        // Now get the CCR from the gateway from the ccrs service on the gateway we found
        Document ccr = parser.parse(client.invoke(accessor, ccrUrl, params.entrySet()).getBodyAsStream());
        
        
        //////////////////  We have the CCR - find the references to DICOM Series inside it /////////////
        
        // Find DICOM references
        NodeList references = (NodeList)xpath.evaluate(
                "//Reference[Type/Text='application/dicom']/Locations/Location/Description/ObjectAttribute[Attribute='URL']/AttributeValue/Value", 
                ccr, XPathConstants.NODESET);
        
        NodeList names = (NodeList)xpath.evaluate(
                "//Reference[Type/Text='application/dicom']/Locations/Location/Description/ObjectAttribute[Attribute='DisplayName']/AttributeValue/Value", 
                ccr, XPathConstants.NODESET);
        
        File outputDir = new File(System.getProperty("user.home"));
        
        // As a demonstration we will make an HTML file containing links
        // to some images from the uploaded data
        File outputFile = new File(outputDir, "images.html");
        Writer out = new FileWriter(outputFile);
        out.write(format("<html><body><h2>Images Uploaded for Order %s</h2><ul>",ref));
        
        for(int i=0; i<references.getLength(); ++i) {
            String referenceURL = references.item(i).getTextContent();
            System.out.println("Found Reference URL " + referenceURL);
            
            // Each series stored in a medcommons account is identified by a guid - here we 
            // extract the guid from the location URL embedded in the CCR 
            String guid = referenceURL.substring(7);
            
            params.clear();
            params.put("mcGUID",guid);
            
            // Get the first image - that happens automatically by leaving other parameters blank
            String imageUrl = gwUrl + "/wado/" + accountId;
            System.out.println("Image url is " + imageUrl);
            
            // We have to sign the image URL to access it
            imageUrl = accessor.newRequestMessage("GET", imageUrl, params.entrySet())
                                      .toHttpRequest(ParameterStyle.QUERY_STRING)
                                      .url.toExternalForm();
            
            System.out.println("Signed url is " + imageUrl);
            
            out.write(format("<li><img src='%s' width=40/> <a href='%s'>"+names.item(i).getTextContent()+"</a></li>", 
                             imageUrl, imageUrl));
        }
        out.write("</ul></body></html>");
        out.close();
        
        Desktop.getDesktop().browse(outputFile.toURI());
    }

    /**
     * Logs into the MedCommons Appliance using the login credentials
     * and returns the 'auth' token from the login response.
     */
    private static String login(OAuthAccessor accessor, OAuthClient client) throws Exception {
        // First, log in to our group to get an auth token that will let us do the rest 
        // of the operations we want to do
        Map<String, String> params = new HashMap<String, String>();
        params.put("email",LOGIN_EMAIL_ADDRESS);
        params.put("password",LOGIN_PASSWORD);
        JSONObject loginResult = new JSONObject(client.invoke(accessor, applianceBaseUrl+"acct/loginservice.php", 
                                                              params.entrySet()).readBodyAsString());
        
        if(!loginResult.getString("status").equals("ok"))
            throw new Exception("Login failed!  Please check credentials.");
        
        System.out.println("Login result is: " + loginResult);
        
        // We need the "auth token" from the login response
        return loginResult.getJSONObject("result").getString("auth");
    }
}
