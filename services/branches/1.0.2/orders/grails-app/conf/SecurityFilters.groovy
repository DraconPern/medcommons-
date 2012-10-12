import javax.crypto.Cipher
public class SecurityFilters {
    private static Map settings 
        RESTUtil.init(new SystemPropertyRESTConfiguration())
    
    def filters = {
        all(controller:'*', action:'*') {
            before = {
                // Is the request signed with OAuth?
                // Have we got an 'mc' cookie
                def c = request.cookies.find {it.name == 'mc'}
                
                // No cookie, not logged in!
                    redirect(url: loginUrl)
                    // return false     
                }
                // Got cookie, is it real?
                if(login?.auth) {
                    return true
                redirect(url:loginUrl)
                return false    
        }
      }
    }
        def enc = mc.split(',').find { it.startsWith('enc=') }?.substring(4)
        
        enc = enc.replaceAll('-','+')
        enc = enc.replaceAll('_','/')
        def secret = settings.SECRET
        def bytes = Base64.decodeBase64(enc.getBytes("UTF-8"))
        result.mcid = Long.parseLong(result.mcid)
        return result
    }
        def mc = "mcid=1117658438174637,from=MedCommons,fn=Demo,ln=Doctor,email=demodoctor@medcommons.net,s=2,enc=7LHdUWUYMUXl8mBS9Ouy39VBkeyENN5LtFysETQ1oSWkKLLrGJShrskT3oJcofNWWukhC6cs43yogi0oDYQSW9r8Jr8DJbwOr7Yp_t9UOEGUJ5uwfAy8You3QNeHDweOE9cfQ5I6u8faR_MqMrBSP5IfqHcYcMFf-GdT3DlZr-crqN_4pf4ageZqrsH737bvzPOW8xsTtrJ-HV04PQwcdg=="
        
        SecurityFilters sf = new SecurityFilters()
        def props = sf.checkCookie(mc)
        assert props.auth == "8093cded752195b9d54f3d970c990ac9845fbac9"
        