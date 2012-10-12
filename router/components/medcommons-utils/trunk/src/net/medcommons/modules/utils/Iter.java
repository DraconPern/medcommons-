/*
 * $Id: Iter.java 2622 2008-05-27 05:49:47Z ssadedin $
 * Created on 22/05/2008
 */
package net.medcommons.modules.utils;

import java.util.Iterator;

    public class Iter<T> implements Iterable<T> {
        
        private Iterator<T> iterator;
        
        public Iter(Iterator<T> iterator) {
          this.iterator = iterator;
        }
        
        public static <T> Iter<T> iter(Iterator<T> i) {
            return new Iter<T>(i);
        }
        
        public Iterator<T> iterator() {
          return iterator;
        }
    }
