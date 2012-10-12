package net.medcommons.client.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.medcommons.application.dicomclient.utils.CxpDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import astmOrgCCR.ActorType;
import astmOrgCCR.CodedDescriptionType;
import astmOrgCCR.ContinuityOfCareRecordDocument;
import astmOrgCCR.IDType;
import astmOrgCCR.PersonNameType;
import astmOrgCCR.PurposeType;
import astmOrgCCR.ReferenceType;
import astmOrgCCR.ActorType.Person;
import astmOrgCCR.ActorType.Person.Name;
import astmOrgCCR.CodedDescriptionType.ObjectAttribute;
import astmOrgCCR.CodedDescriptionType.ObjectAttribute.AttributeValue;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.Actors;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.Patient;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.References;
import astmOrgCCR.LocationDocument.Location;
import astmOrgCCR.LocationsDocument.Locations;

/**
 * Some CCR object handling routines.
 * Perhaps should be CCRUtil
 * @author mesozoic
 *
 */
public class CCRDocumentUtils {
     private static Logger log = Logger.getLogger(CCRDocumentUtils.class
                .getName());

     public ContinuityOfCareRecordDocument parseFile(File f) throws XmlException, IOException{
         return(ContinuityOfCareRecordDocument.Factory.parse(f));
     }
    public String getGuidFromMcidUrl(String mcidUrl) {
        String prefix = "mcid://";
        int i = mcidUrl.indexOf("mcid://");

        if (i != -1)
            return (mcidUrl.substring(prefix.length()));
        else
            throw new RuntimeException(
                    "Expected prefix 'mcid://' at start of value " + mcidUrl);
    }

    public String getObjectAttributeAttribute(ObjectAttribute objectAttribute) {
        String attribute = objectAttribute.getAttribute();
        return (attribute);
    }

    public String getObjectAttributeValue(ObjectAttribute objectAttribute) {
        String value = null;
        AttributeValue attrValue = objectAttribute.getAttributeValueList().get(
                0);
        XmlObject valueObj = attrValue.getValue();
        Node valueNode = valueObj.getDomNode();
        NodeList childList = valueNode.getChildNodes();

        // Take the first element's value.
        for (int i = 0; i < childList.getLength(); i++) {
            Node node = childList.item(i);
            value = node.getNodeValue();
            break;

        }

        return (value);
    }

    public List<CxpDocument> getReferencedDocuments(
            ContinuityOfCareRecordDocument ccr) {
        List<CxpDocument> documents = new ArrayList<CxpDocument>();

        References refs = ccr.getContinuityOfCareRecord().getReferences();
        if (refs != null) {
            if (refs.sizeOfReferenceArray() > 0) {
                List<ReferenceType> references = refs.getReferenceList();
                Iterator<ReferenceType> iter = references.iterator();
                while (iter.hasNext()) {
                    ReferenceType ref = iter.next();
                    documents.add(makeDocument(ref));
                }
            }
        }
        return (documents);
    }

    public String getPurpose(ContinuityOfCareRecordDocument ccr) {
        ContinuityOfCareRecord ccrDoc = ccr.getContinuityOfCareRecord();
        StringBuffer buff = new StringBuffer();
        List<PurposeType> purposeList = ccrDoc.getPurposeList();
        for (int i = 0; i < purposeList.size(); i++) {
            PurposeType aPurpose = purposeList.get(i);
            List<CodedDescriptionType> descriptions = aPurpose
                    .getDescriptionList();
            for (int j = 0; j < descriptions.size(); j++) {
                buff.append(descriptions.get(0).getText());
            }
        }
        return (buff.toString());
    }

