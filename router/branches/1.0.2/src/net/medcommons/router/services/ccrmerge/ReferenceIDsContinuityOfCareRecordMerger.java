package net.medcommons.router.services.ccrmerge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.medcommons.document.ccr.CCRConstants;
import net.medcommons.modules.configuration.Configuration;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.phr.ccr.CCRElementFactory;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.util.xml.CCRLoader;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;

public class ReferenceIDsContinuityOfCareRecordMerger extends
        ContinuityOfCareRecordMerger {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger
            .getLogger(ReferenceIDsContinuityOfCareRecordMerger.class);

    private static XPathCache xpath = null;

    public ReferenceIDsContinuityOfCareRecordMerger() {
        if (xpath == null)
            xpath = (XPathCache) Configuration.getBean("ccrXPathCache");
    }

    public Change merge(CCRElement from, CCRDocument toDocument, CCRElement to)
            throws MergeException {
        log.info("Merging in ReferenceIDsContinuityOfCareRecordMerger");
        // log.info("From CCR:" + from.toXml());
        // log.info("To CCR:" + to.toXml());
        // Iterate over each body section
        ChangeSet result = new ChangeSet(to.getName());

        patchLinkToMedcommonsActor(from, toDocument, to);
        CCRElement body = from.getChild("Body");
        CCRElement purpose = from.getChild("Purpose");
        CCRElement patient = from.getChild("Patient");
        CCRElement actors = from.getChild("Actors");
        CCRElement comments = from.getChild("Comments");
        CCRElement signatures = from.getChild("Signatures");

        CCRElement currentBody = (CCRElement) to.getChild("Body");
        CCRElement currentPurpose = (CCRElement) to.getChild("Purpose");
        CCRElement currentPatient = (CCRElement) to.getChild("Patient");
        CCRElement currentActors = (CCRElement) to.getChild("Actors");
        CCRElement currentComments = (CCRElement) to.getChild("Comments");
        CCRElement currentSignatures = (CCRElement) to.getChild("Signatures");

        CCRElement toBody = (CCRElement) body.clone();
        CCRElement toPatient = (CCRElement) patient.clone();
        CCRElement toActors = (CCRElement) actors.clone();
        CCRElement toComments = null;
        CCRElement toSignatures = null;
        CCRElement toPurpose = null;
        if (comments != null) {
            toComments = (CCRElement) comments.clone();
        }
        if (signatures != null) {
            toSignatures = (CCRElement) signatures.clone();
        }
        if (purpose != null) {
            toPurpose = (CCRElement) purpose.clone();
        }
        // to.removeChild(toBody);
        // log.info("toPatient = " + toPatient.toXml());
        // log.info("fromPatient=" + patient.toXml());
        if (body == null)
            throw new MergeException("Null body in from CCR");

        // Merge the body
        try {
            String mcid = toDocument.getPatientMedCommonsId();
            toDocument.getRoot().removeChild(currentBody); // Just throwing it
            // away.
            toDocument.getRoot().removeChild(currentPatient);
            toDocument.getRoot().removeChild(currentActors);
            if (currentComments != null)
                toDocument.getRoot().removeChild(currentComments);
            if (currentSignatures != null)
                toDocument.getRoot().removeChild(currentSignatures);
            if (purpose != null)
                toDocument.getRoot().removeChild(currentPurpose);
            toDocument.getRoot().addChild(toBody);
            toDocument.getRoot().addChild(toPatient);
            toDocument.getRoot().addChild(toActors);
            if (toComments != null)
                toDocument.getRoot().addChild(toComments);
            if (toSignatures != null)
                toDocument.getRoot().addChild(toSignatures);
            if (toPurpose != null)
                toDocument.getRoot().addChild(toPurpose);
            toDocument.addPatientId(mcid,
                    CCRConstants.MEDCOMMONS_ACCOUNT_ID_TYPE);

            // Get the MedCommons Actor

            toDocument.syncFromJDom();

          
            log.debug(toDocument.getXml());
        } catch (PHRException e) {
            throw new MergeException("Failed to merge", e);
        }

        // ChangeSet actorChanges = mergeActors(from, toDocument, to);
        // TODO: extract actor ids for reference
        // pull out actors that don't match in incoming CCR

        try {
            ReferenceIDsContinuityOfCareRecordMerger
                    .removeDanglingActors(toDocument);

        } catch (Exception e) {
            log.error("Error removing dangling actors", e);
        }
        /*
         * ChangeSet actorChanges = mergeActors(from, toDocument, to);
         * result.add(actorChanges);
         */

        return result;
    }

    public static void removeDanglingActors(CCRDocument document)
            throws PHRException, JDOMException {
        CCRElement root = document.getRoot();

        CCRElement actors = root.getChild("Actors");
        List<CCRElement> actorList = (List<CCRElement>) actors.getChildren();
        List<CCRElement> removeActorList = new ArrayList<CCRElement>();

        // List<CCRElement> actors =
        // (List<CCRElement>)xpath.getXPathResult(root, ".//x:Actor/x:ActorID",
        // Collections.EMPTY_MAP, true);
        for (CCRElement actor : actorList) {
            String actorObjectId = actor.getChildText("ActorObjectID");
            // log.info("ActorObjectID: " + actorObjectId);
            String actorQuery = "//*[x:ActorID='" + actorObjectId + "']";
            // log.info("actor query: " + actorQuery);
            List<CCRElement> actorRefs = (List<CCRElement>) xpath
                    .getXPathResult(root, actorQuery, Collections.EMPTY_MAP,
                            true);
            if ((actorRefs == null) || (actorRefs.size() == 0)) {

                removeActorList.add(actor);

            }

        }
        for (CCRElement actor : removeActorList) {
            String actorObjectId = actor.getChildText("ActorObjectID");
            actors.removeChild(actor);
            log.debug("Removed actor with no references " + actorObjectId);

        }
    }

    /**
     * Patches the link from <Reference>s to a MedCommons Actor.
     * 
     * If there are no references in the to document - simply return.
     * 
     * TODO: May need to merge <Commment>s as well.
     * 
     * @param from
     * @param toDocument
     * @param to
     * @return
     * @throws MergeException
     */
    protected void patchLinkToMedcommonsActor(CCRElement from,
            CCRDocument toDocument, CCRElement to) throws MergeException {

        String medcommonsActorId = null;
        HashMap<String, String> changedActors = new HashMap<String, String>();
        try {
            Namespace ns = toDocument.getRootNamespace();
            CCRElement toActors = toDocument.getRoot().getChild("Actors");
            CCRElement fromActors = from.getChild("Actors");
            // log.info("toActors: " + toActors.toXml());
            // log.info("fromActors" + fromActors.toXml());
            CCRElement toReferences = toDocument.getRoot().getChild(
                    "References");
            if (toReferences == null)
                return;
            else {
                List<CCRElement> referenceList = (List<CCRElement>) toReferences
                        .getChildren();
                Iterator<CCRElement> references = referenceList.iterator();
                log.info("==There are " + referenceList.size() + " References in the document being merged into");
                int refCount = 0;
                while (references.hasNext()) {
                    refCount++;
                    CCRElement reference = references.next();
                    String referenceObjectId = reference
                            .getChildText("ReferenceObjectID");
                    CCRElement source = reference.getChild("Source");
                    if (source != null) {
                        String actorId = source.getChildText("ActorID");
                        log.info("Reference [" + refCount + "] "
                                + referenceObjectId + " has source actor "
                                + actorId);
                        if (actorId != null) {
                            String changedActorID = changedActors.get(actorId);
                            if (changedActorID != null) {
                                source.getChild("ActorID").setText(
                                        changedActorID);
                                log.info("Setting actorID " + actorId
                                        + " to already detected match "
                                        + changedActorID);

                            } else {
                                log.info("actorid " + actorId
                                        + " does not match previous matches");

                                CCRElement fromActor = (CCRElement) xpath
                                        .getElement(from, "actorFromID",
                                                Collections.singletonMap(
                                                        "actorId", actorId));
                                CCRElement toActor = (CCRElement) xpath
                                        .getElement(to, "actorFromID",
                                                Collections.singletonMap(
                                                        "actorId", actorId));
                                // log.info("From actor:" + fromActor);
                                // log.info("To actor " + toActor);
                                if ((fromActor == null) && (toActor == null)) {
				    // Remove reference to missing actorid.
				    source.removeChild("ActorID");
				    source.removeChild("ActorRole");
				    CCRElement actorRole = CCRElementFactory.instance.element("ActorRole",ns);
				    actorRole.addChild(CCRElementFactory.instance.element("Text", ns));
				    actorRole.getChild("Text", ns).setText("Unknown");
				    source.addChild(actorRole);

                                    log.info("Updated reference "
                                            + referenceObjectId
                                            + " from source actor id "
                                            + actorId
                                            + " to ActorRole 'Unknown' "
					     );

                                } else if (fromActor == null) {
                                    // fromActor is null 100% of the time if the
                                    // ActorIDs have
                                    // been changed.
                                    // /MergerFactory.getInstance().create(fromActors).importNode(body,
                                    // toDocument, to);
                                    CCRMerger actorMerger = MergerFactory
                                            .getInstance().create(toActor);
                                    List<CCRElement> actorList = fromActors
                                            .getChildren();
                                    Iterator<CCRElement> actors = actorList
                                            .iterator();
                                    boolean matched = false;
                                    while (actors.hasNext() & (!matched)) {
                                        CCRElement candidate = actors.next();
                                        matched = actorMerger.match(candidate,
                                                toActor);
                                        if (matched) {
                                            // The actor exists; update
                                            // Reference to new value.
                                            String matchingActorID = candidate
                                                    .getChildText("ActorObjectID");
                                            source.getChild("ActorID").setText(
                                                    matchingActorID);

                                            log.info("Updated reference "
                                                    + referenceObjectId
                                                    + " from source actor id "
                                                    + actorId + " to ActorID "
                                                    + matchingActorID);
                                            changedActors.put(actorId,
                                                    matchingActorID);

                                        }

                                    }
                                    if (!matched) {
                                        // Actor doesn't exist in incoming; add
                                        // actor from
                                        // toDocument
                                        fromActors
                                                .addChild((CCRElement) toActor
                                                        .clone());
                                        log
                                                .info("Reference"
                                                        + referenceObjectId
                                                        + "  actor "
                                                        + actorId
                                                        + " does not exist in From document; "
                                                        + " adding actor in from to Document");
                                    }
                                }
                            }

                        }
                    }

                }
            }

        } catch (JDOMException e) {
            throw new MergeException("Unable to merge actors", e);
        } catch (PHRException e) {
            throw new MergeException("Unable to merge actors", e);
        }

        return;
    }

    protected CCRElement createDummyActor(CCRDocument toDocument)
            throws PHRException {

        try {
            // Allocate an actor ID
            String actorId = toDocument.generateObjectID();
            CCRElement root = (CCRElement) toDocument.getRoot();
            Document actorDoc = CCRLoader
                    .loadTemplate(CCRDocument.TEMPLATE_PATH
                            + "/actorTemplate.xml");
            CCRElement actor = (CCRElement) actorDoc.getRootElement();
            Namespace namespace = root.getNamespace();
            actor.setNamespace(namespace);
            actorDoc.removeContent(actor);

            CCRElement actors = toDocument.getRoot().getChild("Actors");


            actor.getChild("ActorObjectID", namespace).setText(actorId);
            actor.removeChild("EMail", namespace);
            actor.getChild("Source", namespace).getChild("Actor", namespace)
                    .getChild("ActorID", namespace).setText(actorId);
            CCRElement infoSystem = actor.getChild("InformationSystem");
            if (infoSystem != null) {
                CCRElement infoSystemName = infoSystem.getChild("Name");
                if (infoSystemName != null) {
                    infoSystemName.setText("Unknown");
                }
            }
            log.info("newly created actor= " + actor.toXml());
            actors.addContent(actor);
            return actor;
        } catch (JDOMException e) {
            throw new PHRException("Unable to create new actor", e);
        } catch (IOException e) {
            throw new PHRException("Unable to create new actor", e);
        }
    }

}
