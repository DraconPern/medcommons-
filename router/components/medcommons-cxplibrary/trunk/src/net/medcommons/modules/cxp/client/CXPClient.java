package net.medcommons.modules.cxp.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import net.medcommons.modules.crypto.io.FileGuid;
import net.medcommons.modules.crypto.io.SHA1InputStream;
import net.medcommons.modules.cxp.CXPConstants;
import net.medcommons.modules.cxp.protocol.MeteredIO;
import net.medcommons.modules.cxp.protocol.MeteredSSLSocketFactory;
import net.medcommons.modules.cxp.protocol.MeteredSocketFactory;
import net.medcommons.modules.utils.PerformanceMeasurement;
import net.medcommons.modules.utils.SpecialFileFilter;
import net.medcommons.modules.utils.SupportedDocuments;
import net.medcommons.modules.utils.UnsupportedDocumentException;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.codehaus.xfire.attachments.StreamedAttachments;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.transport.Transport;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;
import org.codehaus.xfire.transport.http.HttpTransport;
import org.cxp2.Document;
import org.cxp2.GetResponse;
import org.cxp2.Parameter;
import org.cxp2.PutResponse;
import org.cxp2.RegistryParameters;
import org.cxp2.soap.CXPService;

public class CXPClient implements MeteredSocketListener {
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger("CXPClient");

	public static final String HTTP_CLIENT = "httpClient";

	private String endpoint = null;

	protected CXPService service = null;

	File attachmentCacheFolder = null;

	private static String proxyHostname = null;

	private static String proxyPort = null;

	private MeteredIO meteredSocket = null;


	/**
	 * Hack. This is defined in import org.codehaus.xfire.transport.http.CommonsHttpMessageSender
	 * in xfire 1.2.5+ - but we shouldn't upgrade xfire the day before we install at a customer site.
	 */
	private static String DISABLE_PROXY_UTILS = "http.disable.proxy.utils";

	private Client client = null;

	/**
	 * Perhaps a kludge. Can specify a proxy for the HTTP connection. Perhaps
	 * this should go through a configuration file of some sort - but there is
	 * no other configuration in this class. Perhaps these should be passed down
	 * as arguments when instances are created - but it really is a global
	 * setting for a client once it's been set.
	 *
	 * @param hostname
	 * @param port
	 */
	public static void setHttpProxy(String hostname, String port) {
		if (true) throw new RuntimeException("setHttpProxy");
		log.info("Setting CXP proxy to " + hostname + ":" + port);
		proxyHostname = hostname;
		proxyPort = port;
	}

	/**
	 * Number of calculated bytes; used for returning length of SOAP message.
	 * This can be calculated before the transaction starts.
	 */
	private long byteCount = 0;

	/**
	 * Number of bytes transferred. This is updated as the transfer continues.
	 */
	//private long transferByteCount = 0;

	private int objectCount = 0;
	
	/**
	 * If set to true then file extensions will automatically be added to 
	 * downloaded content.
	 */
	private boolean addFileExtensions = true;

	/**
	 * Creates CXP Client object with streaming cache folder in the specified
	 * location.
	 *
	 * @param endpoint
	 * @param attachmentCacheFolder

	 * @throws Exception
	 */
	public CXPClient(String endpoint, File attachmentCacheFolder)
			throws Exception {
		this.endpoint = endpoint;
		this.attachmentCacheFolder = attachmentCacheFolder;
		serviceMTOMSetup();
	}

	/**
	 * Creates CXP Client object.
	 *
	 * @param endpoint
	 * @throws Exception
	 */
	public CXPClient(String endpoint) throws Exception {
		this.endpoint = endpoint;
		serviceMTOMSetup();
	}

	public CXPService getService() {
		return (this.service);
	}

	public int getObjectCount() {
		return (this.objectCount);
	}

	public void resetByteCount() {
		byteCount = 0;
	}

	public long getByteCount() {
		return (this.byteCount);
	}

