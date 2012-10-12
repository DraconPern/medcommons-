package net.medcommons.application.dicomclient.transactions;

import java.util.List;

import net.medcommons.application.dicomclient.utils.LocalHibernateUtil;
import net.medcommons.application.dicomclient.utils.PixIdentifierData;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

public class CCRMatch {


    /**
     * Returns a specific CCRReference or null if no document exists.
     * @param storageId
     * @param guid
     * @return
     */
    public static CCRReference getCCRReference(String storageId, String guid){
        Session session = LocalHibernateUtil.currentSession();

        // Select CCR reference
        Criteria refQuery = session
                .createCriteria(CCRReference.class);


        refQuery.add(Expression.eq("guid", guid));
        refQuery.add(Expression.eq("storageId", storageId));

        List<CCRReference> ccrReferences = refQuery.list();
        if (ccrReferences.size() == 0){
            return(null);
        }
        else if (ccrReferences.size() == 1){
            return(ccrReferences.get(0));
        }
        else{
            throw new IllegalStateException("More than one " + ccrReferences.size() +
                    " CCRs match criteria storageId =" + storageId + "guid=" + guid);
        }
    }
    /**
     * Returns all CCRReferences for a given storageId.
     * @param storageId
     * @param guid
     * @return
     */
    public static List<CCRReference> getCCRReferences(String storageId){
        Session session = LocalHibernateUtil.currentSession();

//      Select CCR reference
        Criteria refQuery = session
                .createCriteria(CCRReference.class);



        refQuery.add(Expression.eq("storageId", storageId));
        // Should sort by time? Priority?
        List<CCRReference> ccrReferences = refQuery.list();
        if (ccrReferences.size() == 0){
            return(null);
        }
        else {
        	return(ccrReferences);
        }
    }

    /**
     * Returns a list of CCRReference objects matching the patient name. If exactMatch is true
     * then the values are tested for equality. If exactMatch is false then a SQL 'LIKE' option
     * is performed.
     * <P>
     * Note that the database values have all been mapped to upper case before database insertion.
     * @param givenName
     * @param familyName
     * @param exactMatch
     * @return
     */
    public static List<CCRReference> getMatchingReferences(String givenName, String familyName, boolean exactMatch){
        List<CCRReference> matches = null;
        return(matches);
    }


}
