package net.medcommons.application.dicomclient.http.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.medcommons.application.dicomclient.Configurations;
import net.medcommons.application.dicomclient.ContextManager;
import net.medcommons.application.dicomclient.dicom.CstoreScu;
import net.medcommons.application.dicomclient.http.utils.ResponseWrapper;
import net.medcommons.application.dicomclient.transactions.TransactionUtils;
import net.medcommons.application.dicomclient.utils.DB;
import net.medcommons.application.dicomclient.utils.DicomOutputTransaction;
import net.medcommons.application.dicomclient.utils.StatusDisplayManager;
import net.medcommons.modules.utils.FileUtils;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.ajax.JavaScriptResolution;

import org.apache.log4j.Logger;
/**
 * Uploads a specified DICOM directory to the DDL via CSTORE.
 * http://localhost:16092/localDDL/UploadDICOMDirectory.action?directory=/Volumes/Macintosh%20HD/Downloads/COMUNIX
 * http://localhost:16092/localDDL/UploadDICOMDirectory.action?directory=/Users/mesozoic/demodata3/tempRat
 * @author mesozoic
 *
 */
public class UploadDICOMDirectoryActionBean extends DDLActionBean{
	 private static Logger log = Logger.getLogger(UploadDICOMDirectoryActionBean.class
             .getName());

	@DefaultHandler
	public Resolution uploadDirectory(){
		Enumeration<String> paramNames = getContext().getRequest().getParameterNames();
		ContextManager cm = ContextManager.get();
		Configurations configs = cm.getConfigurations();
		StatusDisplayManager sdm = StatusDisplayManager
				.get();

		String directoryName = null;
		File directory = null;
		while (paramNames.hasMoreElements()) {
			try {
				String name = paramNames.nextElement();
				String values[] = getContext().getRequest().getParameterValues(
						name);
				if (name.equalsIgnoreCase("directory")){
					directoryName=values[0];

				}
			}
			finally{

			}
		}

		ResponseWrapper wrapper = new ResponseWrapper();
		if (directoryName == null){
			wrapper.setStatus(ResponseWrapper.Status.ERROR);
			wrapper.setMessage("No directory was specified");
			wrapper.setContents("");
		}
		else{
			directory = new File(directoryName);
			if (!directory.exists()){
				wrapper.setStatus(ResponseWrapper.Status.ERROR);
				wrapper.setMessage("Directory or file does not exist:" + directory.getAbsolutePath());
				wrapper.setContents("");
				log.error(wrapper.getMessage());
			}
			else{
				DicomOutputTransaction dicomOutputTransaction = new DicomOutputTransaction();
				dicomOutputTransaction.setStudyDescription("Loading from file " + directory.getAbsolutePath());

				CstoreScu scu = new CstoreScu();
				try{
					scu.setDicomOutputTransaction(dicomOutputTransaction);

					List<File> dicomFiles = new ArrayList<File>();
					dicomFiles = FileUtils.getFilesInDirectory(directory, ".dcm");

		            scu.setRemoteHost("127.0.0.1");
		            scu.setRemotePort(configs.getDicomLocalPort());
		            scu.setCalledAET(configs.getDicomLocalAeTitle());
		            scu.setCalling(configs.getDicomLocalAeTitle());
		            for (int i = 0; i < dicomFiles.size(); i++) {
		                scu.addFile(dicomFiles.get(i));
		            }
		            scu.configureTransferCapability();
		            try {
		                scu.open();
		            } catch (Exception e) {
		                log.error("ERROR: Failed to establish association:"
		                        + e);
		                StatusDisplayManager.get().setErrorMessage(
		                        "DICOM Communication Error - failed to establish DICOM association:",
		                        e.getLocalizedMessage()
		                                + "\nFiles not sent to local DDL target\n"
		                                );

		            }



		            dicomOutputTransaction = scu.send();
		            String message = "Imported " + dicomFiles.size() + " files from directory " + directory.getAbsolutePath();
		            wrapper.setMessage(message);
		            wrapper.setStatus(ResponseWrapper.Status.OK);
		            wrapper.setContents(message);
		            log.info(message);


				}
				catch(IOException e){
					log.error(e.getLocalizedMessage(), e);
					wrapper.setStatus(ResponseWrapper.Status.ERROR);
					wrapper.setMessage(e.getLocalizedMessage());
					wrapper.setContents("");
				}
				finally{
					DB.get().delete(dicomOutputTransaction);
				}

			}
		}


		return(new JavaScriptResolution(wrapper));

	}
}
