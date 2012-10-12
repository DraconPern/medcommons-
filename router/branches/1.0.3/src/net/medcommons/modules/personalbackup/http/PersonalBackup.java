package net.medcommons.modules.personalbackup.http;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.personalbackup.BackupGenerator;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.Rights;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.utils.Str;
import net.medcommons.router.services.UserSession;
import net.medcommons.router.services.wado.InsufficientPrivilegeException;
import net.medcommons.router.services.wado.NotLoggedInException;

import org.apache.log4j.Logger;

public class PersonalBackup extends HttpServlet {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(PersonalBackup.class);
	public void init(){
		
	}
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
			Enumeration e = request.getParameterNames();
			String storageId = null;
			String auth = null;
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String[] value = request.getParameterValues(key);
			
				if ("storageId".equalsIgnoreCase(key)) {
					storageId = value[0];
					log.info("Storage id:" + storageId);
				} else if ("auth".equalsIgnoreCase(key)) {
				    auth = value[0];
				}
				else {
	
					log.info(" Other (ignored) URL parameter:" + key);
					log.info("Value is " + value[0]);
				}
			}
			if (storageId == null){
				log.error("No storage id defined"); 
				return; // Should have error messsage for client.
			}
			
			try {
			    
			    if(blank(auth)) 
			        auth = UserSession.required(request).getAuthenticationToken();
			    
			    RESTProxyServicesFactory services = new RESTProxyServicesFactory(auth);
			    EnumSet<Rights> rights = services.getDocumentService().getAccountPermissions(storageId);
			    if(!rights.contains(Rights.READ))
			        throw new InsufficientPrivilegeException("Provided account / authentication token is not authorized for read access to account " + storageId);
			
                BackupGenerator generator = new BackupGenerator(storageId, auth);
                
                OutputStream out = response.getOutputStream();

                response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
                response.setHeader("Pragma","no-cache"); // HTTP 1.0
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition","inline; filename=" + "MedCommons-Backup-" + storageId + ".zip");
                generator.generateBackup(out);
                out.close();
                /*FileOutputStream fout = new FileOutputStream("MedCommonsBackup-" + System.currentTimeMillis() + ".zip");
                generator.generateBackup(fout);
                fout.close();
                */
                return;
            }
            catch (ServiceException ex) {
                throw new ServletException("Failed to generate backup", ex);
            }
            catch (InsufficientPrivilegeException ex) {
                throw new ServletException("Failed to generate backup", ex);
            }
            catch (NotLoggedInException ex) {
                throw new ServletException("Failed to generate backup", ex);
            }
	}
	
}
