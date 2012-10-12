package net.medcommons.router.services.ccrmerge;

import static net.medcommons.modules.utils.Str.blank;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import net.medcommons.modules.utils.Str;
import net.medcommons.modules.xml.MedCommonsConstants;
import net.medcommons.phr.ccr.CCRActorElement;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;

/**
 * TODO: Match on actors where actorids aren't the same but the names and actor type match.
 * @author mesozoic
 *
 */
public class ActorMerger extends AddMissingChildrenMerger{
	/**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ActorMerger.class);
	
	public boolean match(CCRElement from, CCRElement toActor) {
	    
	    CCRActorElement to = (CCRActorElement)toActor;
	
		boolean matches = false;
		try{
		String fromActorId = getActorId(from);
		String toActorId = getActorId(to);
		if (log.isDebugEnabled())
				log.debug("ActorMerger: matching from " + getActorId(from) + " to " + getActorId(to));
		if ((fromActorId == null) || (toActorId == null)){
			return false;
		}
		//else if (fromActorId.equals(toActorId)){
		else{
			// Possibly the same.
			String parentElementName = from.getParentElement().getName();
			
			//if (log.isInfoEnabled())log.info("ActorMerger: ParentName " + parentElementName);
			if ("Actors".equals(parentElementName)){
				
				String fromActorFlavor = getActorFlavor(from);
				String toActorFlavor   = getActorFlavor(to);
				if(log.isDebugEnabled())
					log.debug("ActorMerger: Use Actors logic " + fromActorFlavor + " , " + toActorFlavor + " toActorId" + toActorId );
				
				if ((fromActorFlavor != null) && (toActorFlavor!= null)){
				
					if (fromActorFlavor.equals(toActorFlavor)){
					    String fromExternalId = null;
                        String toExternalId = null;
					    
						if (log.isDebugEnabled()){
							log.debug("merging actor flavors match :" +fromActorFlavor + ", " + toActorFlavor);
						}
						String fromName = null;
						String toName = null;
						if ("Organization".equals(fromActorFlavor)){
							fromName = getOrganizationName(from);
							toName = getOrganizationName(to);
						}
						else if ("Person".equals(fromActorFlavor)){
							fromName = getPersonName(from);
							toName = getPersonName(to);
							fromExternalId = getExternalID(from, CCRElement.MEDCOMMONS_ACCOUNT_ID_TYPE);
							toExternalId = getExternalID(to, CCRElement.MEDCOMMONS_ACCOUNT_ID_TYPE);
							//log.info("external ids " + fromExternalId + ", " + toExternalId);
							
						}
						else if ("InformationSystem".equals(fromActorFlavor)){
							fromName = getInformationSystemName(from);
							toName = getInformationSystemName(to);
							// Need to test for version
						}
						
						if ((fromExternalId != null) && (toExternalId != null)){
						    if (fromExternalId.equals(toExternalId)) {
						        if((fromName == null) || (toName == null)) {
						            // If the MedCommons ID matches - then missing patient names are OK for 
						            // a match.
						            matches = true;
						        }
						        
						        // Ok to merge to a blank name
						        if(blank(to.getGivenName()) && blank(to.getFamilyName()))  
						            matches = true;
						        
						        if((fromName != null) && (fromName.equals(toName))){
						            matches = true;
						        }
						        
						    }
						}
						else if ((fromName != null) && (toName != null)){
							if (fromName.equals(toName)){
								if (log.isDebugEnabled())log.debug("Actor merge  matches = true for " + toActorId);
								matches = true;
							}
							else {
								if (log.isDebugEnabled()) log.debug("Actor does not match for name from " + fromName + ", to " + toName);
							}
								
						}
						// Assume if no name - that it can'be be a match.
						// This is pretty simple. What's needed here is to look 
						// at the contents of the object more deeply.
						
						else if ((fromName == null)|| (toName == null)){
							log.debug("No match - not enough data");;
							matches = false;
						}
						
						else{
							if (log.isDebugEnabled()) log.debug("Actor does not match for name from " + fromName + ", to " + toName);
						}
						
					}
					else{
						if (log.isDebugEnabled()) log.debug("Actor flavors don't match:" + fromActorFlavor + " , to " + toActorFlavor);
					}
					
				}
				else{
					if (log.isDebugEnabled()) log.debug("Uknown flavors "  + fromActorFlavor + " , to " + toActorFlavor);
				}
				
			}
			if ("Source".equals(parentElementName)){
			    if (fromActorId.equals(toActorId))
			        matches = true;
			    else 
			        matches = false;
			}
			if ("Location".equals(parentElementName)){
                if (fromActorId.equals(toActorId))
                    matches = true;
                else 
                    matches = false;
            }
			
		}
		}
		catch(IOException e){
			log.error("Error testing for match ", e);
			matches = false;
		}
		return(matches);

    
    	
    }
	
	/**
	 * An <Actor> can exist in <Actors> or in <Source>. We test for both cases 
	 * because in context it's never ambiguous.
	 * [Not entirely true. Merging two sources with identical ActorObjectIDs
	 * is different than merging two sources with ActorIDs in the current design.
	 * Actors/Actor/ActorObjectIDs can easily have collisions with the same ID
	 * values for separate Actors - but in a Source usually the same ID means the 
	 * same actor.]
	 * @param actorElement
	 * @return
	 */
	private String getActorId(CCRElement actorElement){
    	CCRElement actorObject = actorElement.getChild("ActorObjectID");
    	if (actorObject == null)
    		actorObject = actorElement.getChild("ActorID");
    	
    	if (actorObject != null){
    		return(actorObject.getTextNormalize());
    	}
    	else
    		return null;
    	
    }
	
