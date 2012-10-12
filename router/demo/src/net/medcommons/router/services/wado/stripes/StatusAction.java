package net.medcommons.router.services.wado.stripes;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.Version;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.utils.metrics.TimeSampledMetric;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.web.stripes.BaseActionBean;
import net.sourceforge.stripes.action.*;

@UrlBinding("/status/{$event}")
public class StatusAction extends BaseActionBean {
    
    //////////////// Outputs /////////////////
    private String expiredAccounts = Metric.getInstance("ExpiryTask.expiredAccounts").getValue().toString();
    private String expirationFailures = Metric.getInstance("ExpiryTask.expirationFailures").getValue().toString();
    
    @DefaultHandler
    public Resolution show() {
        load();
        return new ForwardResolution("/status.jsp");
    }
    
    public Resolution xml() {
        load();
        return new ForwardResolution("/status-xml.jsp");
    }
    
    public Resolution xhtml() {
        load();
        return new ForwardResolution("/status-xhtml.jsp");
    }
    
    void load() {
        
        HttpServletRequest request = ctx.getRequest();
        
        // Check database?
        // boolean statusFlag = this.checkDatabase(request);

        // Return version info
        request.setAttribute("version", Version.getVersionString());
        request.setAttribute("revision", Version.getRevision());

        // String pinkBoxUrl = Configuration.getProperty("PinkBoxURL");    
        // request.setAttribute("pinkBoxUrl", pinkBoxUrl);    
        request.setAttribute("status", "OK");
        request.setAttribute("runningSince",new Date(ManagementFactory.getRuntimeMXBean().getStartTime()));

        request.setAttribute("maxMemory", Runtime.getRuntime().maxMemory());
        request.setAttribute("totalMemory", Runtime.getRuntime()
                .totalMemory());
        request.setAttribute("freeMemory", Runtime.getRuntime()
                .freeMemory());

        request.setAttribute("lastModification", new Date(
                (new File("data")).lastModified()));
        
        // Count the sessions
        Set<UserSession> allDesktops = UserSession.getAllDesktops();
        request.setAttribute("desktops", allDesktops);
        
        
        // CXP
        request.setAttribute("cxpTransactions", Metric.getInstance("cxpTransactions").getValue().toString());
        request.setAttribute("cxpErrors", Metric.getInstance("cxpErrors").getValue().toString());
        request.setAttribute("cxpTransfers", Metric.getInstance("cxpTransfers").getValue().toString());
        request.setAttribute("cxpGets", Metric.getInstance("cxpGets").getValue().toString());
        request.setAttribute("cxpDeletes", Metric.getInstance("cxpDeletes").getValue().toString());
        
        request.setAttribute("imagesEncoded", Metric.getInstance("imagesEncoded").getValue().toString());
        request.setAttribute("bytesEncoded", Metric.getInstance("bytesEncoded").getValue().toString());
        request.setAttribute("bytesPerSecond", new Double(((TimeSampledMetric)Metric.getInstance("imageBytesPerSecond")).getGradient().doubleValue() / 1024.0));
        
        request.setAttribute("backupsCompleted",Metric.getInstance("SmallFileBackupService.BackupsCompleted").getValue().toString());
        request.setAttribute("averageBackupTimeMs",Metric.getInstance("SmallFileBackupService.AverageBackupJobTimeMs").getValue().toString());
        request.setAttribute("averageBackupQueueDelayMs",Metric.getInstance("SmallFileBackupService.AverageBackupDelayMs").getValue().toString());
        request.setAttribute("backupRetries",Metric.getInstance("SmallFileBackupService.BackupRetries").getValue().toString());
        request.setAttribute("backupFailures",Metric.getInstance("SmallFileBackupService.Failures").getValue().toString());
        request.setAttribute("filesRestored",Metric.getInstance("SmallFileBackupService.FilesRestored").getValue().toString());
        request.setAttribute("restoreFailures",Metric.getInstance("SmallFileBackupService.RestoreFailures").getValue().toString());

        try{
            // Try and read the free space in the data partition
            File freespaceFile = new File("data");
            long usableSpace = freespaceFile.getUsableSpace();

            request.setAttribute("freeRepositorySpace", Long.toString(usableSpace));
            request.setAttribute("freeRepositorySpaceAge", "0");
        } catch (Exception ex) {
            request.setAttribute("freeRepositorySpace", "Unknown");
            request.setAttribute("freeRepositorySpaceAge", "Unknown");
        }
        
        
    }

    public String getExpiredAccounts() {
        return expiredAccounts;
    }

    public void setExpiredAccounts(String expiredAccounts) {
        this.expiredAccounts = expiredAccounts;
    }

    public String getExpirationFailures() {
        return expirationFailures;
    }

    public void setExpirationFailures(String expirationFailures) {
        this.expirationFailures = expirationFailures;
    }

}
