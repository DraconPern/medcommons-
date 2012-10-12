package net.medcommons.application.upload;

import net.medcommons.application.dicomclient.utils.StatusDisplayManager;


public class UploadAccountFiles {
	private UploadContext uploadContext;
	
	public UploadAccountFiles(UploadContext uploadContext){
		this.uploadContext = uploadContext;
	}
	public void uploadAccount(){
		// Read files; verify that they exist.
		
	}
	public static void main(String[] args){
		if (args.length == 0){
			StatusDisplayManager.DisplayModalError("Installation complete", "You can now proceed launch the upload utility from the MedCommons viewer", true);
		}
		if (args.length < 4){
			System.err.println("Usage + \n" +
					"java -jar medcommons-upload.jar <CXP endpoint> <Path to local files> <MedCommmons Account Number> <Authorization token> \n" +
					"Examlple:\n" +
					"  java -jar medcommons-upload.jar http://hostname/gateway/services/CXP2/ ~/myPHRfiles 1234567887654321 a42b268bccaa5339ee52ccc8284e673e9e0ee1a9 \n" +
					"\n\n Note that a MedCommons account of -1 will generate a new account if the authorization token permits this action"
					
			);
			return;
		}
		UploadContext uc = new UploadContext(args);
	
		UploadAccountFiles uploadAccountFiles = new UploadAccountFiles(uc);
		
		
	}
}
