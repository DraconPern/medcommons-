package net.medcommons.modules.services.interfaces;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Mapping for users table
 */
public class User implements Serializable {

	/**
	 * Attribute mcid.
	 */
	private Long mcid;
	
	/**
	 * Attribute email.
	 */
	private String email;
	
	/**
	 * Attribute sha1.
	 */
	private String sha1;
	
	/**
	 * Attribute serverId.
	 */
	private Integer serverId;
	
	/**
	 * Attribute since.
	 */
	private Timestamp since;
	
	/**
	 * Attribute firstName.
	 */
	private String firstName;
	
	/**
	 * Attribute lastName.
	 */
	private String lastName;
	
	/**
	 * Attribute mobile.
	 */
	private String mobile;
	
	/**
	 * Attribute smslogin.
	 */
	private Byte smslogin;
	
	/**
	 * Attribute updatetime.
	 */
	private Integer updatetime;
	
	/**
	 * Attribute ccrlogupdatetime.
	 */
	private Integer ccrlogupdatetime;
	
	/**
	 * Attribute chargeclass.
	 */
	private String chargeclass;
	
	/**
	 * Attribute rolehack.
	 */
	private String rolehack;
	
	/**
	 * Attribute affiliationgroupid.
	 */
	private Integer affiliationgroupid;
	
	/**
	 * Attribute startparams.
	 */
	private String startparams;
	
	/**
	 * Attribute stylesheetUrl.
	 */
	private String stylesheetUrl;
	
	/**
	 * Attribute picslayout.
	 */
	private String picslayout;
	
	/**
	 * Attribute photoUrl.
	 */
	private String photoUrl;
	
	/**
	 * Attribute acctype.
	 */
	private String acctype;
	
	/**
	 * Attribute persona.
	 */
	private String persona;
	
	/**
	 * Attribute validparams.
	 */
	private String validparams;
	
	/**
	 * Attribute interests.
	 */
	private String interests;
	
	/**
	 * Attribute emailVerified.
	 */
	private Timestamp emailVerified;
	
	/**
	 * Attribute mobileVerified.
	 */
	private Timestamp mobileVerified;
	
	/**
	 * Attribute amazonUserToken.
	 */
	private String amazonUserToken;
	
	/**
	 * Attribute amazonProductToken.
	 */
	private String amazonProductToken;
	
	/**
	 * Attribute enableVouchers.
	 */
	private Integer enableVouchers;
	
	/**
	 * Attribute amazonPid.
	 */
	private String amazonPid;
	
	/**
	 * Attribute activeGroupAccid.
	 */
	private Long activeGroupAccid;
	
	/**
	 * Attribute enableSimtrak.
	 */
	private Integer enableSimtrak;
	
	/**
	 * Attribute enableDod.
	 */
	private Integer enableDod;
	
	/**
	 * Attribute expirationDate.
	 */
	private Timestamp expirationDate;
	
	/**
	 * Attribute encSkey.
	 */
	private String encSkey;
	
	
	/**
	 * <p> 
	 * </p>
	 * @return mcid
	 */
	public Long getMcid() {
		return mcid;
	}

