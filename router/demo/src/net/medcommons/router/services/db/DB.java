/*
 * $Id: $
 * Created on Aug 8, 2004
 */
package net.medcommons.router.services.db;

/*
 *  Object sessionFactoryObj = iniCtx.lookup("java:/hibernate/HibernateFactory");
 * $Id: $
 * Created on Aug 8, 2004
 */




import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Access to main (mcx) database
 * 
 * @author ssadedin
 */
public class DB {

	/**
	 * Logger that will be used with this class
	 */
	private static Logger log = Logger.getLogger(DB.class);

	/**
	 * ThreadLocal to maintain the session for this thread
	 */
	public static final ThreadLocal session = new ThreadLocal();
	
	private static final SessionFactory sessionFactory;
	static {
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = new Configuration().configure("mcx-hibernate.cfg.xml")
					.buildSessionFactory();
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

