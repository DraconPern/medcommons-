package net.medcommons.application.dicomclient.transactions;

import java.util.List;
import net.sourceforge.pbeans.data.ResultsIterator
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.transactions.*;

import net.sourceforge.pbeans.Store;

public class CCRMatch {

    

    /**
     * Returns a specific CCRReference or null if no document exists.
     * @param storageId
     * @param guid
     * @return 
     */
    public static CCRRef getCCRReference(String storageId, String guid) {
        
        Store store = DB.get();

        // Select CCR reference
        List<CCRRef> CCRRefs = store.select(CCRRef.class, ["guid":guid,storageId:storageId]).all()
        
        switch(CCRRefs.size()) {
	        case 0:
	            return null;
	        case 1:
	            return CCRRefs[0];
	        default:
	            throw new IllegalStateException("More than one " + CCRRefs.size() +
                    " CCRs match criteria storageId =" + storageId + "guid=" + guid);
        }
    }
        
    /**
     * Returns all CCRRefs for a given storageId.
     * @param storageId
     * @param guid
     * @return
     */
    public static List<CCRRef> getCCRReferences(String storageId) {
         
       Store store = DB.get()
       
       println "here"
       
       def params = ['storageId':storageId];
       
       println "1"
       
       Class clazz = CCRRef.class
       
       println "2"
       
       ResultsIterator i = store.select(clazz, params );
       
       println "3"
       
       
       List<CCRRef> CCRRefs = i.all();
        
       return CCRRefs?:null
       // return null
    }
}
