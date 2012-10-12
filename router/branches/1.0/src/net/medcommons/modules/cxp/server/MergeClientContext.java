package net.medcommons.modules.cxp.server;

import net.medcommons.router.services.ccr.CCRMergeLogic;

/**
 * Context state used on CCR merge. 
 * 
 * The mere existence of this class suggests that the merge code
 * may need to be refactored.
 * 
 * @author sdoyle
 *
 */
public class MergeClientContext {
	private String userAgent;
	private boolean createNewTab = false;
	private String auth = null;
	private CCRMergeLogic mergeLogic = null;
	
	/**
	 * Sets the user agent from network input.
	 * This is used because some merge strategies depend on where
	 * the content comes from.
	 * 
	 * @param userAgent
	 */
	public void setUserAgent(String userAgent){
		this.userAgent = userAgent;
	}
	public String getUserAgent(){
		return(this.userAgent);
	}
	
	/**
	 * A new tab will be created if true; otherwise false.
	 * @param createNewTab
	 */
	public void setCreateNewTab(boolean createNewTab){
		this.createNewTab = createNewTab;
	}
	
	public boolean getCreateNewTab(){
		return(this.createNewTab);
	}
	public void setAuth(String auth){
		this.auth = auth;
	}
	
	public String getAuth(){
		return(this.auth);
	}
    public CCRMergeLogic getMergeLogic() {
        return mergeLogic;
    }
    public void setMergeLogic(CCRMergeLogic mergeLogic) {
        this.mergeLogic = mergeLogic;
    } 
	
}
