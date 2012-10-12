package net.medcommons.application.dicomclient.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FormatUtils {
	public final static String TENTHS_PATTERN = "###,###,###.#";

	public static String formatNumberTenths(double number){
		NumberFormat nf = NumberFormat.getNumberInstance();

		DecimalFormat df = (DecimalFormat)nf;
		df.applyPattern(TENTHS_PATTERN);
		String output = df.format(number);
		return(output);

	}
	public static String formatKbPerSecond(long byteCount, long elapsedTime){
		double bytesPerMsec = (double)( (1.0 * byteCount)/(elapsedTime));
		double kBytesPerMsec = bytesPerMsec / 1024.0;
		double kBytesPerSecond = kBytesPerMsec * 1000.0;

		return(FormatUtils.formatNumberTenths(kBytesPerSecond));

	}
	public static String formatPercentComplete(long byteCount, long totalBytes){
		if (totalBytes <= 0) return("?");
		double percent = (byteCount * 100.0) /(totalBytes * 1.0);
		// Note that we don't calculate the size of the SOAP headers here - only the
		// file attachments. So - the number of bytes transferred is greater than
		// the totalBytes specified in the constructor by about 2K bytes.
		if (percent > 100.0) percent = 100.0;

		return(FormatUtils.formatNumberTenths(percent));
	}

	public static String formatMB(long byteCount){
		if (byteCount <= 0) return("?");
		double mbCount =  (byteCount/(1024.0 * 1024.0));
		return(FormatUtils.formatNumberTenths(mbCount));
	}

	public static String formatElapsedTime(long elapsedTime){
		String seconds = FormatUtils.formatNumberTenths(elapsedTime/1000.0);
		return(seconds + " seconds");
	}

	public static int parseIntegerValue(String value){

		int val = Integer.MIN_VALUE;
		if (value != null){
			int i = value.indexOf(".");
			if (i != -1){
				val = Integer.parseInt(value.substring(0,i));
			}
			else {

					val = Integer.parseInt(value);
			}
		}

		return(val);
	}

}
