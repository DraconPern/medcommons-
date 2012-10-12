package net.medcommons.router.web.stripes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

/**
 * A resolution that wraps another resolution and GZIP's the content
 * of that resolution, setting appropriate headers so that the 
 * client interprets the result correctly.  If a client's accept-encoding 
 * header does not include gzip then content is returned unzipped instead.
 * 
 * @author ssadedin
 */
public class ZipResolution implements Resolution {
    
    private static Logger log = Logger.getLogger(ZipResolution.class);
    
    Resolution target = null;

    public ZipResolution(Resolution target) {
        super();
        this.target = target;
    }
    
    PrintWriter writer = null;
    
    ServletOutputStream streamWrapper = null;
    
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        try {
            String ae = req.getHeader("accept-encoding");
            boolean zipContent = ae != null && ae.indexOf("gzip") != -1;
            if(zipContent) {
                resp.addHeader("Content-Encoding","gzip");
                ServletOutputStream out = resp.getOutputStream();
                final GZIPOutputStream zipOut = new GZIPOutputStream(out);
                HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(resp) {
                    @Override
                    public ServletOutputStream getOutputStream() throws IOException {
                        
                        if(streamWrapper == null) {
                            streamWrapper = new ServletOutputStream() {
                                @Override
                                public void write(int b) throws IOException {
                                    zipOut.write(b);
                                }
                                @Override
                                public void write(byte[] b, int off, int len) throws IOException {
                                    zipOut.write(b, off, len);
                                }
                                @Override
                                public void write(byte[] b) throws IOException {
                                    zipOut.write(b);
                                }
                            };
                        }
                        return streamWrapper;
                    }

                    @Override
                    public PrintWriter getWriter() throws IOException {
                        if(writer == null)
                            writer = new PrintWriter(this.getOutputStream());
                        return writer;
                    }
                };
                
                target.execute(req, responseWrapper);
                if(writer != null)
                    writer.flush();
                
                if(streamWrapper != null)
                    streamWrapper.flush();
                
                zipOut.flush();
                zipOut.close();
                
                resp.flushBuffer(); 
            }
            else
                target.execute(req, resp);
        }
        catch(Exception e) {
            log.error("Failed to render zipped output",e);
        }
    }
}
