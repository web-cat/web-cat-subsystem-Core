package net.sf.webcat.core;

import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.foundation.NSMutableDictionary;

import ognl.helperfunction.WOTagProcessor;

/**
 * Adds support for <wo:adiv> and <wo:aspan> tag shortcuts.  Both tags
 * represent an AjaxUpdateContainer; adiv adds no special attributes, but
 * aspan is a shortcut for adding elementName="span" to the tag.
 * 
 * @author Tony Allevato
 */
public class AjaxUpdateContainerTagProcessor extends WOTagProcessor
{
	@SuppressWarnings("unchecked")
	public WODeclaration createDeclaration(String elementName,
			String elementType, NSMutableDictionary associations)
	{
		if(associations.objectForKey("elementName") != null)
		{
			throw new IllegalArgumentException("The " + elementType +
					" tag implies the appropriate elementName attribute; " +
					"do not specify one.");
		}

		if(elementType.equals("aspan"))
		{
			associations.setObjectForKey(
					new WOConstantValueAssociation("span"), "elementName");
		}
		
		return super.createDeclaration(elementName, "AjaxUpdateContainer",
				associations);
	}
}
