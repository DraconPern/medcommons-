/*
 * $Id: Voucher.java 2897 2008-09-08 22:33:02Z ssadedin $
 * Created on 02/09/2008
 */
package net.medcommons.modules.services.interfaces;

public class Voucher {

    /**
     * Coupon Number of voucher (if any)
     */
    private Long couponNum = null;
    
    /**
     * Voucher id of this account if it is associated with a voucher
     */
    private String id = null;
    
    /**
     * SHA-1 hash of one time password for coupon
     */
    private String otpHash = null;
    
    
    private String status;
    
    private String providerAccountId;
    
    @Override
    public String toString() {
        return "Voucher[ Coupon number = " + couponNum + ", voucher id = " + this.id + "]";
    }

    public Long getCouponNum() {
        return couponNum;
    }
    public void setCouponNum(Long voucherCouponNum) {
        this.couponNum = voucherCouponNum;
    }
    public String getId() {
        return id;
    }
    public void setId(String voucherId) {
        this.id = voucherId;
    }
    public String getOtpHash() {
        return otpHash;
    }
    public void setOtpHash(String voucherOtpHash) {
        this.otpHash = voucherOtpHash;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getProviderAccountId() {
        return providerAccountId;
    }
    public void setProviderAccountId(String providerAccountId) {
        this.providerAccountId = providerAccountId;
    }
}
