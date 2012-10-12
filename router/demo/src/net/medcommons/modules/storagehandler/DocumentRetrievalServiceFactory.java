package net.medcommons.modules.storagehandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.medcommons.phr.PHRException;
import net.medcommons.phr.ccr.CCRElement;
import net.medcommons.modules.services.interfaces.DocumentRetrievalService;

import org.apache.log4j.Logger;

public class DocumentRetrievalServiceFactory {
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(DocumentRetrievalServiceFactory.class);

    private static HashMap<String, Class> classMap = new HashMap<String, Class>();
    
    private static DocumentRetrievalServiceFactory factory = null;
    
    private DocumentRetrievalServiceFactory(){}
    
    public static DocumentRetrievalServiceFactory factory(){
        if (factory==null)
            factory = new DocumentRetrievalServiceFactory();
        return(factory);
    }
    
    public DocumentRetrievalService getRetrievalService(String storageHandlerName) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
        String name = storageHandlerName.toLowerCase();
        
        Class docService = classMap.get(name);
        if (docService == null){
            ClassLoader loader = this.getClass().getClassLoader();
            docService = loader.loadClass("net.medcommons." + name + ".DocumentRetrievalService");
            classMap.put(name, docService);
        }
        
        DocumentRetrievalService service = (DocumentRetrievalService) docService.newInstance();
        
        return(service);
       
    }
  
    
    
}
