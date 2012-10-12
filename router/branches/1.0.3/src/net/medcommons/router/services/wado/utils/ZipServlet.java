/*
 * $Id: ZipServlet.java 3341 2009-04-28 07:16:01Z ssadedin $
 * Created on 14/11/2008
 */
package net.medcommons.router.services.wado.utils;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class ZipServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contentType = "text/javascript";
        String resource = req.getParameter("resource");
        File resourceFile = new File("webapps/router/"+resource + ".js");
        String ae = req.getHeader("accept-encoding");
        resp.setContentType("text/javascript");
        resp.setDateHeader("Expires", System.currentTimeMillis() + 30*60*1000);        
        if (ae != null && ae.indexOf("gzip") != -1) {
            sendZipped(resp, resourceFile);
        }
        else {
            resp.setContentLength((int) resourceFile.length());
            
            InputStream in = new FileInputStream(resourceFile);
            OutputStream out = resp.getOutputStream();
            byte [] buffer = new byte[4096];
            int len = -1;
            while(true) {
                len = in.read(buffer);
                if(len == -1)
                    break;
                out.write(buffer,0,len);
            }
            out.flush();
        }         
    }

    public static void sendZipped(HttpServletResponse resp, File resourceFile) throws FileNotFoundException,
            IOException {
        
        FileInputStream in = new FileInputStream(resourceFile);
        try {
	        ByteArrayOutputStream zipped = new ByteArrayOutputStream((int)resourceFile.length());
	        GZIPOutputStream out = new GZIPOutputStream(zipped);
	        IOUtils.copy(in, out);
	        out.flush();
	        out.close();
	        byte [] zippedBytes = zipped.toByteArray();
	        resp.setContentLength(zippedBytes.length);
	        resp.addHeader("Content-Encoding","gzip");
	        resp.getOutputStream().write(zippedBytes);
        }
        finally {
            closeQuietly(in);
        }
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        String resource = req.getParameter("resource");
        File resourceFile = new File("webapps/router/"+resource + ".js");
        if(resourceFile.exists())
	        return resourceFile.lastModified();
        else
            return -1;
    }
    
    

}
