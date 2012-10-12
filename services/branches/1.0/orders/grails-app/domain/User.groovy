public class User {
    static mapping = {        table 'users'        version false        id column: 'mcid',insert: false,update:false        columns {    		activeGroupAccid:'active_group_accid'	    }    }    String activeGroupAccid
}
