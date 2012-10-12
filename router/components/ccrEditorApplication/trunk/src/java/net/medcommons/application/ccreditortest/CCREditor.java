package net.medcommons.application.ccreditortest;

import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.medcommons.application.ccreditortest.DeleteSession;
import net.medcommons.modules.crypto.Base64Coder;

import org.apache.xmlbeans.XmlOptions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.sun.xml.ws.util.Base64Util;

import astmOrgCCR.ActorReferenceType;
import astmOrgCCR.ActorType;
import astmOrgCCR.CodedDescriptionType;
import astmOrgCCR.ContinuityOfCareRecordDocument;
import astmOrgCCR.PurposeType;
import astmOrgCCR.SourceType;
import astmOrgCCR.StructuredProductType;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.References;
import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.Body.Medications;
import astmOrgCCR.StructuredProductType.Product;

/**
 * Trivial application which is launched via JavaWebStart.
 * 
 * This should/will grow to be a more elaborate test program. Currently it 
 * downloads the specified CCR, changes the purpose field, adds some medications, 
 * then PUTs it back to the server.
 * 
 * @author mesozoic
 *
 */
public class CCREditor {

	private static Document parseCCRReference(File f) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
		builder.setFeature(
				  "http://apache.org/xml/features/validation/schema", true);
		Document doc = builder.build(f);
		return (doc);

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
	 
	 public void setPurpose(ContinuityOfCareRecordDocument ccr, String purposeText) {
	        ContinuityOfCareRecord ccrDoc = ccr.getContinuityOfCareRecord();
	        
	        List<PurposeType> purposeList = ccrDoc.getPurposeList();
	        
	        for (int i = 0; i < purposeList.size(); i++) {
	            PurposeType newPurpose = PurposeType.Factory.newInstance();
	            CodedDescriptionType description = newPurpose.addNewDescription();
	            description.setText(purposeText);
	            purposeList.set(i, newPurpose);
	        }
	    }
	 public void removeReferences(ContinuityOfCareRecordDocument ccr) {
	        ContinuityOfCareRecord ccrDoc = ccr.getContinuityOfCareRecord();
	        ccrDoc.setReferences(References.Factory.newInstance()) ;     
	        
	 }
	 
	 public void addMedication(ContinuityOfCareRecordDocument ccr, String name) {
	        ContinuityOfCareRecord ccrDoc = ccr.getContinuityOfCareRecord();
	     
	        Medications medications = ccrDoc.getBody().getMedications();
	        if (medications == null){
	        	medications = ccrDoc.getBody().addNewMedications();
	        }
	        StructuredProductType med = medications.addNewMedication();
	        med.setCCRDataObjectID("MED" + System.currentTimeMillis());
	        
	        CodedDescriptionType status = med.addNewStatus();
	        status.setText("Active");
	        CodedDescriptionType type = med.addNewType();
	        type.setText("Medication");
	        
	        SourceType source = med.addNewSource();
	        ActorReferenceType actor = source.addNewActor();
	        actor.setActorID("AA001");
	        CodedDescriptionType role = actor.addNewActorRole();
	        role.setText("Patient");
	        Product product = med.addNewProduct(); 
	        CodedDescriptionType brandName = product.addNewBrandName();
	        brandName.setText(name);
	        CodedDescriptionType productName = product.addNewProductName();
	        productName.setText("Generic " + name);
	        
	       
	        CodedDescriptionType description = med.addNewDescription();
	        description.setText("Placebo");
	        
	    }
	private static CCREditReference generateCCREditReference(Document doc){
		CCREditReference ccrEditRef = new CCREditReference();
		
		 Element root = doc.getRootElement();
		 Iterator iter = root.getChildren().iterator();
		 System.err.println("past root");
		 while(iter.hasNext()){
			 Element child = (Element) iter.next();
			 String name = child.getName();
			 String text = child.getText();
			 if ("SessionToken".equals(name)){
				 ccrEditRef.token = text;
			 }
			 
			 else if ("ContentType".equals(name)){
				 ccrEditRef.contentType = text;
			 }
			 else if ("SessionURI".equals(name)){
				 ccrEditRef.sessionURI = child.getTextNormalize();
			 }
			
			 else if ("AuthorizationToken".equals(name)){
				 ccrEditRef.authorizationToken = text;
			 }
			 else if ("PersonImage".equals(name)){
				 ccrEditRef.image = text;
			 }
			 else{
				System.err.println("Unknown token" + name + ", value " + text);
			 }
			 
			 System.err.println("Name is " + child.getName() + ", text is " + child.getText());
			 
			 
		 }
		 return ccrEditRef;
		 
	}

