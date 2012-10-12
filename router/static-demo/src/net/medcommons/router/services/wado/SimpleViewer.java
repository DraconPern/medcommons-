/*
 * Created on Apr 29, 2004
 *
 * Generates top level frameset for viewer.
 */
package net.medcommons.router.services.wado;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * SimpleViewer is a frameset - it is a simple container which passes the work down to it's 
 * children.
 * 
 * TODO: Add size parameter for page. If a portait aspect ratio, then the control panel is a row 
 * across the top of the page. If a landscape aspect ratio then the control panel is on the left
 * side of the frameset.
 * 
 */
public class SimpleViewer extends HttpServlet {

	final static Logger log = Logger.getLogger(SimpleViewer.class);

	public void init() {

	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		doPost(req, resp); // Need to put DoS limits.
	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		String studyInstanceUID = null;
		String stylesheet = "/SimpleViewer.xsl";
		boolean landscape = false;
		Enumeration e = req.getParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String[] value = req.getParameterValues(key);
			if (key.equals("studyUID")) {
				studyInstanceUID = value[0];
			} else if (key.equals("stylesheet")) {
				stylesheet = value[0];
			} else if (key.equals("mode")) {
				if ("landscape".equals(value[0]))
					landscape = true;
			}
		}

		StringBuffer buf = new StringBuffer();
		buf.append(" <frame src=\"/router/Part10ToXML?studyUID=");
		buf.append(studyInstanceUID);
		buf.append("&stylesheet=");
		buf.append(stylesheet);
		buf.append("&fname=DICOMDIR");
		buf.append("\" name=\"control\" scrolling=\"NO\">");
		String control = buf.toString();
		resp.setContentType("text/html");
		OutputStream outputStream = resp.getOutputStream();
		PrintStream out = new PrintStream(outputStream);
		out.println("<html>");
		out.println("<head>");
		out.println("  <title> MedCommons Simple Viewer </title>");
		out.println("<script language=\"JavaScript\">");
		out.println("var wheelCount = 0;");
		out.println("function handleMousewheel(){");
		out.println(" wheelCount++;");
		out.println("  status = wheelCount ;");
		out.println("} ");
		out.println("</script>");

		out.println("</head>");
		if (landscape) {
			out.println("<frameset cols=\"132,*,2,\">");
			out.println(
				"  <frame src=\"seriesControl.html\" name=\"seriesControl\" scrolling=\"NO\">");
			out.println(
				"  <frame src=\"blank.html\" name=\"images\" scrolling=\"NO\">");
			out.println(control);

		} else {
			out.println("<frameset rows=\"*,132,2,\">");
			out.println(
				"  <frame src=\"blank.html\" name=\"images\" scrolling=\"NO\">");
			out.println(
				"  <frame src=\"seriesControl.html\" name=\"seriesControl\" scrolling=\"NO\">");
			out.println(control);

		}

		out.println("</frameset>");
		out.println("</html>");

	}

}
