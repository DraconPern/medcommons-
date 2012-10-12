package net.medcommons.application.ccreditortest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.xmlbeans.XmlException;

import astmOrgCCR.ContinuityOfCareRecordDocument;

public class GetCCR {
	private CCREditReference reference;
	private HttpClient client;

	public GetCCR(CCREditReference reference) {
		this.reference = reference;
		client = new HttpClient(new MultiThreadedHttpConnectionManager());
		client.getParams().setParameter("http.useragent","HealthFrame");
		client.getHttpConnectionManager().getParams().setConnectionTimeout(
				30000);
	}

	public ContinuityOfCareRecordDocument downloadCCR() {

		ContinuityOfCareRecordDocument parsedCCR = null;
		GetMethod get = new GetMethod(reference.sessionURI);
		get.setFollowRedirects(true);

		try {
			long startTime = System.currentTimeMillis();
			int status = client.executeMethod(get);
			
			System.err.println("http status " + status);
			String contentType = null;
			if (status < 300){
				
				
				Header headers[] = get.getResponseHeaders();
				for (int i=0;i<headers.length;i++){
					System.err.println("response header " + i + " " + headers[i].getName() + " " +  headers[i].getValue());
					if (headers[i].getName().equalsIgnoreCase("Content-Type")){
						contentType = headers[i].getValue();
					}
				}
				if (contentType.equals("application/x-ccr+xml")){
					InputStream in = get.getResponseBodyAsStream();
					parsedCCR = ContinuityOfCareRecordDocument.Factory.parse(in);
				}
				else{
					String response = get.getResponseBodyAsString();
					throw new RuntimeException("Unexpected response:\n" + response);
				}
				long endTime = System.currentTimeMillis();
				System.err.println("GET OK response in " + (endTime - startTime) +" msec ");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			get.releaseConnection();
		}
		return(parsedCCR);
	}

}
