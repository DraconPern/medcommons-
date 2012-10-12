/*
 * $Id: DirectoryTreeVisitor.java 201 2004-07-14 19:25:02Z mquigley $
 */

package net.medcommons.router.data.filesystem;


public interface DirectoryTreeVisitor {

	public void visit(String path);

}
