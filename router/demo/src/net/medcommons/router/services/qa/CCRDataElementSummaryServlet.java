package net.medcommons.router.services.qa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.phr.PHRException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;


/**
 * Creates a very simple HTML CCR summary for 1 to N CCRs. 
 * Return a summary report - which includes element counts and 
 * some other summary information useful for comparing CCRs.
 * 
 * The main use case for this CCR is comparing two CCRs. Suppose 
 * we had CCR A with 2 medications and we added another medication
 * then the medication count for CCR B is 3. However, none of the other
 * counts should change. This tool is useful for identifying
 * all changes to a CCR at a top level.
 * 
 * Some summary data (patient name, medcommons id, uploaded ccr name) 
 * is presented to make it easier for QA people to record and manage
 * the results.
 * @author sean
 *
 */
public class CCRDataElementSummaryServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(CCRDataElementSummaryServlet.class);
    protected Properties properties = null;
    File scratchDir = null;
    Long ccrCount = new Long(0);
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
    
        ServletFileUpload upload = new ServletFileUpload(factory);
        //File tempFile = null;
        List<CCRSummaryInfo> ccrSummaries = new ArrayList<CCRSummaryInfo>();
        
        try{
            List <FileItem> items = upload.parseRequest(request);
            
            for (Iterator<FileItem> iter = items.iterator(); iter.hasNext();){
                FileItem item = iter.next();
                if (item.getSize() > 0){
                    // Counter to ensure that the filenames are unique 
                    // if currentTimeMillis() doesn't have fine enough
                    // resolution
                    synchronized (ccrCount) {
                        ccrCount++;
                    }
                    
                    File tempFile = new File(scratchDir, "CCRDataElementSummary_" + System.currentTimeMillis() + "_" + ccrCount + ".xml");
                    FileOutputStream out = new FileOutputStream(tempFile);
                    copy(item.getInputStream(), out);
                    out.close();
                    item.getInputStream().close();
                    
                    CCRDocument doc  = CCRDocument.createFromTemplate(
                            ServiceConstants.PUBLIC_MEDCOMMONS_ID,
                            tempFile.getAbsolutePath(), CCRConstants.SCHEMA_VALIDATION_STRICT, false);
                    doc.syncFromJDom();
                    
                    CCRSummaryInfo summary = new CCRSummaryInfo();
                    summary.ccrFile = tempFile;
                    summary.elementCounts = CCRSummaryUtils.generateElementCounts(doc);
                    
                    summary.attributes.add( String.valueOf(doc.isValidated()));
                    summary.attributes.add( String.valueOf(CCRSummaryUtils.getCCRDocumentObjectID(doc)));
                    summary.attributes.add( String.valueOf(doc.getPatientGivenName() + " " + doc.getPatientFamilyName()));
                    summary.attributes.add( String.valueOf(doc.getPatientMedCommonsId()));
                    summary.attributes.add( String.valueOf(new Date(doc.getCreateTimeMs()).toString()));
                    summary.attributes.add(item.getName());
                    ccrSummaries.add(summary);
                }
               
                
            }
            generateSummaryOutput(ccrSummaries, response);
            
            
            
        }
        catch(Exception e){
            log.error(e);
        }
        finally{
            if (ccrSummaries.size() > 0) {
                for (Iterator<CCRSummaryInfo> iter=ccrSummaries.iterator(); iter.hasNext();){
                    CCRSummaryInfo summary = iter.next();
                    if (summary.ccrFile.exists())
                        summary.ccrFile.delete();
                }
            }
        }
        
      }
     
      /**
       * Generate a general summary of CCR attributes
       * @param ccrSummaries
       * @param response
       */
      private void generateSummaryOutput(List<CCRSummaryInfo> ccrSummaries, HttpServletResponse response){
          OutputStream out = null;
          PrintStream printOut = null;
          response.setContentType("text/html");
          
          try{
              
              out = response.getOutputStream();
              printOut = new PrintStream(out);
              printOut.print("<html><head><title>");
              printOut.println("CCR Summary Comparison");
              printOut.println("</title></head>");
              printOut.println("<body>");

              printOut.println("<b>Date output created:</b>");
              printOut.println(new Date().toString());
              printOut.println("<br/>");
             
              if (ccrSummaries.size()==0){
                  printOut.println("No CCRs were uploaded; please check input");
              }
              else{
              
                  printSummary(ccrSummaries, printOut);
              
                  printElementCounts(ccrSummaries, printOut);
              }
              
              
          }
          catch(Exception e){
              log.error("Error generating CCR summary:" + e.getLocalizedMessage(), e);
              if (printOut != null){
                  printOut.println("Error:" + e.getLocalizedMessage());
                  StackTraceElement stack[] = e.getStackTrace();
                  for (int i=0;i<stack.length;i++){
                      printOut.println("<br/> " + stack[i]);
                  }
              }
              
          }
          finally{
              if (printOut != null){
                  printOut.println("</body></html>");
              }
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


