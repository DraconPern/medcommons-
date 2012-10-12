package net.medcommons.modules.cxp.server;

import static net.medcommons.modules.filestore.DicomMetadataKeys.SERIES_INSTANCE_UID;
import static net.medcommons.modules.filestore.DicomMetadataKeys.STUDY_DESCRIPTION;
import static net.medcommons.modules.filestore.DicomMetadataKeys.STUDY_INSTANCE_UID;
import static net.medcommons.modules.utils.DateFormats.EXACT_DATE_TIME_FORMAT;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import net.medcommons.Version;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.dicom.ParseFile;
import net.medcommons.modules.repository.metadata.RepositoryElement;
import net.medcommons.modules.repository.metadata.RepositoryElementConstants;
import net.medcommons.modules.repository.metadata.RepositoryLoader;
import net.medcommons.modules.services.interfaces.CompoundDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DicomMetadata;
import net.medcommons.modules.services.interfaces.DocumentIndexService;
import net.medcommons.modules.services.interfaces.MetadataHandler;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.ThumbnailGenerator;
import net.medcommons.modules.utils.DocumentTypes;
import net.medcommons.modules.xml.MedCommonsConstants;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;


public class RepositoryMetadataHandler implements MetadataHandler,RepositoryElementConstants {
	private static Logger log = Logger.getLogger("RepositoryMetadataHandler");
	
	private List<DicomMetadata> documentMetadata = new ArrayList<DicomMetadata>();
	
	private static String templateDirectory = null;
	
	private final static String dicomFileTemplate = "repositoryDicomFileTemplate.xml";
	private final static String compoundDocumentTemplate = "compoundDocumentTemplate.xml";
	private final static String compoundDocumentDicomMetadataTemplate = "compoundDocumentDicomMetadataTemplate.xml";

	private static String CURRENT_SOFTWARE_REVISION = "UNKNOWN";
	
	private DocumentIndexService indexService = Configuration.getBean("documentIndexService");
	
	private ThumbnailGenerator thumbnailGenerator = null;
	
	static{
		String deployedDirectory = "data/xds-templates";
		String compileDirectory = "etc/static-files/xds-templates";
		File f = new File(deployedDirectory);
		if (f.exists())
			templateDirectory = deployedDirectory;
		else{
			templateDirectory = compileDirectory;
		}
		try{
			CURRENT_SOFTWARE_REVISION = Version.getRevision();
			
		}
		catch(Exception e){
			log.error("Unable to initialize softare revision number");     
			if (CURRENT_SOFTWARE_REVISION==null)
				CURRENT_SOFTWARE_REVISION = "UNKNOWN";
		}
	}
	
	/**
	 * Clears the list of DICOM metadata so that object can be reused.
	 *
	 */
	public void clear(){
		documentMetadata = new ArrayList<DicomMetadata>();
	}
	
	/**
	 * Extracts information from the document and sets it on the CompoundDocumentDescriptor.
	 * 
	 * @param documentType
	 * @param file
	 */
	public Object addDocument(CompoundDocumentDescriptor document, InputStream in) throws IOException{
		//log.info("Adding document of type " + document.getContentType());
		if (!DocumentTypes.DICOM_MIME_TYPE.equals(document.getContentType()))
			throw new IOException("Unexpected document type  - must be DICOM:" + document);
		try{
			ParseFile parseFile = new ParseFile(in);
			DicomMetadata metadata = parseFile.extractMetadata();
			//log.info(metadata.getInstanceNumber() + ", " + metadata.getSopInstanceUID() + "," + metadata.getSeriesDescription());
			return(addDocument(document, metadata));
		}
		catch(NoSuchAlgorithmException e){
			throw new RuntimeException("Error extracting metadata from document of type " +
					document.getContentType(),
					e);
		}
	}
	
	/**
	 * Extracts information from the DICOM metadata and sets it on the CompoundDocumentDescriptor.
	 * 
	 * @return
	 */
	public Object addDocument(CompoundDocumentDescriptor document, DicomMetadata metadata) throws IOException{
      
        if (!DocumentTypes.DICOM_MIME_TYPE.equals(document.getContentType()))
            throw new IOException("Unexpected document type  - must be DICOM:" + document);
        
        
        log.info(" addDocument: " + metadata.getInstanceNumber() + ", " + metadata.getSopInstanceUid() + "," + metadata.getSeriesDescription());
        
        metadata.setGuid(document.getGuid());
        
        metadata.setDocumentName(document.getDocumentName());
        
        documentMetadata.add(metadata);
        
        // Add meta data fields to the document descriptor as well
        document.addMetadata(STUDY_INSTANCE_UID, metadata.getStudyInstanceUid());
        document.addMetadata(SERIES_INSTANCE_UID, metadata.getSeriesInstanceUid());
        document.addMetadata(STUDY_DESCRIPTION, metadata.getStudyDescription());
        return(metadata);
    }
	
