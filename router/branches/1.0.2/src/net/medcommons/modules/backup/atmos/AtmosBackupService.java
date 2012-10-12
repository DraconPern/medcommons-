package net.medcommons.modules.backup.atmos;

import static net.medcommons.modules.utils.Str.join;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.services.interfaces.BackupException;
import net.medcommons.modules.services.interfaces.BackupService;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * An implementation of the backup service that stores data in EMC Atmos
 * 
 * @author ssadedin
 */
@SuppressWarnings("unchecked")
public class AtmosBackupService implements BackupService {
    
    private static final String ERROR_OBJECT_EXISTS = "1016";

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AtmosBackupService.class);

    static final String EMC_ROOT = "http://accesspoint.emccis.com/rest/namespace/medcommons";
        
    /**
     * The token identifying our application in EMC/Atmos
     */
    String tokenId; 
    
    /**
     * Shared secret from Atmos console
     */
    String sharedSecret; 
    
    public AtmosBackupService() throws ConfigurationException {
        this.tokenId = Configuration.getProperty("acAtmosTokenId");
        this.sharedSecret = Configuration.getProperty("acAtmosSharedSecret");
    }

    /**
     * Try first to send using POST (create), and if that fails with 
     * 'already exists' error then try to PUT (update).
     * <p>
     * This may be very inefficient - it might send a whole DICOM series up
     * once with POST only to find it has to send the whole thing again with PUT.
     * <p>
     * It might make sense to use some kind of heuristic based on the file size
     * as to whether we optimistically try to POST first or instead probe to
     * see if the file already exists.
     */
    @Override
    public void backup(String accountId, String name, File f) throws BackupException {
        try {
            PostMethod post = new PostMethod(getDataURL(accountId, name));
            backup(post, accountId, name, f);
            log.info("Successfully backed up file " + name);
        }
        catch(ObjectExistsException exExists) {
            PutMethod post = new PutMethod(getDataURL(accountId, name));
            backup(post, accountId, name, f);
            log.info("Successfully backed up file " + name);
        }
    }

    public void backup(EntityEnclosingMethod method, String accountId, String name, File f) throws BackupException {
        HttpClient client = new HttpClient();
        method.addRequestHeader("Content-Type", "application/octet-stream");
        method.addRequestHeader("Content-Length", String.valueOf(f.length()));
        sign(method);
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            method.setRequestEntity(new InputStreamRequestEntity(in, f.length()));
            int status = client.executeMethod(method);
            if(status >= 400) {
                String xml = method.getResponseBodyAsString();
                Document doc = new SAXBuilder(false).build(new StringReader(xml));
                String errorCode = doc.getRootElement().getChildTextTrim("Code");
                if(ERROR_OBJECT_EXISTS.equals(errorCode)) {
                    log.info("File " + name + " already exists in backup for user " + accountId);
                    throw new ObjectExistsException();
                }
                
                throw new IOException("Send to Atmos failed with status " + status + " and returned content: " + xml);
            }
        }
        catch(ObjectExistsException e) {
            throw e;
        }
        catch (Exception e) {
            throw new BackupException("Failed to send data from file " + f.getAbsolutePath() + " to Atmos",e);
        }
        finally {
            closeQuietly(in);
        }
    }
    
    @Override
    public void delete(String accountId, String name) throws BackupException {

    }

    @Override
    public InputStream openStream(String accountId, String name) throws BackupException {
        try {
            HttpClient client = new HttpClient();
            GetMethod get = new GetMethod(getDataURL(accountId, name));
            get.addRequestHeader("accept", "*/*");
            sign(get);
            
            int status = client.executeMethod(get);
            if(status == 404)
                return null;
            else
            if(status >= 400)
                throw new IOException("Retrieve from Atmos failed with status " + status + " and returned content: " + get.getResponseBodyAsString());
            
            return get.getResponseBodyAsStream();
        }
        catch (Exception e) {
            throw new BackupException("Failed to read resource " + name + " for patient " + accountId + " from Atmos",e);
        }
    }

    @Override
    public void restore(String accountId, String name, File f) throws BackupException {
        log.info("Restoring " + name + " for account " + accountId + " in location " + f.getAbsoluteFile());
        OutputStream out = null;
        InputStream in = null;
        try {
            // Want to avoid partial downloads getting treated as the real file so 
            // we download to a temp file and only rename it when we are sure that we got 
            // the whole thing.
            File tmp = new File(f.getAbsolutePath() + "__atmos_tmp"+System.currentTimeMillis());
            out = new FileOutputStream(tmp);
            in  = openStream(accountId, name);
            if(in == null) {
                tmp.delete();
                return;
            }
            
            IOUtils.copy(in, out);
            
            if(f.exists())
                if(!f.delete())
                    throw new BackupException("Unable to delete existing file " + f.getAbsolutePath() + " for replacement  with backup copy");
            
            out.close();
            
            if(!tmp.renameTo(f))
                throw new BackupException("Unable to rename backup file " + tmp.getAbsolutePath() + " to restore target " + f.getAbsolutePath());
        }
        catch(Exception e) {
            throw new BackupException("Unable to restore file " + name + " to location " + f,e);
        }
        finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    HashMap<Class,String> verbs = new HashMap() {{
      put(GetMethod.class, "GET");
      put(PutMethod.class, "PUT");
      put(PostMethod.class, "POST");
    }};
    
    /**
     * Signs method call according to procedure described in Atmos documentation.
     * 
     * @param method
     * @return
     * @throws BackupException
     */
    private String sign(HttpMethod method) throws BackupException {
        
        try {
            Date date = new Date();
            DateFormat fmt = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH );
            fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            String dateString = fmt.format(date);
            
            method.addRequestHeader("x-emc-uid", tokenId);
            method.addRequestHeader("x-emc-date", dateString);
            method.addRequestHeader("Date", dateString);
            
            
            List<String> emcHeaders = new ArrayList<String>();
            for(Header h : method.getRequestHeaders()) {
                if(h.getName().startsWith("x-emc"))
                    emcHeaders.add(h.getName().toLowerCase() + ":" + join(h.getValue().trim().split("\n"),""));
            }
            
            Collections.sort(emcHeaders);
            String canonicalHeaders = join(emcHeaders,"\n");
            
            // log.info("Canonicalized headers: " + canonicalHeaders);
            
            String verb = verbs.get(method.getClass());
            if(verb == null)
                throw new IllegalArgumentException("Unexpected HTTP Verb " + method.getClass().getName());
            
            String contentType = "";
            Header contentTypeHeader =  method.getRequestHeader("Content-Type");
            if(contentTypeHeader != null) {
                contentType = contentTypeHeader.getValue();
            }
            
            String toHash = verb + "\n"+
                    contentType + "\n"+
                    "\n" + // range
                    dateString + "\n" +
                    method.getPath() + "\n" +
                    canonicalHeaders;
            
            // log.info("signature hashed on value:\n" + toHash);
            
            Base64 b64 = new Base64();
            
            // Let's calculate the signature
            SecretKeySpec key = new SecretKeySpec(b64.decode(sharedSecret.getBytes()), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);        
            String signature = new String(b64.encode(mac.doFinal(toHash.getBytes())),"ASCII");
            method.addRequestHeader("x-emc-signature", signature);
            
            return signature;
        }
        catch (Exception e) {
            throw new BackupException("Unable to sign Atmos request");
        }
    }
    
    private String getDataURL(String accountId, String name) {
        return EMC_ROOT + "/" + accountId + "/" + name;
    }
}
