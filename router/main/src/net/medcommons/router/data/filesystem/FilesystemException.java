/*
 * $Id: FilesystemException.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.data.filesystem;

public class FilesystemException extends Exception {

	public FilesystemException() {
		super();
	}
	
	public FilesystemException(String msg) {
		super(msg);	
	}
	
	public FilesystemException(String msg, Throwable t) {
		super(msg, t);	
	}

}
