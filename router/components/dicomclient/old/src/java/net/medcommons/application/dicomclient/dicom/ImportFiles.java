package net.medcommons.application.dicomclient.dicom;

/**
 * Contains status information for imports from a file system.
 * @author mesozoic
 *
 */
public class ImportFiles {
	private int nFilesImported = 0;
	private int nFilesSkipped = 0;
	
	public ImportFiles(){
		super();
		
	}
	
	public void skippedFile(){
		nFilesSkipped++;
	}
	public void importedFile(){
		nFilesImported++;
	}
	public int getFilesImported(){
		return(this.nFilesImported);
	}
	public int getFilesSkipped(){
		return(this.nFilesSkipped);
	}
	
	
}