	/**
	 * If there is a MedCommons ID (or some other ID in IDs)
	 * then return the string value.
	 * 
	 * @param actorElement
	 * @param idName
	 * @return
	 */
	private String getExternalID(CCRElement actorElement, String idName){
	    String ID = null;
	    List<CCRElement> children = actorElement.getChildren("IDs");
	    for (int i=0;i<children.size();i++){
	        
	        CCRElement idType = children.get(i).getChild("Type");
	        if (idType != null){
	            CCRElement typeElement = idType.getChild("Text");
	            if (typeElement != null){
	                String typeText = typeElement.getTextNormalize();
	                if (idName.equals(typeText)){
    	                CCRElement IDElement = children.get(i).getChild("ID");
    	                if (IDElement != null){
    	                    ID = IDElement.getTextNormalize();
    	                    break;
    	                }
	                }
	                
	            }
	        }
	    }
        return(ID);
        
    }
	
	private String getActorFlavor(CCRElement actorElement){
		String flavor = null;
		CCRElement actorFlavor = actorElement.getChild("Person");
		if (actorFlavor != null)
			flavor = actorFlavor.getName();
		else{
			actorFlavor = actorElement.getChild("InformationSystem");
			if (actorFlavor != null){
				flavor = actorFlavor.getName();
			}
			else{
				actorFlavor = actorElement.getChild("Organization");
				if (actorFlavor != null){
					flavor = actorFlavor.getName();
				}
			}
		}
		return(flavor);
	}
    private String getInformationSystemName(CCRElement actorElement){
    	CCRElement actorType = actorElement.getChild("InformationSystem");
    	CCRElement name = actorType.getChild("Name");
    	return(name.getTextNormalize());
    }
    private String getOrganizationName(CCRElement actorElement){
    	CCRElement actorType = actorElement.getChild("Organization");
    	CCRElement name = actorType.getChild("Name");
    	return(name.getTextNormalize());
    }
    /**
     * Returns everything in the Person's Name element. Useful for 
     * testing equality if the test for actor equality is only the name.
     * This *is* a bad assumption.
     * @param actorElement
     * @return
     * @throws IOException
     */
    private String getPersonName(CCRElement actorElement) throws IOException{
    	String name = null;
    	CCRElement actorType = actorElement.getChild("Person");
    	CCRElement nameElement = actorType.getChild("Name");
    	if (nameElement != null){
    		CCRElement currentNameElement = nameElement.getChild("CurrentName");
    		if (currentNameElement != null)
    			name = Str.toString(currentNameElement);  		
    	}
    	return(name);
    }
    /**
     * Merge contents of element 'from' into element 'to'.
     * 
     * @param from - element to merge 'from'
     * @param toDocument TODO
     * @param to - element to merge into
     * @return
     * @throws MergeException
     */
    public Change merge(CCRElement from, CCRDocument toDocument, CCRElement to) throws MergeException{
    	if (log.isDebugEnabled()){
    		log.debug("ActorMerger: merging from " + from.toXMLString() + " to " + to.toXMLString());
    	}
    	
    	return(super.merge(from, toDocument, to));
    			
    }
    
    /**
     * Import contents of from into toParent as a child node
     * 
     * @param from
     * @param toDocument TODO
     * @param to
     * @throws MergeException
     */
    public Change importNode(CCRElement from, CCRDocument toDocument, CCRElement toParent) throws MergeException{
    	if (log.isDebugEnabled())
    		log.debug("ActorMerger: Import from " + getActorId(from) + " to " + toParent.getName());
    	/*
    	if (
    			("Actors".equals(toParent.getName())) &&
    			("AA0001".equals(getActorId(from)))
    					){
    		throw new RuntimeException("Where is this from");
    	}
    	*/
    	return(super.importNode(from, toDocument, toParent));
    }
    

}
