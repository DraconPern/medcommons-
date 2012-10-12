/*
 * $Id$
 * Created on 13/09/2006
 */
package net.medcommons.router.services.ccr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import net.medcommons.phr.ccr.CCRElement;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

/**
 * Utility element class to make CCR Changes in XML format easily accessible
 * from JSP / as java beans
 * @author ssadedin
 */
public class CCRChangeElement extends Element {
    
    public CCRChangeElement() {
        super();
    }

    public CCRChangeElement(String arg0, Namespace arg1) {
        super(arg0, arg1);
    }

    public CCRChangeElement(String arg0, String arg1, String arg2) {
        super(arg0, arg1, arg2);
    }

    public CCRChangeElement(String arg0, String arg1) {
        super(arg0, arg1);
    }
   
    public CCRChangeElement(String arg0) {
        super(arg0);
    }
    
    public CCRChangeElement getChanges() {
        return (CCRChangeElement) this.getChild("Changes");
    }

    public String getNotificationStatus() {
        if("Change".equals(this.getName())) {
            return ((CCRChangeElement)this.getParent().getParent()).getChildTextTrim("NotificationStatus");
        }
        else
            return this.getChildTextTrim("NotificationStatus");
            
    }
    
    public String getSource() {
        return this.getChildTextTrim("Source");
    }
    
    public String getLocation() {
        return this.getChildTextTrim("Location");
    }
    
    public String getOperation() {
        return this.getChildTextTrim("Operation");
    }
    
    public Date getDateTime() throws ParseException {
        if("Change".equals(this.getName())) {
            return ((CCRChangeElement)(this.getParent().getParent())).getDateTime();
        }
        try {
            return new SimpleDateFormat(CCRElement.EXACT_DATE_TIME_ZULU_FORMAT).parse(this.getChildTextTrim("DateTime"));
        }
        catch(ParseException ex) {
            return null;
        }
    }
    
    public long getAge() throws ParseException {
        Date changeDate = this.getDateTime();
        if(changeDate == null)
            return -1; 
        
        return System.currentTimeMillis() - changeDate.getTime();    
    }
    
    public int getUpdateCount() {
        return countOperations("UPDATE");
    }

    public int getAddCount() {
        return countOperations("ADD");
    }
    /**
     * @param op
     * @return
     */
    private int countOperations(String op) {
        int count = 0;
        Iterator iter = this.getDescendants(new ElementFilter("Operation"));
        while(iter.hasNext()) {
            Element element = (Element)iter.next();
            if(op.equals(element.getText())) {
                ++count;
            }
        }
        return count;
    }
}
