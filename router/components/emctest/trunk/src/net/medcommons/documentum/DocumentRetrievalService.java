package net.medcommons.documentum;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.common.DfException;
import com.emc.solution.osa.client.dao.MasterClientData;

import net.medcommons.emcbridge.DfcClient;
import net.medcommons.emcbridge.data.SeriesObject;


/**
 *
 * Need to close inputstream, then close dfclient.
 * Or -
 * @author sean
 *
 */
public class DocumentRetrievalService {

    DfcClient dfcClient = null;
    Map<String, String> reference = null;
    
  
    public void startTransaction(Map<String, String> reference)
    {
        this.reference = reference;
        try{
             dfcClient = initializeDfcClient(reference);
        }
        catch(Exception e){
            throw new RuntimeException("Exception starting transaction " + e, e);
        }
        
    }
    public void closeTransaction()
    {
        try{
             if (dfcClient != null){
                 dfcClient.endSession();
             }
             dfcClient = null;
        }
        catch(Exception e){
            throw new RuntimeException("Exception ending transaction " + e, e);
        }
        
    }
    public Map<String, SeriesObject>  retrieveMetadata() throws Exception{
        if (dfcClient == null){
            throw new NullPointerException("dfcClient transaction either has not been started or has been ended");
        }
        MasterClientData metadata = null;
        Map<String, SeriesObject> series = null;
       
        String rootDocumentId = reference.get("QueryDocumentIdentifier"); 
        metadata = dfcClient.getMetadata(rootDocumentId); 
        
        if (metadata!= null)
            series = dfcClient.getSeriesMetadata(metadata);
        
        return(series);
    }

  
   
   
    /**
     * Returns a document given the reference information and an object id (perhaps 
     * calculated, perhaps derived from the metadata).
     * @param reference
     * @param objectId
     * @return
     * @throws IOException
     */
    public InputStream retrieveDocument(String objectId) throws IOException{
        InputStream in = null;
        if (dfcClient == null){
            throw new NullPointerException("dfcClient transaction either has not been started or has been ended");
        }
        
        try{
            
            in = dfcClient.getDocumentStream(objectId);
            
        }
        catch(DfException e){
            throw new IOException("Error retrieving document " + objectId, e);
        }
        return(in);
        
    }
    
    private DfcClient initializeDfcClient(Map<String,String>reference) throws IOException, DfException{
        String docBase = reference.get("DocBase");
        String userName = reference.get("UserName");      
        String passWord = reference.get("PassWord");
        String rootDocumentId = reference.get("QueryDocumentIdentifier");
        DfcClient dfcClient  = new DfcClient(docBase, userName, passWord, rootDocumentId);
        dfcClient.startSession();
        return(dfcClient);
    }
}
