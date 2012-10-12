package net.medcommons.modules.repository.metadata;

/*
 * $Id$
 * Created on 9/13/2006
 */




import org.jdom.DefaultJDOMFactory;
import org.jdom.Element;
import org.jdom.Namespace;
/**
 * 
 * @author mesozoic
 *
 */
public class RepositoryElementFactory extends DefaultJDOMFactory {

    public RepositoryElementFactory() {
        super();
    }

    @Override
    public Element element(String arg0, Namespace arg1) {
        return new RepositoryElement(arg0, arg1);
    }

    @Override
    public Element element(String arg0, String arg1, String arg2) {
        return new RepositoryElement(arg0, arg1, arg2);
    }

    @Override
    public Element element(String arg0, String arg1) {
        return new RepositoryElement(arg0, arg1);
    }

    @Override
    public Element element(String arg0) {
        return new RepositoryElement(arg0);
    }

    
    
}