/*
 * $Id$
 * Created on 24/08/2006
 */
package net.medcommons.router.services.ccrmerge;

import java.util.*;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
 
/**
 * The default merge algorithm.  This algorithm checks first if the node is a 
 * Text node and if so just updates the text contents from the source element.
 * 
 * If the source node is an element node the algorithm instead iterates over the
 * children of the source and either updates or adds each child in the target
 * element.
 * 
 * @author ssadedin
 */
public class AddMissingChildrenMerger implements CCRMerger {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(AddMissingChildrenMerger.class);

    @SuppressWarnings("unchecked")
    public Change merge(CCRElement from, CCRDocument toDocument, CCRElement to) throws MergeException {
        
        // If from is a simple text node, then set it's value in to and return
        List<Content> contents = from.getContent();
        boolean allText = false;
        for (Content content : contents) {
            if(content instanceof Text)
                allText = true;
            
            if(content instanceof Element) {
                allText = false;
                break;
            }
        }
        if(allText) {
            //log.debug("From Element " + from.getName() + " is a text node"); 
            if(!to.getTextTrim().equals(from.getTextTrim())) { // don't return a change if there is no change
                to.removeContent();
                if (log.isDebugEnabled()) log.debug("Updating text in " + from.getName() + " from " + to.getText() + " to " + from.getText() );
                to.setText(from.getText());
                return new XPathChange(to.getName(), ChangeOperation.UPDATE, from.getText());
            }
            else
                return null;
        }
        
        //log.debug("Merging non-text-node " + from.getName() + " with " + from.getChildren().size());
        
        List<CCRElement> children = from.getChildren();
        List<CCRElement> toChildren = to.getChildren();
        
        Change changes = mergeChildren(children, toDocument, to, toChildren);
    
        return changes;
    }
    // New mergeChildren - don't respect order (ccrelement enforces this).
    protected ChangeSet mergeChildren(List<CCRElement> fromChildren, CCRDocument toDocument, CCRElement to, List<CCRElement> toChildren) throws MergeException {
        
        // Running total of changes made
    	ChangeSet changes = null;
    	
    	// We keep track of which child elements have been merged "into" and we 
    	// enforce a rule that the same "to" child element may not be merged into 
    	// by more than one "from" child element.  This is necessary for cases
    	// where there is a list of multiple elements with the same name.
    	// Since they have the same name the from elements match against 
    	// the first element multiple times where they should actually 
    	// match up against the corresponding child in the "to" element
        Set<CCRElement> merged = new HashSet<CCRElement>();
        
        for (CCRElement fromChild : fromChildren) {
            
            
            // Search for matching node in To
            boolean found = false;
            int toSize = toChildren.size();
            CCRMerger childMerger = MergerFactory.getInstance().create(fromChild);
            
            for (int toIndex =0;toIndex < toSize; toIndex ++) {
                CCRElement toChild = toChildren.get(toIndex);
                if(merged.contains(toChild))
                    continue;
                
                if(childMerger.match(fromChild,toChild)) { // Found - merge this node
                   if (log.isDebugEnabled())
                    	log.debug("Merging node " + fromChild + "("+fromChild.hashCode()+") to node " + toChild + "(" + toChild.hashCode()  + ")");
                    Change mergeChange = childMerger.merge(fromChild, toDocument, toChild);
                    if(mergeChange != null) {
                        if(changes == null) {
                            changes = new ChangeSet(to.getName());  
                        }
                        changes.add(mergeChange);
                    }
                    merged.add(toChild);
                    found = true;
                    break;                    
                }
            }
            
            if(!found) { // not found - add it
                if(changes==null) {
                    changes = new ChangeSet(to.getName());
                }
               if (log.isDebugEnabled())
                	log.debug("Importing node " + fromChild + " to parent " + to);
                int count = to.getChildren(fromChild.getName(), to.getNamespace()).size();
                String changePath = count > 0 ? 
                                String.format("%s[%d]",fromChild.getName(),count) :  fromChild.getName();
                changes.add(new XPathChange(changePath, ChangeOperation.ADD));
                childMerger.importNode(fromChild, toDocument, to);
            }
        }
        return(changes);
    }
    /*
    protected ChangeSet mergeChildren(List<CCRElement> fromChildren, CCRDocument toDocument, CCRElement to, List<CCRElement> toChildren) throws MergeException {
    	ChangeSet changes = null;
        int toIndex = 0;
        for (CCRElement fromChild : fromChildren) {
            
            
            // Search for matching node in To
            boolean found = false;
            int toSize = toChildren.size();
            int lastMergeIndex = 0;
            CCRMerger childMerger = MergerFactory.getInstance().create(fromChild);
            while(toIndex < toSize) {
                CCRElement toChild = toChildren.get(toIndex);
                if(childMerger.match(fromChild,toChild)) { // Found - merge this node
                    //if (log.isDebugEnabled())
                    	log.info("Merging node " + fromChild + "("+fromChild.hashCode()+") to node " + toChild + "(" + toChild.hashCode()  + ")");
                    Change mergeChange = childMerger.merge(fromChild, toDocument, toChild);
                    if(mergeChange != null) {
                        if(changes == null) {
                            changes = new ChangeSet(to.getName());  
                        }
                        changes.add(mergeChange);
                    }
                    lastMergeIndex = toIndex;
                    found = true;
                    break;                    
                }
                //else
                //    log.debug("nodes with name " + fromChild.getName()  + " mismatch");
                ++toIndex;
            }
            
            if(!found) { // not found - add it
                if(changes==null) {
                    changes = new ChangeSet(to.getName());
                }
               // if (log.isDebugEnabled())
                	log.info("Importing node " + fromChild + " to parent " + to);
                int count = to.getChildren(fromChild.getName(), to.getNamespace()).size();
                String changePath = count > 0 ? 
                                String.format("%s[%d]",fromChild.getName(),count) :  fromChild.getName();
                changes.add(new XPathChange(changePath, ChangeOperation.ADD));
                childMerger.importNode(fromChild, toDocument, to);
                
                // We have to reset to the last position where there was a successful merge because 
                // there may be more nodes in the to parent.
                toIndex = lastMergeIndex;
            }
            ++toIndex;
        }
        return(changes);
    }
    */

