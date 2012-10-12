/*
 * xdsServlet.java
 *
 * Created on October 4, 2004, 3:05 PM
 */

package gov.nist.registry.xds;

import java.io.PrintWriter;
import java.io.IOException;

import java.util.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.soap.*;

import javax.servlet.*;
import javax.servlet.http.*;

import gov.nist.registry.xds.log.*;

import gov.nist.registry.util.submit.ServletParams;


/**
 *
 * @author  andrew
 */
public class XdsServlet extends HttpServlet {
    
    protected MessageFactory messageFactory;
    
    // Option to turn logging off.
    
    static final boolean logging = false;
    
    public void init(ServletConfig config) throws ServletException {
        ServletParams.init(config);
        try {
            messageFactory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            throw new ServletException("Failed to create MessageFactory" , e);
        }
    }
    
    public void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException {
        BaseEntry baseLog = null;
        if(logging) {
            baseLog = new BaseEntry();
            baseLog.setIp(request.getRemoteAddr());
            HttpEntry httpLog = new HttpEntry(baseLog.getId(),request);
            //        BodyEntry bodyEntry = new BodyEntry(baseLog.getId(), request);
            request.setAttribute("baseEntry", baseLog);
        }

        
        // XDS specific information is stored in a bean where it can be accessed
        // by other applications via the request.
        
        XdsRequestBean xds = new XdsRequestBean();
        
        xds.setIpAddress(request.getRemoteAddr());
        xds.setUri(request.getRequestURI());
        
        
        String testId = request.getParameter("testid");
        if(testId != null) {
            if(testId.equals("11722")) {
                
                // Test 11722 is hardcoded into this servlet.
                
                StringBuffer httpBody = new StringBuffer();
                try {
                    ServletInputStream sis = request.getInputStream();
                    while(true) {
                        int i = sis.read();
                        if (i == -1)
                            break;
                        char c = (char) i;
                        httpBody.append(c);
                    }
                    if(logging)
                        new BodyEntry(baseLog.getId(), httpBody.toString());
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    httpBody = new StringBuffer("I/O Error parsing input!");
                }
                try {
                    this.forwardToEcho(request,response,httpBody.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        
        try {
            
            // Pulls MIME headers from HTTP Request.
            
            MimeHeaders mimeHeaders = getMimeHeaders(request);
            
            // Builds a SOAPMessage containing all of the request (attachments and meta data).
            
            SOAPMessage message = messageFactory.createMessage(mimeHeaders,request.getInputStream());
            
            Iterator it = message.getAttachments();
            ArrayList list = new ArrayList();
            int count = 0;
            
            // We place the Iterator into a Collection, for easier handling.
            
            while(it.hasNext()) {
                
                AttachmentPart part = (AttachmentPart) it.next();
                
                
                /* This is for demonstration purposes:
                String contentId = part.getContentId();
                Object content = null;
                int size;
                try {
                    content = part.getContent();
                    size = part.getSize();
                } catch (SOAPException e) {
                    System.err.println("Error processing attachment content");
                }
                 */
                
                
                if(logging) {
                    String contenttype = part.getContentType();
                    ContentBean content = new ContentBean(part.getContent(),contenttype);                    
                    new AttachmentEntry(baseLog,count,content);
                    count++;
                }
                list.add(part);               
            }
            
            // All attachments are saved in the XDSRequestBean
            
            xds.setDocuments(list);
            
            SOAPBody body = message.getSOAPBody();
            NodeList children = body.getChildNodes();
            for(int i = 0; i < children.getLength(); i++) {
                System.out.println(children.item(i).getLocalName());
                if(!children.item(i).getNodeName().equals("#text")) {
                    
                    // We go down to this level, because we aren't interested in the DOM parents
                    // of the meta data.
                    
                    xds.setMetadata(children.item(i));
                    break;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.setAttribute("xdsInfo", xds);
        
        try {

            // Forward this along to the controller which decides which test is being run from
            // the testid parameter.
            
            this.forward(request, response, "/xds/controller.jsp");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        
    }
    
    // We overrider doGet just to give a warning to the user that they shouldn't be entering this way.
    
    public void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws java.io.IOException, ServletException {
        response.setContentType("text/plain");
        PrintWriter wt = response.getWriter();
        wt.print("Are you visiting this URL with a Web browser? You should submit data or query to XDS ebxml Registry/Repository with NIST tools.");
        wt.flush();
        wt.close();
        
    }
    
    // Converts the request Headers into a MimeHeader object for easier processing.
    
    public static MimeHeaders getMimeHeaders(HttpServletRequest request) {
        MimeHeaders mimeHeaders = new MimeHeaders();
        Enumeration names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String headerName = (String) names.nextElement();
            String headerValue = request.getHeader(headerName);
            StringTokenizer st = new StringTokenizer(headerValue, ",");
            while (st.hasMoreTokens()) {
                mimeHeaders.addHeader(headerName, st.nextToken().trim());
            }
        }
        
        
        return mimeHeaders;
    }
    
    
    // forward taken from Hl7Servlet...
    
    private void forward(HttpServletRequest request, HttpServletResponse response, String target) throws ServletException, IOException {
        request.setAttribute("option", "forward");
        RequestDispatcher dispatcher = request.getRequestDispatcher(target);
        dispatcher.forward(request, response);
    }
    
    private void forwardToEcho(HttpServletRequest request, HttpServletResponse response, String httpBody) throws ServletException, IOException {
        request.setAttribute("httpBody", httpBody);
        this.forward(request,response,"/xds/echo.jsp");
    }
}