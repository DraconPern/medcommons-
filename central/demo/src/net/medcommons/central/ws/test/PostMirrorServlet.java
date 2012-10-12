/*
 * $Id: PostMirrorServlet.java 57 2004-05-06 17:38:53Z mquigley $
 */

package net.medcommons.central.ws.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PostMirrorServlet extends HttpServlet {
  
  protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
  
    // Retrieve body of post.
    String input = new String();
    InputStream is = request.getInputStream();
    byte[] buffer = new byte[10240];
    int read = 0;
    while((read = is.read(buffer, 0, 10240)) != -1) {
      input += new String(buffer, 0, read, "UTF-8");
    }  
  
    // Start html.
    PrintWriter out = new PrintWriter(response.getOutputStream());
    out.println("<html><body>");
    out.println("<h1>Your Submission:</h1>");
    
    // Dump parameters.
    out.println("<h2>Parameters:</h2>");
    Enumeration pNames = request.getParameterNames();
    while(pNames.hasMoreElements()) {
      String name = (String) pNames.nextElement();
      out.println("<h3>" + name + "<h3>");
    }
    
    // Dump post body.
    out.println("<h2>POST Body:</h2>");
    out.println("<pre>");
    out.println(input.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
    out.println("</pre>");
    
    // Close html.
    out.println("</body></html>");
    out.close();
  }

}
