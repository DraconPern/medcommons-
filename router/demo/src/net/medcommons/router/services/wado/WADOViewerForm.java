/*
 * $Id$
 * Created on Aug 30, 2004
 */
package net.medcommons.router.services.wado;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.medcommons.router.services.dicom.util.MCSeries;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;

/**
 * The WADOViewerForm marshals parameters to the WADO Viewer.  This should not
 * be mistaken for parameters to the WADO image which are governed by the 
 * WADO Specification.  The WADO Viewer is an abstracted viewere which can
 * show objects of many types - WADO images are one of these.    
 * 
 * Example of parameters that can be submitted to the WADOViewerForm:
 * 
 *    guid=FC6C93A3FFB29621282077037843672F76F01715
 *    tracking=E25A695412D
 *    address=123+Lucky+St
 *    state=MT
 *    city=Butte
 *    zip=83132
 *    cardnumber=7817574478133225
 *    amount=150.00
 *    tax=12.00
 *    charge=162.00
 *    expiration=12%2F09
 *    copyto=agropper@medcommons.org
 *    comments=+CERVICAL+SPINE+
 *    history=%3Cunknown%3E  
 * @author ssadedin
 */
public class WADOViewerForm extends ActionForm {

  /**
   * Logger to use with this class
   */
  private static Logger log = Logger.getLogger(WADOViewerForm.class);
  
  public static class MenuItem {
    public String label;
    public String key;
    public String dataGuids;
    public String protocol;

    /**
     * 
     * @uml.property name="key"
     */
    public String getKey() {
        return key;
    }

    /**
     * 
     * @uml.property name="key"
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 
     * @uml.property name="label"
     */
    public String getLabel() {
        return label;
    }

    /**
     * 
     * @uml.property name="label"
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 
     * @uml.property name="dataGuids"
     */
    public String getDataGuids() {
        return (dataGuids);
    }

    /**
     * 
     * @uml.property name="dataGuids"
     */
    public void setDataGuids(String dataGuids) {
        this.dataGuids = dataGuids;
    }

    /**
     * 
     * @uml.property name="protocol"
     */
    public String getProtocol() {
        return (protocol);
    }

    /**
     * 
     * @uml.property name="protocol"
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    
  }
  
  /**
   * The GUID of the object to be shown
   */
  private String guid;
  
  /**
   * Tracking #
   */
  private String tracking = "";
  
  private String address = "";
  
  private String state = "";
  
  private String zip = "";
  
  private String cardNumber = "";
  
  private String amount = "";
  
  private String tax = "";
  
  private String charge = "";
  
  private String expiration = "";
  
  private String copyTo = "";
  
  private String comments = "";
  
  private String history = "";
  
  private String name="";
  
  private String city="";
  
  private String signature1 = "";
  
  private String signature2 = "";
  
  private String signature3 = "";
  
  private int selectedThumbnail = 0; 
  
  private String orderUrl = null;
  
  private int imageCacheSize = 5;
  
  private boolean cacheDisplay = true;
  
  private String menu = null;
  
  private String apUrl = null;
  
  private String actor = null;
  
  private String location = null;
  
  private String initialSeriesGuid = null;
  
  private Integer initialSeriesIndex = 1;
  
  /**
   * The height of the main image
   */
  private int imageHeight = 720;
  
  /**
   * Whether the page will be displayed in portrait or landscane mode
   */
  private String stylesheet = "WADOLandscape.css";
  
  /**
   * What type of page is being displayed in the main WADO image area
   * The word 'Body.jsp' is appended to this to determine
   * what jsp is displayed in the WADO body area.
   */
  private String formType = "WADO";
  
  /**
   * The list of series associated with this form
   */
  private List seriesList = null;
  
  /**
   * Menu items are parsed from the menu property when it is set
   */
  private List menuItems = new ArrayList();
  
  private String selectedMenuKey = null;

    /**
     * @return Returns the address.
     * 
     * @uml.property name="address"
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address The address to set.
     * 
     * @uml.property name="address"
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return Returns the amount.
     * 
     * @uml.property name="amount"
     */
    public String getAmount() {
        return amount;
    }