	public void generateMetadataFile(File f, boolean add) throws IOException, JDOMException, ServiceException {
	    
		if (f == null) 
		    throw new NullPointerException("Null file specified for metadata");
		
	    OutputStream outStream  = null;
		try {
			// We assume that the metadata is the same for all dicom objects.
			// May need to be revisited
			 
		    Document doc = null;
			RepositoryElement metadata = null;
			if(add && f.exists()) {
			    doc = RepositoryLoader.loadDocument(new FileInputStream(f));
			}
			else {
			    doc = generateCompoundDocument();
			    RepositoryElement root = (RepositoryElement) doc.getRootElement();
				metadata = root.getChild(METADATA);
			    boolean success  = root.removeContent(metadata);
			    if(!success)
			        throw new RuntimeException("Unable to remove " + METADATA + " element ");
			    
			    metadata = generateDicomMetadata(documentMetadata.get(0));
			    root.addChild(metadata);
			}
		
			RepositoryElement root = (RepositoryElement) doc.getRootElement();
			RepositoryElement files = root.getChild(FILES);
			
			log.info("About to create image data in metadata.xml for " + documentMetadata.size() + " images");
			for(DicomMetadata md : documentMetadata) {
				RepositoryElement instance = generateFileElement(md);
				files.addChild(instance);
			}
			
			outStream = new FileOutputStream(f);
			PrintStream out = new PrintStream(outStream);
			out.print(root.toXml());
		}
		finally {
		    closeQuietly(outStream);
		}
	}

	private Document generateCompoundDocument()throws IOException, JDOMException{
		Document doc = RepositoryLoader.loadTemplate(templateDirectory + "/" + compoundDocumentTemplate);
		RepositoryElement element = (RepositoryElement) doc.getRootElement();
		element.getChild(Source).setText("1.0");
		element.getChild(Revision).setText(CURRENT_SOFTWARE_REVISION);
	    element.getChild(CreationDate).setText(formattedDate(new Date(System.currentTimeMillis())));
		return(doc);
		
	}

	
	private RepositoryElement generateDicomMetadata(DicomMetadata dicomMetadata) throws IOException, JDOMException{
		Document doc = RepositoryLoader.loadTemplate(templateDirectory + "/" + compoundDocumentDicomMetadataTemplate);
		RepositoryElement element = (RepositoryElement) doc.getRootElement();
		element.getChild(StudyInstanceUID).setText(dicomMetadata.getStudyInstanceUid());
		if (dicomMetadata.getStudyDate() != null)
			element.getChild(StudyDate).setText(formattedDate(dicomMetadata.getStudyDate()));
		element.getChild(StudyDescription).setText(dicomMetadata.getStudyDescription());
		if (dicomMetadata.getSeriesDate() != null)
			element.getChild(SeriesDate).setText(formattedDate(dicomMetadata.getSeriesDate()));
		element.getChild(SeriesDescription).setText(dicomMetadata.getSeriesDescription());
		element.getChild(SeriesNumber).setText(Integer.toString(dicomMetadata.getSeriesNumber()));
		element.getChild(SeriesInstanceUID).setText(dicomMetadata.getSeriesInstanceUid());
		
		element.getChild(Modality).setText(dicomMetadata.getModality());
		doc.removeContent(element);
		return(element);
		
	}
	private RepositoryElement generateFileElement(DicomMetadata dicomMetadata) throws IOException, JDOMException{
		Document instanceDoc = RepositoryLoader.loadTemplate(templateDirectory + "/" + dicomFileTemplate);
		RepositoryElement instanceReference = (RepositoryElement) instanceDoc.getRootElement();
		instanceReference.getChild("SopInstanceUID").setText(dicomMetadata.getSopInstanceUid());
		int frames = dicomMetadata.getFrames();
		if(frames !=DicomMetadata.INT_MISSING_VALUE)
			instanceReference.getChild("Frames").setText(Integer.toString(frames));
		instanceReference.getChild("ContentType").setText(DocumentTypes.DICOM_MIME_TYPE);
		instanceReference.getChild("WindowWidth").setText(dicomMetadata.getWindowWidth());
		instanceReference.getChild("WindowCenter").setText(dicomMetadata.getWindowCenter());
		instanceReference.getChild("DocumentName").setText(dicomMetadata.getDocumentName());
		int instanceNumber = dicomMetadata.getInstanceNumber();
		instanceReference.getChild("InstanceNumber").setText(Integer.toString(instanceNumber));
		// The display order might changed by a subsequent task; here we just set a default.
		instanceReference.getChild(DisplayOrder).setText(Integer.toString(dicomMetadata.getInstanceNumber()));
		instanceDoc.removeContent(instanceReference);
		return(instanceReference);
	}
	
