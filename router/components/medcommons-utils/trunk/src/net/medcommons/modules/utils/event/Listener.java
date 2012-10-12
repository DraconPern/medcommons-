/*
 * $Id: Listener.java 2971 2008-10-21 06:47:21Z ssadedin $
 * Created on 02/08/2007
 */
package net.medcommons.modules.utils.event;

/**
 * Interface implemented by listeners to events on objects of type T
 * @author ssadedin
 */
public interface Listener<T> {
    
    void onEvent(T obj) throws Exception;
}
