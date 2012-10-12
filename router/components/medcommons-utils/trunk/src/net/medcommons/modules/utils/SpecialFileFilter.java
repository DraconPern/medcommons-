package net.medcommons.modules.utils;

import java.io.File;
import java.io.FilenameFilter;
/**
 * Filters out filenames that match patterns.
 * @author mesozoic
 *
 */
public  class SpecialFileFilter implements FilenameFilter{
	String filterType = null;
	public void setFilterType(String filterType){
		this.filterType = filterType;
	}
	public boolean accept(File dir, String name) {
		boolean accept = false;
		if (name!=null){
			if (name.indexOf(filterType) != 0)
				accept = true;
		}
		return accept;
	}
}