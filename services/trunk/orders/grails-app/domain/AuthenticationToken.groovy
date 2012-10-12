
/**
 * An authentication token proves the identity 
 * or login credentials of a user.
 */
class AuthenticationToken {
    
    static mapping = {        table 'authentication_token'        version false        id column: 'at_id',insert: false, update:false        columns {    		token column:'at_token'
            accountId column: 'at_account_id'
            secret column: 'at_secret'
            parent column: 'at_parent_at_id'	    }    }    String token
    String accountId
    String secret
    AuthenticationToken parent
}
