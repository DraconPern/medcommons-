/*
 * $Id: OAuthVerifierTest.java 3583 2010-01-19 06:30:21Z ssadedin $
 * Created on 17/01/2008
 */
package net.medcommons.security;

import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import junit.framework.Assert;

import net.medcommons.router.util.BaseTestCase;
import net.medcommons.security.OAuthVerifier;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class OAuthVerifierTest extends BaseTestCase {
    
    public OAuthVerifierTest() throws Exception {
        super();
        // TODO Auto-generated constructor stub
    }

    // strict mock forces you to specify the correct order of method calls
    HttpServletRequest request = null;
    
    OAuthVerifier v = new OAuthVerifier();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        request = createMock(HttpServletRequest.class);
        expect(request.getHeader("Authorization")).andReturn(null).anyTimes();        
        expect(request.getParameter("oauth_consumer_key")).andReturn("0685bd9184jfhq22").anyTimes();
        expect(request.getParameter("oauth_token")).andReturn("ad180jjd733klru7").anyTimes();
        expect(request.getParameter("oauth_signature_method")).andReturn("HMAC-SHA1").anyTimes();
        expect(request.getParameter("oauth_signature")).andReturn("wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D").anyTimes();
        expect(request.getParameter("oauth_timestamp")).andReturn("137131200").anyTimes();
        expect(request.getParameter("oauth_nonce")).andReturn("4572616e48616d6d65724c61686176").anyTimes();
        expect(request.getParameter("oauth_version")).andReturn("1.0").anyTimes();
        expect(request.getQueryString()).andReturn("").anyTimes();
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://foo.bar.baz.com")).anyTimes();
        replay(request);
        OAuthVerifier.disableValidation = true;
    }

    @Test
    public void testVerify()  throws Exception {
        
        v.verify(request);
    }


    @Test
    public void testParseOAuthHeader()  throws Exception {
        
        // From OAuth spec
        String header = "Authorization: OAuth realm=\"http://sp.example.com/\", oauth_consumer_key=\"0685bd9184jfhq22\", oauth_token=\"ad180jjd733klru7\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"wOJIO9A2W5mFwDgiDvZbTSMK%2FPY%3D\", oauth_timestamp=\"137131200\", oauth_nonce=\"4572616e48616d6d65724c61686176\", oauth_version=\"1.0\"";
        Map<String, String> params = v.parseOAuthHeader(header);
        
        assertEquals("ad180jjd733klru7",params.get("oauth_token"));
        assertEquals("HMAC-SHA1",params.get("oauth_signature_method"));
        assertEquals("4572616e48616d6d65724c61686176",params.get("oauth_nonce"));
    }

    @Test
    public void testHasOAuthHeaders()  throws Exception {
        assertEquals(false,v.hasOAuthHeaders(request));
    }

    @Test
    public void testDecodeOAuthHeader() throws Exception {
        assertEquals("oauth_token",v.decodeOAuthHeader("oauth_token=\"ad180jjd733klru7\"").name);
        assertEquals("ad180jjd733klru7",v.decodeOAuthHeader("oauth_token=\"ad180jjd733klru7\"").value);
        
        assertEquals("oauth_token",v.decodeOAuthHeader("oauth_token=\"ad180jj+733klru7\"").name);
        assertEquals("ad180jj 733klru7",v.decodeOAuthHeader("oauth_token=\"ad180jj+733klru7\"").value);
    }

}
