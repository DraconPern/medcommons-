/*
 * $Id$
 * Created on 19/07/2007
 */
package net.medcommons.modules.utils;

/**
 * Constants relating to standard Test Data set used in MedCommons 
 *
 * The standard set of data consists of two practices and a number of doctors and a patient.
 *<pre>
 *     +----------------------------+
 *     | GROUP 1 - Good Doctors     |
 *     |                            |
 *     |       doctor1              |
 *     |       doctor2              |
 *     |      +------------+--------+------+
 *     |      |doctor3              |      |
 *     +------+------------+--------+      |
 *            |   GROUP 2 - Better Doctors |
 *            |                            |
 *            |                            |
 *            +----------------------------+
 *</pre> 
 * @author ssadedin
 */
public interface TestDataConstants {

    public  final String DOCTOR_ID = "1259366818364933"; 
    public  final String DOCTOR_EMAIL =  "doctor@medcommons.net";
    public  final String DOCTOR_AUTH =  "d5d813d968b8ae64088b37be1d1ff82addfbab41";
    
    public  final String DOCTOR2_EMAIL = "doctor2@medcommons.net";
    public  final String DOCTOR3_EMAIL = "doctor3@medcommons.net";
    public  final String DOCTOR3_AUTH = "77d49ea6137ec95bc02a3475282b5a1bc47d7872";

    public  final String USER1_EMAIL = "user1@medcommons.net";
    public  final String USER1_AUTH = "97a49ea6137dc95bc02a3775282b5e19c47d7892";
    
    public  final String USER2_EMAIL = "user2@medcommons.net"; 

    public  final long PRACTICE_ID = 20;
    public  final long PRACTICE2_ID = 22;

    public  final long GROUP_INSTANCE_ID = 32;
    public  final String GROUP_ACCT_ID="1175376381039160";

    public  final long GROUP2_INSTANCE_ID = 33;
    public  final String GROUP2_ACCT_ID="1064860359893503";
    public  final String GROUP2_NAME="Better Doctors";

    public  final String DOCTOR2_ID = "1166439538173659";
    public  final String DOCTOR3_ID = "1035582511657478";

    public  final String USER1_ID = "1162164444007929";
    public  final String USER2_ID = "1087997704966332";

    public  final long COVER_SHEET_ID = 10;
    
}
