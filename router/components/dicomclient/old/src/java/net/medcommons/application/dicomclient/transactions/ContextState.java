package net.medcommons.application.dicomclient.transactions;

import net.medcommons.application.dicomclient.utils.ManagedTransaction;

/**
 * Contains a context state containing account, identity, and security information
 * used in CXP or other network messages. Typically the values in an instance of 
 * this class are derived from an HTTP message sent by the browser to DDL. 
 * 
 * ContextState should contain *all* of the information needed to upload and download 
 * data from an account. 
 * 
 * This is a class whose design might change fairly significantly. Perhaps it
 * will be extended to include credentials for other network repositories or sources
 * (Google, Documentum).
 * 
 * @author sean
 *
 */
public class ContextState {
    
    private Long id; // Used by hibernate
    
   
    public enum Flag {
        /**
         * Prevent display of browser for transaction
         */
        NOBROWSER
    }
    
    /**
     * The MedCommons ID where the documents are stored
     */
    private String storageId;
    
    /**
     * The document identifier within the account referenced by 
     * the storageId. 
     */
    private String guid;
    
    /**
     * The port for the CXP service endpoint
     */
    private String cxpPort;
    
    /**
     * The hostname for the CXP service endpoint
     */
    private String cxpHost;
    
    /**
     * The path in the CXP service endpoint
     */
    private String cxpPath;
    
    /**
     * The protocol used by the CXP service (http or https)
     */
    private String cxpProtocol;
    
    /*
     * The MedCommons account of the user accessing the data (aka -the logged
     * in user).
     */
    private String accountId;
    
    /**
     * An authorization token used to access the storageId account as the accountId account.
     */
    private String auth;
    
    /**
     * The MedCommons identifier of the group that the 'accountId' account is in.
     */
    private String groupAccountId;
    
    /**
     * The name of the group that the 'accountId' is in.
     */
    private String groupName;
    
    /**
     * Concatenated version of the cxp* parameters.
     */
    private String gatewayRoot = null;
    
    /**
     * A list of arbitrary flags that may have been set by remote controller.
     * See list of predefined flags, {@link Flag}
     */
    private String [] flags;
    
    public void setId(Long id){
        this.id = id;
    }
    public Long getId(){
        return(this.id);
    }
    
    public void setStorageId(String storageId){
        this.storageId = storageId;
    }
    public String getStorageId(){
        return(this.storageId);
    }
    public void setGuid(String guid){
        this.guid = guid;
    }
    public String getGuid(){
        return(this.guid);
    }
    public void setCxpPort(String cxpPort){
        this.cxpPort = cxpPort;
    }
    public String getCxpPort(){
        return(this.cxpPort);
    }
    public void setCxpHost(String cxpHost){
        this.cxpHost = cxpHost;
    }
    public String getCxpHost(){
        return(this.cxpHost);
    }
    public void setCxpPath(String cxpPath){
        this.cxpPath = cxpPath;
    }
    public String getCxpPath(){
        return(this.cxpPath);
    }
    public void setCxpProtocol(String cxpProtocol){
        this.cxpProtocol = cxpProtocol;
    }
    public String getCxpProtocol(){
        return(this.cxpProtocol);
    }

    /**
     * The account id of the person currently logged in.
     * @param accountId
     */
    public void setAccountId(String accountId){
        this.accountId = accountId;
    }
    public String getAccountId(){
        return(this.accountId);
    }
    /**
     * Authorization context.
     * @param auth
     */
    public void setAuth(String auth){
        this.auth = auth;
    }
    public String getAuth(){
        return(this.auth);
    }
    /**
     * The current group account of the person who is logged in.
     */
    public void setGroupAccountId(String groupAccountId){
        this.groupAccountId = groupAccountId;
    }
    public String getGroupAccountId(){
        return(this.groupAccountId);
    }
    public void setGroupName(String groupName){
        this.groupName = groupName;
    }
    public String getGroupName(){
        return(this.groupName);
    }
    
    public void setGatewayRoot(String gatewayRoot){
        this.gatewayRoot = gatewayRoot;
    }
    
    public String getGatewayRoot(){
        if (this.gatewayRoot == null){
            // Not enough information to make a gatewayRoot
            if ((this.getCxpHost() == null) || (this.getCxpProtocol() == null)){
                this.gatewayRoot = null;
            }
            else{
                if (this.getCxpPort()== null){
                    gatewayRoot = this.getCxpProtocol() + "://" + this.getCxpHost();
                }
                else if ((this.getCxpProtocol().equals("http")) && (this.getCxpPort().equals("80"))){
                    gatewayRoot = this.getCxpProtocol() + "://" + this.getCxpHost();
                }
                else if ((this.getCxpProtocol().equals("https")) && (this.getCxpPort().equals("443"))){
                    gatewayRoot = this.getCxpProtocol() + "://" + this.getCxpHost();
                }
                else{
                    gatewayRoot = this.getCxpProtocol() + "://" + this.getCxpHost() + ":" + this.getCxpPort();
                }
            }
        }
          
        return(gatewayRoot);
    }
    public boolean isFlagSet(Flag flag) {
        String name = flag.name();
        if (flags != null){
	        for(int i=0; i<flags.length; ++i) {
	            if(name.equals(flags[i]))
	                return true;
	        }
        }
        return false;
    }

    public String [] getFlags() {
        return flags;
    }


    public void setFlags(String flags) {
        this.flags = flags.split(",");
    }
    public String toString(){
        StringBuffer buff = new StringBuffer("ContextManager[");
        buff.append("storageId=");
        buff.append(storageId);
        buff.append(",guid=");
        buff.append(guid);
        buff.append(",cxpProtocol=");
        buff.append(cxpProtocol);
        buff.append(",cxpHost=");
        buff.append(cxpHost);
        buff.append(",cxpPort=");
        buff.append(cxpPort);
        buff.append(",cxpPath=");
        buff.append(cxpPath);
        buff.append(",accountId=");
        buff.append(accountId);
        buff.append(",groupAccountId=");
        buff.append(groupAccountId);
        buff.append(",groupName=");
        buff.append(groupName);
        buff.append(",auth=");
        buff.append(auth);
        buff.append("flags=");
        buff.append(flags);
        if (gatewayRoot != null){
            buff.append(",gatewayRoot=");
            buff.append(gatewayRoot);
        }
        buff.append("]");
        
        return(buff.toString());
    }
    public String getCXPEndpoint(){
        return(getGatewayRoot() +  ManagedTransaction.CXP_ENDPOINT);
    }

}