    /**
     * @param amount The amount to set.
     * 
     * @uml.property name="amount"
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }

    /**
     * @return Returns the cardNumber.
     * 
     * @uml.property name="cardNumber"
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * @param cardNumber The cardNumber to set.
     * 
     * @uml.property name="cardNumber"
     */
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * @return Returns the charge.
     * 
     * @uml.property name="charge"
     */
    public String getCharge() {
        return charge;
    }

    /**
     * @param charge The charge to set.
     * 
     * @uml.property name="charge"
     */
    public void setCharge(String charge) {
        this.charge = charge;
    }

    /**
     * @return Returns the comments.
     * 
     * @uml.property name="comments"
     */
    public String getComments() {
        return comments;
    }

    /**
     * @param comments The comments to set.
     * 
     * @uml.property name="comments"
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * @return Returns the copyTo.
     * 
     * @uml.property name="copyTo"
     */
    public String getCopyTo() {
        return copyTo;
    }

    /**
     * @param copyTo The copyTo to set.
     * 
     * @uml.property name="copyTo"
     */
    public void setCopyTo(String copyTo) {
        this.copyTo = copyTo;
    }

    /**
     * @return Returns the expiration.
     * 
     * @uml.property name="expiration"
     */
    public String getExpiration() {
        return expiration;
    }

    /**
     * @param expiration The expiration to set.
     * 
     * @uml.property name="expiration"
     */
    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    /**
     * @return Returns the guid.
     * 
     * @uml.property name="guid"
     */
    public String getGuid() {
        return guid;
    }

    /**
     * @param guid The guid to set.
     * 
     * @uml.property name="guid"
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * @return Returns the history.
     * 
     * @uml.property name="history"
     */
    public String getHistory() {
        return history;
    }

    /**
     * @param history The history to set.
     * 
     * @uml.property name="history"
     */
    public void setHistory(String history) {
        this.history = history;
    }

    /**
     * @return Returns the state.
     * 
     * @uml.property name="state"
     */
    public String getState() {
        return state;
    }

    /**
     * @param state The state to set.
     * 
     * @uml.property name="state"
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return Returns the tax.
     * 
     * @uml.property name="tax"
     */
    public String getTax() {
        return tax;
    }

    /**
     * @param tax The tax to set.
     * 
     * @uml.property name="tax"
     */
    public void setTax(String tax) {
        this.tax = tax;
    }

    /**
     * @return Returns the tracking.
     * 
     * @uml.property name="tracking"
     */
    public String getTracking() {
        return tracking;
    }

    /**
     * @param tracking The tracking to set.
     * 
     * @uml.property name="tracking"
     */
    public void setTracking(String tracking) {
        this.tracking = tracking;
    }

    /**
     * @return Returns the zip.
     * 
     * @uml.property name="zip"
     */
    public String getZip() {
        return zip;
    }

    /**
     * @param zip The zip to set.
     * 
     * @uml.property name="zip"
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return Returns the stylesheet.
     * 
     * @uml.property name="stylesheet"
     */
    public String getStylesheet() {
        return stylesheet;
    }

    /**
     * @param stylesheet The stylesheet to set.
     * 
     * @uml.property name="stylesheet"
     */
    public void setStylesheet(String layoutMode) {
        this.stylesheet = layoutMode;
    }

    /**
     * @return Returns the seriesList.
     * 
     * @uml.property name="seriesList"
     */
    public List getSeriesList() {
        return seriesList;
    }

    /**
     * @param seriesList The seriesList to set.
     * 
     * @uml.property name="seriesList"
     */
    public void setSeriesList(List seriesList) {
        this.seriesList = seriesList;
        log.info("setSeriesList - size is " + seriesList.size());
    }

    /**
     * @return Returns the formType.
     * 
     * @uml.property name="formType"
     */
    public String getFormType() {
        return formType;
    }

    /**
     * @param formType The formType to set.
     * 
     * @uml.property name="formType"
     */
    public void setFormType(String formType) {
        this.formType = formType;
    }

