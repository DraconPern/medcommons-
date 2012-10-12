/*
 * $Id: OrderProcessorServlet.java 57 2004-05-06 17:38:53Z mquigley $
 */

package net.medcommons.central.ws.order;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class OrderProcessorServlet extends HttpServlet {

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
          
    System.out.println("\n\n" + input + "\n\n");
          
    try {
      StringBufferInputStream sis = new StringBufferInputStream(input);
  
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(sis);
     
      String firstName = "";
      Node firstNameNode = (Node) XPathAPI.selectSingleNode(document, "/BasicOrderForm/MedCommonsOrderFields/PatientDetailsSubForm/FirstName");
      if(firstNameNode != null) {
        firstNameNode = firstNameNode.getFirstChild();
      }
      if(firstNameNode != null) {
        firstName = firstNameNode.getNodeValue();
      }

      String lastName = "";
      Node lastNameNode = (Node) XPathAPI.selectSingleNode(document, "/BasicOrderForm/MedCommonsOrderFields/PatientDetailsSubForm/LastName");
      if(lastNameNode != null) {
        lastNameNode = lastNameNode.getFirstChild();
      }
      if(lastNameNode != null) {
        lastName = lastNameNode.getNodeValue();
      }
      
      String trackingNumber = "";
      Node trackingNumberNode = (Node) XPathAPI.selectSingleNode(document, "/BasicOrderForm/MedCommonsOrderFields/TrackingNumber");
      if(trackingNumberNode != null) {
        trackingNumberNode = trackingNumberNode.getFirstChild();
      }
      if(trackingNumberNode != null) {
        trackingNumber = trackingNumberNode.getNodeValue(); 
      }
       
      PrintWriter out = new PrintWriter(response.getOutputStream());
      out.println("<html><head><title>Confirmation</title></head><body>");
      out.println("<h1>Thank you, " + firstName + " " + lastName + "!</h1>");
      out.println("<p>We have received your order. Your order confirmation number is:</p>");
      out.println("<h2>" + trackingNumber + "</h2>");
      out.println("</body></html>");
      out.close();
      
    } catch(Exception e) {
      e.printStackTrace();
      throw new ServletException(e.toString());
    }
          
  }

}
