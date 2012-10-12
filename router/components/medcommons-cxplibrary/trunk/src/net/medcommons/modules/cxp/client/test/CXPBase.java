package net.medcommons.modules.cxp.client.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import net.medcommons.modules.crypto.SHA1;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.utils.FileUtils;
import net.medcommons.test.interfaces.ResourceNames;

import org.apache.log4j.Logger;
import org.codehaus.xfire.client.Client;
import org.cxp2.Document;
import org.cxp2.GetRequest;
import org.cxp2.Parameter;
import org.cxp2.PutRequest;
import org.cxp2.RegistryParameters;

/**
 * TODO
 * Need to run non-MTOM tests too.
 * @author mesozoic
 *
 */
public class CXPBase extends TestCase implements ResourceNames, DocumentTypes {

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("CXPBase");
	
	
	protected String endpoint;
	

	// private EndpointReference targetEPR = new
	protected Properties properties = new Properties();

	protected File resources;

	protected File scratch;

	//protected CXPService service = null;

	protected SHA1 sha1 = null;


	/**
	 * Sets up client stub to use standard SOAP protocol (no streaming, no MTOM)
	 * @throws MalformedURLException
	 */
	/*
	private void serviceTraditionalSOAPSetup() throws MalformedURLException {
		Service serviceModel = new ObjectServiceFactory().create(
				CXPService.class, "CXPService", "http://org.cxp2", null);

		service = (CXPService) new XFireProxyFactory().create(serviceModel,
				endpoint);

		// Setup properties (use defaults here)
		Client client = Client.getInstance(service);
		client.setProperty("mtom-enabled", "false");
		client.setProperty(HttpTransport.CHUNKING_ENABLED, "true");
		client.setProperty(StreamedAttachments.ATTACHMENT_DIRECTORY, new File(
				"tempclientdir"));
		client.setProperty(StreamedAttachments.ATTACHMENT_MEMORY_THRESHOLD,
				new Integer(1000000));

	}
*/
	private void dynamicClientSetup() {
		String dynamicURL = endpoint + "?WSDL";
		try {
			Client clientDynamic = new Client(new URL(dynamicURL));
			System.out.println("Created dynamic client (reached WSDL) "
					+ dynamicURL);
		} catch (Exception e) {
			System.out.println("Failed to create dynamic client " + dynamicURL);
			e.printStackTrace(System.out);
		}
	}

	public void setUp() throws Exception{
		super.setUp();
		setUpClientStub();
	}
	
	public void setUpClientStub() {
		try {
			

			resources = FileUtils.resourceDirectory();
			if (!resources.exists())
				throw new FileNotFoundException(resources.getAbsolutePath());
			log.info("Loading resources from " + resources.getAbsolutePath());
			File propFile = new File(resources, "Junit_test.properties");
			if (!propFile.exists())
				throw new FileNotFoundException(propFile.getAbsolutePath());
			FileInputStream in = new FileInputStream(propFile);
			properties.load(in);

		
			// Client clientx = new Client(new URL(endpoint + "?WSDL"));
			String scratchDir = properties.getProperty(ScratchDirectory);
			File scratch = new File(scratchDir);
			boolean isDeleted = scratch.delete();
			
			boolean isCreated = scratch.mkdirs();
			
			endpoint = properties.getProperty(JUNIT_CXPEndpoint);
			log.info("Base scratch directory is:" + scratch.getAbsolutePath());
			log.info("SOAP endpoint:" + endpoint);

		} catch (Exception e) {
			throw new RuntimeException("Error in setup", e);
		}

	}

	public String getEndpoint(){
		return(this.endpoint);
	}
	public boolean statusOK(int status) {
		boolean OK = false;
		if ((status >= 200) && (status <= 299))
			OK = true;
		return (OK);
	}
	public boolean statusMissing(int status) {
		boolean OK = false;
		if (status==404)
			OK = true;
		return (OK);
	}
	public String documentToString(Document doc) {
		StringBuffer buff = new StringBuffer("Document[");
		String guid = doc.getGuid();
		String name = doc.getDocumentName();
		buff.append(guid);
		if (name != null) {
			buff.append(",");
			buff.append(name);
		}
		buff.append("]");
		return (buff.toString());
	}
	protected File initFile(String imageName) throws FileNotFoundException {

		if (imageName == null){
			throw new NullPointerException("initFile passed null argument");
		}
		File dir = FileUtils.resourceDirectory();
		if (!dir.exists()) {
			throw new FileNotFoundException("Directory not found:"
					+ dir.getAbsolutePath());
		}
		File imageFile = new File(dir, imageName);
		if (!imageFile.exists()) {
			throw new FileNotFoundException("Image not found:"
					+ imageFile.getAbsolutePath());
		}
		return (imageFile);
	}
	
	public void displayRegistryParameters(List<RegistryParameters> registryParameters){
		if (registryParameters == null){
			log.info("Null RegistryParameters");
		}
		for (int i=0;i<registryParameters.size(); i++){
			RegistryParameters r = (RegistryParameters) registryParameters.get(i);
			log.info("Registry Parameters:" + r.getRegistryId() + "," + r.getRegistryName());
			List<Parameter> params = r.getParameters();
			for (int k=0;k<params.size();k++){
				Parameter p = params.get(k);
				log.info("  Parameter name=" + p.getName() + ", value=" + p.getValue());
			}
			
		}
	}
	public String getMedCommonsParameter(List<RegistryParameters> registryParameters, String name){
		String value = null;
		if (registryParameters == null) return null;
		for (int i=0;i<registryParameters.size();i++){
			RegistryParameters r = registryParameters.get(i);
			if (r.getRegistryId().equals(CXPConstants.MEDCOMMMONS_REGISTRY_ID)){
				List<Parameter> params = r.getParameters();
				for (int j=0;j<params.size();j++){
					Parameter p = params.get(j);
					if (p.getName().equals(name)){
						value = p.getValue();
						break;
					}
						
				}
			}
		}
		return(value);
	}

	/**
	 * Creates a registry parameter block specified name/value.
	 * Note it does not (yet) append the value -it creates a new registry parameter block.
	 * @param request
	 * @param pin
	 */
	public void assignMedCommonsParameter(PutRequest request, String name, String value){
		if (value != null){
			RegistryParameters registryParameters = new RegistryParameters();
			registryParameters.setRegistryName(CXPConstants.MEDCOMMMONS_REGISTRY);
			registryParameters.setRegistryId(CXPConstants.MEDCOMMMONS_REGISTRY_ID);
			Parameter parameter = new Parameter();
			parameter.setName(name);
			parameter.setValue(value);
			registryParameters.getParameters().add(parameter);
			request.getRegistryParameters().add(registryParameters);
			
		}
	}
	/**
	 * Creates a registry parameter block specified name/value.
	 * Note it does not (yet) append the value -it creates a new registry parameter block.
	 * @param request
	 * @param pin
	 */
	public void assignMedCommonsParameter(GetRequest request, String name, String value){
		if (value != null){
			RegistryParameters registryParameters = new RegistryParameters();
			registryParameters.setRegistryName(CXPConstants.MEDCOMMMONS_REGISTRY);
			registryParameters.setRegistryId(CXPConstants.MEDCOMMMONS_REGISTRY_ID);
			Parameter parameter = new Parameter();
			parameter.setName(name);
			parameter.setValue(value);
			registryParameters.getParameters().add(parameter);
			request.getRegistryParameters().add(registryParameters);
			
		}
	}
}
