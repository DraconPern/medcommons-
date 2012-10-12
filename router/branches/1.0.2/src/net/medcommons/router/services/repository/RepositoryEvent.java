package net.medcommons.router.services.repository;

import java.io.InputStream;
import java.io.OutputStream;

import net.medcommons.modules.filestore.SimpleDocumentDescriptor;
import net.medcommons.modules.services.interfaces.DocumentDescriptor;

/**
 * A general class defining repository events 
 * 
 * @author ssadedin
 */
public class RepositoryEvent {
    
    public RepositoryEvent(DocumentDescriptor document) {
        this.desc = document;
    }

    public RepositoryEvent(DocumentDescriptor document, InputStream in) {
        this.desc = document;
        this.in = in;
    }

    public RepositoryEvent(DocumentDescriptor document, OutputStream out) {
        this.desc = document;
        this.out = out;
    }

    public DocumentDescriptor desc;
    
    public InputStream in;
    
    public OutputStream out;
}
