/*
 * $Id: EventManager.java 3817 2010-08-26 05:50:39Z ssadedin $
 * Created on 02/08/2007
 */
package net.medcommons.modules.utils.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * A *very* simple publish subscribe framework for allowing loosely coupled events
 * to be implemented on arbitrary objects.
 * 
 * @author ssadedin
 */
public class EventManager<E> implements Serializable {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(EventManager.class);
    
    /**
     * Map keyed on the subject object with values being maps of event names to subscriber lists
     */
    private Map< E, HashMap<String, ArrayList<Listener<E>> > > objects = new HashMap< E, HashMap<String, ArrayList<Listener<E>>>>();
    
    public void publish(String evt, E subject) throws PublishException {
        
        try {
            // Resolve any listeners for the object
            HashMap<String, ArrayList<Listener<E> > >  events;
            synchronized (objects) {
                events = objects.get(subject);
            }
            
            if(events == null)
                return;
            
            ArrayList<Listener<E> >  listeners = null;
            synchronized (events) {
                // Get listeners for the specific event
                listeners = events.get(evt);
                if(listeners == null)
                    return; 
                
                // For each listener, signal the event
                for (Listener<E> listener : listeners) {
                    if(listener != null) {
                        listener.onEvent(subject);
                        if(listener.getClass().getAnnotation(SingleShot.class)!=null) {
                            unsubscribe(evt, subject, listener);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new PublishException("Publishing event " + evt + " on object " + subject + " failed.",e);
        }
        
    }

    /**
     * Subscribe to listen for events by name occurring on the given object
     * 
     * @param evt
     * @param subject
     * @param listener
     */
    public void subscribe(String evt, E subject, Listener<E> listener) {
        
        HashMap<String, ArrayList<Listener<E> > > events = null;
        synchronized (objects) {
            events = objects.get(subject);
            if(events == null)
                objects.put(subject, events = new HashMap<String, ArrayList<Listener<E> > >() );
            
        }
        
        ArrayList<Listener<E> > listeners = null;
        synchronized (events) {
            listeners = events.get(evt);
            if(listeners == null)
                events.put(evt, listeners = new ArrayList<Listener<E>>());
            
            listeners.add(listener);
        }
    }
    
    
    public void unsubscribe(String evt, E subject, Listener<E> listener) {
        
        HashMap<String, ArrayList<Listener<E> > > events = null;
        synchronized (objects) {
            events = objects.get(subject);
            if(events == null)
                return;
        }
        
        ArrayList<Listener<E> > listeners = null;
        synchronized (events) {
            listeners = events.get(evt);
            if(listeners == null)
                return;
            
            // If found, remove it.  Note that we just set to null
            // because actually removing it causes concurrent modification exception while looping through
            int index = listeners.indexOf(listener);
            if(index >=0)
                listeners.set(index,null);
        }        
    }

}
