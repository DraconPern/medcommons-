package net.medcommons.router.services.wado.actions;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.Version;
import net.medcommons.modules.backup.SmallFileBackupService;
import net.medcommons.modules.utils.metrics.Metric;
import net.medcommons.modules.utils.metrics.TimeSampledMetric;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.WADOImageJob2;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Checks various services and returns a chunk of XML as a status
 * 
 * @author ssadedin
 */
public class StatusAction extends Action {

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(StatusAction.class);
	
	static {
	    // The idea here is to force these classes to get instantiated and therefore initialized
	    if(WADOImageJob2.class != null);
	    if(SmallFileBackupService.class != null);
	}

	/**
	 * Method execute
	 * 
	 * @return ActionForward
	 * @throws
	 * @throws ConfigurationException -
	 *           if configuration cannot be accessed
	 * @throws SelectionException -
	 *           if a problem scanning the selections occurs
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		try {
			
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

			if ("xml".equals(request.getParameter("fmt"))) {
				response.setContentType("text/xml");
				return mapping.findForward("xml");
			} else if ("xhtml".equals(request.getParameter("fmt"))) {
				return mapping.findForward("xhtml");
			} else
				return mapping.findForward("success");

		} catch (Exception e) {
			log.error("Error in status", e);
			throw e;
		}
	}

}