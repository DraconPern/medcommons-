/*
 * Created on Apr 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package net.medcommons.router.services.dicom;

import javax.naming.InitialContext;
import javax.naming.Name;

import org.apache.log4j.Logger;
import org.jboss.naming.NonSerializableFactory;

/**
 * Implementation of the MedCommons CSTORE SCP service. 
 * 
 * Implements two basic services: CSTORE and Verify SCP. 
 * @author sean
 *
 * 
 * 
 */
public class CStoreSCPService implements CStoreSCPServiceMBean {

	private static String jndiName = "medcommons/CStoreSCP";

	private static Logger log = Logger.getLogger(CStoreSCPService.class);

	public void start() throws Exception {

		log.info("Starting CStoreSCPService MBean.....");

		InitialContext ctx = new InitialContext();
		Name name = ctx.getNameParser("").parse(jndiName);
		
		CStoreSCP scp = new CStoreSCP();

		NonSerializableFactory.rebind(name, scp, true);

		CStoreSCP c = (CStoreSCP) new InitialContext().lookup(jndiName);
		c.start();
	}

	public void stop() {
		try {
			log.info("Stopping CStoreSCPService MBean.");

			InitialContext ctx = new InitialContext();
			CStoreSCP c = (CStoreSCP) ctx.lookup(jndiName);
			c.stop();
			ctx.unbind(jndiName);
			NonSerializableFactory.unbind(jndiName);

			log.info("Removed CStoreSCP instance from JNDI at: " + jndiName);


		} catch (Exception e) {
			log.error("Error unbinding CStoreSCP: " + e.toString());
		}

	}

}
