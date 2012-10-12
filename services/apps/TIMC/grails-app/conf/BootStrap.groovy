class BootStrap {

     def init = { servletContext ->
       new DicomOrder(
          callersOrderReference:'123456789',
          protocolId: 'FullBodyDiag',
          patientId:  1200,
          modality: 'CT',
          scanDateTime: new Date(),
          dueDateTime: (new Date() + 5),
          comments: 'Please expedite',
          destination: 'MedCommons',
          ddlStatus: 'DDL_ORDER_ACCEPTED').save()

     }
     def destroy = {
     }
} 
