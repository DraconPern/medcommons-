package net.medcommons.s3;

import static net.medcommons.modules.utils.Str.blank;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.medcommons.modules.crypto.Base64Coder;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.io.IOUtils;


class S3InputStream extends FilterInputStream {
    private HttpMethod method;

    S3InputStream(HttpMethod method) throws IOException {
        super(method.getResponseBodyAsStream());
        this.method = method;
    }

    public void close() throws IOException {
        super.close();
        this.method.releaseConnection();
    }
}

/**
 * Sends and receives content from Amazon's S3 storage service.
 *
 * If your application keeps track of Amazon S3 credentials (the
 * 20-character key ID, and 40-character secret key), then create
 * an S3Client with the 2-argument constructor.
 *
 *  <pre>
 *     new S3Client(publicKeyId, privateKey);
 *  </pre>
 *
 * Otherwise, create an S3Client with the default constructor, and
 * the key and key ID will be read from the standard files '.s3/key' and
 * '.s3/key_id' relative to the user's home directory.
 *
 *  <pre>
 *     new S3Client()
 *  </pre>
 *
 * Getting content from S3 is easy enough... just call the get() method,
 * passing in the bucket and object names.
 *
 * Putting content is trickier.  S3 requires an md5 hash of the entire
 * content, and of course HTTP requires the content length.  This
 * requires two passes over the content: the application must read
 * through all the content to find the md5 hash (see the md5 method for
 * details on how to do this) and then call the put() method with
 * the bucket and object names, along with the input stream (reset to
 * the beginning of the content, of course) and the md5 hash and the
 * content length.
 */
public class S3Client {

    public static final String REST_HOST = "s3.amazonaws.com";

    public static final String REST_NS = "http://s3.amazonaws.com/doc/2006-03-01/";

    public static final String MAC_ALGORITHM = "HmacSHA1";
    public static final String HASH_ALGORITHM = "MD5";

    public static final String ENCODING = "UTF-8";

    private String key_id;
    private String secret;
    
    /**
     * SS: optional product and user tokens to use for storage
     */
    private String productToken;
    