    /**
     * The default match test checks first whether the elements under
     * consideration are data elements or structural elements within the
     * CCR.  If they are structural then they are assumed to match if their 
     * names match.   If they are data elements then they are considered
     * to match only if the CCRDataObjectID matches.
     */
    public boolean match(CCRElement from, CCRElement to) {
        // Is there a CCRDataObjectID?
        String ccrDataObjectID = from.getChildTextTrim("CCRDataObjectID");
        //log.info("match?"+ from.getName() + "==" + to.getName());
        if(!Str.blank(ccrDataObjectID)) { // it is a data element
        	if (log.isDebugEnabled())
            	log.debug("match from id = " + ccrDataObjectID + " to id = " +to.getChildTextTrim("CCRDataObjectID") );
            return Str.equals(ccrDataObjectID, to.getChildTextTrim("CCRDataObjectID"));
        }
        else { //  it is as structural element
            return from.getName().equals(to.getName());
        }
    }

    public Change importNode(CCRElement from, CCRDocument toDocument, CCRElement toParent) throws MergeException {
        toParent.addChild((CCRElement) from.clone());
        
        if (from == null){
            throw new NullPointerException("Null from document in import");
        }
        if (toDocument == null){
            throw new NullPointerException("Null toDocument in import");
            
        }
        if (toParent == null){
            throw new NullPointerException("Null toParent in import");
        }
        // Search for actor references in the elements being imported.
        CCRElement toRoot = (CCRElement) toParent.getDocument().getRootElement();
        try {
            XPathCache xpath = (XPathCache) Configuration.getBean("ccrXPathCache");

            List<CCRElement> results = (List<CCRElement>)xpath.getXPathResult(from, ".//x:Actor/x:ActorID", Collections.EMPTY_MAP, true);
            if (log.isDebugEnabled())
            	log.debug("matching .//x:Actor/x:ActorID " + results.size() + " in from " + from.getName());
            for (CCRElement actorIdElement : results) {
                String actorId = actorIdElement.getTextTrim();
                
                // Find the actor
                CCRElement fromActor =  (CCRElement) xpath.getElement( from.getDocument(), "actorFromID", Collections.singletonMap("actorId",actorId));                 
                
                CCRElement toActor =  (CCRElement) xpath.getElement( toParent.getDocument(), "actorFromID", Collections.singletonMap("actorId",actorId));                 
                
                if(fromActor != null) {
                    CCRMerger actorMerger = MergerFactory.getInstance().create(fromActor);
                    if(toActor == null) {
                    	
                        log.info("###Found missing dependent actor " + actorId + ", skipping");
                       
                       // actorMerger.importNode(fromActor, toDocument, (CCRElement)toRoot.getChild("Actors"));
                       //log.info("##Skipped.. importing actor"); // Not sure this logic is correct. Actor merged in elsewhere?
                    }
                    else {
                    	log.info("### Merge in actor to actorId " + actorId);
                    	if (actorMerger.match(fromActor, toActor))
                    		actorMerger.merge(fromActor, toDocument, toActor);
                    	else
                    		log.info("Should import " + actorId);
                    }
                }
                else {
                    log.warn("Unresolved Actor ID reference " + actorId);
                    continue;
                }
            }
        }
        catch (JDOMException e) {
            throw new MergeException("Unable to find actors for import",e);
        }
        
        return null;
    }
}