	public long getOutputBytes() {
		if (meteredSocket != null)
			return(meteredSocket.getOutputBytes());
		else
			return(0);

	}
	public long getInputBytes() {
		if (meteredSocket != null){
			//log.info("getInputBytes:" + meteredSocket.getInputBytes());
			return(meteredSocket.getInputBytes());
		}
		else
			return(0);


	}

	// private Soap11Binding binding;

	/**
	 * Sets up client stub to use MTOM transfers.
	 *
	 * @throws MalformedURLException
	 */
	private void serviceMTOMSetup() throws MalformedURLException {

		URL cxpEndpointURL = new URL(endpoint);
		String cxpProtocol = cxpEndpointURL.getProtocol();
		log.debug("cxpProtocol is :" +cxpProtocol);
		
		MeteredSocketFactory.register(cxpProtocol,this);
		
		log.info("Socket:" + Protocol.getProtocol(cxpProtocol));

		ObjectServiceFactory osf = new ObjectServiceFactory();
		Service serviceModel = osf.create(CXPService.class, "CXPService",
				"http://org.cxp2", null);

		service = (CXPService) new XFireProxyFactory().create(serviceModel,
				endpoint);

		// Setup properties
		client = Client.getInstance(service);
/*
 *  TODO:
 *  Make new class that extends default socket factory
 *  Make new socket class that extends socket
 *  Make i/o streams (input and output) that count bytes)
 */
		/*
		MultiThreadedHttpConnectionManager manager = new        MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams myParams = new HttpConnectionManagerParams();
		myParams.setDefaultMaxConnectionsPerHost(25);
		myParams.setMaxTotalConnections(50);

		manager.setParams(myParams);

		MeteredHttpClient myHTTPClient = new MeteredHttpClient(manager);


		client.setProperty(CommonsHttpMessageSender.HTTP_CLIENT, myHTTPClient);
*/

		client.setProperty("mtom-enabled", "true");
		client.setProperty(HttpTransport.CHUNKING_ENABLED, "true");

		if (attachmentCacheFolder == null)
			attachmentCacheFolder = new File("CXPCache");
		
		if (!attachmentCacheFolder.exists()) {
			attachmentCacheFolder.mkdirs();
		}

		log.info("Attachment directory set to " + attachmentCacheFolder.getAbsolutePath());
		client.setProperty(StreamedAttachments.ATTACHMENT_DIRECTORY,
				attachmentCacheFolder);

		client.setProperty(StreamedAttachments.ATTACHMENT_MEMORY_THRESHOLD,
				new Integer(10000));


		if ((proxyHostname != null) && (proxyPort != null)) {
			// Force the client to use the HTTP binding and bypass the
			// LocalTransport
			//Transport transport = osf.getTransportManager().getTransport(
			//		SoapHttpTransport.SOAP12_HTTP_BINDING);
			Collection<Transport> transports = (Collection<Transport>) osf
					.getTransportManager().getTransports();
			Iterator<Transport> iter = transports.iterator();
			while (iter.hasNext()) {
				Transport trans = iter.next();
				log.info("Transport:" + trans.getClass() + ":" + trans);

			}
			Transport cxpTranport = osf.getTransportManager()
					.getTransportForUri(endpoint);
			log.info("Transport for cxp endpoint is " + cxpTranport.getClass());
			//client.setTransport(transport);
			client.setProperty(CommonsHttpMessageSender.HTTP_PROXY_HOST,
					proxyHostname);
			client.setProperty(CommonsHttpMessageSender.HTTP_PROXY_PORT,
					proxyPort);
			log.info("Client initialized to use proxy " + proxyHostname + " "
					+ proxyPort);

			//client.setProperty(CommonsHttpMessageSender.DISABLE_PROXY_UTILS,"true");
			client.setProperty(DISABLE_PROXY_UTILS,"true");
		} else {
			log.info("no proxy configured:" + proxyHostname + " " + proxyPort);
		}
		log.info("Final configs");
		//displayClientConfigs();



	}

