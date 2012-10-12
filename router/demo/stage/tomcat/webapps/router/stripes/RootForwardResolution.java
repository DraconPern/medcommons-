/*
 * $Id: RootForwardResolution.java 2354 2008-01-21 08:40:24Z ssadedin $
 * Created on 16/01/2008
 */
package net.medcommons.router.web.stripes;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ForwardResolution;

/**
 * Attempts to forward to a JSP that is relative to root context instead of current path 
 * 
 * @author ssadedin
 */
public class RootForwardResolution extends ForwardResolution {
    
    /**
     * Optional error code - if set to anything other than 200 will cause header to get
     */
    private int errorCode = 200;

    public RootForwardResolution(String path) {
        super(path);
    }

    /**
     * Creates a resolution that sets an error code
     * @param errorCode
     */
    public RootForwardResolution(String path, int errorCode) {
        super(path);
        this.errorCode = errorCode;
    }


    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String path = request.getRequestURL().toString();
        String context = request.getContextPath();
        String afterContext = path.substring(path.indexOf(context) + context.length()+1);
        
       // Now prepend ".." enough times to get us up to the parent
        int i=0;
        String prepend = "";
        while(i<afterContext.length()) {
           if(afterContext.charAt(i) == '/')
               prepend = "../" + prepend;
           ++i;
        }
        
        String forwardPath = this.getPath();
        if((forwardPath.charAt(0) == '/') && prepend.endsWith("/")) // void double //
            forwardPath = forwardPath.substring(1);
        
        this.setPath(prepend + forwardPath);
        
        if(errorCode != 200)
            response.setStatus(errorCode);
        
        super.execute(request, response);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

}
