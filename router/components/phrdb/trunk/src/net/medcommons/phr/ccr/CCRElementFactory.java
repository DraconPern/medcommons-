/*
 * $Id$
 * Created on 4/07/2006
 */
package net.medcommons.phr.ccr;

import java.util.HashMap;

import net.medcommons.phr.db.xml.XMLPHRDocument;

import org.jdom.DefaultJDOMFactory;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Customizes the classes created by JDOM so that elements that extend
 * CCRElement are created.  Some special case subclass elements are also created
 * such as actors which have special support methods.
 * 
 * @author ssadedin
 */
public class CCRElementFactory extends DefaultJDOMFactory {
    
    static HashMap<String, Class<? extends CCRElement>> classes = new HashMap<String, Class<? extends CCRElement> >();
    static {
        classes.put("Actor", CCRActorElement.class);
        classes.put("Reference", CCRReferenceElement.class);
    }
    
    public static CCRElementFactory instance = new CCRElementFactory();
    
    public CCRElementFactory() {
        super();
    }

    @Override
    public CCRElement element(String name, Namespace arg1) {
        try {
            if(classes.containsKey(name))
                return classes.get(name).getConstructor(String.class).newInstance(name);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Unable to instantiate CCRElement of type " + name,e);
        }
        return new CCRElement(name, arg1);
    }

    @Override
    public CCRElement element(String name, String arg1, String arg2) {
        
        try {
            if(classes.containsKey(name))
                return classes.get(name).getConstructor(String.class, String.class, String.class).newInstance(name,arg1,arg2);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Unable to instantiate CCRElement of type " + name,e);
        }
        
        return new CCRElement(name, arg1, arg2);
    }

    @Override
    public CCRElement element(String name, String arg1) {
        try {
            if(classes.containsKey(name))
                return classes.get(name).getConstructor(String.class, String.class).newInstance(name,arg1);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Unable to instantiate CCRElement of type " + name,e);
        }
        
        return new CCRElement(name, arg1);
    }

    @Override
    public CCRElement element(String name) {
        try {
            if(classes.containsKey(name))
                return classes.get(name).getConstructor(String.class).newInstance(name);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Unable to instantiate CCRElement of type " + name,e);
        }
        return new CCRElement(name);
    }

    @Override
    public Document document(Element arg0, DocType arg1, String arg2) {
        return new XMLPHRDocument(arg0, arg1, arg2);
    }

    @Override
    public Document document(Element arg0, DocType arg1) {
        return new XMLPHRDocument(arg0, arg1);
    }

    @Override
    public Document document(Element arg0) {
        return new XMLPHRDocument(arg0);
    }
    
    /**
     * Convenience method to create CCRElements using this factory
     */
    public static CCRElement el(String name) {
        return instance.element(name);
    }

}
