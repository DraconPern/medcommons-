/*
 * $Id: AllowUnconfigured.java 1986 2007-09-06 09:57:13Z ssadedin $
 * Created on 17/08/2007
 */
package net.medcommons.router.web.stripes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * NOT currently implemented, but experimenting with making it easy to 
 * specify that login is required to access partiular actions via annotations.
 * 
 * @author ssadedin
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowUnconfigured {

}
