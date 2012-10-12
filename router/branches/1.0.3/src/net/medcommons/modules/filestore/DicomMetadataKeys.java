/*
 * $Id: DicomMetadataKeys.java 3055 2008-11-07 13:39:07Z ssadedin $
 * Created on 07/11/2008
 */
package net.medcommons.modules.filestore;

import net.medcommons.modules.services.interfaces.DocumentDescriptor;

/**
 * Constants used as keys for meta data entries for DICOM {@link DocumentDescriptor}
 * classes.
 * 
 * @author ssadedin
 */
public interface DicomMetadataKeys {
    
    public static final String STUDY_INSTANCE_UID = "dicom.studyinstanceuid";
    
    public static final String SERIES_INSTANCE_UID = "dicom.seriesinstanceuid";

    public static final String STUDY_DESCRIPTION = "dicom.studydescription";

}
