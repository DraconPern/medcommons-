package net.medcommons.application.dicomclient.utils;

/*
 * $Id: $
 * Created on Aug 8, 2004
 */




import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * HibernateUtil provides easy access to Hibernate facilities

 * @author ssadedin
 */
public class LocalHibernateUtil {

	/**
	 * Logger that will be used with this class
	 */
	private static Logger log = Logger.getLogger(LocalHibernateUtil.class);

	/**
	 * ThreadLocal to maintain the session for this thread
	 */
	public static final ThreadLocal session = new ThreadLocal();



	private static  SessionFactory sessionFactory;


	public static void initializeSessionFactory(File rootFile){
		try {

			if (!rootFile.exists()){
				throw new FileNotFoundException("Database root directory doesn't exist:" + rootFile.getAbsolutePath());
			}
			File f = new File(rootFile, "ddlDatabase");

			log.info("About to access local file database:" + f.getAbsolutePath());
			Configuration config = new Configuration().configure();
			//
			//config.setProperty("hibernate.connection.url", "jdbc:hsqldb:hsql://localhost/hibernate");
			config.setProperty("hibernate.connection.url", "jdbc:hsqldb:file:" + f.getAbsolutePath());
			log.info("connection info:" + config.getProperty("hibernate.connection.url"));
			// Create the SessionFactory from hibernate.cfg.xml

			sessionFactory =config.buildSessionFactory();

		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			log.error("Initial SessionFactory creation failed.", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}


	/**
	 * Returns the current hibernate session, creating one if necessary.
	 *
	 * @return
	 * @throws HibernateException
	 */
	public static Session currentSession() throws HibernateException {
		Session s = (Session) session.get();
		// Open a new Session, if this thread has none yet
		if (s == null) {
			s = getSessionFactory().openSession();
			session.set(s);
		}
		return s;
	}

	/**
	 * Closes the session used by the current thread.
	 *
	 * @throws HibernateException
	 */
	public static void closeSession() throws HibernateException {
		Session s = (Session) session.get();
		session.set(null);
		if (s != null)
			s.close();
	}


	public static SessionFactory getSessionFactory() {
		return sessionFactory;

	}
}
