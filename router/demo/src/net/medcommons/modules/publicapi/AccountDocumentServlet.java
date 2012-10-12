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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.publicapi.utils.AccountDocumentParameters;
import net.medcommons.modules.publicapi.utils.UploadContentHandler;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;
import net.medcommons.modules.services.interfaces.ServiceConstants;
import net.medcommons.modules.utils.SupportedDocuments;
import net.medcommons.modules.utils.UnsupportedDocumentException;
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
 * Class for accepting uploads of DICOM and other objects into a specific account.
 * 
 * The service assumes that the following parameters have been passed:
 * <ul>
 * <li> storageid - this is the medcommons account that the documents are placed into.
 * <li> accountid - this the the medcommons account which is putting these documents into the system.
 * <li> auth - an authentication token (which answers the question - does account <i>accountid</i> 
 *      have permission to write into account <i>storageid</i>?
 * <li> mimetype - 
 * 
 * </ul>
 * The account <i>storageid</i> must already exist before this service is invoked.
 * 
 * State machine:
 * <ol>
 *  <li> First test the parameters: does the storageid exist on this machine? Does the account have
 *       authorization to enter the data?
 *  <li> If not - return with an error.
 *  <li> If all parameters OK - process each file
 *  <li> for each file:
 *    <ol>
 *     <li> Get the content type
 *     <li> Check to see that the parameters are valid for that mime type.
 *     <li> Check the validity of the file.
 *     <li> If valid - import the file.
 *    </ol> 
 * </ol>
 * 
 * File import process - depends on mime type.
 * <ul>
 * <li> For PDF/JPEG/PNG - there should be some basic validity check on the first few bytes and the size of the document.
 * <li> For DICOM - make sure that required parameters are there? 
 * 
 * 
 * TODO: 
 * Should this all be JSON?
 * What about 'future' or 'metadata-only' uploads for DICOM?
 * 
 * 
 * @author sean
 *
 */
public class AccountDocumentServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(AccountDocumentServlet.class);
    protected Properties properties = null;
    File scratchDir = null;
    private static final String FILEPREFIX = "AccountDocument_";
    
    public void init() throws ServletException{
        super.init();
        scratchDir = new File("data/Repository/Scratch");
      }

      public void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
        errorResponse(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "GET Not yet supported");
      }

      public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
       
        
      
        //          Create a new file upload handler
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(scratchDir);
      
        AccountDocumentParameters params = new AccountDocumentParameters();
        
        ServletFileUpload upload = new ServletFileUpload(factory);
      
        String timeToken = Long.toString(System.currentTimeMillis());
        
        try{
            UploadContentHandler uploadContentHandler = new UploadContentHandler(request.getParameter("auth"));
            List <FileItem> items = upload.parseRequest(request);
            Properties prop = new Properties();
            String uploadedFilename = FILEPREFIX + timeToken;
            HashMap<String, DocumentDescriptor> openTransactions = new HashMap<String, DocumentDescriptor>();
            for (Iterator<FileItem> iter = items.iterator(); iter.hasNext();){
                
                FileItem item = iter.next();
                log.info("Item: name=" + item.getName() + ", contentType=" + item.getContentType() + ", fieldname=" + item.getFieldName());
                if (item.getSize() > 0){
                    String contentType = item.getContentType();
                    int semiLoc = contentType.indexOf(";");
                    if (semiLoc != -1){
                        contentType = contentType.substring(0, semiLoc);
                    }
                    if (contentType.indexOf("text/plain") != -1){
                        params.addParameter(item);
                    }
  
                    else{
                        if (!params.requiredParametersValid()){
                            log.error("Missing required parameter");
                         //   errorResponse(request, response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter");
                        }
                        if ( (item.getName()== null) && (item.getContentType() == null)){
                            prop.put(item.getFieldName(), item.getString());
                        }
                        else{
                            File uploadedFile;
                            prop.put(item.getFieldName(), item.getName());
                            uploadedFilename = "AccountDocumentUpload_" + timeToken + "_"  + item.getName();
                            uploadedFile = new File(scratchDir, uploadedFilename);
                            FileOutputStream out = new FileOutputStream(uploadedFile);
                            copy(item.getInputStream(), out);
                            out.close();
                            item.getInputStream().close();
                            log.info("Uploaded file " + uploadedFile.getAbsolutePath() + ",fieldname=" + item.getFieldName() + 
                                    ", name=" + item.getName() + ", content type = " + contentType);
                            SupportedDocuments supportedDoc = SupportedDocuments.getDocumentType(contentType);
                            DocumentDescriptor docDescriptor = uploadContentHandler.importFile(params, supportedDoc,uploadedFile);
                            String transactionHandle = docDescriptor.getTransactionHandle();
                            if (transactionHandle != null){
                                DocumentDescriptor openTransaction = openTransactions.get(transactionHandle);
                                if (openTransaction == null){
                                    openTransactions.put(transactionHandle, docDescriptor);
                                }
                            }
                            uploadedFile.delete();
                        } 
                    }
                }
                
               
                
            }
            Set transactionSet = openTransactions.entrySet();
            Iterator<Entry<String, DocumentDescriptor>> it = transactionSet.iterator();
            while(it.hasNext()){
                String transactionHandle = it.next().getValue().getTransactionHandle();
                uploadContentHandler.closeTransaction(params, transactionHandle);
              
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
            log.error("Error uploading account information", e);
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
     
      
      private void errorResponse(HttpServletRequest request, HttpServletResponse response, int status, String message) throws IOException{
          OutputStream outStream = response.getOutputStream();
          PrintStream out = new PrintStream(outStream);
          try{
              response.setContentType("text/plain");
              response.setStatus(status);
              out.print(message);
          }
          finally{
              try{out.close();}catch(Exception e){;}
              try{outStream.close();}catch(Exception e){;}
          }
          
      }
      
   
      private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];

        int n = in.read(buffer);

        while (n > 0) {
            out.write(buffer, 0, n);
            n = in.read(buffer);
        }
      }
     
      
    
    
      
}


