package net.medcommons.modules.filestore;

import net.medcommons.modules.services.interfaces.DocumentDescriptor;

/**
 * A single, stand alone document that is not a member of any collection.
 * <p>
 * In general, a simple document is identified by its SHA-1 has, so {@link #guid} is
 * the same as #sha1.  This is disobeyed when this class is used in indexing:  
 * to streamline the hierarchy and make the parent child semantics obvious, #guid is 
 * set to NULL and #sha1 holds the document identity.  Therefore, after getting
 * a {@link SimpleDocumentDescriptor} back from the IndexService you might need
 * to set #guid equal to #sha1 for it to work in some cases in the repository.
 * 
 * @author ssadedin
 */
public class SimpleDocumentDescriptor extends DocumentDescriptor {

}