	public void displayClientConfigs() {
		log.info("displayClientConfigs");

		log.info("http proxy host: " + client.getProperty(CommonsHttpMessageSender.HTTP_PROXY_HOST));
		log.info("http proxy port: " + client.getProperty(CommonsHttpMessageSender.HTTP_PROXY_PORT));
		log.info("Cache directory: " + client.getProperty(StreamedAttachments.ATTACHMENT_DIRECTORY));
		log.info("Memory threshold:" + client.getProperty(StreamedAttachments.ATTACHMENT_MEMORY_THRESHOLD));
		log.info("Transport:" + client.getTransport());

		Transport t = client.getTransport();
		String bindings[] = t.getSupportedBindings();
		log.debug("Bindings:");
		for (int i = 0; i < bindings.length; i++) {
			log.debug(" " + bindings[i]);
		}
		List handlers = t.getInHandlers();
		log.debug("in handlers:");
		for (int i = 0; i < handlers.size(); i++) {
			log.debug(" " + handlers.get(i));
		}
		handlers = t.getOutHandlers();
		log.debug("out handlers:");
		for (int i = 0; i < handlers.size(); i++) {
			log.debug(" " + handlers.get(i));
		}

	}

	/**
	 * Creates a simple document to upload via CXP PUT.
	 *
	 * @param file
	 * @param mimeType
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public Document createSimpleDocument(File file, String mimeType, String name)
			throws IOException, NoSuchAlgorithmException {
		Document document = new Document();

		log.info("Create simple document:" + file.getAbsolutePath() + " "
				+ mimeType + " " + name);
		if (!file.exists())
			throw new FileNotFoundException(file.getAbsolutePath());

		String guid = FileGuid.calculateFileGuid(file);
		byteCount += file.length();

		document.setContentType(mimeType);
		document.setDescription(name);
		document.setDocumentName(name);
		document.setGuid(guid); // Not necessary - but useful for housekeeping

		FileDataSource dataSource = new FileDataSource(file);

		DataHandler dh = new DataHandler(dataSource);

		document.setData(dh);
		return (document);

	}

	public List<Document> createCompoundDocument(File seriesDir,
			String mimeType, String name, boolean useDocumentNames)
			throws IOException, NoSuchAlgorithmException {
		List<Document> documents = new ArrayList<Document>();

		// Want to filter out files like .DS_Store - any file that starts with
		// '.'.
		SpecialFileFilter filter = new SpecialFileFilter();
		filter.setFilterType(".");

		File files[] = seriesDir.listFiles(filter);
		DocumentInfo allDocs[] = new DocumentInfo[files.length];

		String contentType = mimeType;

		List<String> sha1List = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {

			DocumentInfo aDoc = new DocumentInfo();
			allDocs[i] = aDoc;
			aDoc.f = files[i];
			if (!aDoc.f.exists())
				throw new FileNotFoundException(aDoc.f.getAbsolutePath());
			if (!aDoc.f.canRead()){
			    throw new FileNotFoundException(aDoc.f.getAbsolutePath() + " can not be read (permissions)");
			}
			aDoc.sha1 = FileGuid.calculateFileGuid(aDoc.f);
			sha1List.add(aDoc.sha1);
			aDoc.size = aDoc.f.length();
			aDoc.contentType = contentType;
			byteCount += aDoc.size;
			Document docinfo = new Document();

			FileDataSource dataSource = new FileDataSource(aDoc.f);

			DataHandler dh = new DataHandler(dataSource);
			docinfo.setParentName(seriesDir.getName());
			docinfo.setData(dh);
			docinfo.setContentType(contentType);
			if (useDocumentNames)
				docinfo.setDocumentName(aDoc.f.getName());

			documents.add(docinfo);

		}

		return (documents);

	}

	public static boolean statusOK(int status) {
		boolean OK = false;
		if ((status >= 200) && (status <= 299))
			OK = true;
		return (OK);
	}

	public static boolean statusMissing(int status) {
		boolean OK = false;
		if (status == 404)
			OK = true;
		return (OK);
	}

	private static String responseToString(Document responseDoc) {
		StringBuffer buff = new StringBuffer("Response Document[");
		buff.append("guid=");
		buff.append(responseDoc.getGuid());
		buff.append(", documentName=");
		buff.append(responseDoc.getDocumentName());
		buff.append(", contentType=");
		buff.append(responseDoc.getContentType());
		buff.append(", sha1=");
		buff.append(responseDoc.getSha1());
		buff.append(", parentName=");
		buff.append(responseDoc.getParentName());
		buff.append("]");
		return (buff.toString());
	}

	/**
	 * Writes CXP result status into log
	 * @param resp
	 */
	public static void displayResponseInfo(PutResponse resp) {
		List<Document> responseDocs = resp.getDocinfo();

		Iterator<Document> iter = responseDocs.iterator();
		log.info("Number of files successfully stored:" + responseDocs.size());

		List registryParameters = resp.getRegistryParameters();
		for (int i = 0; i < registryParameters.size(); i++) {
			RegistryParameters r = (RegistryParameters) registryParameters
					.get(i);
			log.info("Registry Parameters:" + r.getRegistryId() + ","
					+ r.getRegistryName());
			List<Parameter> params = r.getParameters();
			if (params.size() > 0) {
				log.info(" Parameters:");
				for (int k = 0; k < params.size(); k++) {
					Parameter p = params.get(k);
					log.info("  Parameter name=" + p.getName() + ", value="
							+ p.getValue());
				}
			} else {
				log.info("   Parameter list empty");
			}

		}

		log.info("Response: " + resp.getStatus() + ", " + resp.getReason());
	}

