package net.medcommons.modules.utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Simple filter based on filenames in a directory.
 * 
 * Typical use - invoke setFilenameFilter(".properties") to 
 * get all of the properties files in a given directory.
 * @author mesozoic
 *
 */
public class FilenameFileFilter implements FilenameFilter{
	String filenameFilter = null;
	public void setFilenameFilter(String filenameFilter){
		this.filenameFilter = filenameFilter;
	}
	public boolean accept(File dir, String name) {
		boolean accept = false;
		if (name!=null){
			if (name.indexOf(filenameFilter) != -1)
				accept = true;
		}
		return accept;
	}
}