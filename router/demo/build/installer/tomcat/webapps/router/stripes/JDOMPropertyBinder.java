/*
 * $Id$
 * Created on 26/06/2006
 */
package net.medcommons.router.web.stripes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.medcommons.modules.configuration.Configuration;
import net.medcommons.modules.utils.Str;
import net.medcommons.phr.PHRException;
import net.medcommons.phr.db.xml.XPathCache;
import net.medcommons.router.services.wado.CCROperationException;
import net.medcommons.router.util.struts.JDomForm;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.ParameterName;
import net.sourceforge.stripes.controller.StripesConstants;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class JDOMPropertyBinder extends DefaultActionBeanPropertyBinder {
    private static Set<String> SPECIAL_KEYS = new HashSet<String>();

    static {
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_SOURCE_PAGE);
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_FIELDS_PRESENT);
        SPECIAL_KEYS.add(StripesConstants.URL_KEY_FLASH_SCOPE_ID);
    }
    
    /**
     * Logger to use with this class
     */
    private static Logger log = Logger.getLogger(JDOMPropertyBinder.class);
    
    private XPathCache xpath = (XPathCache) Configuration.getBean("ccrXPathCache");

    public JDOMPropertyBinder() {
        super();
    }
    
    /**
     * <p>Loops through the parameters contained in the request and attempts to bind each one to the
     * supplied ActionBean.  Invokes validation for each of the properties on the bean before
     * binding is attempted.  Only fields which do not produce validation errors will be bound
     * to the ActionBean.</p>
     *
     * <p>Individual property binding is delegated to the other interface method,
     * bind(ActionBean, String, Object), in order to allow for easy extension of this class.</p>
     *
     * @param bean the ActionBean whose properties are to be validated and bound
     * @param context the ActionBeanContext of the current request
     * @param validate true indicates that validation should be run, false indicates that only
     *        type conversion should occur
     */
    public ValidationErrors bind(ActionBean bean, ActionBeanContext context, boolean validate) {
        HttpServletRequest request = bean.getContext().getRequest();
        
        ValidationErrors errors = super.bind(bean,context,validate);
        
        if (bean instanceof CCRActionBean) {
            CCRActionBean ccrBean = (CCRActionBean)bean;
            JDomForm jdomForm = ccrBean.getCCRForm();
            Document doc = null;
            try {                
                if(jdomForm != null)
                    doc = jdomForm.getDocument(request); 
            }
            catch (CCROperationException e) {
                log.error("Unable to get JDOM Document for JDOMForm " + jdomForm.getClass(), e);
            }
            catch (PHRException e) {
                log.error("Unable to get JDOM Document for JDOMForm " + jdomForm.getClass(), e);
            }
            
            if(doc != null) {                
                ValidationErrors fieldErrors = context.getValidationErrors();
        
                // Take the ParameterMap and turn the keys into ParameterNames
                Map<ParameterName,String[]> parameters = getParameters(bean);
        
                // Run the required validation first to catch fields that weren't even submitted
                if (validate) {
                    validateRequiredFields(parameters, bean, fieldErrors);
                }
        
                // Converted values for all fields are accumulated in this map to make post-conversion
                // validation go a little easier
                Map<ParameterName,List<Object>> allConvertedFields = new HashMap<ParameterName,List<Object>>();
        
                ActionBeanContext tmpCtx = jdomForm.getContext();
                jdomForm.setContext(context);
                
                // First we bind all the regular parameters
                for (Map.Entry<ParameterName,String[]> entry : parameters.entrySet() ) {
                    List<Object> convertedValues = null;
                    ParameterName name = entry.getKey();
                    String pname = name.getName(); // exact name of the param in the request
                    
                        log.debug("Setting properties on JDOM Document " + doc.hashCode());
                        try {
                            if (jdomBind(jdomForm, doc, pname, request.getParameter(pname)))
                                jdomForm.setModified(context.getRequest());
                        }
                        catch (Exception e) {
                            log.info("Failed to set xpath value " + pname + " with exception " + e.toString());
                        }
                        try {
                            super.bind(jdomForm, pname, request.getParameter(pname));
                        }
                        catch (Exception e) {
                            log.debug("Failed to set OGNL property on jdomForm with exception " + e.toString());
                        }
                }                    
                jdomForm.setContext(tmpCtx);
            }            
            else
                log.debug("No JDOM Document available - form " + jdomForm + " not being populated with xpath");            
        }

        return errors;
    }
    
    public boolean jdomBind(JDomForm jdomForm, Document doc, String name, String newValue) {
        boolean modified = false;
        String xPath = name.replace('.','/');
        try {
            Element element = xpath.getElement(doc, xPath);
            if(element == null) {
                // Bug #442 - prevent creating "empty" elements - only create if the
                // user actually specified them
                if(!Str.blank(newValue)) {
                    element = jdomForm.createPath(xPath);
                }
            }
            if(element!=null && !xpath.isMultiValued(xPath)) {
                if(log.isDebugEnabled()) {
                    log.debug("Setting xpath " + xPath + " to value " + newValue + " on document " + doc.hashCode());
                }
                String oldValue = element.getText();
                
                if(oldValue != null) {
                    if(!oldValue.equals(newValue)) {
                        // jdomForm.setModified(request);
                        modified = true;
                    }
                }
                element.setText(newValue);
            }
        }
        catch (JDOMException e) {
            log.debug("Failed to locate XPath expression " + xPath + ": " + e.getMessage());
        }
        catch (PHRException e) {
            log.debug("Failed to locate XPath expression " + xPath + ": " + e.getMessage());
        }
        return modified;
    }

/*    protected void bindNonNullValue(ActionBean bean,
                                    String property,
                                    List<Object> valueOrValues,
                                    Class targetType) throws Exception {
        
        super.bindNonNullValue(bean,property,valueOrValues, targetType);
        
        Class valueType = valueOrValues.iterator().next().getClass();

        // If the target type is an array, set it as one, otherwise set as scalar
        if (targetType.isArray() && !valueType.isArray()) {
            Object[] typedArray = (Object[]) Array.newInstance(valueType, valueOrValues.size());
            OgnlUtil.setValue(property, bean, valueOrValues.toArray(typedArray));
        }
        else if (Collection.class.isAssignableFrom(targetType) &&
                !Collection.class.isAssignableFrom(valueType)) {
            Collection collection = null;
            if (targetType.isInterface()) {
                collection = (Collection) ReflectUtil.getInterfaceInstance(targetType);
            }
            else {
                collection = (Collection) targetType.newInstance();
            } 

            collection.addAll(valueOrValues);
            OgnlUtil.setValue(property, bean, collection);
        }
        else {
            this.bind(bean, property, valueOrValues.get(0));
        }
    }   
    */
}