	/**
	 * @param mcid new value for mcid 
	 */
	public void setMcid(Long mcid) {
		this.mcid = mcid;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email new value for email 
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return sha1
	 */
	public String getSha1() {
		return sha1;
	}

	/**
	 * @param sha1 new value for sha1 
	 */
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return serverId
	 */
	public Integer getServerId() {
		return serverId;
	}

	/**
	 * @param serverId new value for serverId 
	 */
	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return since
	 */
	public Timestamp getSince() {
		return since;
	}

	/**
	 * @param since new value for since 
	 */
	public void setSince(Timestamp since) {
		this.since = since;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName new value for firstName 
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName new value for lastName 
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return mobile
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * @param mobile new value for mobile 
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return smslogin
	 */
	public Byte getSmslogin() {
		return smslogin;
	}

	/**
	 * @param smslogin new value for smslogin 
	 */
	public void setSmslogin(Byte smslogin) {
		this.smslogin = smslogin;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return updatetime
	 */
	public Integer getUpdatetime() {
		return updatetime;
	}

	/**
	 * @param updatetime new value for updatetime 
	 */
	public void setUpdatetime(Integer updatetime) {
		this.updatetime = updatetime;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return ccrlogupdatetime
	 */
	public Integer getCcrlogupdatetime() {
		return ccrlogupdatetime;
	}

	/**
	 * @param ccrlogupdatetime new value for ccrlogupdatetime 
	 */
	public void setCcrlogupdatetime(Integer ccrlogupdatetime) {
		this.ccrlogupdatetime = ccrlogupdatetime;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return chargeclass
	 */
	public String getChargeclass() {
		return chargeclass;
	}

	/**
	 * @param chargeclass new value for chargeclass 
	 */
	public void setChargeclass(String chargeclass) {
		this.chargeclass = chargeclass;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return rolehack
	 */
	public String getRolehack() {
		return rolehack;
	}

	/**
	 * @param rolehack new value for rolehack 
	 */
	public void setRolehack(String rolehack) {
		this.rolehack = rolehack;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return affiliationgroupid
	 */
	public Integer getAffiliationgroupid() {
		return affiliationgroupid;
	}

	/**
	 * @param affiliationgroupid new value for affiliationgroupid 
	 */
	public void setAffiliationgroupid(Integer affiliationgroupid) {
		this.affiliationgroupid = affiliationgroupid;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return startparams
	 */
	public String getStartparams() {
		return startparams;
	}

	/**
	 * @param startparams new value for startparams 
	 */
	public void setStartparams(String startparams) {
		this.startparams = startparams;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return stylesheetUrl
	 */
	public String getStylesheetUrl() {
		return stylesheetUrl;
	}

	/**
	 * @param stylesheetUrl new value for stylesheetUrl 
	 */
	public void setStylesheetUrl(String stylesheetUrl) {
		this.stylesheetUrl = stylesheetUrl;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return picslayout
	 */
	public String getPicslayout() {
		return picslayout;
	}

	/**
	 * @param picslayout new value for picslayout 
	 */
	public void setPicslayout(String picslayout) {
		this.picslayout = picslayout;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return photoUrl
	 */
	public String getPhotoUrl() {
		return photoUrl;
	}

	/**
	 * @param photoUrl new value for photoUrl 
	 */
	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return acctype
	 */
	public String getAcctype() {
		return acctype;
	}

	/**
	 * @param acctype new value for acctype 
	 */
	public void setAcctype(String acctype) {
		this.acctype = acctype;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return persona
	 */
	public String getPersona() {
		return persona;
	}

	/**
	 * @param persona new value for persona 
	 */
	public void setPersona(String persona) {
		this.persona = persona;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return validparams
	 */
	public String getValidparams() {
		return validparams;
	}

	/**
	 * @param validparams new value for validparams 
	 */
	public void setValidparams(String validparams) {
		this.validparams = validparams;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return interests
	 */
	public String getInterests() {
		return interests;
	}

	/**
	 * @param interests new value for interests 
	 */
	public void setInterests(String interests) {
		this.interests = interests;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return emailVerified
	 */
	public Timestamp getEmailVerified() {
		return emailVerified;
	}

	/**
	 * @param emailVerified new value for emailVerified 
	 */
	public void setEmailVerified(Timestamp emailVerified) {
		this.emailVerified = emailVerified;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return mobileVerified
	 */
	public Timestamp getMobileVerified() {
		return mobileVerified;
	}

	/**
	 * @param mobileVerified new value for mobileVerified 
	 */
	public void setMobileVerified(Timestamp mobileVerified) {
		this.mobileVerified = mobileVerified;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return amazonUserToken
	 */
	public String getAmazonUserToken() {
		return amazonUserToken;
	}

	/**
	 * @param amazonUserToken new value for amazonUserToken 
	 */
	public void setAmazonUserToken(String amazonUserToken) {
		this.amazonUserToken = amazonUserToken;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return amazonProductToken
	 */
	public String getAmazonProductToken() {
		return amazonProductToken;
	}

	/**
	 * @param amazonProductToken new value for amazonProductToken 
	 */
	public void setAmazonProductToken(String amazonProductToken) {
		this.amazonProductToken = amazonProductToken;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return enableVouchers
	 */
	public Integer getEnableVouchers() {
		return enableVouchers;
	}

	/**
	 * @param enableVouchers new value for enableVouchers 
	 */
	public void setEnableVouchers(Integer enableVouchers) {
		this.enableVouchers = enableVouchers;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return amazonPid
	 */
	public String getAmazonPid() {
		return amazonPid;
	}

	/**
	 * @param amazonPid new value for amazonPid 
	 */
	public void setAmazonPid(String amazonPid) {
		this.amazonPid = amazonPid;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return activeGroupAccid
	 */
	public Long getActiveGroupAccid() {
		return activeGroupAccid;
	}

	/**
	 * @param activeGroupAccid new value for activeGroupAccid 
	 */
	public void setActiveGroupAccid(Long activeGroupAccid) {
		this.activeGroupAccid = activeGroupAccid;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return enableSimtrak
	 */
	public Integer getEnableSimtrak() {
		return enableSimtrak;
	}

	/**
	 * @param enableSimtrak new value for enableSimtrak 
	 */
	public void setEnableSimtrak(Integer enableSimtrak) {
		this.enableSimtrak = enableSimtrak;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return enableDod
	 */
	public Integer getEnableDod() {
		return enableDod;
	}

	/**
	 * @param enableDod new value for enableDod 
	 */
	public void setEnableDod(Integer enableDod) {
		this.enableDod = enableDod;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return expirationDate
	 */
	public Timestamp getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate new value for expirationDate 
	 */
	public void setExpirationDate(Timestamp expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	/**
	 * <p> 
	 * </p>
	 * @return encSkey
	 */
	public String getEncSkey() {
		return encSkey;
	}

	/**
	 * @param encSkey new value for encSkey 
	 */
	public void setEncSkey(String encSkey) {
		this.encSkey = encSkey;
	}
}