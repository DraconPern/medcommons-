package net.medcommons.emcbridge;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


import net.medcommons.emcbridge.data.DicomWrapper;
import net.medcommons.emcbridge.data.ReferenceObject;
import net.medcommons.emcbridge.data.SeriesObject;
import net.medcommons.modules.services.interfaces.DicomMetadata;

import com.documentum.fc.common.DfException;

/**
 * Downloads data from Documentum and uploads to MedCommons.
 * 
 * @author mesozoic
 *
 */
public class GenerateCCRFromDFCClient extends DfcClient{
	
	
	//SeriesObject seriesObject = null;
	

	Hashtable<String, ReferenceObject> referencedObjects = new Hashtable<String, ReferenceObject>();
	 /**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(GenerateCCRFromDFCClient.class.getName());
	
	public GenerateCCRFromDFCClient(String docBase, String userName, String passWord, String rootDocumentId) throws IOException{
		super(docBase, userName, passWord,rootDocumentId);
		
	}
	
	
	
	
	

	public  void dumpMap(String title, Map<String, List<String>> aMap){
		log.info("Title:" + title);
		//Map<String, List<String>> imageMap = masterClientData.getImageMap();
		Set<String> keySet = aMap.keySet();
		Iterator<String> keys = keySet.iterator();
		while (keys.hasNext()){
			String key = keys.next();
			Object obj = aMap.get(key);
			log.info("Key:" + key + "=> " + obj );
		}
	}
	

	
	public void getDICOMFiles(Map<String, List<String>> aMap) throws DfException, IOException, NoSuchAlgorithmException{
		Set<String> keySet = aMap.keySet();
		Iterator<String> keys = keySet.iterator();
		while (keys.hasNext()){
			String key = keys.next();
			Object obj = aMap.get(key);
			String dicomDocumentId = null;
			log.info("DICOM Key is " + key + ", obj=" + obj);
			if (obj instanceof LinkedList){
				LinkedList lobj = (LinkedList) obj;
				Iterator it = lobj.iterator();
				while(it.hasNext()){
					Object itOb = it.next();
					
					log.info("linked list member is " + itOb);
					//dicomDocumentId = ((String[]) itObj)[0];
				}
			}
			else{
				dicomDocumentId = (String) obj;
			}
			boolean readData = false;
			if (readData){
				DicomWrapper dicomObject = createMetadataFromImage(dicomDocumentId);
			
				String seriesInstanceUID = dicomObject.getDicomMetadata().getSeriesInstanceUid();
				SeriesObject series = (SeriesObject) referencedObjects.get(seriesInstanceUID);
				if (series == null){
					DicomMetadata d = dicomObject.getDicomMetadata();
					series = new SeriesObject(d.getSeriesInstanceUid(), d.getStudyInstanceUid(),series.getIdentifier());
					referencedObjects.put(d.getSeriesInstanceUid(), series);
					log.info("Put DICOM object into series");
				}
				series.addImage(dicomObject);
				log.info("Key:" + key + "=> " + obj );
			}
			else{
				log.info("Extracting dicom data from documentum metadata");
				DicomWrapper dicomObject = createMetadataFromMetadata(dicomDocumentId);
				log.info("Extracting dicom data from documentum metadata" + dicomObject.getDicomMetadata());
				String seriesInstanceUID = dicomObject.getDicomMetadata().getSeriesInstanceUid();
				SeriesObject series = (SeriesObject) referencedObjects.get(seriesInstanceUID);
				if (series == null){
					DicomMetadata d = dicomObject.getDicomMetadata();
					series = new SeriesObject(d.getSeriesInstanceUid(), d.getStudyInstanceUid(),series.getIdentifier());
					referencedObjects.put(d.getSeriesInstanceUid(), series);
					log.info("Put DICOM object into series");
				}
				series.addImage(dicomObject);
				log.info("Key:" + key + "=> " + obj );
			}
		}
	}
	
	
}





