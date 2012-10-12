package net.medcommons.router.services.wado.utils;

import static net.medcommons.modules.utils.Str.blank;

public class AccountUtil {
    
    public static boolean isRealAccountId(String accid) {
        return !blank(accid)&& !"0000000000000000".equals(accid);
    }

}
