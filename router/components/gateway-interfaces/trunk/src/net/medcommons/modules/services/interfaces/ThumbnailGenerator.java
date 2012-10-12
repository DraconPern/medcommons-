package net.medcommons.modules.services.interfaces;

import java.io.File;
import java.io.IOException;

/**
 * Generates thumbnails for DICOM images (and perhaps other things in the future)
 * on import.  
 * @author sdoyle
 *
 */
public interface ThumbnailGenerator {
	
	public void generateThumbnail(File inputFile, File outputFile, Object metadata) throws IOException;

}
