/**
 * Represents a Patient and their dicom images / series / studies / documents
 */

/* ---------------------------Patient Object------------------------------------- */

function Patient(PatientName, PatientID){
  this.PatientName =PatientName;
  this.PatientID = PatientID;
  this.studies = new Array();
}; 
  
  
/* ---------------------------Study Object--------------------------------------- */
function Study(StudyDescription, StudyInstanceUID, StudyDate, StudyTime ){
  this.StudyDescription = StudyDescription;
  this.StudyInstanceUID = StudyInstanceUID;
  this.StudyDate = StudyDate;
  this.StudyTime = StudyTime;
  this.series=new Array();
};

/* ---------------------------Series Object-------------------------------------- */
function Series(SeriesDescription, mcGUID, SeriesInstanceUID, Modality, SeriesNumber){
  this.SeriesDescription=SeriesDescription;
  this.mcGUID = mcGUID;
  this.SeriesInstanceUID=SeriesInstanceUID;
  this.Modality=Modality;
  this.SeriesNumber = parseInt(SeriesNumber);
  this.instances = [];
}

/* ---------------------------Instance Object------------------------------------ */
function Instance(SOPInstanceUID, InstanceNumber, FileReferenceID, window, level, numFrames){
  this.InstanceNumber= parseInt(InstanceNumber);
  this.SOPInstanceUID = SOPInstanceUID;
  this.FileReferenceID=FileReferenceID;
  this.numFrames = numFrames;
  // DEMO HACK only.
  // In real world if the window/level values aren't set
  // the server should make some based on 
  // image attributes.
  var windowSpecified = !((window == null) || (window == 0));
  if(!windowSpecified) {
    this.defaultWindow = 500;
  }
  else {
    this.defaultWindow = window;
  }
  
  if(!windowSpecified && ((level==null) || (level == 0))) {
      this.defaultLevel = 200;
  }
  else 
    this.defaultLevel = level;
}