	/**
	 * Returns the specified parameter from the registry parameter block.
	 *
	 * Probably should have one for MEDCOMMONS_REGISTRY_ID and a more generic
	 * one.
	 *
	 * @param registryParameters
	 * @param name
	 * @return
	 */
	public static String getMedCommonsParameter(
			List<RegistryParameters> registryParameters, String name) {
		String value = null;
		if (registryParameters == null)
			return null;
		for (int i = 0; i < registryParameters.size(); i++) {
			RegistryParameters r = registryParameters.get(i);
			if (r.getRegistryId().equals(CXPConstants.MEDCOMMMONS_REGISTRY_ID)) {
				List<Parameter> params = r.getParameters();
				for (int j = 0; j < params.size(); j++) {
					Parameter p = params.get(j);
					if (p.getName().equals(name)) {
						value = p.getValue();
						break;
					}
				}
			}
		}
		return (value);
	}

	public static RegistryParameters generateSenderIdParameters(String senderId) {
		RegistryParameters params = new RegistryParameters();
		params.setRegistryName(CXPConstants.MEDCOMMMONS_REGISTRY);
		params.setRegistryId(CXPConstants.MEDCOMMMONS_REGISTRY_ID);
		
		if(senderId != null) {
		    Parameter param = new Parameter();
		    param.setName(CXPConstants.SenderProviderId);
		    param.setValue(senderId);
		    params.getParameters().add(param);
		}
		return (params);
	}
	public static RegistryParameters generateSenderIdParameters(String senderId, String authorizationToken){
		RegistryParameters params = generateSenderIdParameters(senderId);
		Parameter param = new Parameter();
		param.setName(CXPConstants.AUTHORIZATION_TOKEN);
		param.setValue(authorizationToken);
		params.getParameters().add(param);
		return(params);
	}

