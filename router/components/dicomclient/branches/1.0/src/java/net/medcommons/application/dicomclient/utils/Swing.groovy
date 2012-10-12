package net.medcommons.application.dicomclient.utils

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

class Swing {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(Swing.class);
    
    public static void later(Closure c) {
        SwingUtilities.invokeLater { 
            try {
                c()
            }
            catch(Throwable t) {
                log.error("Swing operation failed", t)   
            }
        }
    }

}