	public static List<DicomMetadata> parseMetadata(InputStream in) throws IOException, JDOMException{
		Document metadataDoc = RepositoryLoader.loadDocument(in);
		Namespace namespace =Namespace.getNamespace(MedCommonsConstants.MC_REPOSITORY_NAMESPACE_URN);
		
		RepositoryElement root = (RepositoryElement) metadataDoc.getRootElement();
		root.setNamespace(namespace);
		List <DicomMetadata> list = new ArrayList<DicomMetadata>();
		RepositoryElement meta = root.getChild(METADATA);
		RepositoryElement files = root.getChild(FILES);
		files.setNamespace(namespace);
		List allFiles = files.getChildren();
		Iterator<RepositoryElement> iter  = allFiles.iterator();
		while (iter.hasNext()){
			RepositoryElement file = iter.next();
			
			DicomMetadata dicomMetadata = new DicomMetadata();
			extractDicomMetadata(meta, dicomMetadata);
			extractFileData(file, dicomMetadata);
			list.add(dicomMetadata);
		}
		return(list);
		
	}



	private static void  extractDicomMetadata(RepositoryElement metadataElement, DicomMetadata dicomMetadata){
		
		
		dicomMetadata.setDocumentName(metadataElement.getChildText(DocumentName));
		dicomMetadata.setWindowCenter(metadataElement.getChildText(WindowCenter));
		dicomMetadata.setWindowWidth(metadataElement.getChildText(WindowWidth));
		
		dicomMetadata.setStudyInstanceUid(metadataElement.getChildText(StudyInstanceUID));
		String studyDate = metadataElement.getChildText(StudyDate);
		if (!isblank(studyDate)){
			try{
				dicomMetadata.setStudyDate(parseFormattedDate(studyDate));
			}
			catch(ParseException e){
				throw new RuntimeException("Unable to parse date string:" + studyDate);
			}
		}
		String seriesDate = metadataElement.getChildText(SeriesDate);
		if (!isblank(seriesDate)){
			try{
				dicomMetadata.setSeriesDate(parseFormattedDate(seriesDate));
			}
			catch(ParseException e){
				throw new RuntimeException("Unable to parse date string:" + seriesDate);
			}
		}
		dicomMetadata.setStudyInstanceUid(metadataElement.getChildText(StudyInstanceUID));
		dicomMetadata.setStudyDescription(metadataElement.getChildText(StudyDescription));
		dicomMetadata.setSeriesDescription(metadataElement.getChildText(SeriesDescription));
		dicomMetadata.setSeriesInstanceUid(metadataElement.getChildText(SeriesInstanceUID));
		
		dicomMetadata.setModality(metadataElement.getChildText(Modality));
		String seriesNumber = metadataElement.getChildText(SeriesNumber);
		if (!isblank(seriesNumber)){
			dicomMetadata.setSeriesNumber(Integer.parseInt(seriesNumber));
		}
		
}
	private static void extractFileData(RepositoryElement fileData, DicomMetadata dicomMetadata){
		//log.info("file:" + fileData.toXml());
		dicomMetadata.setDocumentName(fileData.getChildText(DocumentName));
		dicomMetadata.setWindowCenter(fileData.getChildText(WindowCenter));
		dicomMetadata.setWindowWidth(fileData.getChildText(WindowWidth));
		String instanceNumber = fileData.getChildText(InstanceNumber);
		if (!isblank(instanceNumber)){
			dicomMetadata.setInstanceNumber(Integer.parseInt(instanceNumber));
		}
		String displayOrder = fileData.getChildText(DisplayOrder);
		//
		if (!isblank(displayOrder)){
			dicomMetadata.setDisplayOrder(Integer.parseInt(displayOrder));
		}
		
		String nFrames = fileData.getChildText(Frames);
		if (!isblank(nFrames)){
			dicomMetadata.setFrames(Integer.parseInt(nFrames));
		}
		else
			dicomMetadata.setFrames(1);
		
		dicomMetadata.setSopInstanceUid(fileData.getChildText(SopInstanceUID));
	}
	private static boolean isblank(String s){
		if ((s==null) || (s.trim().equals("")))
			return(true);
		else
			return(false);
	}
	  /**
     * Return a CCR compatible format of the time.
     * @return
     */
    private static String formattedDate(Date d) {        
        DateFormat df = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(d);
    }
    
    private static Date parseFormattedDate(String dateString) throws ParseException{
    	if (dateString == null) throw new NullPointerException("Null dateString");
    	DateFormat df = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
    	 df.setTimeZone(TimeZone.getTimeZone("GMT"));
    	 return(df.parse(dateString));
    }
   
    public void setThumbnailGenerator(ThumbnailGenerator t){
    	this.thumbnailGenerator = t;
    }
    
    public ThumbnailGenerator getThumbnailGenerator(){
    	return(thumbnailGenerator);
    }
}