	public static void main(String[] args) {
		CCREditor ccrEditor= new CCREditor();
		String action = null;
		String launchFilename = null;
		if (args.length == 0) {
			System.err.println("No arguments - done");
			return;
		}

		for (int i = 0; i < args.length; i++) {
			System.err.println("Arg " + i + ":" + args[i]);
		}
		if (args.length == 2) {
			action = args[0];
			launchFilename = args[1];
		}
		if ("-open".equalsIgnoreCase(action)) {
			File launchFile = new File(launchFilename);
			try {
				System.err.println("About to open file "
						+ launchFile.getAbsolutePath());
				Document doc = parseCCRReference(launchFile);
				CCREditReference ref = generateCCREditReference(doc);
				GetCCR getCCR = new GetCCR(ref);
				ContinuityOfCareRecordDocument ccrDoc = getCCR.downloadCCR();
				boolean isValid = ccrEditor.validateCCR(ccrDoc);
				if (!isValid){
					throw new RuntimeException("CCR read from server is invalid");
				}
				ccrEditor.setPurpose(ccrDoc, "New purpose from " + new Date().toString());
				ccrEditor.addMedication(ccrDoc, "sugar pills");
				//ccrEditor.removeReferences(ccrDoc);
				isValid = ccrEditor.validateCCR(ccrDoc);
				if (!isValid){
					throw new RuntimeException("Invalid CCR generated");
				}
				PostCCR postCCR = new PostCCR(ref);
				postCCR.post(ccrDoc);
				
				ccrEditor.setPurpose(ccrDoc, "New purpose from " + new Date().toString());
				ccrEditor.addMedication(ccrDoc, "salt pills");
				postCCR = new PostCCR(ref);
				if (!isValid){
					throw new RuntimeException("Invalid CCR generated");
				}
				postCCR.post(ccrDoc);
				
				DeleteSession delete = new DeleteSession(ref);
				delete.delete();
				if (ref.image != null){
					char imageChars[] = ref.image.toCharArray();
					byte imageBytes[] = Base64Coder.decode(imageChars);
					File f = new File("sessionImage" + System.currentTimeMillis() + ".png");
					FileOutputStream out = new FileOutputStream(f);
					out.write(imageBytes);
					out.close();
					System.err.println("Finished writing out " + f.getAbsolutePath());
				}
				else{
					System.err.println("No images in file");
				}
				
				}

			 catch (IOException e) {
				e.printStackTrace(System.err);
			}
			 catch(JDOMException e){
				 e.printStackTrace(System.err);
			 }

		} else {
			System.err.println("Unknown action:" + action);
		}
		/**
		String launchFilename = args[0];
		
		if (!launchFile.exists()){
			throw new RuntimeException("File not found:"  + launchFile.getAbsolutePath());
		}
		 **/
		//Document xmlDoc
	}
	private boolean validateCCR(ContinuityOfCareRecordDocument ccrDoc){
		// Set up the validation error listener.
		ArrayList<XmlOptions> validationErrors = new ArrayList<XmlOptions>();
		XmlOptions validationOptions = new XmlOptions();
		validationOptions.setErrorListener(validationErrors);

		

		// Do some editing to myDoc.

		// During validation, errors are added to the ArrayList for
		// retrieval and printing by the printErrors method.
		boolean isValid = ccrDoc.validate(validationOptions);

		// Print the errors if the XML is invalid.
		if (!isValid)
		{
		    Iterator iter = validationErrors.iterator();
		    while (iter.hasNext())
		    {
		        System.err.println(">> " + iter.next() + "\n");
		    }
		}
		return(isValid);
	}
}
