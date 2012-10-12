package net.medcommons.router.services.wado.stripes;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import astmOrgCCR.ContinuityOfCareRecordDocument.ContinuityOfCareRecord.Patient;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.services.interfaces.AccountSettings;
import net.medcommons.modules.services.interfaces.ServiceException;
import net.medcommons.modules.services.interfaces.User;
import net.medcommons.router.services.db.DB;
import net.medcommons.router.web.stripes.JSONActionBean;
import net.medcommons.router.web.stripes.JSONResolution;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;

/**
 * Queries for the list of patients that are members of the 
 * user's active group's "roster".
 * 
 * @author ssadedin
 */
public class QueryRosterMembersAction extends JSONActionBean { 
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(QueryRosterMembersAction.class);

    @DefaultHandler
    public Resolution query() throws Exception {
        Session s = DB.currentSession();
        try {
            JSONArray json = queryPatients(s);
            result.put("result", json);
        }
        finally {
            DB.closeSession();
        }
        
        return new JSONResolution(result);
    }

    @SuppressWarnings("unchecked")
    private JSONArray queryPatients(Session s) throws ServiceException {
        AccountSettings settings = session.getAccountSettings();
        
        JSONArray json = new JSONArray();
        Long practiceId = settings.getPracticeId();
        if(practiceId == null) {
            log.info("Unable to query for patients because user has no active practice");
            return json;
        }
        
        log.info("Querying roster members for practice " + practiceId);
        
        List<User> patients =   
            s.createSQLQuery(
                // "select u.* from users u, practice_patient pp where pp.pp_accid = u.mcid and pp.pp_name = 'members' and pp.pp_practice_id = ?")
                "select u.* \r\n" + 
                "            from practiceccrevents e, practice_patient pp, users u\r\n" + 
                "            where pp.pp_name = 'members'\r\n" + 
                "            and pp.pp_practice_id = ?\r\n" + 
                "            and e.practiceid = pp.pp_practice_id\r\n" + 
                "            and e.PatientIdentifier = pp.pp_accid\r\n" + 
                "            and e.ViewStatus  = 'Visible'\r\n" + 
                "            and u.mcid = pp.pp_accid\r\n" + 
                "            GROUP BY e.PatientGivenName, e.PatientFamilyName, e.Guid\r\n" + 
                "            ORDER BY e.CreationDateTime DESC")
             .addEntity(User.class)
             .setLong(0, practiceId)
             .list();
        
        for(User u : patients) {
            log.info("Found roster member: " + u.getFirstName() + " " + u.getLastName());
            json.put(new JSONObject().put("name", u.getFirstName() + " " + u.getLastName())
                                     .put("accid", String.valueOf(u.getMcid())));
        }
        return json;
    }
}
