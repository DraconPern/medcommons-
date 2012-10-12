package net.medcommons.modules.utils;

public class PerformanceMeasurement {

	/**
	 * Returns a formatted string containing throughput information: elapsed time, total bytes, mb/second.
	 * @param title
	 * @param elapsedTimeMsec
	 * @param totalBytes
	 * @return
	 */
	public static String throughputString(String title, long elapsedTimeMsec, long totalBytes){
		StringBuffer buff = new StringBuffer(title);
		buff.append(": total time = ");
		buff.append(elapsedTimeMsec);
		buff.append(" msec; totalBytes = ");
		buff.append(totalBytes);
		buff.append("; KB/sec = " );
		double totalKB = ((totalBytes)/(1024.0));
		double elapsedSeconds = (elapsedTimeMsec/1000.0);
		double kbPerSecond = totalKB/elapsedSeconds;
		buff.append(kbPerSecond);
		return(buff.toString());
	}
}
