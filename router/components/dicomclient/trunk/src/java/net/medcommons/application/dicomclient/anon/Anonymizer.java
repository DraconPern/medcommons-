package net.medcommons.application.dicomclient.anon;

import java.io.File;
import java.io.IOException;

/**
 * Takes a DICOM file as input and returns a stream for reading
 * the file that replaces PHI with anonymous identifiers
 * 
 * @author ssadedin
 */
public interface Anonymizer {

    File anonymize(File obj) throws IOException;
}
