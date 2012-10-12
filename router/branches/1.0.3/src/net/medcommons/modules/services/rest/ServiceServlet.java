/*
 * $Id$
 * Created on Dec 15, 2004
 */
package net.medcommons.modules.services.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.medcommons.modules.services.impl.TrackingNumber;
import net.medcommons.modules.utils.HibernateUtil;


import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author ssadedin
 */
public class ServiceServlet extends HttpServlet {

    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(ServiceServlet.class);

    /**
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig arg0) throws ServletException {
    }    
    
    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        this.doPost(arg0, arg1);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        log.info("Service handler invoked with path " + pathInfo);
        if(pathInfo.startsWith("/test")) {
            testDatabase(response);        
        }
        else 
        if (pathInfo.startsWith("/tracking")) {
            this.trackingService(request,response);
        }
        else
            throw new ServletException("Invalid service " + pathInfo + " requested");
    }

    private void trackingService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Tracking service invoked");
        
        TrackingNumber trackingNumber = new TrackingNumber();
        trackingNumber.setTrackingNumber(String.valueOf(System.currentTimeMillis()));
        trackingNumber.setRightsId(new Long(0));
        trackingNumber.setEncryptedPIN("1234");

        Session session = null;
        try {            
            session = HibernateUtil.currentSession();            
            Transaction tx = session.beginTransaction();
            session.save(trackingNumber);
            tx.commit();
            
            response.getOutputStream().println("<html><body><p>Saved a new TrackingNumber " + trackingNumber.getTrackingNumber() + "</p></body></html>");
        }
        catch (HibernateException e) {
            response.getOutputStream().println("<html><body><p>It's NOT working</p></body></html>");
            e.printStackTrace();
        }
        finally {
            try {
                HibernateUtil.closeSession();
            }
            catch (HibernateException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @param response
     * @throws IOException
     */
    private void testDatabase(HttpServletResponse response) throws IOException {
        Session session = null;
        try {
            session = HibernateUtil.currentSession();
            
            // Prove hibernate has a connection
            Statement stmt = session.connection().createStatement();
            stmt.execute("select * from tracking_number");
            stmt.close();
            response.getOutputStream().println("<html><body><p>It's working</p></body></html>");
        }
        catch (HibernateException e) {
            response.getOutputStream().println("<html><body><p>It's NOT working</p></body></html>");
            e.printStackTrace();
        }
        catch (SQLException e) {
            response.getOutputStream().println("<html><body><p>It's NOT working</p></body></html>");
            e.printStackTrace();
        }
        finally {
            try {
                HibernateUtil.closeSession();
            }
            catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Dummy
     */
    public ServletConfig getServletConfig() {
        return null;
    }

    /**
     * Dummy
     * 
     * @see javax.servlet.Servlet#getServletInfo()
     */
    public String getServletInfo() {
        return null;
    }

}
