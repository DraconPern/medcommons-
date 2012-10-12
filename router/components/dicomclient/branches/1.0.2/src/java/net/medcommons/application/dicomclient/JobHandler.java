package net.medcommons.application.dicomclient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This is an in-memory copy of the currently running jobs.
 * @author mesozoic
 */

public class JobHandler {
    
	private static Logger log = Logger.getLogger(JobHandler.class);
	
	Map<Long, Job> activeCxpJobs = null;
	Map<Long, Job> activeDicomJobs = null;

	private static JobHandler jobHandler = null;
	
	public JobHandler(){
		activeCxpJobs =  Collections.synchronizedMap (new HashMap<Long, Job>());
		activeDicomJobs = Collections.synchronizedMap (new HashMap<Long, Job>());
	}
	public static JobHandler JobHandlerFactory(){
		if (jobHandler== null){
			jobHandler = new JobHandler();
		}
		return(jobHandler);
	}
	public void addCxpJob(Job cxpTransaction){
		Long id = cxpTransaction.getId();
		log.info("Added CXP job with id " + id);
		activeCxpJobs.put(id, cxpTransaction);
	}
	public Job getCxpJob(Long id){
		return(activeCxpJobs.get(id));
	}
	public Job deleteCxpJob(Long id){
		Job transaction = activeCxpJobs.remove(id);
		log.info("Deleted CXP job with id " + id);
		return(transaction);
	}

	public void addDicomJob(Job transaction){
		Long id = transaction.getId();
		activeDicomJobs.put(id, transaction);
	}
	public Job getDicomJob(Long id){
		return(activeDicomJobs.get(id));
	}
	public Job deleteDicomJob(Long id){

		Job transaction = activeDicomJobs.remove(id);
		return(transaction);
	}
}