	public static void displayRegistryParameters(
			List<RegistryParameters> registryParameters) {
		if (registryParameters == null) {
			log.info("Null RegistryParameters");
		}
		if (registryParameters.size() == 0) {
			log.info("There are zero registry parameters specied in List");
		}
		for (int i = 0; i < registryParameters.size(); i++) {
			RegistryParameters r = (RegistryParameters) registryParameters
					.get(i);
			log.info("Registry Parameters:" + r.getRegistryId() + ","
					+ r.getRegistryName());
			List<Parameter> params = r.getParameters();
			for (int k = 0; k < params.size(); k++) {
				Parameter p = params.get(k);
				log.info("  Parameter name=" + p.getName() + ", value="
						+ p.getValue());
			}

		}
	}

	/**
	 * This inner class is used as a placeholder. The SHA1 hash can be put in
	 * here on input. This can be tested against the output in the response.
	 */
	private class DocumentInfo {
		File f = null;

		String sha1 = null;

		long size = -1;

		String contentType = null;
	}

	public void processGetResponse(GetResponse resp, long startTime,
			File resultsDirectory) throws IOException, NoSuchAlgorithmException {

		long totalBytes = 0;
		log.info("Response: " + resp.getStatus() + ", " + resp.getReason());

		boolean success = resultsDirectory.exists();
		if (!success)
			success = resultsDirectory.mkdir();

		if (!success)
			throw new RuntimeException("Unable to create results directory:"
					+ resultsDirectory.getAbsolutePath());
		displayRegistryParameters(resp.getRegistryParameters());
		List<Document> docs = resp.getDocinfo();

		Iterator<Document> allDocs = docs.iterator();
		int i = 0;
		while (allDocs.hasNext()) {
			Document doc = allDocs.next();
			log.debug("Document[" + i + "]" + doc.getGuid() + ", name="
					+ doc.getDocumentName() + ", contentType="
					+ doc.getContentType() + ", parentName="
					+ doc.getParentName());
			try{
				File documentFile = scratchFile(resultsDirectory, doc);
				doc.setDocumentName(documentFile.getAbsolutePath());
				String hash = writeFile(doc, documentFile);
				totalBytes += documentFile.length();
				log.debug("Completed writing file with server sha1 " + doc.getSha1()
						+ " and local calculated hash " + hash);
				if (!hash.equalsIgnoreCase(doc.getSha1())){
					throw new IOException("SHA1 mismatch on file - server value:\n " + doc.getSha1() +
							", locally calculated value:\n " + hash);
				}

				// String displayName = documentToString(doc);

				// assertEquals("SHA1 hash of input and output match:" +
				// displayName, hash, doc.getSha1());
				// log.info("Document:" + displayName + " successfully retrieved");
				i++;
			}
			catch(UnsupportedDocumentException e){
				log.error("No client support for document of type :" + doc.getContentType());
			}
		}
		long endTime = System.currentTimeMillis();
		log.info(PerformanceMeasurement.throughputString("GET:"
				+ resultsDirectory.getAbsolutePath(), (endTime - startTime),
				totalBytes));
		//log.info("output bytes = " + getOutputBytes());
		//log.info("input bytes = " + getInputBytes());
	}

	/**
	 * Writes out file referenced in Document to specified file.
	 * <P>
	 * Returns SHA-1 hash of the contents of the file.
	 *
	 * @param doc
	 * @param documentFile
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private String writeFile(Document doc, File documentFile)
			throws IOException, NoSuchAlgorithmException {
		// InputStream is = null;
		FileOutputStream fileOutput = null;
		SHA1InputStream shaIs = null;
		String sha1_value = null;
		/*
		log.info("About to create document:" + documentFile.getAbsolutePath()
				+ " current byte count is " + transferByteCount
				+ " objectcount = " + objectCount);
				*/
		// String displayName = documentToString(doc);

		// assertNotNull("Data stream for document:" + displayName,
		// doc.getData());
		DataHandler dh = doc.getData();
		InputStream is = dh.getInputStream();
		// s = doc.getData().getInputStream();
		shaIs = new SHA1InputStream(is);

