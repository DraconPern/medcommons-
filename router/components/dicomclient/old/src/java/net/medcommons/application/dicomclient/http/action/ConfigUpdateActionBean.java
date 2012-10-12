package net.medcommons.application.dicomclient.http.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.utils.Commands;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

import org.apache.log4j.Logger;

/**
 * Updates a subset of the configurations on the system and saves them to a file.
 * @author mesozoic
 *
 */
public class ConfigUpdateActionBean extends DDLActionBean implements Commands {

	private static Logger log = Logger.getLogger(ConfigUpdateActionBean.class);

	public ContextManager getContextManager() {
		return (super.getContextManager());
	}

	@DefaultHandler
	public Resolution update() throws IOException {
		Enumeration<String> paramNames = getContext().getRequest().getParameterNames();
		ContextManager cm = ContextManager.getContextManager();
		Configurations configs = cm.getConfigurations();
		StatusDisplayManager sdm = StatusDisplayManager
				.getStatusDisplayManager();
		boolean restartCstoreScp = false;
		String currentDicomLocalAETitle = configs.getDicomLocalAeTitle();
		int currentDicomLocalPort = configs.getDicomLocalPort();
		int currentDicomTimeout = configs.getDicomTimeout();


		while (paramNames.hasMoreElements()) {
			try {
				String name = paramNames.nextElement();
				String values[] = getContext().getRequest().getParameterValues(
						name);
				String value = values[0]; // Just grab the first
				log.info("Parameter name = " + name + ", value = " + value);
				if ("dicomRemotePort".equalsIgnoreCase(name)) {
					int dicomRemotePort = Integer.parseInt(value.trim());
					configs.setDicomRemotePort(dicomRemotePort);
				} else if ("dicomRemoteAeTitle".equalsIgnoreCase(name)) {
					configs.setDicomRemoteAeTitle(value.trim());
				} else if ("dicomRemoteHost".equalsIgnoreCase(name)) {
					configs.setDicomRemoteHost(value.trim());
				} else if ("dicomLocalAeTitle".equalsIgnoreCase(name)) {
					configs.setDicomLocalAeTitle(value.trim());
					if (!configs.getDicomLocalAeTitle().equals(currentDicomLocalAETitle)){
						restartCstoreScp = true;
					}
				} else if ("dicomLocalPort".equalsIgnoreCase(name)) {
					int dicomLocalPort = Integer.parseInt(value.trim());
					configs.setDicomLocalPort(dicomLocalPort);
					if (configs.getDicomLocalPort() != currentDicomLocalPort ){
						restartCstoreScp = true;
					}
				} else if ("dicomTimeout".equalsIgnoreCase(name)) {
					int dicomTimeout = Integer.parseInt(value.trim());
					configs.setDicomTimeout(dicomTimeout);
					if (configs.getDicomTimeout() != currentDicomTimeout){
						restartCstoreScp = true;
					}
				}
				else if ("AutomaticUploadToVoucher".equalsIgnoreCase(name)) {
					boolean automaticUpload = Boolean.parseBoolean(value.trim());
					configs.setAutomaticUploadToVoucher(automaticUpload);
					
				}
				// AutomaticUploadToVoucher
				else if ("exportMethod".equalsIgnoreCase(name)) {
					String oldExportMethod = configs.getExportMethod();
					if ((value == null) || ("".equals(value))) {
						value = EXPORT_METHOD_CSTORE;
					}
					if (EXPORT_METHOD_CSTORE.equalsIgnoreCase(value)
							|| EXPORT_METHOD_FILE.equalsIgnoreCase(value)) {
						configs.setExportMethod(value);
						if (!value.equals(oldExportMethod)) {
							sdm.setMessage("Configuration update",
									"Export method set to:" + value);
						}
					} else {
						configs.setExportMethod(EXPORT_METHOD_CSTORE); // Default

						sdm
								.setErrorMessage(
										"Error setting export method",
										"Comand \n"
												+ value
												+ "\n is not a valid value. Must be set to either \n"
												+ EXPORT_METHOD_CSTORE + " or "
												+ EXPORT_METHOD_FILE);

					}
				} else if ("exportDirectory".equalsIgnoreCase(name)) {
					File oldExportDirectory = configs.getExportDirectory();
					if ((value == null) || ("".equals(value))) {
						if (oldExportDirectory != null){
							configs.setExportDirectory(oldExportDirectory);
							log.error("New value of Export directory is null; keeping old value :" + oldExportDirectory.getAbsolutePath());
						}
						else{
							log.error("Setting ExportDirectory to null");
							configs.setExportDirectory(null);
						}


					} else {
						File exportDirectory = new File(value);

						if ((exportDirectory.exists() && (exportDirectory
								.isDirectory()))) {
							if (oldExportDirectory != null) {
								String oldDirectory = oldExportDirectory
										.getAbsolutePath();
								String newDirectory = exportDirectory
										.getAbsolutePath();
								if (!newDirectory.equals(oldDirectory)) {
									log.info("Export directory now set to "
											+ newDirectory);
									sdm.setMessage("Configuration update",
											"Export directory now set to "
													+ newDirectory);
								}
							} else {
								String newDirectory = exportDirectory
										.getAbsolutePath();
								log.info("Export directory now set to "
										+ newDirectory);
								sdm.setMessage("Configuration update",
										"Export directory now set to "
												+ newDirectory);
							}
							configs.setExportDirectory(exportDirectory);
						} else {
							configs.setExportDirectory(null);
							String message = "File \n"
									+ exportDirectory.getAbsolutePath()
									+ "\ndoes not exist and has been ignored. Please set the export directory to an existing directory";
							log.error(message);
							sdm.setErrorMessage(
									"Error setting export directory", message);

						}
					}
				} else {
					; // Ignore
					log.info("Ignoring value " + name + ":" + value);
				}

			} catch (Exception e) {
				log.error("Error updating configuration", e);
				sdm.setErrorMessage("Error updating configuration", e
						.getLocalizedMessage());

			}
		}
		log.info("Saving configurations to file:"
				+ configs.getConfigurationFile().getAbsolutePath());
		configs.save(configs.getConfigurationFile());

		if (restartCstoreScp){
			cm.stopDcmServer();
			cm.startDcmServer();
			sdm.setMessage("Restarted DICOM CSTORE SCP server", "DICOM CSTORE SCP service restarted due to configuration changes");
		}

		return new ForwardResolution("configure.html");
	}

}
