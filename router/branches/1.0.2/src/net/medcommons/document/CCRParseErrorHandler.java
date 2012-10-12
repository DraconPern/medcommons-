package net.medcommons.document;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * SAX error parsing handler for the CCR.
 * <P>
 * Errors are not thrown; they are simply accumulated. Accessors permit a
 * calling program to determine if a warning/error/fatal error occurred; the
 * messages for each type of messages is individually retrievable.
 * <P>
 * Display of the errors need some care. It's probably the case that if there
 * are fatal errors that the warning messages need not be displayed. This needs
 * to be determined empirically to see what makes sense for a user.
 * <P>
 * Note that the error handler does *not* throw any errors. Errors are
 * noted, accumulated, and return. This may mean that there for a CCR with many
 * errors that a long time is taken parsing/accumulating errors. In the future
 * perhaps an option is needed for throwing an exception on (say) the first fatal
 * error to prevent a bunch of useless messages from being generated.
 * @author sean
 * 
 */
public class CCRParseErrorHandler implements ErrorHandler {
	private StringBuffer warningMessages = null;

	private StringBuffer errorMessages = null;

	private StringBuffer fatalMessages = null;

	private boolean parseErrors = false;

	private boolean parseWarnings = false;

	private boolean parseFatal = false;

	/**
	 * Handles parse warning exception.
	 */
	public void warning(SAXParseException exception) {

		parseWarnings = true;
		if (warningMessages == null)
			warningMessages = new StringBuffer();
		warningMessages.append("\nWarning: " + exception.getMessage());
		warningMessages.append(" at line " + exception.getLineNumber()
				+ ", column " + exception.getColumnNumber());
		warningMessages.append(" in entity " + exception.getSystemId());

	}

	/**
	 * Handles parse error message.
	 */
	public void error(SAXParseException exception) {
		parseErrors = true;
		if (errorMessages == null)
			errorMessages = new StringBuffer();
		errorMessages.append("\nError: " + exception.getMessage());
		errorMessages.append(" at line " + exception.getLineNumber()
				+ ", column " + exception.getColumnNumber());
		errorMessages.append(" in entity " + exception.getSystemId());

	}

	/**
	 * Handles parse fatal message.
	 */
	public void fatalError(SAXParseException exception) {
		parseFatal = true;
		if (fatalMessages == null)
			fatalMessages = new StringBuffer();
		fatalMessages.append("\nFatal Error: " + exception.getMessage());
		fatalMessages.append(" at line " + exception.getLineNumber()
				+ ", column " + exception.getColumnNumber());
		fatalMessages.append(" in entity " + exception.getSystemId());

	}

	/**
	 * Returns true if there were parse warnings, false otherwise.
	 * 
	 * @return
	 */
	public boolean getParseWarnings() {
		return (parseWarnings);
	}

	/**
	 * Returns true if there were parse errors, false otherwise.
	 * 
	 * @return
	 */
	public boolean getParseErrors() {
		return (parseErrors);
	}

	/**
	 * Returns true if there were fatal parse errors, false otherwise.
	 * 
	 * @return
	 */
	public boolean getParseFatal() {
		return (parseFatal);
	}

	/**
	 * Returns warning messages or null if there were no warning messages.
	 * 
	 * @return
	 */
	public String getWarningMessages() {
		if (!parseWarnings)
			return (null);
		else
			return (warningMessages.toString());
	}

	/**
	 * Returns error messages or null if there were no error messages.
	 * 
	 * @return
	 */
	public String getErrorMessages() {
		if (!parseErrors)
			return (null);
		else
			return (errorMessages.toString());
	}

	/**
	 * Returns fatal parsing messages or null if there were no fatal messsages.
	 * 
	 * @return
	 */
	public String getFatalMessages() {
		if (!parseFatal)
			return (null);
		else
			return (fatalMessages.toString());
	}
}