    /**
     * Returns the first element in a list. Returns null
     * if it is an empty list; throws an IllegalStateException if there
     * is more than one element in the list.
     * TODO: Not sure this is the right API for this.
     * @param aList
     * @return
     */
    public String getStringValue(List<String> aList) {
        String value = null;
        if (aList.size() == 1) {
            value = aList.get(0);
        }
        else if (aList.size()>1){
        	throw new IllegalStateException("Expecting a single element in list, got " + aList.size() + ", initial element is " +
        			aList.get(0));
        }
        return (value);
    }

    public String getPatientName(ContinuityOfCareRecordDocument ccr) {
        String actorId = getPatientActorId(ccr);

        ActorType patientActor = getActorObject(ccr, actorId);
        Person person = patientActor.getPerson();
        
        Name name = person.getName();
        if (name == null){
        	return (null);
        }
        PersonNameType personName = name.getCurrentName();
        String family = getStringValue(personName.getFamilyList());
        String given = getStringValue(personName.getGivenList());
        String middle = getStringValue(personName.getMiddleList());
        String title = getStringValue(personName.getTitleList());
        String suffix = getStringValue(personName.getSuffixList());
        StringBuffer buff = new StringBuffer();
        if (title != null) {
            buff.append(title);
            buff.append(" ");
        }
        if (given != null) {
            buff.append(given);
            buff.append(" ");
        }
        if (middle != null) {
            buff.append(middle);
            buff.append(" ");
        }
        if (family != null) {
            buff.append(family);
            buff.append(" ");
        }
        if (suffix != null) {
            buff.append(suffix);
            buff.append(" ");
        }
        return (buff.toString());

    }

    public String getIdType(IDType idType) {
        CodedDescriptionType type = idType.getType();
        return (type.getText());
    }

    public String getIdValue(IDType idType) {
        return (idType.getID());
    }

    public List<IDType> getPatientIds(ContinuityOfCareRecordDocument ccr) {

        String actorId = getPatientActorId(ccr);
        ActorType patientActor = getActorObject(ccr, actorId);
        List<IDType> idList = patientActor.getIDsList();
        return (idList);
    }

    public String getPatientActorId(ContinuityOfCareRecordDocument ccr) {
        ContinuityOfCareRecord ccrDoc = ccr.getContinuityOfCareRecord();
        List<Patient> patients = ccrDoc.getPatientList();
        Patient firstPatient = patients.get(0);
        String actorId = firstPatient.getActorID();
        return (actorId);
    }

