package net.medcommons.modules.crypto.cookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import net.medcommons.modules.utils.Str;

import org.apache.commons.codec.binary.Base64;

/**
 * Encrypts and decodes a string containing MedCommons Cookie information
 * so that it can be securely stored by a client.
 * 
 * @author ssadedin
 */
public class CookieCodec {
    
    public static class Values {
        
        public String accid;
        
        public String auth;
    }
    
    /**
     * Secret used for encrypting the cookie contents
     */
    private String secret;
    
    public CookieCodec(String secret) {
        super();
        this.secret = secret;
    }

    public Values decode(String enc) throws CookieDecodeException {
        
        enc = enc.replaceAll("-","+");
        enc = enc.replaceAll("_","/");
        
        try {
            byte [] bytes = Base64.decodeBase64(enc.getBytes("UTF-8"));
            
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] key_data = md.digest(secret.getBytes("UTF-8"));
            IvParameterSpec ivSpec = new IvParameterSpec(bytes, 0, 16);
            SecretKeySpec k = new SecretKeySpec(key_data, 0, 16, "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, k, ivSpec);
            String decoded = new String(c.doFinal(bytes, 16, bytes.length - 16));
            
            String parts [] = decoded.split("&");
            
            Values values = new Values();
            for(String part : parts) {
                if(part.startsWith("auth=")) {
                    values.auth = URLDecoder.decode(part.substring(5),"UTF-8");
                }
                else
                if(part.startsWith("mcid=")) {
                    values.accid = URLDecoder.decode(part.substring(5),"UTF-8");
                }
            }
            
            return values;
        }
        catch (InvalidKeyException e) {
            throw new CookieDecodeException("Unable to decode encrypted cookie: " + enc, e);
        }
        catch (UnsupportedEncodingException e) {
            throw new CookieDecodeException("Unable to decode encrypted cookie: " + enc, e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CookieDecodeException("Unable to decode encrypted cookie: " + enc, e);
        }
        catch (NoSuchPaddingException e) {
            throw new CookieDecodeException("Unable to decode encrypted cookie: " + enc, e);
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CookieDecodeException("Unable to decode encrypted cookie: " + enc, e);
        }
        catch (IllegalBlockSizeException e) {
            throw new CookieDecodeException("Unable to decode encrypted cookie: " + enc, e);
        }
        catch (BadPaddingException e) {
            throw new CookieDecodeException("Unable to decode encrypted cookie: " + enc, e);
        }    }
    

}
