/*$Id: Modality.java 60 2004-04-28 14:53:59Z sdoyle $
 * Created on Apr 21, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package net.medcommons.simulator;
import java.util.Locale;
import java.util.ResourceBundle;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import net.medcommons.router.services.dicom.util.DICOMProperties;
import net.medcommons.simulator.util.CStoreSCU;

import org.dcm4che.util.DcmURL;
/**
 * Utility application for sending DICOM studies via CSTORE; also can invoke CECHO.
 * 
 * Initial version only sends files; eventually this will 
 * <ul>
 * <li> Be used to simulate loads at difference rate for performance tuning.
 * <li> perform MWL queries and use the resulting demographics fill
 * 		in the DICOM demographic fields.
 * <li> Perform storage committment operations.
 * </ul>
 * 
 * For right now this is a command line application; it will probably be using
 * SWT for the GUI layer later.
 * 
 * @author sean
 *
 */
// TODO: Finish moving main here - have CStoreSCU be initialized by constructor.
// Move config files to this directory. 
// Make into executable jar file.
// Need to think about arguments:
// Study directory
// Target DICOM URL


public class Modality{
	
	private static CStoreSCU cstoreSCU = null;
	private static ResourceBundle messages =
		ResourceBundle.getBundle("CStoreSCU", Locale.getDefault());
	public ResourceBundle getMessages(){
		return(messages);
	}
	private static final LongOpt[] LONG_OPTS =
			new LongOpt[] {
				new LongOpt("ac-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("dimse-timeout", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("so-close-delay", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("prior-high", LongOpt.NO_ARGUMENT, null, 'P'),
				new LongOpt("prior-low", LongOpt.NO_ARGUMENT, null, 'p'),
				new LongOpt("max-pdu-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("max-op-invoked", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("pack-pdvs", LongOpt.NO_ARGUMENT, null, 'k'),
				new LongOpt("trunc-post-pixeldata", LongOpt.NO_ARGUMENT, null, 't'),
				new LongOpt("buf-len", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("set", LongOpt.REQUIRED_ARGUMENT, null, 's'),
				new LongOpt("tls-key", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("tls-key-passwd", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("tls-cacerts", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt(
					"tls-cacerts-passwd",
					LongOpt.REQUIRED_ARGUMENT,
					null,
					2),
				new LongOpt("poll-dir", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("poll-period", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("poll-retry-open", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt(
					"poll-delta-last-modified",
					LongOpt.REQUIRED_ARGUMENT,
					null,
					2),
				new LongOpt("poll-done-dir", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("repeat-dimse", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("repeat-assoc", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("uid-suffix", LongOpt.REQUIRED_ARGUMENT, null, 2),
				new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
				new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'v'),
				};
	public Modality(){
		
	}
	public static void main(String args[]) throws Exception {
			Getopt g = new Getopt("CStoreSCU", args, "", LONG_OPTS);

			DICOMProperties cfg =
				new DICOMProperties(CStoreSCU.class.getResource("/CStoreSCU.cfg"));

			int c;
			while ((c = g.getopt()) != -1) {
				switch (c) {
					case 2 :
						cfg.put(LONG_OPTS[g.getLongind()].getName(), g.getOptarg());
						break;
					case 'P' :
						cfg.put("prior", "1");
						break;
					case 'p' :
						cfg.put("prior", "2");
						break;
					case 'k' :
						cfg.put("pack-pdvs", "true");
						break;
					case 't' :
						cfg.put("trunc-post-pixeldata", "true");
						break;
//					case 's' :
//						set(cfg, g.getOptarg());
//						break;
					case 'v' :
						exit(messages.getString("version"), false);
					case 'h' :
						exit(messages.getString("usage"), false);
					case '?' :
						exit(null, true);
						break;
				}
			}
			int optind = g.getOptind();
			int argc = args.length - optind;
			if (argc == 0) {
				exit(messages.getString("missing"), true);
			}
			//      listConfig(cfg);
			try {
				cstoreSCU =
					new CStoreSCU(cfg, new DcmURL(args[optind]), argc);
				cstoreSCU.setMessages(messages);
				cstoreSCU.execute(args, optind + 1);
			} catch (IllegalArgumentException e) {
				exit(e.getMessage(), true);
			}
		}
	private static void exit(String prompt, boolean error) {
			if (prompt != null)
				System.err.println(prompt);
			if (error)
				System.err.println(messages.getString("try"));
			System.exit(1);
		}
}

