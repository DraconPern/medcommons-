package net.medcommons.application.dicomclient.utils

import groovy.beans.Bindable;

import java.awt.HeadlessException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;

import net.medcommons.application.dicomclient.http.utils.Voucher;
import net.medcommons.application.dicomclient.transactions.ContextState;
import net.medcommons.modules.utils.FormatUtils;

/**
 * Specialization of AWT / Swing Menu class to add behavior for displaying 
 * voucher details.
 * 
 * @author ssadedin
 */
@Bindable
class VoucherMenu extends Menu {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(VoucherMenu.class);
    
    /**
     * The voucher that the menu is created for
     */
    Voucher voucher
    
    /**
     * The context of the voucher (server, credentials, etc.)
     */
    ContextState ctx
    
    /**
     * The percentage complete of the voucher - this is updated 
     * periodically as an upload proceeds
     * @see ImageSender.
     */
    float percentComplete
    
    /**
     * Create a menu for displaying voucher information
     */
    public VoucherMenu(ContextState ctx, Voucher voucher) throws HeadlessException {
        
        this.percentComplete = 0.0f
        this.ctx = ctx
        this.voucher = voucher
        
        this.propertyChange = { evt ->
            update()
        }
        
        update()
    }
    
    /**
     * Update the menus - called repeatedly as voucher is uploaded.  One way this
     * happens is due to a property change listener on the voucher property.
     */
    void update() {
        
        log.info "Updating voucher menu: ${voucher.voucherid} with percent ${percentComplete}"
        
        def status = "- Initializing"
        if(percentComplete >= 1.0f) {
            status = ""
            updateChildMenu()
        }
        else
        if(percentComplete > 0.0f) {
            status = FormatUtils.formatNumberTenths(100*percentComplete) + " % "
            updateChildMenu()
        }
        
        
        // Use different format for time if the voucher was created today
        /*
        def midnight = new GregorianCalendar()
        midnight.set(calendar.hour,0)
        midnight.set(calendar.minute,0)
        midnight.set(calendar.second,0)        
        def format = voucher.createDateTime > midnight ?
                "MM HH:mm a" : 
                "MM/dd/yyyy HH:mm ";
        */
        
        def format = "MM/dd HH:mm a";
        
        this.label = String.format("%s %s  %s %s",
                voucher.getPatientGivenName(), 
                voucher.getPatientFamilyName(), 
                voucher.createDateTime.format(format),
                status)        
        
    }
    
    
    /**
     * Menus that may be created in various states as a voucher upload progresses
     */
    def menus = [
        openHealthURL : null,
        voucher : null,
        remove : null
    ]
    
    /**
     * Add and or update the child menus for the voucher
     */
    void updateChildMenu() {
        
        if(!menus.openHealthURL) {
            menus.openHealthURL = addMenu("Open HealthURL", {
               showHealthURLInBrowser()
            })
            
            menus.voucher = addMenu("Voucher $voucher.voucherid / $voucher.otp", {
                StatusDisplayManager.showDocument(new URL(ctx.applianceRoot + "/mod/voucherclaim.php?voucherid=$voucher.voucherid"))
            })
            
            menus.remove = addMenu("Remove", {
                this.getParent().remove(this)
            })
        }
    }
    
    /**
     * Generic helper method to add a menu
     */
    MenuItem addMenu(String label, Closure handler) {
        def menu = new MenuItem(label) 
        menu.addActionListener({ evt -> handler() } as ActionListener )
        this.add(menu)
        return menu
    }
    
    void showHealthURLInBrowser() {
        try {
            StatusDisplayManager.showDocument(new URL(ctx.applianceRoot + "/$voucher.patientMedCommonsId"))
        }
        catch(Throwable t) {
            log.error("Failed to show health url in browser for voucher $voucher", t)
        }
    }
}
