package net.medcommons.modules.publicapi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.qa.CCRDataElementSummaryServlet;
import net.medcommons.router.services.qa.CCRSummaryUtils;
import net.medcommons.router.services.qa.ElementCount;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Utility for uploading files to a specific account.
 * 
 * This is currently a bit experimental - it needs a jsp to generate the HTML which 
 * gets invoked for the upload (so it has dynamic storageid and auth parameters). Once these are
 * real it needs a back end to validate the data and put  it into the right account.
 * 
 * @author sean
 *
 */
public class UploadAccountFilesServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(UploadAccountFilesServlet.class);
    protected Properties properties = null;
    File scratchDir = null;
  
    public void init() throws ServletException{
        super.init();
        scratchDir = new File("data/Repository/Scratch");
      }

      public void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
        doPost(request, response); // Need to put DoS limits.
      }

      public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        
      
        //          Create a new file upload handler
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(scratchDir);
       // Map<String, String> parameters =request.getParameterMap();
        Enumeration<String> names = request.getParameterNames();
        int pCount = 0;
        while (names.hasMoreElements()){
            String name = names.nextElement();
            String values[] = request.getParameterValues(name);
            for (int i=0;i<values.length;i++){
                log.info("parameter name=" + name + ", value[" + i + "]=" + values[i]);
                pCount++;
            }
        }
        log.info("Parameter count = " + pCount);
        ServletFileUpload upload = new ServletFileUpload(factory);
        
        String timeToken = Long.toString(System.currentTimeMillis());
        
        try{
            List <FileItem> items = upload.parseRequest(request);
            Properties prop = new Properties();
            String uploadedFilename = "UNKNOWN_" + timeToken;
            for (Iterator<FileItem> iter = items.iterator(); iter.hasNext();){
                
                FileItem item = iter.next();
                if (item.getSize() > 0){
                    
                    
                    if ( (item.getName()== null) && (item.getContentType() == null)){
                        prop.put(item.getFieldName(), item.getString());
                    }
                    else{
                        File uploadedFile;
                        prop.put(item.getFieldName(), item.getName());
                        uploadedFilename = "UploadServlet_" + timeToken + "_"  + item.getName();
                        uploadedFile = new File(scratchDir, uploadedFilename);
                        FileOutputStream out = new FileOutputStream(uploadedFile);
                        copy(item.getInputStream(), out);
                        out.close();
                        item.getInputStream().close();
                        log.info("Uploaded file " + uploadedFile.getAbsolutePath() + ",fieldname=" + item.getFieldName() + 
                                ", name=" + item.getName() + ", content type = " + item.getContentType());
                    }
                    
                    
                    
                   
                }
                
               
                
            }
            
            File propertyFile = new File(scratchDir, uploadedFilename + ".properties");
            FileOutputStream propertyOut = new FileOutputStream(propertyFile);
            prop.store(propertyOut, "Uploaded file at " + timeToken);
            propertyOut.close();
            response.setContentType("text/plain");
            response.setStatus(HttpStatus.SC_OK);
            PrintWriter out = response.getWriter();
            out.print("Upload complete");   
            out.close();
        }
        catch(Exception e){
            log.error(e);
            response.setContentType("text/plain");
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.print("Upload failed:");    
            out.print(e.getLocalizedMessage());
            out.close();
        }
        finally{
           
        }
        
      }
     
      
      private void printCountRow(ElementCount element, PrintStream out){
          out.println("<tr><td>");
          out.println(element.getName());
          out.println("</td><td>");
          out.println(element.getCount());
          out.println("</td>");
          if (element.getMessage() != null){
              out.println("<td>");
              out.println(element.getMessage());
              out.println("</td>");
          }
          out.println("</tr>");
      }
      private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];

        int n = in.read(buffer);

        while (n > 0) {
            out.write(buffer, 0, n);
            n = in.read(buffer);
        }
      }
      private void printAttributeRow(String name, String value, PrintStream out){
          out.println("<tr><td>");
          out.println(name);
          out.println("</td><td>");
          out.println(value);
          out.println("</td></tr>");
          
      }
      String summaryLabels[] = {
              "Valid CCR",
              "CCRDocumentObjectID",
              "Patient Name",
              "MedCommons ID",
              "Creation Time",
              "Filename"
      };
      
      private void printElementCounts(List<CCRSummaryInfo> ccrSummaries, PrintStream out) throws PHRException, JDOMException, ParseException{
          out.println("<br/><b>CCR Element Counts between CCRs</b></br>");
          out.println("<table>");
          out.println("<tr><td>CCR #</td>");
          for (int i=0;i<ccrSummaries.size(); i++){
              out.print("<td>"); out.print(i+1); out.print("</td>");
          }
          out.println("</tr>");
          for (int i=0;i<ccrSummaries.get(0).elementCounts.length; i++){
              out.println("<tr>");
              ElementCount[] firstCounts = ccrSummaries.get(0).elementCounts;
              out.print("<td>"); out.print(firstCounts[i].getName()); out.print("</td>");
              for (int j=0;j<ccrSummaries.size(); j++){
                  ElementCount[] counts = ccrSummaries.get(j).elementCounts;
                  out.print("<td>");
                  out.print(counts[i].getCount());
                  if (counts[i].getMessage() != null){
                      out.print(" ");
                      out.print(counts[i].getMessage());
                  }
                  out.print("</td>");
              }
              out.println("</tr>");
          }
          out.println("</table>");
      }
      
     private void printSummary(List<CCRSummaryInfo> ccrSummaries, PrintStream out) throws PHRException, JDOMException, ParseException{
         out.println("<br/><b>CCR Summary between " + ccrSummaries.size() + " CCRs </b></br>");
         out.println("<table>");
         out.println("<tr><td>CCR #</td>");
         for (int i=0;i<ccrSummaries.size(); i++){
             out.print("<td>"); out.print(i+1); out.print("</td>");
         }
         out.println("</tr>");
         for (int i=0;i<ccrSummaries.get(0).attributes.size(); i++){
             out.println("<tr>");
             out.print("<td>"); out.print(summaryLabels[i]); out.print("</td>");
             for (int j=0;j<ccrSummaries.size(); j++){
                 out.print("<td>");
                 out.print(ccrSummaries.get(j).attributes.get(i));
                 out.print("</td>");
             }
             out.println("</tr>");
         }
         out.println("</table>");
     }
     
     private class CCRSummaryInfo{
         File ccrFile;
        
         ElementCount[] elementCounts;
         ArrayList<String>attributes = new ArrayList<String>();
     }
      
}


