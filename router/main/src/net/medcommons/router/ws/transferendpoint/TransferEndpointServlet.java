/*
 * $Id: TransferEndpointServlet.java 237 2004-08-06 04:23:15Z mquigley $
 */
package net.medcommons.router.ws.transferendpoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.router.data.DataManager;
import net.medcommons.router.transfer.Transfer;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

public class TransferEndpointServlet extends HttpServlet {
  
  private static Logger log = Logger.getLogger(TransferEndpointServlet.class);

  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    log.info("Incoming request.");

    try {
	    InputStream is = request.getInputStream();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    int read = 0;
	    byte[] buffer = new byte[10240];
	    while((read = is.read(buffer, 0, 10240)) != -1) {
	      baos.write(buffer, 0, read);
	    }
	    
	    String payload = new String(baos.toByteArray());
	    
	    PrintWriter out = response.getWriter();
	    out.println("<html><body><h1>TransferEndpoint<h1></body></html>");
	    out.close();
	
	    int headersEndIdx = payload.indexOf("\n\n");
	    
	    String header = payload.substring(0, headersEndIdx);
	    String data = payload.substring(headersEndIdx + 2, payload.length());
	
			IBindingFactory bfact = BindingDirectory.getFactory(Transfer.class);
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			StringBufferInputStream sbis = new StringBufferInputStream(header);
			Transfer xfer = (Transfer) uctx.unmarshalDocument(sbis, null);    
	    
			log.info("Header: (folderGuid: " + xfer.getFolderGuid() + ")");
			log.info("Header: (path: " + xfer.getPath() + ")");
			
			DataManager dmgr = DataManager.getInstance();
			dmgr.putFolderFile("incoming", xfer.getPath(), data.getBytes());
			
    } catch(Exception e) {
      log.error("Communication exception: " + e.toString());
    }
  }
  
}
