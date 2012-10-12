
class GroupInstance {
    static mapping = {        table 'groupinstances'        version false        id column: 'groupinstanceid'
    }

    String name
	String accid
    Long grouptypeid
    String logoUrl
	String uploadNotification
}
