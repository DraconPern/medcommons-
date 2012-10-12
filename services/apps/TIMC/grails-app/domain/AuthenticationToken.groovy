
/**
 * An authentication token proves the identity 
 * or login credentials of a user.
 */
class AuthenticationToken {
    
    static mapping = {
            accountId column: 'at_account_id'
            secret column: 'at_secret'
            parent column: 'at_parent_at_id'
    String accountId
    String secret
    AuthenticationToken parent
}