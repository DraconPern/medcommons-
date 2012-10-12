package net.medcommons.router.services.qa;

/**
 * Trivial class used to summarize the number of elements
 * in a CCR. This class is used by JUnit tests and summary
 * servlets used in the QA package.
 * 
 * @author sean
 *
 */
public class ElementCount {
    String name;
    int count;
    String message = null;
    
    public ElementCount (String name, int count){
        this.name = name;
        this.count = count;
    }
    
    public ElementCount(String name, int count, String message){
        this(name, count);
        this.message = message;
    }
    
    /**
     * Returns the name of the elements being counted.
     * @return
     */
    public String getName(){
        return(this.name);
    }
    
    /**
     * Returns the count for the number of elements.
     * @return
     */
    public int getCount(){
        return(this.count);
    }
    
    /**
     * Returns the message (if any) associated with these items. 
     * @return
     */
    public String getMessage(){
        return(this.message);
    }
    
    
}

