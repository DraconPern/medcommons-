package net.medcommons.router.services.wado.stripes;

import java.util.Date;

import net.medcommons.modules.crypto.SHA1;

public class DDLResult {
    
    public DDLResult(String command, String data) {
        super();
        this.command = command;
        this.sha1 = SHA1.sha1(data);
        this.data = data;
    }

    public String command;
    
    public String sha1;
    
    public String data;
    
    public Date timestamp = new Date();
}