    /**
     * @return Returns the name.
     * 
     * @uml.property name="name"
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     * 
     * @uml.property name="name"
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the city.
     * 
     * @uml.property name="city"
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city The city to set.
     * 
     * @uml.property name="city"
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return Returns the signature1.
     * 
     * @uml.property name="signature1"
     */
    public String getSignature1() {
        return signature1;
    }

    /**
     * @param signature1 The signature1 to set.
     * 
     * @uml.property name="signature1"
     */
    public void setSignature1(String signature1) {
        this.signature1 = signature1;
    }

    /**
     * @return Returns the signature2.
     * 
     * @uml.property name="signature2"
     */
    public String getSignature2() {
        return signature2;
    }

    /**
     * @param signature2 The signature2 to set.
     * 
     * @uml.property name="signature2"
     */
    public void setSignature2(String signature2) {
        this.signature2 = signature2;
    }

    /**
     * @return Returns the signature3.
     * 
     * @uml.property name="signature3"
     */
    public String getSignature3() {
        return signature3;
    }

    /**
     * @param signature3 The signature3 to set.
     * 
     * @uml.property name="signature3"
     */
    public void setSignature3(String signature3) {
        this.signature3 = signature3;
    }

    /**
     * @return Returns the selectedThumbnail.
     * 
     * @uml.property name="selectedThumbnail"
     */
    public int getSelectedThumbnail() {
        return selectedThumbnail;
    }

    /**
     * @param selectedThumbnail The selectedThumbnail to set.
     * 
     * @uml.property name="selectedThumbnail"
     */
    public void setSelectedThumbnail(int selectedThumbnail) {
        this.selectedThumbnail = selectedThumbnail;
    }

    /**
     * @return Returns the orderUrl.
     * 
     * @uml.property name="orderUrl"
     */
    public String getOrderUrl() {
        return orderUrl;
    }

    /**
     * @param orderUrl The orderUrl to set.
     * 
     * @uml.property name="orderUrl"
     */
    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    /**
     * 
     * @uml.property name="imageCacheSize"
     */
    public int getImageCacheSize() {
        return imageCacheSize;
    }

    /**
     * 
     * @uml.property name="imageCacheSize"
     */
    public void setImageCacheSize(int imageCacheSize) {
        this.imageCacheSize = imageCacheSize;
    }

    /**
     * 
     * @uml.property name="cacheDisplay"
     */
    public boolean isCacheDisplay() {
        return cacheDisplay;
    }

    /**
     * 
     * @uml.property name="cacheDisplay"
     */
    public void setCacheDisplay(boolean cacheDisplay) {
        this.cacheDisplay = cacheDisplay;
    }

    /**
     * 
     * @uml.property name="apUrl"
     */
    public String getApUrl() {
        return apUrl;
    }

    /**
     * 
     * @uml.property name="apUrl"
     */
    public void setApUrl(String apUrl) {
        this.apUrl = apUrl;
    }

    /**
     * 
     * @uml.property name="menu"
     */
    public String getMenu() {
        return menu;
    }

    
  /**
   * Regex pattern used to parse incoming XML
   */
  private Pattern pattern 
    = Pattern.compile(".*<Label>(.*)</Label>.*<Key>(.*)</Key>.*", Pattern.DOTALL);

