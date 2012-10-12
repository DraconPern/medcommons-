package net.medcommons.application.dicomclient.utils;

import java.util.HashMap;;

/**
 * A simple utility to help make db access using pBeans into 
 * a fluent style api. Eg:
 * <p>
 * <code>db.all(Foo.class, where("x", 2).and("y",3"));</code>
 * 
 * @author ssadedin
 */
public class Params extends HashMap<String, Object> {

    /**
     * Convenience method to assist with chaining
     */
    public Params and(String k, Object v) {
        put(k,v);
        return this;
    }
     
    /**
     * A very minor convenience method to prevent callers having to write 'new'
     */
    public static Params where(String k, Object v) {
        return new Params().and(k, v);
    }
}
