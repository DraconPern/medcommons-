package net.medcommons.modules.dicom;

import java.util.Comparator;

import net.medcommons.modules.services.interfaces.DicomMetadata;

public class SopInstanceComparator implements Comparator<DicomMetadata>{
	/**
	 * Currently just checks instance numbers.
	 * In general- one might want to sort by 3D coordinates or
	 * other sequence numbers. 
	 */
	public int compare(DicomMetadata o1, DicomMetadata o2){
		
		int i1 = o1.getInstanceNumber();
		int i2 = o2.getInstanceNumber();
		if (i1 == i2)
			return(0);
		else if (i1>i2)
			return(1);
		else
			return(-1);
		
	}
	/**
	 * odd function - why necessary if it's just going to 
	 * invoke the super's equals?
	 */
	public boolean equals(Object obj){
		return(this.equals(obj));
			
	}
}
