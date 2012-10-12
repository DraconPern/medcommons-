package net.medcommons.modules.services.client.rest;

import net.medcommons.modules.services.interfaces.ServiceException;

/**
 * Indicates that a tracking number was added by someone who was not registered
 * to the email address required for accessing the content.
 * 
 * @author ssadedin
 */
public class IncorrectEmailAddressException extends ServiceException {
    
    String email;

    public IncorrectEmailAddressException() {
    }

    public IncorrectEmailAddressException(String tn, String email) {
        super("The given tracking number " + tn + " can only be accessed by a registered account with email address " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