    /**
     * Sets the list of menu items known to the WADO viewer.
     * These are passed as XML in the following form:
     * 
     * <Menu>
     * <Item><Label>Xfer to Sean on cypher</Label><Key>CYPHER-sdoyle@medcommons.org</Key></Item>
     *   <Item><Label>CSTORE to OSIRIX</Label><Key>OSIRIX</Key></Item>
     * </Menu> 
     * 
     * @param menu
     * 
     * @uml.property name="menu"
     */
    public void setMenu(String menu) {
        this.menu = menu;

        this.menuItems.clear();
        if (menu == null) {
            return;
        } else {
            int index = 0;
            while ((index = menu.indexOf("<Item>", index)) >= 0) {
                int endIndex = menu.indexOf("</Item>", index);
                String item = menu.substring(index + 6, endIndex);
                Matcher matcher = pattern.matcher(item);

                if (matcher.matches()) {
                    MenuItem menuItem = new MenuItem();
                    menuItem.key = matcher.group(2).trim();
                    menuItem.label = matcher.group(1).trim();
                    if (menuItem.label.indexOf("CSTORE") != -1)
                        menuItem.setProtocol("CSTORE");
                    else
                        menuItem.setProtocol("http");
                    this.menuItems.add(menuItem);
                    log.info("menu item found:  label="
                        + menuItem.label
                        + " key = "
                        + menuItem.key
                        + " protocol = "
                        + menuItem.getProtocol());
                } else {
                    log.warn("Unable to parse item from menu XML: " + item);
                }
                index = endIndex;
            }
        }
    }

    /**
     * 
     * @uml.property name="menuItems"
     */
    public List getMenuItems() {
        return menuItems;
    }

    /**
     * Only added because struts likes to have it
     * @param menuItems
     * 
     * @uml.property name="menuItems"
     */
    public void setMenuItems(List menuItems) {
        log.warn("setMenuItems called");
        this.menuItems = menuItems;
    }

  
  public String getDataGuids()
  {
    if (seriesList==null){
      return("null");
    }
    else{
      StringBuffer  buff = new StringBuffer();
      for (int i = 0; i < seriesList.size(); i++) {
        
        MCSeries series = (MCSeries) seriesList.get(i);
        
        if (i!=0)
          buff.append(",");
        buff.append(series.getMcGUID());
        
      }
      String guids = buff.toString();
      log.info("guids = " + guids);
      return(guids);
    }
  }
  public void setDataGuids(String dataGuids){
    log.warn("setDataGuids called - this is a read-only attribute");
  }
  
  public String getItemType(){
    return("DATA");
  }
  public void setItemType(String itemType){
    log.warn("setItemType called - this is a read-only attribute");
  }

  public String getGlobalStatus(){
    log.info("getGlobalStatus - about to return NEW");
    return("NEW");
  }
  public void setGlobalStatus(String globalStatus){
    log.warn("setGlobalStatus called - this is a read-only attribute");
  }

    /**
     * 
     * @uml.property name="selectedMenuKey"
     */
    public String getSelectedMenuKey() {
        return (this.selectedMenuKey);
    }

    /**
     * 
     * @uml.property name="selectedMenuKey"
     */
    public void setSelectedMenuKey(String selectedMenuKey) {
        this.selectedMenuKey = selectedMenuKey;
        log.error("just set menu key to " + selectedMenuKey);
    }

 
  public String getProtocol(){
   
      for (int i=0;i<menuItems.size();i++){
        MenuItem item = (MenuItem) menuItems.get(i);
        if (item.key.equals(selectedMenuKey))
          return(item.getProtocol());
      }
      return("http");
      
  }
  public void setProtocol(String protocol){
    log.warn("setProtocol called - this is a read-only attribute");
  }

    /**
     * 
     * @uml.property name="actor"
     */
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * 
     * @uml.property name="actor"
     */
    public String getActor() {
        return (this.actor);
    }

    /**
     * 
     * @uml.property name="location"
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 
     * @uml.property name="location"
     */
    public String getLocation() {
        return (this.location);
    }

    /**
     * 
     * @uml.property name="imageHeight"
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * 
     * @uml.property name="imageHeight"
     */
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    /**
     * 
     * @uml.property name="initialSeriesGuid"
     */
    public String getInitialSeriesGuid() {
        return initialSeriesGuid;
    }

    /**
     * 
     * @uml.property name="initialSeriesGuid"
     */
    public void setInitialSeriesGuid(String initialSeriesGuid) {
        this.initialSeriesGuid = initialSeriesGuid;
    }

    public Integer getInitialSeriesIndex() {
        return initialSeriesIndex;
    }

    public void setInitialSeriesIndex(Integer initialSeriesIndex) {
        this.initialSeriesIndex = initialSeriesIndex;
    }
}