    private String userToken;

    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);


    private static final String ERR_DESC = " not supported?  Give me a break!";


    public static void main(String args[]) {
        try {
            if (args.length == 0)
                usage();
            else if (args.length == 3 && (args[0].equals("get"))) {
                S3Client s3client = new S3Client();

                s3client.get(args[1], args[2], System.out);
            }
            else if (args.length == 4 && (args[0].equals("put"))) {
                S3Client s3client = new S3Client();

                File f = new File(args[3]);

                byte[] hash = md5(new java.io.FileInputStream(f));

                s3client.put(args[1], args[2], hash,
                             "application/octet-stream", f.length(),
                             new java.io.FileInputStream(f));
            }

            else
                usage();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    static void usage() {
        System.err.println("Usage: java S3Client get {bucket} {object}");
        System.err.println("       java S3Client put {bucket} {object} {file}");
    }

    /**
     * Create an S3Client with Amazon-supplied public key ID and
     * private key.
     *
     * @param publicKeyId  the _key_id_ property
     * @param privateKey   the _key_ property
     */
    public S3Client(String publicKeyId, String privateKey) {
        this.key_id = publicKeyId;
        this.secret = privateKey;

        init();
    }

    /**
     * Create an S3Client, reading the Amazon-supplied keys from
     * files.
     *
     * <pre>
     *  ~/.s3/key
     *  ~/.s3/key_id
     * </pre>
     */
    public S3Client() throws IOException {
        File homeDir = new File(System.getProperty("user.home"), ".s3");

        this.key_id = readFile(homeDir, "key_id");
        this.secret = readFile(homeDir, "key");

        init();
    }

    private final String readFile(File dir, String file) throws IOException {
        FileReader r = new FileReader(new File(dir, file));

        try {
            char[] buffer = new char[80];

            int i = r.read(buffer);

            return new String(buffer, 0, i).trim();
        } finally {
            r.close();
        }
    }

    private final void init() {
        /* GMT */
        dateFormatter.setTimeZone(TimeZone.getTimeZone(""));
    }
        
    /**
     * Get the _key_id_ property.
     *
     * The key ID is a 20-character base64 encoded public ID,
     * given to an Amazon S3 account holder.
     */
    public String getKeyId() {
        return this.key_id;
    }

    /**
     * Set the _key_id_ property.
     *
     * The key ID is a 20-character base64 encoded public ID,
     * given to an Amazon S3 account holder.
     */
    public void setKeyId(String keyId) {
        this.key_id = keyId;
    }

    /**
     * Get the _key_ property.
     *
     * The key is a 40-character base64 encoded secret, given
     * to an Amazon S3 account holder.  The _key_ is intended to stay
     * private, so take care not to release it!
     */
    public String getKey() {
        return this.secret;
    }

    /**
     * Set the _key_ property.
     *
     * The key is a 40-character base64 encoded secret, given
     * to an Amazon S3 account holder.  The _key_ is intended to stay
     * private, so take care not to release it!
     */
    public void setKey(String key) {
        this.secret = key;
    }

    /**
     * Copies an object from Amazon S3 to an OutputStream
     *
     * @param bucket  an Amazon S3 bucket, must have been created already
     * @param object  object name for this data
     * @param out     output stream to copy to
     */
    public int get(String bucket, String object,
                   OutputStream out) throws IOException {
        String url = "https://" + REST_HOST + '/' + bucket + '/' + object;

        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);

        headers(method, null, null);

        try {
            int status = client.executeMethod(method);
            InputStream in = method.getResponseBodyAsStream();

            try {
                copy(in, out);
                return status;
            } 
            finally {
                closeQuietly(in);
            }
        } finally {
            method.releaseConnection();
        }
    }
    
    
    /**
     * Attempts to delete the specified object from the specified bucket 
     * 
     * @param bucket
     * @param object
     * @return
     * @throws IOException
     */
    public int delete(String bucket, String object) throws IOException {
        String url = "https://" + REST_HOST + '/' + bucket + '/' + object;

        HttpClient client = new HttpClient();
        HttpMethod method = new DeleteMethod(url);
        headers(method, null, null);
        try {
            int status = client.executeMethod(method);
            return status;
        } 
        finally {
            method.releaseConnection();
        }
    }
    

    public InputStream getInputStream(String bucket,
                                      String object) throws IOException {
        String url = "https://" + REST_HOST + '/' + bucket + '/' + object;

        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);

        headers(method, null, null);

        int status = client.executeMethod(method);

        if (status / 100 != 2)
            return null;

        return new S3InputStream(method);
    }
    
    /**
     * Sends a file to S3.  
     * <p>
     * Internally calculates md5 hash for you, but this means it iterates
     * the stream twice - so do not use this method if you can do that 
     * more efficiently.
     */
    public void put(String bucket, String object, String contentType, File f) throws IOException {
        InputStream in = new FileInputStream(f);
        try {
                byte[] hash = md5(in);
                IOUtils.closeQuietly(in);
                in = new FileInputStream(f);
                put(bucket, object, hash, contentType, f.length(), in);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Copies an InputStream to Amazon S3
     *
     * @param bucket  an Amazon S3 bucket, must have been created already
     * @param object  object name for this data
     * @param hash    binary md5 hash of entire input stream
     * @param contentType  MIME type of input stream, can use "application/octet-stream"
     * @param contentLength  length, in bytes or octets of entire input stream
     * @param in     input stream to copy to
     */
    public void put(String bucket, String object, byte[] hash,
                    String contentType, long contentLength,
                    InputStream in) throws IOException {
        
        String url = "https://" + REST_HOST + '/' + bucket + '/' + object;
        
        HttpClient client = new HttpClient();
        
        HostConfiguration hc = new HostConfiguration();
        // hc.setProxy("127.0.0.1", 8080);
        client.setHostConfiguration(hc);
        
        PutMethod method = new PutMethod(url);
        
        RequestEntity content = new InputStreamRequestEntity(in, contentLength,
                contentType);
        
        headers(method, hash, contentType);
        method.addRequestHeader("Content-Length",
                Long.toString(contentLength));
        
        method.setRequestEntity(content);
        
        try {
            int status = client.executeMethod(method);
            method.getResponseBodyAsString();
            if (status / 100 != 2) {
                throw new IOException("Status " + status + " returned from put to URL " + url);
            }
        } finally {
            method.releaseConnection();
        }
    }
    
    public void headers(HttpMethod method, byte[] md5, String contentType) {
        StringBuffer canonicalFormat = new StringBuffer();

        String date = getDateTime();
        String md5_hash;

        if (contentType != null) {
            md5_hash = new String(net.medcommons.modules.crypto.Base64Coder.encode(md5));
            method.setRequestHeader("Content-Md5", md5_hash);
            method.setRequestHeader("Content-type", contentType);
        }
        else {
            md5_hash = contentType = "";
            method.removeRequestHeader("Content-type");
        }
        
        // SS: send DevPay details if they were provided
        if(!blank(userToken)) {
            method.addRequestHeader("x-amz-security-token", userToken + "," + productToken);
        }

        method.setRequestHeader("Date", date);

        canonicalFormat.append(method.getName());
        canonicalFormat.append('\n');

        canonicalFormat.append(md5_hash);
        canonicalFormat.append('\n');

        canonicalFormat.append(contentType);
        canonicalFormat.append('\n');

        canonicalFormat.append(date);
        canonicalFormat.append('\n');

        canonicalFormat.append(method.getPath());

        byte[] hash = hmac(secret, canonicalFormat.toString());

        StringBuffer authorization = new StringBuffer();
        authorization.append("AWS ");
        authorization.append(key_id);
        authorization.append(':');
        authorization.append(Base64Coder.encode(hash));

        method.addRequestHeader("Authorization", authorization.toString());
    }

    /**
     * Format today's date/time in S3 format, namely:
     *   Fri, 26 Jan 2007 07:38:06 GMT
     */
    public String getDateTime() {
        synchronized(dateFormatter) {
            return dateFormatter.format(new Date());
        }
    }


    /*
     * Return the binary HMAC encoding of _value_, with key _secret_.
     */
    private static byte[] hmac(String secret, String value) {
        try {
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(ENCODING),
                                                  MAC_ALGORITHM);

            Mac mac = Mac.getInstance(MAC_ALGORITHM);

            mac.init(key);

            return mac.doFinal(value.getBytes(ENCODING));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ENCODING + ERR_DESC);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(MAC_ALGORITHM + ERR_DESC);
        } catch (InvalidKeyException ex) {
            throw new RuntimeException();
        }
    }

    /*
     * Return the binary MD5 sum of _value_
     */
    public static byte[] md5(String value) {
        try {
            MessageDigest d = MessageDigest.getInstance(HASH_ALGORITHM);
            return d.digest(value.getBytes(ENCODING));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ENCODING + ERR_DESC);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(MAC_ALGORITHM + ERR_DESC);
        }
    }

    /*
     * Return the binary MD5 sum of _in_
     */
    public static byte[] md5(InputStream in) throws IOException {
        try {
            MessageDigest d = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] buffer = new byte[4096];

            int n = in.read(buffer);
            while (n > 0) {
                d.update(buffer, 0, n);
                n = in.read(buffer);
            }
            return d.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(MAC_ALGORITHM + ERR_DESC);
        }
    }

    public String getProductToken() {
        return productToken;
    }

    public void setProductToken(String productToken) {
        this.productToken = productToken;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

}
