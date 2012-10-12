package net.medcommons.router.services.ccrmerge;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

public class SourceMerger extends AddMissingChildrenMerger{

	/**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(SourceMerger.class);

	@Override
	public Change merge(CCRElement from, CCRDocument toDocument, CCRElement to)
			throws MergeException {
		if (true){
			super.merge(from, toDocument, to);
		}
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
                //log.debug("Updating text " + to.getText() + " to " + from.getText());
                to.setText(from.getText());
                return new XPathChange(to.getName(), ChangeOperation.UPDATE, from.getText());
            }
            else
                return null;
        }
        
        //log.debug("Merging non-text-node " + from.getName() + " with " + from.getChildren().size());
        
        List<CCRElement> children = from.getChildren();
        List<CCRElement> toChildren = to.getChildren();
        int toIndex = 0;
        ChangeSet changes = null;
        for (CCRElement fromChild : children) {
            /*if(log.isDebugEnabled())
                log.debug("Merging from child " + fromChild.getName());
            */
            
            // Search for matching node in To
            boolean found = false;
            int toSize = toChildren.size();
            int lastMergeIndex = 0;
            CCRMerger childMerger = MergerFactory.getInstance().create(fromChild);
            while(toIndex < toSize) {
                CCRElement toChild = toChildren.get(toIndex);
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
                    lastMergeIndex = toIndex;
                    found = true;
                    break;                    
                }
                //else
                //    log.debug("nodes with name " + fromChild.getName()  + " mismatch");
                ++toIndex;
            }
            
            if(!found) { // not found - add it
                
                String marked = from.getAttributeValue("marked");
                if (marked == null){
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
	                
	                // We have to reset to the last position where there was a successful merge because 
	                // there may be more nodes in the to parent.
	                toIndex = lastMergeIndex;
                }
                // Else - skip it.
            }
            ++toIndex;
        }
        return changes;
	}
	public Change importNode(CCRElement from, CCRDocument toDocument, CCRElement toParent) throws MergeException {
		return null;
		/*
		String marked = from.getAttributeValue("marked");
		if (marked != null){
			from.removeAttribute("marked");
		
		}
		
		return(super.importNode(from, toDocument, toParent));
		*/
	}

}
