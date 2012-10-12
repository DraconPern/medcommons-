/*
 * $Id$
 */

package net.medcommons.router.services.order.processing;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;

public class OrderProcessorServlet extends HttpServlet {
  
  private Logger log = Logger.getLogger(OrderProcessorServlet.class);

  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      writeHipaaLog(request);

    } catch(Exception e) {
      throw new ServletException("Unable to write HIPAA log entry: " + e.toString(), e); 
    }
   
    PrintWriter out = new PrintWriter(response.getOutputStream());
    out.println("<html><body>");
    
    String name = request.getParameter("name");
    out.println("<h1>Thank you for your order, " + name + ".</h1>");
    
    String tracking = request.getParameter("tracking");
    out.println("<p>Your tracking number is: " + tracking);  
    
    out.println("<p>See your <a href=\"http://medcommons.net:8080/central/LogViewer.jsp?filter=" + tracking + "\">HIPAA entries</a>.</p>");  
    
    out.println("</body></html>");
    out.close();
  }
  
  private void writeHipaaLog(HttpServletRequest request) throws Exception {
    log.info("Writing HIPAA log.");
    
    String endpoint = "http://medcommons.net:8080/jboss-net/services/HipaaLogService";
    
    Service service = new Service();
    Call call = (Call) service.createCall();
    
    call.setTargetEndpointAddress(new java.net.URL(endpoint));
    call.setOperationName("write");
    
    String logText = formatLogText(request);
    
    call.invoke(new Object[] { logText });
    
    log.info("Wrote HIPAA log with text (logText: " + logText + ")");    
  }
  
  private String formatLogText(HttpServletRequest request) throws Exception {
    String tracking = request.getParameter("tracking");
    String name = request.getParameter("name");
    
    String guid = "No Guid Specified";
    if(request.getParameter("guid") != null) {
      guid = request.getParameter("guid"); 
    }
    
    String logText="<tr>";
    logText += "<td>" + new Date() + "</td>";
    logText += "<td>" + name + "</td>";
    logText += "<td>Accepted Order</td>";
    logText += "<td><a href=\"http://medcommons.net:9080/router/WADOViewer.jsp?guid=" + guid + "\">" + tracking + "</a></td>";
    logText += "<td>" + guid + "</td>"; 
    logText += "</tr>";
    
    return logText; 
  }

}
