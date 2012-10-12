package net.medcommons.document.ccr;

import static java.util.regex.Pattern.compile;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import net.medcommons.phr.PHRException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

/**
 * Attempts to read a given file and minimally parse it as a "Blue Button" document
 * to turn it into a CCR.
 * 
 * @author ssadedin
 */
public class BlueButtonParser {
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(BlueButtonParser.class);

    private static final Pattern BEGIN_DEMOGRAPHICS = 
        compile("^[\\s]*-----.*demographic.*---- *", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern END_DEMOGRAPHICS = 
        compile("^[\\s]*-{3,200}.*$", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern FIRST_NAME = 
        compile("^[\\s]*((First)|(Given))[\\s]*Name[\\s]*:.{1,}$", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern LAST_NAME = 
        compile("^[\\s]*(Last|Family)[\\s]*Name[\\s]*:.{1,}$", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern FULL_NAME = 
        compile("^[\\s]*Full[\\s]*Name[\\s]*:.{1,}$", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern DATE_OF_BIRTH = 
        compile("^[\\s]*((Date Of Birth)|(DOB))[\\s]*:.{1,}$", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern SEX = 
        compile("^[\\s]*((Sex)|(Gender))[\\s]*:.{1,}$", Pattern.CASE_INSENSITIVE);
    
    /**
     * We assume American date format for now because this seems to be what the VA 
     * is exporting.
     */
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    
    /**
     * First name of patient 
     */
    private String firstName;
    
    /**
     * Last / Family name of patient
     */
    private String lastName;
    
    /**
     * Parsed date of birth, assuming it was in MM/DD/YYYY format
     */
    private Date dateOfBirth;
    
    /**
     * Raw date of birth string that was extracted
     */
    private String dateOfBirthValue;
    
    /**
     * Sex / Gender (not currently extracted)
     */
    private String sex;
    
    /**
     * Data extraction proceeds in several phases - first look for the 
     * section containing demographics, then try and extract demographics
     * from that section.
     */
    enum State {
        SEARCH_DEMOGRAPHICS,
        EXTRACT_DEMOGRAPHICS,
        FINISHED
    }
    
    /**
     * Current state of the data extraction
     */
    State state = State.SEARCH_DEMOGRAPHICS;

    /**
     * Read text from the given input stream and scan it for 
     * useful patient demographics information.  Outputs are 
     * set as properties on this class.
     */
    public void extractFromStream(InputStream in) throws IOException, PHRException, NoSuchAlgorithmException, JDOMException, ParseException {
        
        List<String> lines = IOUtils.readLines(in, "UTF-8");
        
        // Scan for a demographics section
        for(String line : lines) {
            log.debug("Parsing line: " + line);
            
            // What we do with the line depends on what state the 
            // the process is in
            switch(state) {
                case SEARCH_DEMOGRAPHICS:
                    if(BEGIN_DEMOGRAPHICS.matcher(line).matches()) {
                        state = State.EXTRACT_DEMOGRAPHICS;
                        log.info("Found demographics section: " + line);
                    }
                    break;
                    
                case EXTRACT_DEMOGRAPHICS:
                    extractDemographics(line);
                    break;
                    
                case FINISHED:
                    return;
            }
        }
    }

    /**
     * Inspect the given line for potential matches to intestering 
     * patient demographics attributes.  If a section end marker
     * is located, switch state to FINISHED.
     * 
     * @param line
     * @throws ParseException
     */
    private void extractDemographics(String line) throws ParseException {
        // log.info("Searching for demographics in line: " + line);
        if(END_DEMOGRAPHICS.matcher(line).matches()) {
            state = State.FINISHED;
            log.info("Identified end of demographics: " + line);
            return;
        }
        else
        if(FIRST_NAME.matcher(line).matches()) {
            this.firstName = line.substring(line.indexOf(":")+1).trim();
            log.info("Identified first name : " + line);
        }
        else
        if(LAST_NAME.matcher(line).matches()) {
            this.lastName = line.substring(line.indexOf(":")+1).trim();
            log.info("Identified last name : " + line);
        }
        else
        if(FULL_NAME.matcher(line).matches()) {
            
            String full = line.substring(line.indexOf(":")+1).trim();
            
            String [] parts = full.split(",");
            
            if(parts.length > 1) {
                this.firstName = parts[1].trim();
                this.lastName = parts[0].trim();
            }
            else {
                // Shove it all in the last name field?
                this.lastName = full;
            }
            
            log.info("Identified full name : " + line);
        }
        else
        if(DATE_OF_BIRTH.matcher(line).matches()) {
            this.dateOfBirthValue = line.substring(line.indexOf(":")+1).trim();
            try {
                this.dateOfBirth = DEFAULT_DATE_FORMAT.parse(this.dateOfBirthValue);
            }
            catch (Exception e) {
                log.info("Failed to parse date: " + this.dateOfBirthValue + " : " + e.getMessage());
            }
            log.info("Identified date of birth: " + line);
        }
        else
        if(SEX.matcher(line).matches()) {
            this.sex = line.substring(line.indexOf(":")+1).trim();
            if(sex.indexOf(' ')>0||sex.indexOf('\t')>0)
                sex = sex.split("[\\s]")[0];
            log.info("Identified sex: " + line);
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDateOfBirthValue() {
        return dateOfBirthValue;
    }
}
