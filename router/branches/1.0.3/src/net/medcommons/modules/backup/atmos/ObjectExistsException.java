package net.medcommons.modules.backup.atmos;

import net.medcommons.modules.services.interfaces.BackupException;

public class ObjectExistsException extends BackupException {

    public ObjectExistsException() {
    }

    public ObjectExistsException(String message) {
        super(message);
    }

    public ObjectExistsException(Throwable cause) {
        super(cause);
    }

    public ObjectExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
