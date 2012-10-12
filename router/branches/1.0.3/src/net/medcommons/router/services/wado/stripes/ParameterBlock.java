/*
 * $Id: ParameterBlock.java 3446 2009-08-06 08:58:10Z ssadedin $
 * Created on 20/01/2009
 */
package net.medcommons.router.services.wado.stripes;

import static net.medcommons.modules.utils.Str.blank;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Utility class used for binding DDL configuration parameters that 
 * may be passed to the {@link StartDDLAction}.
 * 
 * @author ssadedin
 */
public class ParameterBlock {
    
        String auth = null;
        String guid = null;
        String storageid = null;
        String cxphost = "";
        String cxpprotocol = null;
        String cxppath = null;
        String cxpport = null;
        String command = null;
        String accountid = null;
        String groupname = null;
        String aeTitle = null;
        String ipAddress = null;
        
        
        /**
         * Prevent attempts to create shortcuts / icons
         */
        boolean quiet = false;
        
        int port = -1;
        
        /**
         * Returns a parameter string for passing the non-gateway determined 
         * parameters that represent this command.
         * @throws UnsupportedEncodingException 
         */
        public String toParameterString() {
            try {
                StringBuilder s = 
                    new StringBuilder(command)
                            .append("?ddl.guid=").append(guid)
                            .append("&auth=").append(auth); 
                            
                if(!blank(storageid))
                    s.append("&ddl.storageid=").append(storageid);
                
                if(!blank(groupname))
                    s.append("&ddl.groupname=").append(URLEncoder.encode(groupname,"UTF-8")); 
                
                if(!blank(aeTitle)) 
                    s.append("&ddl.aeTitle=").append(URLEncoder.encode(aeTitle,"UTF-8")); 
                    
                if(!blank(ipAddress)) 
                    s.append("&ddl.ipAddress=").append(URLEncoder.encode(ipAddress,"UTF-8")); 
                
                if(port >= 0)
                    s.append("&ddl.port=").append(port);
                
                if(quiet) 
                    s.append("&quiet=true");
                
                return s.toString();
            } 
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 not supported for encoding parameters - help!");
            }
        }
        
        public String getAuth() {
            return auth;
        }
        public void setAuth(String auth) {
            this.auth = auth;
        }
        public String getGuid() {
            return guid;
        }
        public void setGuid(String guid) {
            this.guid = guid;
        }
        public String getStorageid() {
            return storageid;
        }
        public void setStorageid(String storageid) {
            this.storageid = storageid;
        }
        public String getCxphost() {
            return cxphost;
        }
        public void setCxphost(String cxphost) {
            this.cxphost = cxphost;
        }
        public String getCxpprotocol() {
            return cxpprotocol;
        }
        public void setCxpprotocol(String cxpprotocol) {
            this.cxpprotocol = cxpprotocol;
        }
        public String getCxppath() {
            return cxppath;
        }
        public void setCxppath(String cxppath) {
            this.cxppath = cxppath;
        }
        public String getCxpport() {
            return cxpport;
        }
        public void setCxpport(String cxpport) {
            this.cxpport = cxpport;
        }
        public String getCommand() {
            return command;
        }
        public void setCommand(String command) {
            this.command = command;
        }
        public String getAccountid() {
            return accountid;
        }
        public void setAccountid(String accountid) {
            this.accountid = accountid;
        }

        public String getGroupname() {
            return groupname;
        }

        public void setGroupname(String groupname) {
            this.groupname = groupname;
        }

        public String getAeTitle() {
            return aeTitle;
        }

        public void setAeTitle(String aeTitle) {
            this.aeTitle = aeTitle;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
}
