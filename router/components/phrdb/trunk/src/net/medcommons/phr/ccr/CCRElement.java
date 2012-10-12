/*
 * $Id$
 * Created on 4/07/2006
 */
package net.medcommons.phr.ccr;

import static net.medcommons.modules.utils.Str.eq;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRElement;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XMLPHRDocument;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.phr.resource.Spring;
import net.medcommons.phr.util.StringUtil;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Parent;
import org.jdom.filter.ElementFilter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class CCRElement extends Element implements PHRElement {

	/**
	 * The URN of the namespace for CCR documents. This is the value that appears
	 * in the XML header.
	 */
	public static final String CCR_NAMESPACE_URN = "urn:astm-org:CCR";

	/**
	 * The type assigned to IDs that represent MedCommons accounts
	 */
	public static final String MEDCOMMONS_ACCOUNT_ID_TYPE = "MedCommons Account Id";

	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(CCRElement.class);

	/**
	 * Merge any count of Array.
	 *
	 * @param arrays manay arrays
	 * @return merged array
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] arrayMerge(T[]... arrays) {
		int count = 0;
		for (T[] array : arrays) {
			count += array.length;
		}
		// create new array
		T[] rv = (T[]) Array.newInstance(arrays[0][0].getClass(), count);
		int start = 0;
		for (T[] array : arrays) {
			System.arraycopy(array, 0, rv, start, array.length);
			start += array.length;
		}
		return (T[]) rv;
	}

	/*
	 * <xs:complexType name="TestType">
		<xs:complexContent>
			<xs:extension base="CCRCodedDataObjectType">
				<xs:sequence>
					<xs:element name="Method" type="CodedDescriptionType" minOccurs="0" maxOccurs="unbounded"/>
					<xs:element ref="Agent" minOccurs="0" maxOccurs="unbounded"/>
					<xs:element name="TestResult" type="TestResultType"/>
					<xs:element name="NormalResult" minOccurs="0">
						<xs:complexType>
							<xs:sequence>
								<xs:element name="Normal" type="NormalType" maxOccurs="unbounded"/>
							</xs:sequence>
						</xs:complexType>
					</xs:element>
					<xs:element name="Flag" type="CodedDescriptionType" minOccurs="0" maxOccurs="unbounded"/>
					<xs:element name="ConfidenceValue" type="CodedDescriptionType" minOccurs="0"/>
				</xs:sequence>
			</xs:extension>
		</xs:complexContent>
	</xs:complexType>
	
	<xs:complexType name="TestResultType">
		<xs:sequence>
			<xs:group ref="MeasureGroup"/>
			<xs:element ref="Description" minOccurs="0" maxOccurs="unbounded"/>
			<xs:element name="ResultSequencePosition" type="xs:integer" minOccurs="0"/>
			<xs:element name="VariableResultModifier" type="CodedDescriptionType" minOccurs="0"/>
		</xs:sequence>
	</xs:complexType>
		
	
	 */
	/**
	 * Hashmap of rules governing CCR Element order.  
	 * 
	 * Keys of the hashmap are "parent" elements and the array values are an ordered list
	 * of children.  Elements created with getOrCreate() are guaranteed to be created
	 * consistent with order in the array associated with the key of corresponding 
	 * parent element name. 
	 */
	public static HashMap<String, String[]> CCR_ELEMENT_ORDER = new HashMap<String, String[]>();

	public static HashSet<String> CCR_DATA_OBJECTS = new HashSet<String>();

	/**
	 * The ORDER_* string arrays contain ordering information within a type
	 * as defined in the CCR XSD. Some of these are components used for
	 * other types via xs:group or xs:extension definitions in the schema.
	 * 
	 * The value for '*' is derived from the XSD.
	 * 
	 * Note: may have a problem because there are multiple incompatible element
	 * types defined for 'Product' in the CCR schema.
	 */
	final static String[] ORDER_SLRCGroup = new String[] { "Source",
			"InternalCCRLink", "ReferenceID", "CommentID", "Signature" };
	final static String[] ORDER_MeasureGroup = new String[] { "Value", "Units",
			"Code" };
	final static String[] ORDER_Measure = new String[] { "Value", "Units",
			"Code" };
	final static String[] ORDER_Code = new String[] { "Value", "CodingSystem",
			"Version" };
	final static String[] ORDER_PersonName = new String[]{"Given", "Middle", "Family","Suffix", "Title", "NickName" };
	final static String[] ORDER_InformationSystem = new String[]{"Name", "Type", "Version"};
	final static String[] ORDER_Comment = new String[] { "CommentObjectID",
			"DateTime", "Type", "Description", "Source", "ReferenceID" };
	final static String[] ORDER_CCRCodedDataObject = arrayMerge(new String[] {
			"CCRDataObjectID", "DateTime", "IDs", "Type", "Description",
			"Status" }, ORDER_SLRCGroup);
	final static String[] ORDER_CodedDescription = new String[] { "Text",
			"ObjectAttribute", "Code" };
	final static String[] ORDER_Alert = arrayMerge(ORDER_CCRCodedDataObject,
			new String[] { "Agent", "Reaction" });
	final static String[] ORDER_SocialHistory = arrayMerge(
			ORDER_CCRCodedDataObject, new String[] { "Episodes" });
	final static String[] ORDER_StructuredProductType = arrayMerge(
			ORDER_CCRCodedDataObject, new String[] { "Product", "Quantity",
					"Directions", "PatientInstructions",
					"FulfillmentInstructions", "Refills", "SeriesNumber",
					"Consent", "FulfillmentHistory" });
	final static String[] ORDER_Result = arrayMerge(ORDER_CCRCodedDataObject,
			new String[] { "Procedure", "Substance", "Test" });
	final static String[] ORDER_Intervention = arrayMerge(
			ORDER_CCRCodedDataObject, new String[] { "Procedures", "Products",
					"Medications", "Immunizations", "Services", "Encounters",
					"Authorizations" });
	final static String[] ORDER_PlanOfCare = arrayMerge(ORDER_Intervention,
			new String[] { "Goals", "OrderSequencePosition",
					"MultipleOrderModifier" });

	final static String[] ORDER_Test = arrayMerge(ORDER_CCRCodedDataObject,
			new String[] { "Method", "Agent", "TestResult", "NormalResult",
					"Flag", "ConfidenceValue" });
	final static String[] Order_Procedure = arrayMerge(new String[] {
			"CCRDataObjectID", "DateTime", "IDs", "Type", "Description",
			"Status" }, ORDER_SLRCGroup, new String[] { "Locations",
			"Practitioners", "Frequency", "Interval", "Duration",
			"Indications", "Instructions", "Consent" });
	final static String[] ORDER_TestResult = arrayMerge(ORDER_MeasureGroup,
			new String[] { "Description", "ResultSequencePosition",
					"VariableResultModifier" });
	final static String[] ORDER_Normal = arrayMerge(
			new String[] {"Description", "Type"},
			ORDER_MeasureGroup,
			new String[]{"ValueSequencePosition", "VariableNomalModifier"},
			ORDER_SLRCGroup
			);
	
	final static String[] ORDER_Problem = arrayMerge(ORDER_CCRCodedDataObject,
			new String[] { "Episodes", "HealthStatus", "PatientKnowledge" });

	final static String[] ORDER_Plan = arrayMerge(ORDER_CCRCodedDataObject,
			new String[] { "OrderRequest" });

	final static String[] ORDER_Product = new String[] { "ProductName",
			"BrandName", "Strength", "Form", "Concentration", "Size",
			"Manufacturer", "IDs", "ProductSequencePosition",
			"MultipleProductModifier" };
	final static String[] ORDER_Product_Mangled = ORDER_StructuredProductType;
	final static String[] ORDER_ObjectAttribute = new String[] { "Attribute",
			"AttributeValue", "Code", "UniqueValues" };
	final static String[] ORDER_Direction = arrayMerge(new String[] {
			"Description", "DoseIndicator", "DeliveryMethod", "Dose" },
			ORDER_Measure, new String[] { "Rate", "DoseSequencePosition",
					"VariableDoseModifier" }, new String[] { "DoseCalculation",
					"Vehicle", "Route", "Site", "AdministrationTiming",
					"Frequency", "Interval", "Duration", "DoseRestriction",
					"Indication", "StopIndicator", "DirectionSequencePosition",
					"MultipleDirectionModifier" });
	final static String[] ORDER_Insurance = arrayMerge(
			ORDER_CCRCodedDataObject, new String[] { "PaymentProvider",
					"Subscriber", "Authorizations" });

	final static String[] ORDER_FamilyHistory = arrayMerge(
			ORDER_CCRCodedDataObject,
			new String[] { "FamilyMember", "Problem" });

	final static String[] ORDER_Form_Mangled = ORDER_CCRCodedDataObject;
	final static String[] ORDER_Form = arrayMerge(
			ORDER_CCRCodedDataObject,
			new String[]{"FormSequencePosition","MultipleFormModifier"}
			);
	
	final static String[] ORDER_Encounter = arrayMerge(
			ORDER_CCRCodedDataObject, new String[] { "Locations",
					"Practitioners", "Frequency", "Interval", "Duration",
					"Indications", "Instructions", "Consent" });

	final static String[] ORDER_Communication = new String[] { "Value", "Type",
			"Priority", "Status" };
	// Note that Actor is ambiguous - this contains both 

	final static String[] ORDER_ActorReference = new String[] { "ActorID",
			"ActorRole" };

	final static String[] ORDER_Actor = arrayMerge(
			new String[] { "ActorObjectID", "Person", "Organization",
					"InformationSystem", "IDs", "Relation","Specialty", "Address",
					"Telephone", "EMail", "URL", "Status" }, ORDER_SLRCGroup);

	final static String[] ORDER_Function = arrayMerge(ORDER_CCRCodedDataObject,
			new String[] { "Problem", "Test" });

	final static String[] ORDER_Reference = new String[] { "ReferenceObjectID",
			"DateTime", "Type", "Description", "Source", "Locations" };

	final static String[] ORDER_Signature = new String[] { "SignatureObjectID",
			"ExactDateTime", "Type", "IDs", "Source", "Signature" };

	final static String[] ORDER_ID = arrayMerge(new String[] { "DateTime",
			"Type", "ID", "IssuedBy" }, ORDER_SLRCGroup);

	final static String[] ORDER_Episode = arrayMerge(ORDER_CCRCodedDataObject,
			new String[] { "Duration" });

	final static String[] ORDER_Episodes = arrayMerge(new String[] { "Number",
			"Frequency", "Episode" }, ORDER_SLRCGroup);

	final static String[] ORDER_Frequency = arrayMerge(
			new String[] { "Description" }, ORDER_MeasureGroup, new String[] {
					"FrequencySequencePosition", "VariableFrequencyModifier" });

	
	
	final static String[] ORDER_OrderRxHistory = arrayMerge(
			ORDER_CCRCodedDataObject, new String[] { "FulfillmentMethod",
					"Provider", "Location", "Reaction", "ProductName",
					"BrandName", "Manufacturer",
					/*"IDs", */"Strength", "Form", "Concentration", "Quanity",
					"LabelInstructions", "SeriesNumber"
			// Note that IDs also occur in ORDER_CCRCodedDataObject

			});

	final static List<String> CCR_DATA_OBJECT_ORDER = Arrays
			.asList(ORDER_CCRCodedDataObject);

	static {
		// All the following elements inherit the object order from CCR_DATA_OBJECT_ORDER above.
		CCR_DATA_OBJECTS.addAll(Arrays.asList(new String[] {
				"AdvanceDirective", "EnvironmentalAgent", "Alert",
				"Authorization", "Episode", "Encounter", "Consent", "Outcome",
				"Payer", "Intervention", "OrderRxHistory", "OrderRequest",
				"Plan", "Procedure", "Problem", "SocialHistory",
				"StructuredProduct", "Result", "Test",
				"FamilyProblemHistory" }));

		CCR_ELEMENT_ORDER.put("ContinuityOfCareRecord", new String[] {
				"CCRDocumentObjectID", "Language", "Version", "DateTime",
				"Patient", "From", "To", "Purpose", "Body", "Actors",
				"References", "Comments", "Signatures" });

		CCR_ELEMENT_ORDER.put("Address", new String[] { "Type", "Line1",
				"Line2", "City", "County", "State", "Country", "PostalCode",
				"Priority", "Status", "Preferred" });
		CCR_ELEMENT_ORDER.put("Actor", arrayMerge(ORDER_Actor,
		                
				ORDER_ActorReference));
		CCR_ELEMENT_ORDER.put("CurrentName", new String[] { "Given", "Middle",
				"Family" });
		CCR_ELEMENT_ORDER.put("Body",
				new String[] { "Payers", "AdvanceDirectives", "Support",
						"FunctionalStatus", "Problems", "FamilyHistory",
						"SocialHistory", "Alerts", "Medications",
						"MedicalEquipment", "Immunizations", "VitalSigns",
						"Results", "Procedures", "Encounters", "PlanOfCare",
						"HealthCareProviders" });
		CCR_ELEMENT_ORDER.put("FamilyProblemHistory", ORDER_FamilyHistory);
		CCR_ELEMENT_ORDER.put("Result", ORDER_Result);
		CCR_ELEMENT_ORDER.put("Person", new String[] { "Name", "DateOfBirth",
				"Gender" });
		CCR_ELEMENT_ORDER.put("Name", new String[]{"BirthName", "AdditionalName", "CurrentName", "DisplayName"});

		CCR_ELEMENT_ORDER.put("Strength", ORDER_Measure);
		CCR_ELEMENT_ORDER.put("Test", ORDER_Test);
		CCR_ELEMENT_ORDER.put("TestResult", ORDER_TestResult);
		CCR_ELEMENT_ORDER.put("Procedure", Order_Procedure);
		CCR_ELEMENT_ORDER.put("Plan", ORDER_Plan);
		CCR_ELEMENT_ORDER.put("OrderRequest", ORDER_PlanOfCare);
		CCR_ELEMENT_ORDER.put("Medication", ORDER_StructuredProductType);
		CCR_ELEMENT_ORDER.put("Immunization", ORDER_StructuredProductType);
		CCR_ELEMENT_ORDER.put("Equipment", ORDER_StructuredProductType);
		CCR_ELEMENT_ORDER.put("Product", ORDER_Product);

		CCR_ELEMENT_ORDER.put("Comment", new String[] { "CommentObjectID",
				"DateTime", "Type", "Description", "Source" });
		CCR_ELEMENT_ORDER.put("Source", new String[] { "Description", "Actor",
				"DateTime", "ReferenceID", "CommentID" });
		CCR_ELEMENT_ORDER.put("Alert", ORDER_Alert);
		CCR_ELEMENT_ORDER.put("SocialHistoryElement", ORDER_SocialHistory);
		CCR_ELEMENT_ORDER.put("Purpose", new String[] { "DateTime",
				"Description", "OrderRequest", "Indications", "ReferenceID",
				"CommentID" });
		CCR_ELEMENT_ORDER.put("Problem", ORDER_Problem);
		CCR_ELEMENT_ORDER.put("Payer", ORDER_Insurance);
		CCR_ELEMENT_ORDER.put("Encounter", ORDER_Encounter);
		CCR_ELEMENT_ORDER.put("Telephone", ORDER_Communication);
		CCR_ELEMENT_ORDER.put("EMail", ORDER_Communication);
		CCR_ELEMENT_ORDER.put("URL", ORDER_Communication);
		
		CCR_ELEMENT_ORDER.put("DateTime", new String[] { "Type", "ExactDateTime", "Age", "ApproximateDateTime", "DateTimeRange" });
		CCR_ELEMENT_ORDER.put("DateOfBirth", CCR_ELEMENT_ORDER.get("DateTime"));
		
		CCR_ELEMENT_ORDER.put("Direction", ORDER_Direction);
		CCR_ELEMENT_ORDER.put("AdvanceDirective", ORDER_CCRCodedDataObject);
		CCR_ELEMENT_ORDER.put("SupportProvider", ORDER_ActorReference);
		CCR_ELEMENT_ORDER.put("Provider", ORDER_ActorReference);
		CCR_ELEMENT_ORDER.put("Reference", ORDER_Reference);
		CCR_ELEMENT_ORDER.put("Signature", ORDER_Signature);
		CCR_ELEMENT_ORDER.put("Relation", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("Specialty", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("ProductName", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("Instruction", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("InternalCCRLink", new String[] { "LinkID",
				"LinkRelationship", "Source" });
		CCR_ELEMENT_ORDER.put("IDs", ORDER_ID);
		CCR_ELEMENT_ORDER.put("Location",
				new String[] { "Description", "Actor" });
		CCR_ELEMENT_ORDER.put("VariableDurationModifier",
				ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("Frequency", ORDER_Frequency);
		CCR_ELEMENT_ORDER.put("Manufacturer", ORDER_ActorReference);
		CCR_ELEMENT_ORDER.put("Strength", ORDER_MeasureGroup);
		CCR_ELEMENT_ORDER.put("Consent", ORDER_CCRCodedDataObject);
		CCR_ELEMENT_ORDER.put("Fulfillment", ORDER_OrderRxHistory);
		CCR_ELEMENT_ORDER.put("ObjectAttribute", ORDER_ObjectAttribute);
		CCR_ELEMENT_ORDER.put("Code", ORDER_Code);
		CCR_ELEMENT_ORDER.put("Description", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put(CCRMangler.MANGLED_PRODUCTNAME, ORDER_Product_Mangled);
		
		CCR_ELEMENT_ORDER.put("BirthName", ORDER_PersonName);
		CCR_ELEMENT_ORDER.put("AdditionalName", ORDER_PersonName);
		CCR_ELEMENT_ORDER.put("CurrentName", ORDER_PersonName);
		CCR_ELEMENT_ORDER.put("Normal", ORDER_Normal);
		CCR_ELEMENT_ORDER.put("Method", ORDER_CCRCodedDataObject);
		CCR_ELEMENT_ORDER.put("Flag", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("ConfidenceValue", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("BrandName", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("VariableStrengthModifier", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("MultipleFormModifier", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("VariableQuantityModifier", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("MultipleVehicleModifier", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("VariableRateModifier", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put("Substance", ORDER_CodedDescription);
		CCR_ELEMENT_ORDER.put(CCRMangler.MANGLED_FORMNAME, ORDER_Form_Mangled);
		CCR_ELEMENT_ORDER.put("Form", ORDER_Form);
		CCR_ELEMENT_ORDER.put("Function", ORDER_Function);
		CCR_ELEMENT_ORDER.put("InformationSystem", ORDER_InformationSystem);
		

		
		
		for (String name : CCR_DATA_OBJECTS) {
			if (CCR_ELEMENT_ORDER.containsKey(name)) {
				List<String> order = new ArrayList<String>();
				order.addAll(CCR_DATA_OBJECT_ORDER);
				order.addAll(Arrays.asList(CCR_ELEMENT_ORDER.get(name)));
				CCR_ELEMENT_ORDER.put(name, order.toArray(new String[order
						.size()]));
			}
		}
	}

	/**
	 * These elements are inherently represented as lists, ie. a child with one of these
	 * names may appear multiple times under it's parent. This is used, for example,
	 * when returning JSON to automatically return an array for these elements.
	 */
	public static HashSet<String> CCR_LIST_OBJECTS = new HashSet<String>();

	static {
		CCR_LIST_OBJECTS.addAll(Arrays.asList(new String[] { "Actor",
				"ActorLink", "Reference", "ObjectAttribute" }));
		CCR_LIST_OBJECTS.addAll(CCR_DATA_OBJECTS);
	}

	public static final String EXACT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";

	/**
	 * Format used for parsing Zulu dates (dates known to be in GMT time zone)
	 */
	public static final String EXACT_DATE_TIME_ZULU_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	/**
	 * Shared (thread safe) formatter for formatting dates
	 */
	public static final FastDateFormat EXACT_DATE_TIME_FORMATTER = FastDateFormat
			.getInstance(EXACT_DATE_TIME_FORMAT);

	/**
	 * Pattern for testing if dates are in the "standard" CCR form including a time zone offset
	 */
	public static final Pattern CCR_DATE_PATTERN_WITH_TIME_ZONE = Pattern
			.compile("([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}[\\+-][0-9]{1,2}):([0-9]{2})");

	private boolean replaceOnMerge = false;

	/**
	 * @param arg0
	 * @param arg1
	 */
	protected CCRElement(String arg0, Namespace arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	protected CCRElement(String arg0, String arg1, String arg2) {
		super(arg0, arg1, arg2);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	protected CCRElement(String arg0, String arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	protected CCRElement(String arg0) {
		super(arg0, Namespace.getNamespace(CCR_NAMESPACE_URN));
	}

	protected CCRElement() {
		this.setNamespace(Namespace.getNamespace(CCR_NAMESPACE_URN));
	}

	/**
	 * Check if a CCRDataObjectID is a required field and if one is required
	 * but not present, create it.
	 */
	public String createObjectID() {
		if (CCR_DATA_OBJECTS.contains(this.getName())) {
			if (this.getChild("CCRDataObjectID") == null) {
				String objectId = generateObjectID();
				this.createPath("CCRDataObjectID", objectId);
				return objectId;
			}
		}
		return null;
	}
	
    /**
     * A utility method for generating ObjectIDs inside the CCR. This just
     * generates a random 16 digit number.
     * 
     * @return - a new, unique object id
     */
    public String generateObjectID() {
        // TODO: add validation of uniqueness
        String objId = "";
        for (int i = 0; i < 16; ++i) {
            int digit = (int) Math.floor(Math.abs(Math.random() * 10));
            objId += digit;
        }
        return objId;
    }
	

	public CCRElement getChild(String name) {
		return (CCRElement) super.getChild(name, this.namespace);
	}

	/**
	 * Search for a child of this item with the specified CCRDataObjectID
	 */
	public CCRElement getChildByObjectID(final String ccrDataObjectID) {
		// Find the PlanOfCare to delete
		Iterator<CCRElement> iter = this.getDescendants(new ElementFilter(
				"CCRDataObjectID") {
			public boolean matches(Object obj) {
				if (!super.matches(obj))
					return false;
				return ((CCRElement) obj).getTextTrim().equals(ccrDataObjectID);
			}
		});

		return iter.hasNext() ? iter.next().getParentElement() : null;
	}

	/**
	 * Return a CCR compatible format of the current system time
	 * @return
	 */
	public static String getCurrentTime() {
		// DateFormat df = new SimpleDateFormat(EXACT_DATE_TIME_FORMAT);
		//df.setTimeZone(TimeZone.getTimeZone("GMT"));
		//return df.format(new Date(System.currentTimeMillis()));
		return EXACT_DATE_TIME_FORMATTER.format(System.currentTimeMillis());
	}

	/**
	 * Return a CCR compatible format of the current system time
	 * @return
	 * @throws ParseException 
	 */
	public static Date parseDate(String date) throws ParseException {
		DateFormat df = new SimpleDateFormat(EXACT_DATE_TIME_ZULU_FORMAT);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.parse(date);
	}

	private static Pattern slashPattern = Pattern.compile("/");

	/**
	 * Attempts to create the given xpath value by recursively retrieving and creating
	 * each segment.
	 * 
	 * @param path - path to be created
	 * @param value - value to set (if any)
	 */
	public CCRElement createPath(String xpath, String value) {
		CCRElement segmentElement = this;
		String[] segments = slashPattern.split(xpath);
		for (String path : segments) {
			segmentElement = segmentElement.getOrCreate(path);
		}
		if(!eq(value,segmentElement.getText()))
		    segmentElement.setText(value);
		
		return segmentElement;
	}

	/**
	 * Attempts to create the given xpath value by recursively retrieving and creating
	 * each segment.
	 * 
	 * @param path - path to be created
	 * @param value - value to set (if any)
	 */
	public CCRElement createPath(String xpath, Content value) {
		CCRElement segmentElement = getPath(xpath);
		segmentElement.setContent(value);
		return segmentElement;
	}

	/**
	 * Attempts to resolve the given xpath value by recursively retrieving and creating
	 * each segment.  The target path is returned without modifying or setting it's content.
	 */
	public CCRElement getPath(String xpath) {
		CCRElement segmentElement = this;
		String[] segments = xpath.split("/");
		for (String path : segments)
			segmentElement = segmentElement.getOrCreate(path);
		return segmentElement;
	}
	
    /**
     * Attempts to find the value of given xpath by recursively retrieving 
     * each segment.  If any part of the path is not found, null is returned.
     * The target value is returned without modifying or setting it's content.
     */
    public String eval(String xpath) {
        CCRElement segmentElement = this;
        String[] segments = xpath.split("/");
        for (String path : segments) {
            if((segmentElement = segmentElement.getChild(path))==null)
                break;
        }
        return segmentElement != null ? segmentElement.getTextTrim() : null;
    }

	

	/**
	 * Attempts to retrieve a child element of the given name from the parent.
	 * If the child is not found, creates it.
	 * 
	 * @param parent -
	 *            element to retrieve/create child from/in
	 * @param name -
	 *            name of child element (assumed to be in CCR namespace)
	 *            ###### Assumed to be in the namespace of the parent?
	 * @return
	 */
	public CCRElement getOrCreate(String name) {
		CCRElement child = this.getChild(name);
		if (child == null) {
			child = el(name);
			child.createObjectID();
			this.addChild(child);
		}
		return child;
	}

	public CCRElement addChild(CCRElement child) {
		// If there is a defined order for children of this parent then 
		// use that order.
		if (CCRElement.CCR_ELEMENT_ORDER.get(this.getName()) != null)
			this.addChild(child, CCRElement.CCR_ELEMENT_ORDER.get(this
					.getName()));
		else
			super.addContent(child);

		if (this.getDocument() != null)
			this.getDocument().getDeletedElements().remove(child);
		return child;
	}

	/**
	 * Attempts to create the given element consistent with the order
	 * in the supplied array of element names.
	 * @param name
	 * @param afterName
	 * @return
	 */
	public CCRElement createChild(String name, String afterName[]) {

		CCRElement child = this.getChild(name);
		if (child == null) {
			child = el(name);
			child.createObjectID();
			log.debug("New child created for " + name);
			this.addChild(child, afterName);
		}
		return child;
	}

	public CCRElement addChild(CCRElement child, String afterName[]) {

		List thisContents = this.getContent();
		Iterator iter = thisContents.iterator();
		if (log.isDebugEnabled()) {
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof Element) {
					Element element = (Element) obj;
					// log.debug("Found element " + element.getName());
				} else
					log.debug("Non-element value:"
							+ obj.getClass().getCanonicalName());
			}
		}
		int index = -1;

		try {
			List<CCRElement> insertAfter = null;
			for (int i = 0; i < afterName.length; i++) {
				
				if (afterName[i].equals(child.getName()))
					break;
				
				insertAfter = this.getChildren(afterName[i], this.getNamespace());

				// Note that there may be multiple children that match - the
				// insertion should happen after the last one.
				if ((insertAfter != null) && (insertAfter.size() > 0)) {
					int insertIndex = index;
					for (int ii = 0; ii < insertAfter.size(); ii++) {
						//log.info("index " + ii + " of  " + insertAfter.size());
						
						int iIndex = this.indexOf(insertAfter.get(ii));
						
						if (log.isDebugEnabled()){
							log.debug("insertAfter size is " + insertAfter.size()
								+ " " + ii + " " + child.getName() + " "
								+ iIndex);
						}

						
						if (iIndex > insertIndex) {
							insertIndex = iIndex;

						}

					}

					if (insertIndex >= index)
						index = insertIndex;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			log.error("index out of bounds exception", e);
			log.error("Parent is " + this.toXMLString());
			log.error("Child is " + child.toXMLString());
		}

		if (log.isDebugEnabled()){
			log.debug("Will insert " + child.getName() + " after  index " + index);
		}
		if (index == -1)
			this.addContent(0, child);
		else
			this.addContent(index + 1, child);
		return (child);
	}

	/**
	 * Sets the given time in an ExactDateTime element as a child of this one.
	 * If there is an ApproximateDateTime child then it is removed.
	 * 
	 * @param timeMs
	 */
	public void setExactDateTime(long timeMs) {
		this.removeChild("ApproximateDateTime");
		getOrCreate("ExactDateTime").setText(
				EXACT_DATE_TIME_FORMATTER.format(timeMs));
	}

	/**
	 * Tries to parse the given date using the given format.  If it parses,
	 * creates an ExactDateTime for it and sets it as a child of this element.
	 * 
	 * @param dob
	 * @param dobElement
	 * @return - true if the format is parsed.
	 */
	private boolean trySetDateFormat(String dob, String format) {
		SimpleDateFormat df;
		Date d;
		df = new SimpleDateFormat(format);
		try {
			if ((d = df.parse(dob)) != null) {
				this.removeChild("ApproximateDateTime");
				this.getOrCreate("ExactDateTime").setText(dob);
				return true;
			}
		} catch (ParseException e) {
			// didn't parse, try next format
		}
		return false;
	}

	public void setDate(String dateText) {
		// Try and parse in some different formats that the user may have entered
		SimpleDateFormat df = null;
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		df.setLenient(true);
		Date d;
		try {
			if (df.parse(dateText) != null) {
				this.removeChild("ApproximateDateTime", namespace);
				getOrCreate("ExactDateTime").setText(dateText);
			}
		} catch (ParseException e) {
			// didn't parse, try next format
		}

		if (trySetDateFormat(dateText, "yyyy-MM-dd'T'HH:mm:ss"))
			return;

		// not part of CCR Spec, but standard US date format
		if (trySetDateFormat(dateText, "MM-dd-yyyy"))
			return;

		df = new SimpleDateFormat("MM/dd/yyyy");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		df.setLenient(true);
		try {
			if ((d = df.parse(dateText)) != null) {
				this.removeChild("ApproximateDateTime");
				df = new SimpleDateFormat("yyyy-MM-dd");
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
				getOrCreate("ExactDateTime").setText(df.format(d));
				return;
			}
		} catch (ParseException e) {
			// didn't parse, try next format
		}

		if (trySetDateFormat(dateText, "yyyy-MM-dd"))
			return;

		if (trySetDateFormat(dateText, "yyyy"))
			return;

		// Not a parseable format?  Just set it as approximate
		log.info("Date " + dateText
				+ " not parseable.  Setting as ApproximateDateTime");
		this.removeChild("ExactDateTime", namespace);
		this.createPath("ApproximateDateTime/Text", dateText);
	}

	public boolean removeChild(String name) {
		if (this.getDocument() != null)
			this.getDocument().getDeletedElements().add(getChild(name));
		
		notifyModified();
		
		return super.removeChild(name, this.namespace);
	}

	public String toXml() {
		try {
			StringWriter sw = new StringWriter();
			new XMLOutputter().output(this, sw);
			return (sw.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String queryTextProperty(String path) throws PHRException {
	    
	    // This error can be confusing so hopefully make it less cryptic 
	    if(this.getDocument() == null) 
	        throw new PHRException("Cannot query xpaths on elements detached from document!");

		try {
			XPathCache xpath = this.getDocument().getXPathCache();
            Object pathResult = xpath
					.getXPathResult(this, path, Collections.EMPTY_MAP, false);

			if (pathResult == null)
				return null;

			if (pathResult instanceof Attribute) {
				return ((Attribute) pathResult).getValue();
			} else if (pathResult instanceof Element) {
				return ((Element) pathResult).getTextTrim();
			} else if (pathResult instanceof String) {
				return (String) pathResult;
			} else
				throw new PHRException("Unknown result " + pathResult
						+ " of unexpected type "
						+ pathResult.getClass().getName()
						+ " returned for property " + path);
		} catch (JDOMException e) {
			throw new PHRException("Unable to query for path " + path
					+ " on element " + this, e);
		}
	}

	public CCRElement queryProperty(String path) throws PHRException {
		XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");
		try {
			return (CCRElement) xpath.getElement(this, path);
		} catch (JDOMException e) {
			throw new PHRException("Unable to query path " + path, e);
		}
	}

	public String getElementValue() {
		return this.getTextTrim();
	}

	public void setElementValue(String value) {
		this.setText(value);
	}
	
	public Element setText(String value) {
		super.setText(value);
		notifyModified();
		return this;
	}
	
	protected void notifyModified() {
	    if(this.getDocument() != null)
		    this.getDocument().setModified(true);
	}

	public PHRElement addChild(PHRElement child) {
		if (child instanceof CCRElement) {
		    notifyModified();
			return this.addChild((CCRElement) child);
		} else
			throw new IllegalArgumentException(
					"Cannot add non-CCRElement as child of CCRElement");
	}

	public PHRElement createProperty(String property) throws PHRException {

		try {
			XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");

			if ("ccrPurpose".equals(property)) {
				return this.getRoot().getOrCreate("Purpose").getOrCreate(
						"Description").getOrCreate("Text");
			}

			//Element root = this.getJDOMDocument().getRootElement();
			if ("patientAge".equals(property)) {
				CCRElement patientDob = this.createPatientDateOfBirth();

				// Add age
				CCRElement age = patientDob.getOrCreate("Age");
				CCRElement value = age.getOrCreate("Value");
				//getOrCreate(age, "Units").setText("Years");
				return value;
			}

			if ("patientGender".equals(property)) {
				CCRElement person = this.getPatientPerson();
				return person.getOrCreate("Gender").getOrCreate("Text");
			}

			if ("patientExactDateOfBirth".equals(property)) {
				CCRElement patientDob = this.createPatientDateOfBirth();
				return patientDob.getOrCreate("ExactDateTime");
			}

			// TODO: possibly should NOT allow this to be created
			// - a user creating this doesn't really make sense.
			// Logic should be:
			// If a DICOMPatient identifier exists - test for equality.
			// If it is the same - don't change anything.
			// If it's new - then append a new one.
			// If there isn't one -add a new <IDs>.
			// Never delete/pave over old one.
			if ("patientDICOMId".equals(property)) {
				CCRElement patient = getPatientActor();
				CCRElement dicomID = (CCRElement) xpath.getElement(this
						.getDocument(), "patientDICOMId");
				if (dicomID != null) {
					return dicomID;
				} else {
					CCRElement ids = el("IDs");
					ids.createPath("Type/Text", "DICOM Patient Id");
					String fromActorId = this.getFromActor().getChildText(
							"ActorObjectID");
					CCRElement id = el("ID");
					ids.addChild(id);
					ids.createPath("Source/Actor/ActorID", fromActorId);
					patient.addChild(ids);
					return id;
				}
			}

			if ("patientAddress1".equals(property)) {
				CCRElement patient = this.getPatientActor();
				return patient.getOrCreate("Address").getOrCreate("Line1");
			}

			if ("patientCity".equals(property)) {
				CCRElement patient = this.getPatientActor();
				return patient.getOrCreate("Address").getOrCreate("City");
			}

			if ("patientState".equals(property)) {
				CCRElement patient = this.getPatientActor();
				return patient.getOrCreate("Address").getOrCreate("State");
			}

			if ("patientPostalCode".equals(property)) {
				CCRElement patient = this.getPatientActor();
				return patient.getOrCreate("Address").getOrCreate("PostalCode");
			}

			if ("patientCountry".equals(property)) {
				CCRElement patient = this.getPatientActor();
				return patient.getOrCreate("Address").getOrCreate("Country");
			}

			if ("purposeText".equals(property)) {
				return this.createPurposeText("User Comment");
			}

			if ("objectiveText".equals(property)) {
				return this.createPurposeText("Objective");
			}

			if ("assessmentText".equals(property)) {
				return this.createPurposeText("Assessment");
			}

			if ("planText".equals(property)) {
				return this.createPurposeText("Plan");
			}

			if ("toEmail".equals(property)) {
				CCRElement toActor = this.getToActor();
				return toActor.getOrCreate("EMail").getOrCreate("Value");
			}

			if ("toEmail".equals(property)) {
				CCRElement toActor = this.getToActor();
				return toActor.getOrCreate("EMail").getOrCreate("Value");
			}

			if ("patientEmail".equals(property)) {
				CCRElement patient = this.getPatientActor();
				return patient.getOrCreate("EMail").getOrCreate("Value");
			}

			if ("sourceEmail".equals(property)) {
				CCRElement from = this.getPrimaryFromActor();
				if (from == null) {
					from = this.getFromActor();
				}
				return from.getOrCreate("EMail").getOrCreate("Value");
			}

			log.info("checking for property " + property);
			if("sourceFamilyName".equals(property)) { 
				CCRElement from = this.getPrimaryFromActor(); 
				if (from == null) {
					from = this.getFromActor();
				}
				return from.getOrCreate("Person").getOrCreate("Name").getOrCreate("CurrentName").getOrCreate("Family");
			}
			if("sourceGivenName".equals(property)) {
				CCRElement from = this.getPrimaryFromActor();
				if (from == null) {
					from = this.getFromActor();
				}
				return from.getOrCreate("Person").getOrCreate("Name").getOrCreate("CurrentName").getOrCreate("Given");
			}
	
			if ("patientGivenName".equals(property)) {
				CCRElement patient = this.getPatientPerson();
				return patient.getOrCreate("Name").getOrCreate("CurrentName")
						.getOrCreate("Given");
			}

			if ("patientFamilyName".equals(property)) {
				CCRElement patient = this.getPatientPerson();
				return patient.getOrCreate("Name").getOrCreate("CurrentName")
						.getOrCreate("Family");
			}

			if ("patientMiddleName".equals(property)) {
				CCRElement patient = this.getPatientPerson();
				return patient.createPath("Name/CurrentName/Middle", "");
			}

			if ("patientPhoneNumber".equals(property)) {
				CCRElement patient = this.getPatientActor();
				return patient.createPath("Telephone/Value", "");
			}

			if ("patientMedCommonsId".equals(property)) {
				CCRElement patient = this.getPatientActor();
				CCRElement ids = new CCRElement("IDs");
				ids.createPath("Type/Text", MEDCOMMONS_ACCOUNT_ID_TYPE);
				patient.addChild(ids);
				return ids.getOrCreate("ID");
			}
		} catch (JDOMException e) {
			throw new PHRException("Unable to create property " + property, e);
		} catch (IOException e) {
			throw new PHRException("Unable to create property " + property, e);
		} catch (InvalidCCRException e) {
			throw new PHRException("Unable to create property " + property, e);
		}

		throw new PHRException("Unknown property " + property
				+ " specified in property create operation");
	}

	/**
	 * Finds or creates the Patient "Person" element and returns it.
	 * @throws InvalidCCRException 
	 */
	private CCRElement getPatientPerson() throws JDOMException, IOException,
			InvalidCCRException {
		CCRElement patientActor = this.getPatientActor();
		if (patientActor == null) {
			throw new InvalidCCRException(
					"Unable to locate Patient Actor.  Patient Actor is required by CCR Standard.");
		}
		CCRElement patientPerson = patientActor.getOrCreate("Person");
		//getOrCreate(patientActor, "Person", new String[]{ "Source" });
		return patientPerson;
	}

	private CCRElement createPatientDateOfBirth() throws JDOMException,
			IOException {
		return getPatientActor().getOrCreate("Person").getOrCreate(
				"DateOfBirth");
	}

	/**
	 * Finds and returns the patient Actor for this CCR
	 */
	private CCRElement getPatientActor() throws JDOMException, IOException {
		XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");
		CCRElement patientActor = (CCRElement) xpath.getElement(this
				.getDocument(), "patientActor");
		return patientActor;
	}

	public CCRElement getRoot() {
		return (CCRElement) this.getDocument().getRootElement();
	}

	/**
	 * Returns the From actor, creating one if there is not currently an existing one 
	 * @return
	 * @throws JDOMException 
	 */
	private CCRElement getFromActor() throws PHRException {
		try {
			if (this.getDocument().getRootElement().getChild("From") == null) {
				CCRElement actor = this.createActor();
				this.getRoot().createPath("From/ActorLink/ActorID",
						actor.getChildText("ActorObjectID"));
				this.getRoot().getChild("Actors").addChild(actor);
				return actor;
			} else {
				XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");
				return (CCRElement) xpath.getElement(this.getDocument(),
						"fromActor");
			}
		} catch (JDOMException e) {
			throw new PHRException("Unable to create From actor", e);
		}
	}

	/**
	 * Returns the To actor, creating one if there is not currently an existing one 
	 * @throws JDOMException 
	 */
	private CCRElement getToActor() throws JDOMException {
		if (this.getDocument().getRootElement().getChild("To", namespace) == null) {
			CCRElement actor = this.createActor();
			this.getRoot().createPath("To/ActorLink/ActorID",
					actor.getChildText("ActorObjectID"));
			this.getRoot().getChild("Actors").addChild(actor);
			return actor;
		} else {
			XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");
			return (CCRElement) xpath.getElement(this.getDocument(), "toActor");
		}
	}

	/**
	 * Create an actor with default fields initialized
	 */
	protected CCRElement createActor() {
		CCRElement actor = el("Actor");
		String objId = this.generateObjectID();
		actor.createPath("ActorObjectID", objId);
		actor.createPath("InformationSystem/Name", "Medcommons Notification");
		actor.createPath("InformationSystem/Type", "Repository");
		actor.createPath("EMail/Value", "");
		actor.createPath("Source/Actor/ActorID", objId);
		return actor;
	}
	
    public CCRElement el(String name) {
        return CCRElementFactory.instance.element(name);
    }

	/**
	 * Creates a comment field in the CCR for the MedCommons Comment.
	 * 
	 * @param type - the type of comment being set.  
	 */
	public CCRElement createPurposeText(String type) throws PHRException {
		// Create purpose text
		CCRElement comment = this.getRoot().getOrCreate("Comments").addChild(
				new CCRElement("Comment"));

		// Add an object id
		comment.createPath("CommentObjectID", generateObjectID());

		String dateToSet = CCRElement.getCurrentTime();
		comment.createPath("DateTime/ExactDateTime", dateToSet);

		// Add the comment type
		comment.createPath("Type/Text", "MedCommons " + type);

		// create the comment node itself
		CCRElement purposeText = comment.createPath("Description/Text", "");

		// Set the From actor as the source
		String fromActorID = this.getFromActor().getChildTextTrim(
				"ActorObjectID");
		comment.createPath("Source/ActorID", fromActorID);
		return purposeText;
	}

	/**
	 * Searches for the first From actorLink that links to an actor with an
	 * email address and returns the ActorLink element.
	 * 
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public CCRElement getPrimaryFromActor() throws JDOMException, IOException {

		// Get the from node, if it exists
		Element fromNode = this.getRoot().getChild("From");

		if (fromNode == null)
			return null;

		XPathCache xpath = (XPathCache) Spring.getBean("ccrXPathCache");

		// For each actorlink
		Iterator actorLinks = fromNode.getDescendants(new ElementFilter(
				"ActorLink", namespace));
		Map<String, String> params = new HashMap<String, String>();
		for (Iterator iter = fromNode.getDescendants(new ElementFilter(
				"ActorLink", namespace)); iter.hasNext();) {
			CCRElement actorLink = (CCRElement) iter.next();

			// Does this actor have an email address
			String actorObjId = actorLink.getChildText("ActorID", namespace);
			params.put("actorId", actorObjId);
			if (actorObjId != null) {
				Element emailElement = xpath.getElement(this.getDocument(),
						"emailFromActorID", params);
				if (emailElement != null) {
					if (!StringUtil.blank(emailElement.getText())) {
						return actorLink;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns a JSON representation of this element
	 */
	public String getJSON() {
		StringBuilder result = new StringBuilder("{");
		Iterator<CCRElement> i = this.getChildren().iterator();
		HashSet<String> processed = new HashSet<String>();
		while (i.hasNext()) {

			CCRElement c = i.next();

			String elementName = c.getName();
			if (processed.contains(elementName))
				continue;

			if (result.length() > 1)
				result.append(",");

			// HACK: Stop CCR getting rendered as 'cCR'
			if (elementName.startsWith("CCR")) {
				result.append("ccr");
				result.append(elementName.substring(3));
			} else {
				result.append(Character.toLowerCase(elementName.charAt(0)));
				result.append(elementName.substring(1));
			}
			result.append(":");
			if (CCR_LIST_OBJECTS.contains(elementName)) {
				result.append("[");
				for (ListIterator ci = this.getChildren(elementName, namespace)
						.listIterator(); ci.hasNext();) {
					if (result.charAt(result.length() - 1) != '[')
						result.append(",");

					CCRElement cc = (CCRElement) ci.next();
					cc.generateJSON(result);
				}
				result.append("]");
				processed.add(elementName);
			} else
				c.generateJSON(result);
		}
		result.append("}");
		return result.toString();
	}

	private void generateJSON(StringBuilder result) {
		if (this.getChildren().size() == 0) {
			result.append("'");
			result.append(StringUtil.escapeForJavaScript(this.getValue()));
			result.append("'");
		} else {
			result.append(this.getJSON());
		}
	}

	
	@Override
	public CCRElement getParentElement() {
		Parent p = super.getParentElement();
		if (p instanceof CCRElement)
			return (CCRElement) p;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public List<CCRElement> getChildren(String name) {
		return super.getChildren(name, Namespace
				.getNamespace(CCR_NAMESPACE_URN));
	}

	public PHRElement removeChild(PHRElement child) {
		int index = this.indexOf((CCRElement) child);
		if (index >= 0) {
			this.removeContent(index);
			notifyModified();
			if (this.getDocument() != null)
				this.getDocument().getDeletedElements().add(child);
			return child;
		}
		return null;
	}

	public XMLPHRDocument getDocument() {
		return (XMLPHRDocument) super.getDocument();
	}

	public void replaceChild(PHRElement oldChild, PHRElement newChild) {
		int index = this.indexOf((Content) oldChild);
		this.setContent(index, (Content) newChild);
		notifyModified();
		this.getDocument().getDeletedElements().add(oldChild);
	}

	public boolean removeContent(Content child) {
		boolean result = super.removeContent(child);
		if (result) {
			if (this.getDocument() != null) {
				if (child instanceof PHRElement) {
					PHRElement phrChild = (PHRElement) child;
					this.notifyModified();
					this.getDocument().getDeletedElements().add(phrChild);
				}
			}
		}
		return result;
	}

	public String toXMLString() {
		String xmlString = null;
		Format utfOutputFormat = Format.getPrettyFormat();
		utfOutputFormat.setEncoding("UTF-8");
		try {
			StringWriter sw = new StringWriter();
			new XMLOutputter(utfOutputFormat).output(this, sw);
			xmlString = sw.toString();

		} catch (IOException e) {
			log.error("error", e);
		}
		return (xmlString);

	}

	public void setReplaceOnMerge(boolean replaceOnMerge) {
		this.replaceOnMerge = replaceOnMerge;
	}

	public boolean getReplaceOnMerge() {
		return (this.replaceOnMerge);
	}
}
