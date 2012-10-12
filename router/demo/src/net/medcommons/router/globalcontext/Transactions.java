/*
 * $Id: Transactions.java 3020 2008-10-28 10:50:12Z ssadedin $
 * Created on 28/10/2008
 */
package net.medcommons.router.globalcontext;

import java.util.Hashtable;
import java.util.List;

/**
 * This class serves no purpose other than to be visible across
 * all webapp contexts in the router so that the different
 * webapps can talk to each other.
 * 
 * @author ssadedin
 */
public class Transactions {
    
    /**
     * Map of source guid to list of response guids for CXP transactions
     */
    public static Hashtable<String, List<String> > transactions = new Hashtable<String, List<String>>();

}
