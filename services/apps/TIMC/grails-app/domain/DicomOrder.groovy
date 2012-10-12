class DicomOrder {
  
    static constraints = {
      callersOrderReference(maxSize:100, unique:true)
      groupAccountId(maxSize:16,nullable: true)
      protocolId(maxSize:30)
      patientId(maxSize:30)
      modality(maxSize:20)
      comments(nullable: true)
      destination(nullable: true)
      mcid(nullable: true, maxSize:16)
      ddlStatus(maxSize: 30)
      statusCallback(nullable: true)
      dueDateTime()
      scanDateTime()
      errorCode(nullable: true)
      notificationEmail(nullable: true)
      uploadNotificationEmail(nullable: true)
      healthurl(nullable: true)
      statusurl(nullable: true)
      custom00(nullable: true)
      custom01(nullable: true)
      custom02(nullable: true)
      custom03(nullable: true)
      custom04(nullable: true)
      custom05(nullable: true)
      custom06(nullable: true)
      custom07(nullable: true)
      custom08(nullable: true)
      custom09(nullable: true)
    }


    static mapping = {
      columns {
        statusCallback(type: 'text')
        comments(type:'text')
		custom00 column:'custom_00'
		custom01 column:'custom_01'
		custom02 column:'custom_02'
		custom03 column:'custom_03'
		custom04 column:'custom_04'
		custom05 column:'custom_05'
		custom06 column:'custom_06'
		custom07 column:'custom_07'
		custom08 column:'custom_08'
		custom09 column:'custom_09'
      }
    }

    static displayProperties = 
      ['callersOrderReference', 'patientId', 'protocolId', 'modality', 
       'scanDateTime', 'dueDateTime', 'comments',
       'destination', 'ddlStatus', 'errorCode','baseline', 
       'dateCreated', 'notificationEmail']

    // static hasMany = [ history : DicomOrderHistory ]
    
    String callersOrderReference
    String groupAccountId
    String protocolId
    String patientId
    String modality
    Date scanDateTime
    Date dueDateTime
    String comments
    String destination
    String ddlStatus = 'DDL_ORDER_NEW'
    String errorCode
    Boolean baseline
    Boolean email
    String mcid
    String statusCallback
    String notificationEmail
    String uploadNotificationEmail
    Date dateCreated
    Date lastUpdated
    String healthurl
    String statusurl
    String custom00
    String custom01
    String custom02
    String custom03
    String custom04
    String custom05
    String custom06
    String custom07
    String custom08
    String custom09
}
