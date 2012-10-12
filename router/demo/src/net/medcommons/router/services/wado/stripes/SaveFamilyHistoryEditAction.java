/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.phr.ccr.CCRElementFactory.el;

import java.util.ArrayList;
import java.util.HashMap;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.router.web.stripes.CCRActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.jdom.Element;

/**
 * Saves edits of a FamilyHistory section
 * 
 * @author ssadedin
 */
public class SaveFamilyHistoryEditAction extends CCRActionBean {

    public static class FamilyMember {
        public String actorRole;
        public String actorID;
        public String getActorID() {
            return actorID;
        }
        public void setActorID(String actorID) {
            this.actorID = actorID;
        }
        public String getActorRole() {
            return actorRole;
        }
        public void setActorRole(String actorRole) {
            this.actorRole = actorRole;
        }
    }
    
    public static class Problem {
        public String type;
        public String description;
        public String dateTime;
        public String code;
        
        
        public String getDateTime() {
            return dateTime;
        }
        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }        
    }
    
    public static class FamilyHistory {
        public FamilyMember familyMember;
        public Problem problem;
        public String dateTime;
        public String comment;
        
        public FamilyMember getFamilyMember() {
            return familyMember;
        }
        public void setFamilyMember(FamilyMember familyMember) {
            this.familyMember = familyMember;
        }
        public Problem getProblem() {
            return problem;
        }
        public void setProblem(Problem problem) {
            this.problem = problem;
        }
        public String getComment() {
            return comment;
        }
        public void setComment(String comment) {
            this.comment = comment;
        }
    }
    
    private ArrayList<FamilyHistory> familyhistory = new ArrayList<FamilyHistory>();
    
    public SaveFamilyHistoryEditAction() {
        super();
    }
     
    @DefaultHandler
    public Resolution save() throws Exception {
        try {
            HashMap<String, Object> result = new HashMap<String,Object>();
            Element familyHistory = 
                ccr.getOrCreate((CCRElement)ccr.getJDOMDocument().getRootElement(),"Body").getOrCreate("FamilyHistory");
             
            familyHistory.removeContent(); 
            
            for (FamilyHistory f  : familyhistory) {
                if(f==null)
                    continue;
                CCRElement fph = el("FamilyProblemHistory");
                fph.createPath("CCRDataObjectID",ccr.generateObjectID());
                fph.createPath("Status/Text", "Active");
                if(!Str.blank(f.comment) && !"Type Here".equals(f.comment)) {
                    fph.createPath("CommentID",
                                    ccr.addComment(f.comment).getChildText("CommentObjectID",fph.getNamespace()));                        
                }
                
                CCRElement fm = el("FamilyMember");
                fm.createPath("ActorRole/Text",f.familyMember.actorRole);
                
                // todo: deal with unique actors more cleverly than always creating new ones
                if("Mother".equals(f.familyMember.actorRole) || "Father".equals(f.familyMember.actorRole)) {
                }
                
                CCRElement actor = ccr.createActor();
                actor.removeChild("EMail",actor.getNamespace());
                actor.getOrCreate("Relation").setText(f.familyMember.actorRole);
                fm.getOrCreate("ActorID").setText(actor.getChildText("ActorObjectID"));                       
                fph.addContent(fm);            
                
                CCRElement pb = fph.getOrCreate("Problem");
                pb.getOrCreate("Type").setText("Problem"); // Type of problem hard coded.  TODO: make it editable
                CCRElement desc = pb.getOrCreate("Description");
                desc.createPath("Text", f.problem.description); 
                desc.createPath("Code/Value", f.problem.code);
                desc.createPath("Code/CodingSystem", "Unknown");
                
                fph.getOrCreate("DateTime").setDate(f.dateTime);
                familyHistory.addContent(fph);
            }
            return new JavaScriptResolution("ok");            
        }
        catch(Exception e) {
            return new JavaScriptResolution(e.getMessage());            
        }
    }

    public ArrayList<FamilyHistory> getFamilyhistory() {
        return familyhistory;
    }

    public void setFamilyhistory(ArrayList<FamilyHistory> familyHistory) {
        this.familyhistory = familyHistory;
    }
    
}
