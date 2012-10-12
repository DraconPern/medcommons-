package net.medcommons.router.services.wado.stripes;

import static java.net.URLEncoder.encode;
import static net.medcommons.modules.utils.Str.blank;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.configuration.ConfigurationException;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.wado.utils.ZipServlet;
import net.medcommons.router.web.stripes.JSONActionBean;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Supports features for saving a problem report, 
 * notifying operations by email and displaying the 
 * problem details.
 * 
 * @author ssadedin
 */
public class ProblemReportAction extends JSONActionBean {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ProblemReportAction.class);
    
    /**
     * A unique id for the problem report - created by sender
     */
    @Validate(required=true)
    String problemId;

    /**
     * Description of the problem as submitted by the user
     */
    @Validate(required=true,on="report,logfile")
    String description;
    
    
    @Validate(required=false)
    String encoding;
    
    @Validate(required=false)
    String env;
    
    @DefaultHandler
    public Resolution report() throws Exception {
        
        log.info("Received problem report " + problemId  + " with description: " + description);
        
        if(!blank(env)) {
            this.description = Str.nvl(description, "") + "\n\n-----------Client Information----------\n\n" + env;
        }
        
        String fileName = "description.txt";
        
        writeReport(fileName);
        
        sendEmail();
        
        result.put("status", "ok");
        return new StreamingResolution("text/plain", result.toString());
    }
    
    
    /**
     * Output field - full path to log file
     */
    File logFilePath;
    
    long logFileLength = -1;
    
    Date logFileWritten = null;
    
    long jsLogLength = 0;
    
    public Resolution show() throws Exception {
        
        File reportDir = new File("data/problems/" + problemId);
        
        description = "No description submitted";
        File descFile = new File(reportDir, "description.txt");
        if(descFile.exists()) {
            description = FileUtils.readFileToString(descFile);
        }
        
        logFilePath = new File(reportDir, "logfile.txt");
        
        logFileLength = logFilePath.length();
        logFileWritten = new Date(logFilePath.lastModified());
        
        File jsLog = new File(reportDir, "js.txt");
        if(jsLog.exists()) {
            jsLogLength = jsLog.length();
        }
        
        return new ForwardResolution("/problemReport.ftl");
    }
    
    public Resolution download() throws Exception {
        File reportDir = new File("data/problems/" + problemId);
        logFilePath = new File(reportDir, "logfile.txt");
        ZipServlet.sendZipped(ctx.getResponse(), logFilePath);
        return null;
    }
   
    public Resolution downloadjs() throws Exception {
        File reportDir = new File("data/problems/" + problemId);
        logFilePath = new File(reportDir, "js.txt");
        ZipServlet.sendZipped(ctx.getResponse(), logFilePath);
        return null;
    }
    
     /**
     * Target for DDL to send logs
     */
    public Resolution logfile() throws Exception {
        
        writeReport("logfile.txt");
        
        result.put("status", "ok");
        return new StreamingResolution("text/plain", result.toString());
    }
    
    /**
     * Target for javascript to send logs
     */
    public Resolution jslog() throws Exception {
        
        writeReport("js.txt");
        
        result.put("status", "ok");
        return new StreamingResolution("text/plain", result.toString());
    }
    
     /**
     * Save description to specified file name in directory identified by
     * problem id.
     */
    private void writeReport(String fileName) throws IOException {
        // Make the directory
        File reportDir = new File("data/problems/" + problemId);
        if(!reportDir.exists()) {
            if(!reportDir.mkdirs()) 
                throw new RuntimeException("Unable to make report directory");
        }
        
        File file = new File(reportDir,fileName);
        
        // Unencode, if necessary
        if("gzip-b64".equals(encoding)) {
            byte[] data = Base64.decodeBase64(description.getBytes());
            GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data));
            FileOutputStream out = null;
            try {
	            out = new FileOutputStream(file);
	            IOUtils.copy(in, out);
            }
            finally {
              closeQuietly(in);
              closeQuietly(out);
            }
        }
        else {
	        // Write the description
	        FileUtils.writeStringToFile(file, description);
        }
        
        log.info("Report for problem " + problemId + " written to file " + file.getAbsolutePath());
    }
    
  
    /**
     * Send a notification email to alert MedCommons to 
     * the problem report.
     */
    public void sendEmail() throws ConfigurationException, MessagingException, UnsupportedEncodingException {
        
        String mailHost = Configuration.getProperty("smtp.host", "localhost");
        String from = Configuration.getProperty("smtp.from","noreply@medcommons.net");
        String to = Configuration.getProperty("problems.notifyAddress","cmo@medcommons.net");
        String appliance = Configuration.getProperty("AccountsBaseUrl");

        log.info("Sending problem report notification to " + to + " using mail host " + mailHost);
        
        String accessUrl = Configuration.getProperty("RemoteAccessAddress") + "/ProblemReport.action?show&problemId="+encode(problemId, "UTF-8");
        
        log.info("problem report notification url is " + accessUrl);
        
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailHost);
        
        Session mailSession = Session.getDefaultInstance(props, null);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject("Problem report received @ " + appliance);
        message.setContent("A problem report has been received " + "at appliance " + appliance + ".\n\n" + "Please see details at " + accessUrl,
        		"text/plain");
        message.setFrom( new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        transport.connect();
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }    
    
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String id) {
        this.problemId = id;
    }

    @Override
    protected void checkSID() {
        // No-op
        // It's okay to call this method without a session id 
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public File getLogFilePath() {
        return logFilePath;
    }

    public long getLogFileLength() {
        return logFileLength;
    }

    public void setLogFileLength(long logFileLength) {
        this.logFileLength = logFileLength;
    }

    public Date getLogFileWritten() {
        return logFileWritten;
    }

    public void setLogFileWritten(Date logFileWritten) {
        this.logFileWritten = logFileWritten;
    }

    public long getJsLogLength() {
        return jsLogLength;
    }

    public void setJsLogLength(long jsLogLength) {
        this.jsLogLength = jsLogLength;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

}
