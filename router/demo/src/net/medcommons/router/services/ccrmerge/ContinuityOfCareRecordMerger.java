/*
 * $Id$
 * Created on 25/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.filestore.TransactionException;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.repository.RepositoryException;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Responsible for merging root level CCR documents.
 * 
 * @author ssadedin
 */
public class ContinuityOfCareRecordMerger implements CCRMerger {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ContinuityOfCareRecordMerger.class);
    
    
    private static XPathCache xpath = null;

    public ContinuityOfCareRecordMerger() {
        if(xpath == null)
           xpath =(XPathCache) Configuration.getBean("ccrXPathCache");
    }

    public Change merge(CCRElement from, CCRDocument toDocument, CCRElement to) throws MergeException {
       // Iterate over each body section 
       ChangeSet result = new ChangeSet(to.getName());
       CCRElement body = from.getChild("Body");
       CCRElement toBody = (CCRElement) to.getChild("Body");

       // no toBody?  Copy the from body
       if(body != null) {
           if(toBody == null) {
               toBody = (CCRElement) body.clone();
               MergerFactory.getInstance().create(body).importNode(body, toDocument, to);
           }
           result.add(MergerFactory.getInstance().create(body).merge(body, toDocument, toBody));
       }
     
       
       // Now merge the comment sections - SOAP
       result.add(this.combineComment(from,toDocument,"purposeText"));
       result.add(this.combineComment(from,toDocument,"objectiveText"));
       result.add(this.combineComment(from,toDocument,"assessmentText"));
       result.add(this.combineComment(from,toDocument,"planText"));
       
       ChangeSet referenceChanges = mergeReferences(from, toDocument, to);
       result.add(referenceChanges); 
       if(referenceChanges != null) {
           try {
               toDocument.parseReferences();
           }
           catch (RepositoryException e) {
               throw new MergeException("Unable to merge references",e);
           }
           catch (PHRException e) {
               throw new MergeException("Unable to merge references",e);
           }
       }
       
      CCRElement toActors = (CCRElement) to.getChild("Actors");
      CCRElement fromActors = (CCRElement) from.getChild("Actors");
      
      try{
    	  // Remove the fromPatient actor - it was merged in above.
    	  CCRElement fromPatient = (CCRElement) xpath.getElement(from,"patientActor");
    	  /**
    	  if (fromPatient != null){
    		  fromActors.removeChild(fromPatient);
    		  log.info("Removed fromPatient actor before merge ");
    	  }
    	  */
      }
      
     
      catch(JDOMException e){
    	  log.error("Error removing redundant patient actor ", e);
      }
      
      // result.add(
      // Don't record changes to Actors?
    		 //  MergerFactory.getInstance().create(fromActors).merge(fromActors, toDocument, toActors);
    //		   );
       
       ChangeSet actorChanges = mergeActors(from, toDocument, to);
     //result.add(actorChanges);
   
       
     
     
       return result;
    }

    
    /**
     * Attempts to import references from the "from" document into the "to" document
     */
    protected ChangeSet mergeReferences(CCRElement from, CCRDocument toDocument, CCRElement to) throws MergeException {
        ChangeSet result = new ChangeSet("References");
        
        try {
            // First check if one of the documents does not have any references,
            // in which case the merge is simple
            CCRElement toRefs = toDocument.getRoot().getChild("References");
            CCRElement fromRefs = from.getChild("References");
            if(fromRefs == null) {
                log.info("No References in 'from' CCR ");
                return null;
            }

            if(toRefs==null) {
                log.info("No References in target document.  Importing all from source.");
                result.add(MergerFactory.getInstance().create(fromRefs).importNode(fromRefs, toDocument, to));
                return result;
            } 

            // Both documents have references - we have to do a real merge
            List<CCRElement> refList = fromRefs.getChildren();
            for(CCRElement ref : refList) {

                String refId = ref.getChildTextTrim("ReferenceObjectID");
                log.info("Merging reference " + refId);

                // Never merge a document's change history
                if(isChangeHistoryReference(ref)) 
                    continue; 

                // Does this reference exist in the toDocument?
                CCRElement toRef = 
                    (CCRElement) xpath.getElement(toDocument.getJDOMDocument(),"referenceFromId", Collections.singletonMap("ref", refId));

                if(toRef != null) { // found same reference in toDocument
                    // HACK:  Continue
                    // However because ReferenceObjectIDs are not guaranteed unique across different
                    // CCRs this will result in references sometimes randomly being skipped
                    // TODO: Fix merging of references with same RefID
                    log.info("Merging identical reference found in both source and target merge documents " + refId);
                    result.add(MergerFactory.getInstance().create(toRef).merge(ref, toDocument, toRef));
                    continue;
                }
                else {
                    int count = toRefs.getChildren(ref.getName(), ref.getNamespace()).size();
                    String changePath = count > 0 ? 
                                    String.format("%s[%d]",ref.getName(),count) :  ref.getName();
                                    
                    result.add(new XPathChange(changePath, ChangeOperation.ADD));
                    log.info("Importing reference " + refId + " from source document");
                    MergerFactory.getInstance().create(ref).importNode(ref, toDocument, toRefs);                    
                }                
            }
        }
        catch (JDOMException e) {
            throw new MergeException("Unable to merge references", e);
        }
        catch (PHRException e) {
            throw new MergeException("Unable to merge references", e);
        }
        return result;
    }

    /**
     * Attempts to import actors from the "from" document into the "to" document
     * 
     * Strategy:
     * 1. Get the ActorObjectIDs from all actors for from/to.
     * 2. Get all 'matches' 
     * 3. Test to see if the matches in ActorObjectIds are really matches.
     * 4. If they are - merge will merge them. No
     * 5. If they aren't - do a global rename of the incoming actor with a new id, then let merge take its course.
     */
    protected ChangeSet mergeActors(CCRElement from, CCRDocument toDocument, CCRElement to) throws MergeException {
        ChangeSet result = new ChangeSet("Actors");
        
        
        try {
            // First check if one of the documents does not have any references,
            // in which case the merge is simple
            CCRElement toActors = toDocument.getRoot().getChild("Actors");
            CCRElement fromActors = from.getChild("Actors");
            
          
           
            if(fromActors == null) {
                log.info("No Actors in source document");
                return null;
            }

            if(toActors==null) {
                log.info("No Actors in target document.  Importing all from source.");
                result.add(MergerFactory.getInstance().create(fromActors).importNode(fromActors, toDocument, to));
                return result;
            } 

            List<String> fromActorIds = getActorIds(fromActors);
            List<String> toActorIds = getActorIds(toActors);
            List<String> potentialConfictIds = new ArrayList<String>();
            
            Set<String> s = new HashSet<String>(toActorIds);
            for (String a : fromActorIds)
                if (!s.add(a)){
                    potentialConfictIds.add(a);
                    log.info("Conflicting ActorObjectID:" + a);
                }

            log.info("Conflicting ActorObjectIDs:"  + potentialConfictIds.size());
            // Both documents have references - we have to do a real merge
            
            for (String actorId : potentialConfictIds){
            	// Then it's a potential match. Two cases
        		// 1. True - then they will be merged as part of the merge.
        		// 2. False - then we need to remap the ids of the incoming ('to') elements 
            	// to a new ID both here and in the entire document.
        		
            	CCRElement fromActor = (CCRElement) xpath.getElement(from,"actorFromID", Collections.singletonMap("actorId", actorId));
            	CCRElement toActor = (CCRElement) xpath.getElement(toDocument.getJDOMDocument(),"actorFromID", Collections.singletonMap("actorId", actorId));
            	boolean same = MergerFactory.getInstance().create(fromActor).match(fromActor, toActor);
            	if (!same){
            		// Have to remap ids.
            		String newID = toActor.generateObjectID();
            		toActor.getChild("ActorObjectID").setText(newID);
            		//String xpathActorIDs = "//x:Actor[x:ActorID='" + actorId + "']";
            		String xpathActorIDs = "//x:ActorID";
            		
            		List<CCRElement> results = (List<CCRElement>)xpath.getXPathResult(
            				toDocument.getJDOMDocument(), 
            				xpathActorIDs,
            				 Collections.EMPTY_MAP, true);
            		int counter = 0;
            		for (CCRElement actorRef : results){
            			if (actorRef.getText().equals(actorId)){
            				actorRef.setText(newID);
            				counter ++;
            			}
            		}
            		log.info("Reset " + counter + " actor references");
            	}
            }
            MergerFactory.getInstance().create(fromActors).merge(fromActors, toDocument, toActors);
            
            /*
            List<CCRElement> refList = fromActors.getChildren();
            for(CCRElement ref : refList) {

                String actorId = ref.getChildTextTrim("ActorObjectID");
                log.info("Merging actor " + actorId);

             

                // Does this reference exist in the toDocument?
                CCRElement toRef = 
                    (CCRElement) xpath.getElement(toDocument.getJDOMDocument(),"actorFromID", Collections.singletonMap("actorId", actorId));

                if(toRef != null) { // found same reference in toDocument
                   
                   
                    if (MergerFactory.getInstance().create(toRef).match(ref,toRef)){
                    	 log.info("Merging identical actors found in both source and target merge documents " + actorId);
                    	result.add(MergerFactory.getInstance().create(toRef).merge(ref, toDocument, toRef));
                    }
                    else{
                    	log.info("Actors have the same id " + actorId + " but do not match");
                    	continue;
                    	
                    }
                    continue;
                }
                else {
                    int count = toActors.getChildren(ref.getName(), ref.getNamespace()).size();
                    String changePath = count > 0 ? 
                                    String.format("%s[%d]",ref.getName(),count) :  ref.getName();
                                    
                    result.add(new XPathChange(changePath, ChangeOperation.ADD));
                    log.info("Importing actor " + actorId + " from source document");
                    MergerFactory.getInstance().create(ref).importNode(ref, toDocument, toActors); 
                   
                }                
            }
            */
        }
        catch (JDOMException e) {
            throw new MergeException("Unable to merge actors", e);
        }
        catch (PHRException e) {
            throw new MergeException("Unable to merge actors", e);
        }
    
        return result;
    }
    /**
     * Returns true if the given reference is a MedCommons change history document
     */
    private boolean isChangeHistoryReference(CCRElement ref) {
        return ref.getChild("Type")!= null && 
                        CCRConstants.CCR_CHANGE_HISTORY_MIME_TYPE.equals(ref.getChild("Type").getChildText("Text"));
    }
    
    /**
     * Attempts to combine the comments under the given path from both documents
     * together.  This is done by appending one to the other, separated by 
     * a line of "----" 
     *  
     * @return a Change object indicating what change, if any, was made to the toDocument
     */
    protected Change combineComment(CCRElement from, CCRDocument toDocument, String path) throws MergeException {
        Change c = null;
        ChangeOperation op =  null;
        try {
            String fromValue = xpath.getValue(from, path);
            if(Str.blank(fromValue)) {
                return null; // no change
            }
             
            Element toComment = xpath.getElement(toDocument.getRoot(), path);
            if(toComment==null) {
                toComment = toDocument.createPath(path);
                op = ChangeOperation.ADD;
            }
            
            String toValue = toComment.getTextTrim();
            if(Str.blank(toValue)) {
                toComment.setText(fromValue);
                return new CommentChange(path, ChangeOperation.ADD);
            }
            
            // neither is blank

            // Find index of the to text in the from text.  If it exists,
            // then just replace to with from.  Otherwise, add
            fromValue = Str.normalize(fromValue);
            toValue = Str.normalize(toValue);
            int toTextIndex = fromValue.indexOf(toValue);
            if(toTextIndex >= 0) { // the from comment is prepended to the to comment - just keep from
                toComment.setText(fromValue);
            }
            else { // Both have different comments:  combine them together
                toComment.setText(fromValue+"\n---\n"+toComment.getTextTrim());
            }
            
            return new CommentChange(path, ChangeOperation.UPDATE);
        }
        catch (JDOMException e) {
            throw new MergeException("Unable to merge contents of path " + path,e);
        }
        catch (IOException e) {
            throw new MergeException("Unable to merge contents of path " + path,e);
        }
        catch (PHRException e) {
            throw new MergeException("Unable to merge contents of path " + path,e);
        }
    }

    public boolean match(CCRElement from, CCRElement to) {
        return false;
    }

    public Change importNode(CCRElement from, CCRDocument toDocument, CCRElement toParent) throws MergeException {
        return null;
    }
    
    /**
     * Returns a list of the ActorObjectID in a CCR.
     * @param element
     * @return
     * @throws JDOMException
     */
    protected List<String> getActorIds(CCRElement element) throws JDOMException{
    	List<CCRElement> actorIDElements = (List<CCRElement>)xpath.getXPathResult(element, ".//x:ActorObjectID", Collections.EMPTY_MAP, true);
        List<String> actorIds = new ArrayList<String>();
        for(CCRElement actorId : actorIDElements){
        	actorIds.add(actorId.getTextNormalize());
        }
        return(actorIds);
    }

}
