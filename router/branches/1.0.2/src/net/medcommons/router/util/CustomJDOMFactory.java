/*
 * $Id$
 * Created on 13/09/2006
 */
package net.medcommons.router.util;

import net.medcommons.router.services.ccr.CCRChangeElement;

import org.jdom.Element;
import org.jdom.Namespace;

public class CustomJDOMFactory extends org.jdom.DefaultJDOMFactory {

    Class<? extends Element> clazz = null;

    public CustomJDOMFactory(Class<? extends Element> t) {
        this.clazz = t;
    }

    @Override
    public Element element(String arg0, Namespace arg1) {
        try {
            Element e = clazz.newInstance();
            e.setName(arg0);
            e.setNamespace(arg1);
            return e;
        }
        catch (InstantiationException e) {
            return super.element(arg0, arg1);
        }
        catch (IllegalAccessException e) {
            return super.element(arg0, arg1);
        }

    }

    @Override
    public Element element(String arg0, String arg1, String arg2) {
        try {
            Element e = clazz.newInstance();
            e.setName(arg0);
            e.setNamespace(Namespace.getNamespace(arg1, arg2));
            return e;
        }
        catch (InstantiationException e) {
            return super.element(arg0, arg1, arg2);
        }
        catch (IllegalAccessException e) {
            return super.element(arg0, arg1, arg2);
        }
    }

    @Override
    public Element element(String arg0, String arg1) {
        try {
            Element e = clazz.newInstance();
            e.setName(arg0);
            e.setNamespace(Namespace.getNamespace(arg1));
            return e;
        }
        catch (InstantiationException e) {
            return super.element(arg0, arg1);
        }
        catch (IllegalAccessException e) {
            return super.element(arg0, arg1);
        }
    }

    @Override
    public Element element(String arg0) {
        try {
            Element e = clazz.newInstance();
            e.setName(arg0);
            e.setNamespace(Namespace.NO_NAMESPACE);
            return e;
        }
        catch (InstantiationException e) {
            return super.element(arg0);
        }
        catch (IllegalAccessException e) {
            return super.element(arg0);
        }
    }

}
