package net.medcommons.modules.repository.metadata;
/*
 * $Id$
 * Created on 4/07/2006
 */




import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import net.medcommons.modules.xml.MedCommonsConstants;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

public class RepositoryElement extends Element   {
    
	
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(RepositoryElement.class);
    
    private static String NAMESPACE_URN = MedCommonsConstants.MC_REPOSITORY_NAMESPACE_URN;
    /**
     * Hashmap of rules governing CCR Element order.  
     * 
     * Keys of the hashmap are "parent" elements and the array values are an ordered list
     * of children.  Elements created with getOrCreate() are guaranteed to be created
     * consistent with order in the array associated with the key of corresponding 
     * parent element name. 
     */
    //public static HashMap<String, String[]> CCR_ELEMENT_ORDER = new HashMap<String,String[]>();

    //public static HashSet<String> CCR_DATA_OBJECTS = new HashSet<String>();
    
    //final static List<String> CCR_DATA_OBJECT_ORDER = Arrays.asList( new String[]{"CCRDataObjectID","DateTime","Type","Status","Description","Source","CommentID" });
    /*
    static {
        // All the following elements inherit the object order from CCR_DATA_OBJECT_ORDER above.
        CCR_DATA_OBJECTS.addAll( Arrays.asList(new String[] {
                        "AdvanceDirective", 
                        "EnvironmentalAgent", 
                        "Alert", 
                        "Authorization", 
                        "Episode", 
                        "Encounter",
                        "Consent",
                        "Outcome",
                        "Insurance",
                        "Intervention",
                        "OrderRxHistory",
                        "Plan",
                        "Problem",
                        "SocialHistory",
                        "StructuredProduct", 
                        "Result", 
                        "Test",
                        "AdvanceDirective",
                        "Medication", 
                        "FamilyProblemHistory"
                        }));
                             
        CCR_ELEMENT_ORDER.put("Address", new String[] { "Line1","Line2","City","State","Country","PostalCode" } );
        CCR_ELEMENT_ORDER.put("Actor", new String[] {"ActorObjectID","Person","IDs","Relation","Address","Telephone","EMail","Source" });
        CCR_ELEMENT_ORDER.put("Body", new String[] { "Insurance","Medications","Advance Directives","FunctionalStatus","Support","VitalSigns","Immunizations","Procedures","Problems","Encounters","FamilyHistory","PlanOfCare","SocialHistory","Alerts","HealthCareProviders"});
        CCR_ELEMENT_ORDER.put("FamilyProblemHistory", new String[] { "FamilyMember","Problem"});
        CCR_ELEMENT_ORDER.put("Result", new String[] { "Substance","Test"});
        CCR_ELEMENT_ORDER.put("Strength", new String[] { "Value","Units"});
        CCR_ELEMENT_ORDER.put("Test", new String[] { "Description","TestResult"});
        CCR_ELEMENT_ORDER.put("TestResult", new String[] { "Value","Units"});
        CCR_ELEMENT_ORDER.put("Procedure", new String[] { "CCRDataObjectID","DateTime","IDs","Type","Description","Status", "Source", "InternalCCRLink","ReferenceID","CommentID","Signature","Locations","Indications"});
        CCR_ELEMENT_ORDER.put("Medication", new String[] { "Product","Quantity","Directions","PatientInstructions","Refills"});
        CCR_ELEMENT_ORDER.put("Product", new String[] { "ProductName","BrandName","Strength","Form"});
        CCR_ELEMENT_ORDER.put("Comment", new String[] { "CommentObjectID","DateTime","Type","Description","Source"});
        
        
        for (String name : CCR_DATA_OBJECTS) {
            if(CCR_ELEMENT_ORDER.containsKey(name)) {
                List<String> order = new ArrayList<String>();
                order.addAll(CCR_DATA_OBJECT_ORDER);
                order.addAll(Arrays.asList(CCR_ELEMENT_ORDER.get(name)));
                CCR_ELEMENT_ORDER.put(name, order.toArray(new String[order.size()]));
            }
        }
    }
    */
    /**
     * Pattern used to parse javabean property style parameters
     */
    private static final Pattern INDEX_PATTERN = Pattern.compile(".*\\[([0-9]*)\\]$");
    
    private static final Pattern PROPERTY_PART_REGEX = Pattern.compile("\\.");
    
    private static final Pattern DATE_TIME_REGEX = Pattern.compile("datetime$", Pattern.CASE_INSENSITIVE);

