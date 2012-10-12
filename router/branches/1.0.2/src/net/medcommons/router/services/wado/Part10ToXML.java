/*
 * Created on Apr 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.wado;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.medcommons.modules.configuration.Configuration;

import org.apache.log4j.Logger;
/*
 * 
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
*/

/**
 * Obsolete?
 * 2007/09/10 - SWD - Commented out.
 * This file should be removed if not used; if it is used
 * then the DICOM routines from the ddl should be factored out into
 * a component and re-used here.

 */
public class Part10ToXML extends HttpServlet {
/*
	private File imageRootDirectory = null;
	private static final DcmParserFactory pfact =
		DcmParserFactory.getInstance();
	
	
	private LinkedList xsltParams = new LinkedList();
	private boolean xsltInc = false;
	private int stopTag = Tags.PixelData;
	private TagDictionary dict =
		DictionaryFactory.getInstance().getDefaultTagDictionary();
	final static Logger log = Logger.getLogger(Part10ToXML.class);

	public void init() {
		try {

			String rootDir =
				(String) Configuration.getInstance().getConfiguredValue(
					"net.medcommons.dicom.directory");

			imageRootDirectory = new File(rootDir);
			if (!imageRootDirectory.exists())
				throw new FileNotFoundException(
					imageRootDirectory.getCanonicalPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		doPost(req, resp); // Need to put DoS limits.
	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		String filename = null;
		String studyInstanceUID = null;
		String stylesheet = null;
		File part10File = null;
		URL xslt = null;
		

		Enumeration e = req.getParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String[] value = req.getParameterValues(key);
			if (key.equals("studyUID")) {
				studyInstanceUID = value[0];
			} else if (key.equals("fname")) {
				filename = value[0];
			} else if (key.equals("stylesheet")) {
				stylesheet = value[0];
			}
			if (stylesheet == null)
				resp.setContentType("text/xml");
			else
			resp.setContentType("text/html");
			// Other parameters: lut, size.
		}
		log.info("imageRootDirectory=" + imageRootDirectory);

		File studyDirectory = new File(imageRootDirectory, studyInstanceUID);
		if (!studyDirectory.exists())
			throw new FileNotFoundException(studyDirectory.getCanonicalPath());
		part10File = new File(studyDirectory, filename);
		if (stylesheet != null)
			xslt = Part10ToXML.class.getResource(stylesheet);
		try{
			process(part10File, resp.getOutputStream(), xslt);
		}
		catch(TransformerConfigurationException ex){
			ex.printStackTrace();
			throw new ServletException(ex);
		}
		

	}

    /**
     * 
     * @uml.property name="stopTag"
     * /
    public void setStopTag(int stopTag) {
        this.stopTag = stopTag;
    }


	public void setTagDictionary(TagDictionary dict) {
		this.dict = dict;
	}

    /**
     * 
     * @uml.property name="xsltInc"
     * /
    public void setXsltInc(boolean xsltInc) {
        this.xsltInc = xsltInc;
    }

	public void addXsltParam(String expr) {
		if (expr.indexOf('=') <= 0) {
			throw new IllegalArgumentException(expr);
		}
		this.xsltParams.add(expr);
	}

	

	private TransformerHandler getTransformerHandler(OutputStream out, URL xslt)
		throws TransformerConfigurationException, IOException {
		SAXTransformerFactory tf =
			(SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler th = null;
		if (xslt != null) {
			if (xsltInc) {
				tf.setAttribute(
					"http://xml.apache.org/xalan/features/incremental",
					Boolean.TRUE);
			}
			th =
				tf.newTransformerHandler(
					new StreamSource(xslt.openStream(), xslt.toExternalForm()));
			Transformer t = th.getTransformer();
			for (Iterator it = xsltParams.iterator(); it.hasNext();) {
				String s = (String) it.next();
				int eqPos = s.indexOf('=');
				t.setParameter(s.substring(0, eqPos), s.substring(eqPos + 1));
			}
		} else {
			th = tf.newTransformerHandler();
			th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
		}
		th.setResult(new StreamResult(out));
		return th;
	}

	public void process(File file, OutputStream out, URL xslt)
		throws IOException, TransformerConfigurationException {
		DataInputStream in =
			new DataInputStream(
				new BufferedInputStream(new FileInputStream(file)));
	
			DcmParser parser = pfact.newDcmParser(in);
			parser.setSAXHandler(getTransformerHandler(out,xslt), dict);
			parser.parseDcmFile(null, stopTag);
		
	}
	*/
}


