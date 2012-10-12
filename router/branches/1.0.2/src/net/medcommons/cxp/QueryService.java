package net.medcommons.cxp;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.medcommons.cxp.utils.ParameterHandling;
import net.medcommons.router.services.xds.consumer.web.action.Contact;
import net.medcommons.modules.services.client.rest.RESTProxyServicesFactory;
import net.medcommons.modules.services.interfaces.SecondaryRegistryService;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ServicesFactory;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.output.XMLOutputter;

/**
 * Basically just a shell so far.
 * Need to put in configuration to turn off if the service isn't activated on a particular machine.
 * @author sean
 *
 */
public class QueryService {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger
			.getLogger(QueryService.class);
	 /**
     * The factory that will be used to access services
     */
    private  ServicesFactory serviceFactory;
    
  
  
    public QueryService(){
    	String clientId = "unknown";
		serviceFactory = new RESTProxyServicesFactory(clientId);
    }
    
    
	public QueryResponse[] query(RegistryParameters parameters) throws CXPException{
		try{
		SecondaryRegistryService secondaryRegistryService = serviceFactory.getSecondaryRegistryService();
		
		String registryName = parameters.getRegistryName();
		String registryID = parameters.getRegistryId();
		if (!registryName.equals("TEPR"))
			throw new CXPException("Unknown registry name:" + registryName);
		if (!registryID.equals("https://secure.private.medcommons.net/reg2/"))
			throw new CXPException("Unknown registry id:" + registryID);
		Parameter[] inputParameters = parameters.getParameters();
		if (inputParameters == null)
			throw new NullPointerException("Null input parameter block");
		
		String PatientGivenName = ParameterHandling.getParameterValue("PatientGivenName",inputParameters,"");
		String PatientFamilyName = ParameterHandling.getParameterValue("PatientFamilyName",inputParameters,"");
		String PatientIdentifier = ParameterHandling.getParameterValue("PatientIdentifier",inputParameters,"");
		String PatientIdentifierSource = ParameterHandling.getParameterValue("PatientIdentifierSource",inputParameters,"");
		String SenderProviderId = ParameterHandling.getParameterValue("SenderProviderId",inputParameters,"");
		String ReceiverProviderId = ParameterHandling.getParameterValue("ReceiverProviderId",inputParameters,"");
		String DOB = ParameterHandling.getParameterValue("DOB",inputParameters,"");
		String ConfirmationCode = ParameterHandling.getParameterValue("ConfirmationCode",inputParameters,"");
		String Limit = ParameterHandling.getParameterValue("Limit",inputParameters,"5");
		log.info("Input parameters: PatientGivenName='" + PatientGivenName +"', PatientFamilyName='" + 
				PatientFamilyName + "', SenderProviderId='" + SenderProviderId + "'");
		Document doc = secondaryRegistryService.queryRLS(
				PatientGivenName, // Patient Given Name
				PatientFamilyName, // Patient Family Name
				PatientIdentifier, // Patient Identifier
				PatientIdentifierSource, // Patient Identifier Source
				SenderProviderId, // SenderProviderID
				ReceiverProviderId, // Receiver Provider ID
				DOB, // DOB
				ConfirmationCode, // Confirmation Code
				Limit, // Limit
                registryID // SS: Assume registryID contains the url to query !
                           // TODO: verify/fix this
				);
			String qResponse = dumpXML(doc.getRootElement());
			log.info("Query response:" + qResponse);
		
		Element outputs = doc.getRootElement().getChild("outputs");
		if (outputs == null){
			log.error("Outputs element of server's response is null:" + dumpXML(doc.getRootElement()));
			throw new NullPointerException("Outputs element of server's response is null");
		}
		String status = outputs.getChildTextTrim("status");
		if (status == null){
			log.error("Status element missing from server outputs:" + dumpXML(outputs));
			throw new NullPointerException("Status element missing from server outputs");
		}
		if (status.indexOf("failed 0 rows returned") != -1){
			log.info("query status returned:" + status);
			return(null);
		}
        String statusSummary = status.substring(0,2);
        if (!statusSummary.equalsIgnoreCase("ok")){
        	log.error("Status is not OK: '" + statusSummary + "', full status " + status);
			throw new CXPException("Status not OK:" + status);
        }
        ArrayList<QueryResponse> queryResponses = new ArrayList<QueryResponse>();
        Iterator iter = doc.getDescendants(new ElementFilter("RLSentry"));
        
		while (iter.hasNext()) {
			Element rlsElement = (Element) iter.next();
			log.info("RLSElement:" + dumpXML(rlsElement));
			
			ArrayList<Parameter> args = new ArrayList<Parameter>();
			List children = rlsElement.getChildren();
			Iterator argIter = children.iterator();
			
			while (argIter.hasNext()){
				Element argElement = (Element) argIter.next();
				String name = argElement.getName();
				String value = argElement.getValue();
				log.info("name=" + name + ", value=" + value);
				Parameter p = new Parameter();
				p.setName(name);
				p.setValue(value);
				args.add(p);
			}
			int nArgs = args.size();
			Parameter returnedParameters [] = new Parameter[nArgs];
			
			for (int i=0;i<nArgs;i++){
				returnedParameters[i] = (Parameter) args.get(i);
			}
			
			/*
			String pfn =  rlsElement.getChildTextTrim("pfn");
			String pgn =  rlsElement.getChildTextTrim("pgn");
			String pid =  rlsElement.getChildTextTrim("pid");
			String pis =  rlsElement.getChildTextTrim("pis");
			String dob =  rlsElement.getChildTextTrim("dob");
			String guid = rlsElement.getChildTextTrim("guid");
			String purp = rlsElement.getChildTextTrim("purp");
			String cc =   rlsElement.getChildTextTrim("cc");
			String dt =   rlsElement.getChildTextTrim("dt");
			String spid = rlsElement.getChildTextTrim("spid");
			String rpid = rlsElement.getChildTextTrim("rpid");
			*/
			
			QueryResponse qr = new QueryResponse();	
			qr.setParameters(returnedParameters);
			queryResponses.add(qr);
			
		}
		
		int nRecords = queryResponses.size();
		QueryResponse[] response = null;
		if (nRecords != 0){
			
			response = new QueryResponse[nRecords];
			
			for (int i=0;i<nRecords;i++){
				response[i] = queryResponses.get(i);
			}
		}
		return(response);
	}
		catch(Exception e){
			log.error("Error during query:", e);
			throw new CXPException("Error during query" + e.toString());
		}
		
	}
	private String dumpXML(Element element) {
		 try{
		    	StringWriter sw = new StringWriter();
				new XMLOutputter().output(element, sw);
				return(sw.toString());
		    }
		    catch(Exception e){
		    	log.error("dumpXML: error creating xml output:", e);
		    }
		    return(null);
	}
	
}