    public static final String EXACT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * @param arg0
     * @param arg1
     */
    public RepositoryElement(String arg0, Namespace arg1) {
    	
        super(arg0, arg1);
       // log.info("RepositoryElement1:" + arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public RepositoryElement(String arg0, String arg1, String arg2) {
        super(arg0, arg1, arg2);
        //log.info("RepositoryElement2:" + arg0 +"," + arg1 + "," + arg2);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public RepositoryElement(String arg0, String arg1) {
        super(arg0, arg1);
        //log.info("RepositoryElement3:" + arg0 +"," + arg1);
    }
    /**
     * @param arg0
     */
    public RepositoryElement(String arg0) {
        super(arg0, Namespace.getNamespace(NAMESPACE_URN));
        //log.info("RepositoryElement4:" + arg0 );
    }

    public RepositoryElement() {
        this.setNamespace(Namespace.getNamespace(NAMESPACE_URN));
        //log.info("RepositoryElement5:");
    }
    
    public RepositoryElement getChild(String name){
        return (RepositoryElement)super.getChild(name,this.namespace);
    }
    
   
  
    
    /**
     * Return a CCR compatible format of the current system time
     * @return
     */
    public static String getCurrentTime() {        
        DateFormat df = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date(System.currentTimeMillis()));
    }
    
    /**
     * Attempts to create the given xpath value by recursively retrieving and creating
     * each segment.
     * 
     * @param path - path to be created
     * @param value - value to set (if any)
     */
    public RepositoryElement createPath(String xpath, String value) {       
    	RepositoryElement segmentElement = this;
       String [] segments = xpath.split("/");       
       for (String path: segments) {
          segmentElement = segmentElement.getOrCreate(path);
       }       
       segmentElement.setText(value);
       return segmentElement;
    }
    
    /**
     * Attempts to create the given xpath value by recursively retrieving and creating
     * each segment.
     * 
     * @param path - path to be created
     * @param value - value to set (if any)
     */
    public RepositoryElement createPath(String xpath, Content value) {       
    	RepositoryElement segmentElement = this;
       String [] segments = xpath.split("/");       
       for (String path: segments) {
          segmentElement = segmentElement.getOrCreate(path);
       }       
       segmentElement.setContent(value);
       return segmentElement;
    }

    /**
     * Attempts to retrieve a child element of the given name from the parent.
     * If the child is not found, creates it.
     * 
     * @param parent -
     *            element to retrieve/create child from/in
     * @param name -
     *            name of child element (assumed to be in CCR namespace)
     *            ###### Assumed to be in the namespace of the parent?
     * @return
     */
    public RepositoryElement getOrCreate(String name) {
    	RepositoryElement child = this.getChild(name);
        if (child == null) {
            child = new RepositoryElement(name);
            this.addChild(child);
            
        }
        return child;
    }  
    
    public RepositoryElement addChild(RepositoryElement child) {
    	super.addContent(child);
    	
    	return(child);
        // If there is a defined order for children of this parent then 
        // use that order.
       /* if(RepositoryElement.CCR_ELEMENT_ORDER.get(this.getName()) != null)
            this.addChild(child, RepositoryElement.CCR_ELEMENT_ORDER.get(this.getName()));
        else
            super.addContent(child);
        
        return child;
        */
    }

    /**
     * Attempts to create the given element consistent with the order
     * in the supplied array of element names.
     * @param name
     * @param afterName
     * @return
     */
    public RepositoryElement createChild(String name, String afterName[]) {

    	RepositoryElement child = this.getChild(name);
        if (child == null) {
            child = new RepositoryElement(name);
            log.debug("New child created for " + name);
            this.addChild(child, afterName);
        }        
        return child;
    }
    
    public RepositoryElement addChild(RepositoryElement child, String afterName[]) {
        List thisContents = this.getContent();
        Iterator iter =thisContents.iterator();
        if(log.isDebugEnabled()){
            while (iter.hasNext()){
                Object obj = iter.next();
                if (obj instanceof Element){
                    Element element = (Element) obj;
                    log.debug("Found element " + element.getName());
                }
                else
                    log.debug("Non-element value:" + obj.getClass().getCanonicalName());
            }
        }

        Element insertAfter = null;
        int index = -1;
        for (int i=0;i<afterName.length;i++){
            if(afterName[i].equals(child.getName()))
                break;
            insertAfter = this.getChild(afterName[i], this.getNamespace());
            //log.debug("Insert fter is " + insertAfter);
            if (insertAfter != null){
                int insertIndex = this.indexOf(insertAfter);
                //log.debug("found match with " + insertAfter.getName());
                if(insertIndex >= index)
                    index = insertIndex;
            }
        }

        if (index == -1)
            this.addContent(0,child);
        else
            this.addContent(index+1, child);
        return(child);
    }
    



    
    
    public String toXml() {
        try {
            StringWriter sw = new StringWriter();            
            new XMLOutputter().output(this, sw);
            return (sw.toString());
        } 
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void toOutputStream(OutputStream out) throws IOException{
    	new XMLOutputter().output(this, out);
    }
}
