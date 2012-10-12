package net.medcommons.modules.backup;

import net.medcommons.modules.services.interfaces.BackupException;

/**
 * Indicates that an unrecoverable problem has occurred in backing
 * up a document.
 * 
 * @author ssadedin
 */
public class PermanentBackupError extends BackupException {

    public PermanentBackupError() {
    }

    public PermanentBackupError(String message) {
        super(message);
    }

    public PermanentBackupError(Throwable cause) {
        super(cause);
    }

    public PermanentBackupError(String message, Throwable cause) {
        super(message, cause);
    }

}
