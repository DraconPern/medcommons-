package net.medcommons.router.services.ccrmerge;

import net.medcommons.phr.ccr.CCRElement;

public class StructuredProductTypeMerger extends CCRCodedDataObjectTypeMerger {
///ContinuityOfCareRecord/Body[1]/Medications[1]/Medication[4]/Product[1]/ProductName[1]/Text[1]
	public String getProductName(CCRElement element){
		String productName = null;
		CCRElement productElement = element.getChild("Product");
		if (productElement != null){
			CCRElement productNameElement = productElement.getChild("ProductName");
			if (productNameElement !=null){
				CCRElement productNameText = productNameElement.getChild("Text");
				if (productNameText != null){
					productName = productNameText.getTextNormalize();
					if (productName != null){
						if ("".equals(productName)){
							productName = null; // Protection against blank <ProductName><Text>
						}
					}
				}
			}
		}
		return(productName);
	}
	
	
	public boolean match(CCRElement from, CCRElement to) {
		boolean matches = super.match(from, to);
		
		if (matches){
			if (to.getReplaceOnMerge())
				return (true); // No further testing needed - it matches. 
			else{
			 // Test to see that the product name matches to see if they are equal.
			    String fromTest = getProductName(from);
			    String toTest = getProductName(to);
			    if (fromTest != null){
			        matches = ElementUtils.isEqualNotNull(getProductName(from), getProductName(to));
			    }
			    
			}
			    
		}
		
		
		return(matches);
	}
}