		fileOutput = new FileOutputStream(documentFile);
		try {
		    byte[] buff = new byte[4096 * 8];
		    int n = 0;
		    
		    while ((n = shaIs.read(buff, 0, buff.length)) != -1) {
		        fileOutput.write(buff, 0, n);
		    }
		}
		finally {
		    fileOutput.close();
		}
		objectCount++;
		// is = null;
		fileOutput = null;

		sha1_value = shaIs.getHash();

		return (sha1_value);
	}

	private String writeFile2(Document doc, File documentFile)
			throws IOException, NoSuchAlgorithmException {
		// InputStream is = null;
		FileOutputStream fileOutput = null;
		SHA1InputStream shaIs = null;
		String sha1_value = null;
		log.debug("About to create document:" + documentFile.getAbsolutePath());
		//String displayName = documentToString(doc);

		//assertNotNull("Data stream for document:" + displayName, doc.getData());
		DataHandler dh = doc.getData();
		InputStream is = dh.getInputStream();
		// s = doc.getData().getInputStream();
		shaIs = new SHA1InputStream(is);

		fileOutput = new FileOutputStream(documentFile);
		byte[] buff = new byte[4096 * 8];
		int n = 0;

		while ((n = shaIs.read(buff, 0, buff.length)) != -1) {
			fileOutput.write(buff, 0, n);

		}
		// is = null;
		fileOutput = null;

		sha1_value = shaIs.getHash();

		return (sha1_value);
	}

	/**
	 * Creates a scratch file for data to be written to. The file extension is a
	 * function of the contentType in the CXP returned document array.
	 *
	 * @param parentDirectory
	 * @param doc
	 * @return
	 */
	protected File scratchFile(File parentDirectory, Document doc) throws UnsupportedDocumentException{
		File out = null;

		if (doc.getDocumentName() == null) {
			// It's a simple document.
			SupportedDocuments docType 
			    = SupportedDocuments.getDocumentType(doc.getContentType());
			
			String fileName = doc.getGuid();
			if(addFileExtensions)
			    fileName += "."+docType.getFileExtension();
			
            out = new File(parentDirectory, fileName);
		} else {
			File dir = new File(parentDirectory, doc.getGuid());
			dir.mkdir();
			SupportedDocuments docType = SupportedDocuments.getDocumentType(doc
					.getContentType());
			out = new File(dir, doc.getDocumentName() + "." + docType.getFileExtension());
		}
		return (out);

	}
	/*
	 * class CXPFileDataSource extends FileDataSource{ InputStream
	 * inputStream=null; OutputStream outputStream = null;
	 *
	 * public CXPFileDataSource(File f){ super(f); } public InputStream
	 * getInputStream() throws IOException{ log.info("CXPFileDataSource:
	 * getInputStream()"); inputStream = super.getInputStream(); InputStream
	 * countInputStream = new CountInputStream(inputStream);
	 * return(countInputStream); } public OutputStream getOutputStream() throws
	 * IOException{ outputStream = super.getOutputStream();
	 * log.info("CXPFileDataSource: getOutputStream()");
	 * return(super.getOutputStream()); } }
	 */

	/* (non-Javadoc)
     * @see net.medcommons.modules.cxp.client.MeteredSocketListener#setMeteredSocket(net.medcommons.modules.cxp.protocol.MeteredIO)
     */
	public void setMeteredSocket(MeteredIO meteredSocket){
		//log.info("setMeteredSocket for " + this + " with " + meteredSocket);
		this.meteredSocket = meteredSocket;
	}

	/**
	 * Cancels the current read or write of the data on the next read()/write() call by the
	 * underlying IO streams.
	 *
	 */
	public void cancelStream(){
		if (this.meteredSocket != null){
			this.meteredSocket.cancelStream();
		}
	}

    public boolean getAddFileExtensions() {
        return addFileExtensions;
    }

    public void setAddFileExtensions(boolean addFileExtensions) {
        this.addFileExtensions = addFileExtensions;
    }
}
