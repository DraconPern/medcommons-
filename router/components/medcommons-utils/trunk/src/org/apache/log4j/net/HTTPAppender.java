/*
 * Copyright 1999-2007 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import net.medcommons.modules.utils.Str;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

/**
 * This is a RESTful HTTP appender that POSTs an atom publishing protocol (APP)
 * XML message to the URL specified in the logUrl parameter.
 * 
 * @author Adrian Blakey 
 * TODO test https
 */
public class HTTPAppender extends AppenderSkeleton {

	/**
	 * implemented to perform the logging, as per super class description
	 */
	protected void append(LoggingEvent event) {
		if (url == null)
			return;
		try {
			sendRequest(event); // Send it
		} catch (IOException ioe) {
			String errMsg = "An exception: "
					+ ioe
					+ " was thrown trying to send the resquest to the server URL: "
					+ logURL;
			LogLog.error(errMsg);
			// throw new IllegalStateException(errMsg);
		}
	}

	/**
	 * nop
	 */
	public synchronized void close() {
	}

	/**
	 * called by configurators - no layout required
	 */
	public boolean requiresLayout() {
		return false;
	}

	/**
	 * overridden as per superclass description
	 */
	public void activateOptions() {
		if (logURL == null) {
			String errMsg = "The logURL option must be set for HTTPAppender named ["
					+ name + "].";
			LogLog.error(errMsg);
			// throw new IllegalStateException(errMsg);
		} else {
			try {
				url = new URL(logURL);
			} catch (MalformedURLException e) {
				String errMsg = "The URL [" + logURL
						+ "] in the logURL option for HTTPAppender named ["
						+ name + "] is not a proper URL.";
				LogLog.error(errMsg);
				// throw new IllegalStateException(errMsg);
			}
		}
		cacheDomainname(); // cache our name
		super.activateOptions();
	}

	/**
	 * Set the logURL property
	 */
	public void setLogURL(String logURL) {
		this.logURL = logURL;
	}

	/**
	 * return logUrl
	 * @return logUrl
	 */
	public String getLogURL() {
		return logURL;
	}

	/**
	 * sends the request
	 * @param event
	 */
	private void sendRequest(LoggingEvent event) throws IOException {
		if (event == null)
			return;
		Namespace atom = Namespace.getNamespace("http://www.w3.org/2005/Atom");
		Element entry = new Element("entry", atom);
		Namespace logNs = Namespace.getNamespace("log",
				"http://log4j.apache.org/2007/Log"); // local ns
		entry.addNamespaceDeclaration(logNs);

		Element title = new Element("title", atom);
		title.setAttribute("type", "text");
		title.setText("HTTPAppender log record");
		entry.addContent(title);

		Element id = new Element("id", atom);
		id.setText("urn:uuid:" + UUID.randomUUID().toString());
		entry.addContent(id);

		Element updated = new Element("updated", atom); // current time
		updated.setText(getDateAsISO8601String(new Date().getTime()));
		entry.addContent(updated);

		Element hostName = new Element("hostName", logNs);
		hostName.setText(localHostname);
		entry.addContent(hostName);

		Element fqnOfCategoryClass = new Element("fqnOfCategoryClass", logNs);
		fqnOfCategoryClass.setText(event.fqnOfCategoryClass);
		entry.addContent(fqnOfCategoryClass);

		Element name = new Element("loggerName", logNs);
		name.setText(event.getLoggerName());
		entry.addContent(name);

		Element timeStamp = new Element("timeStamp", logNs);
		timeStamp.setText(getDateAsISO8601String(event.timeStamp));
		entry.addContent(timeStamp);

		Element msg = new Element("msg", logNs);
		msg.setText(event.getMessage() == null ? "null" : event.getMessage().toString());
		entry.addContent(msg);

		Element threadName = new Element("threadName", logNs);
		threadName.setText(event.getThreadName());
		entry.addContent(threadName);

		Element ndc = new Element("ndc", logNs);
		ndc.setText(event.getNDC());
		entry.addContent(ndc);

		Element level = new Element("level", logNs);
		level.setText(event.getLevel().toString());
		entry.addContent(level);

		String[] s = event.getThrowableStrRep();
		if (s != null) {
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < s.length; j++) {
				sb.append(s[j]).append("\n");
			}
			sb.setLength(sb.length() - 1);
			Element stackTrace = new Element("stackTrace", logNs);
			stackTrace.setText(sb.toString());
			entry.addContent(stackTrace);
		}
		LocationInfo li = event.getLocationInformation();

		Element className = new Element("className", logNs);
		className.setText(li.getClassName());
		entry.addContent(className);

		Element fileName = new Element("fileName", logNs);
		fileName.setText(li.getFileName());
		entry.addContent(fileName);

		Element lineNumber = new Element("lineNumber", logNs);
		lineNumber.setText(li.getLineNumber());
		entry.addContent(lineNumber);

		Element methodName = new Element("methodName", logNs);
		methodName.setText(li.getMethodName());
		entry.addContent(methodName);

		Document doc = new Document(entry);
		XMLOutputter serializer = new XMLOutputter();
		postItem(serializer.outputString(doc));
	}

	/**
	 * helper to format a iso8601 date string. Converts a YYYYMMDDTHH:mm:ss+HH00
	 * into YYYYMMDDTHH:mm:ss+HH:00 - note the added colon for the Timezone
	 * 
	 * @param date
	 * @return
	 */
	private String getDateAsISO8601String(long date) {
		SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
		ISO8601FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		String result = ISO8601FORMAT.format(date);
		result = result.substring(0, result.length() - 2) + ":"
				+ result.substring(result.length() - 2);
		return result;
	}

	/**
	 * POST a request to the url
	 * 
	 * @param xml
	 * @throws IOException
	 *             if an I/O exception occurs while creating/writing/ reading
	 *             the request
	 */
	public void postItem(String xml) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/atom+xml");
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(xml.getBytes());
		outputStream.close();
		int responseCode = connection.getResponseCode();
		InputStream inputStream;
		if (responseCode == HttpURLConnection.HTTP_CREATED) {
			inputStream = connection.getInputStream();
		} else {
			inputStream = connection.getErrorStream();
			LogLog.error(toString(inputStream));
		}
		if (inputStream != null) inputStream.close();
	}

	/**
	 * helper to get the network name of the machine we are running on.
	 * Returns "UNKNOWN_HOST" in the unlikely case where the host name cannot be
	 * found.
	 * 
	 * @return String
	 */
	private void cacheDomainname() {
		if (localHostname != null) return;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			localHostname = addr.getCanonicalHostName();
		} catch (UnknownHostException uhe) {
			LogLog.error("Could not determine local host name for HTTPAppender"
					+ "named [" + name + "]. ", uhe);
			localHostname = "UNKNOWN_HOST";
		}
	}

	/**
	 * helper to write the response from the server
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	private String toString(InputStream inputStream) throws IOException {
		String string;
		StringBuilder outputBuilder = new StringBuilder();
		if (inputStream != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			while (null != (string = reader.readLine())) {
				outputBuilder.append(string).append('\n');
			}
		}
		return outputBuilder.toString();
	}

	/**
	 * property the must be set in the log4j property file
	 */
	private String logURL;

	/**
	 * the http/s URL to which the log is sent
	 */
	private URL url;

	/**
	 * our hostname
	 */
	private String localHostname;
}

