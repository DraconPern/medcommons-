package net.medcommons.modules.utils;



/**
 * This is currently a bridge file to the net.medcommons.Version class.
 * <P>
 * Eventually the parent class may be moved to the module framework; for the moment
 * the Version file will stay where it is to avoid changing the build process.
 * @author mesozoic
 *
 */
public class Version{
	// Right now a no-op. Need to figure out what the version is a property of (the component or the application)?
}
/*
public class Version extends net.medcommons.Version{

	public static Properties generateVersionProperties() {
		Properties p = new Properties();
		p.setProperty("Version", Version.getVersionString());
		p.setProperty("Revision", Version.getRevision());
		return(p);
	}
}
*/