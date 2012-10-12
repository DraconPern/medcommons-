/*
 * $Id: Function.java 2622 2008-05-27 05:49:47Z ssadedin $
 * Created on 22/05/2008
 */
package net.medcommons.modules.utils;

public interface Function<T,U> {
    T $(U u);
}
