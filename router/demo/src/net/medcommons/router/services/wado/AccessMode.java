/*
 * $Id: AccessMode.java 2869 2008-08-27 05:34:09Z ssadedin $
 * Created on 17/10/2007
 */
package net.medcommons.router.services.wado;

public enum AccessMode {
    DOCTOR,
    PATIENT,
    INCOMPLETE_VOUCHER,
    ACCOUNT_IMPORT_RESULT;
    
    
    /**
     * Returns name - only to satisfy JSP / EL
     * @return name
     */
    public String getValue() {
        return this.name();
    }
}