    public ActorType getActorObject(ContinuityOfCareRecordDocument ccr, String actorId) {
        ActorType matchingActor = null;
        ContinuityOfCareRecord ccrDoc = ccr.getContinuityOfCareRecord();
        Actors actors = ccrDoc.getActors();
        List<ActorType> actorList = actors.getActorList();
        Iterator<ActorType> iter = actorList.iterator();
        while (iter.hasNext()) {
            ActorType anActor = iter.next();
            if (actorId.equals(anActor.getActorObjectID())) {
                matchingActor = anActor;
                break;
            }
        }
        return (matchingActor);
    }
/**
 * Returns the Reference/Type/Text field from a ReferenceType object
 * <Reference>
      <ReferenceObjectID>5589618412580536</ReferenceObjectID>
      <Type>
        <Text>application/x-medcommons-ccr-history</Text>
      </Type>
 */
    public String getReferenceType(ReferenceType reference){
    	String refType = null;
    	CodedDescriptionType cdtType = reference.getType();
    	if (cdtType != null){
    		refType = cdtType.getText();
    	}
    	return(refType);
    }
    /*
     * @param reference
     * @return
     */
    private CxpDocument makeDocument(ReferenceType reference) {
        CxpDocument doc = new CxpDocument();
        doc.setContentType(doc.getContentType());

        Locations locations = reference.getLocations();
        List<Location> locationList = locations.getLocationList();
        Iterator<Location> iter = locationList.iterator();
        doc.setSize(0);
        while (iter.hasNext()) {
            Location loc = iter.next();
            List<ObjectAttribute> attrs = loc.getDescription()
                    .getObjectAttributeList();
            Iterator<ObjectAttribute> objectIter = attrs.iterator();
            while (objectIter.hasNext()) {
                ObjectAttribute obj = objectIter.next();
                String objectAttr = getObjectAttributeAttribute(obj);
                String objectValue = getObjectAttributeValue(obj);
                log.info("attribute=" + objectAttr + ", value=" + objectValue);
                if (objectAttr.equalsIgnoreCase("displayName"))
                    doc.setDocumentName(objectValue);
                else if (objectAttr.equalsIgnoreCase("URL")) {
                    String guid = getGuidFromMcidUrl(objectValue); // trims mcid:// ..
                    log.info(objectValue + " => " + guid);
                    doc.setGuid(guid);
                } else if (objectAttr.equalsIgnoreCase("Size")){
                    doc.setSize(Long.parseLong(objectValue));

                }

                else
                    log.info("Ignored attribute " + objectAttr);
            }
        }
        return (doc);
    }
    /**
     * Hacky routine for returning the 'default' patient id and patient id type from the
     * CCR.
     * @param ccr
     * @return
     */
    public String[] getDefaultPatientIdType(ContinuityOfCareRecordDocument ccr){
    	List<IDType> patientIds = getPatientIds(ccr);
    	String defaultPatientId[] = new String[2];
    	 String patientId = "";
         String patientIdType = "";
        // Very hacky. Sets the id to be the last non-MedCommons Patient ID available.
        // Need to have logic for what the domain should be of the id.

        for (int i=0;i<patientIds.size(); i++){
            IDType anId = patientIds.get(i);
            log.info("Patient id:" + getIdType(anId) + " value = " + getIdValue(anId));
            String idType = getIdType(anId);
            if (idType == null){
            	idType = "UNKNOWN";// Kludge
            }
            if (idType.indexOf("MedCommons") == 0){
                if (patientId.equals("")){
                    patientId = getIdValue(anId);
                    patientIdType = idType;
                }
            }
            else{
                patientId = getIdValue(anId);
                patientIdType = idType;
            }
        }
        defaultPatientId[0] = patientId;
        defaultPatientId[1] = patientIdType;
        return(defaultPatientId);
    }
    public static ContinuityOfCareRecordDocument parseAndCheckSchemaValidation(File ccrFile) throws XmlException, IOException,CCRParseException{
    	ContinuityOfCareRecordDocument aCCR;
    	StringBuffer buff = new StringBuffer();
    	// Set up the validation error listener.
		ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
		XmlOptions validationOptions = new XmlOptions();
		validationOptions.setLoadLineNumbers();
		validationOptions.setErrorListener(validationErrors);
		
		aCCR = ContinuityOfCareRecordDocument.Factory.parse(ccrFile,validationOptions);
		
		// During validation, errors are added to the ArrayList for
		// retrieval and printing by the printErrors method.
		boolean isValid = aCCR.validate(validationOptions);
		
		if (!isValid){
			 Iterator<XmlError> iter = validationErrors.iterator();
			    while (iter.hasNext())
			    {
			    	XmlError error = iter.next();
			    	buff.append("\n >> Line:");
			        buff.append(error.getLine());
			        buff.append(", Column:");
			        buff.append(error.getColumn());
			        buff.append(", ");
			        buff.append(error.getMessage());
			       
			        
			    }
			    throw new CCRParseException("CCR validation failed for " + ccrFile.getAbsolutePath() + "\n" + buff.toString());
		}
		
		return(aCCR);
    }
    public static void saveCCR(ContinuityOfCareRecordDocument document, File ccrFile) throws IOException{
        XmlOptions opts = new XmlOptions();
        opts.setSavePrettyPrint();
        opts.setSavePrettyPrintIndent(4);
        opts.setUseDefaultNamespace();
        opts.setCharacterEncoding("UTF-8");

       document.save(ccrFile, opts);
   }

}
