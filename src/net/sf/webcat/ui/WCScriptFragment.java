/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU Affero General Public License as published
 |  by the Free Software Foundation; either version 3 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU Affero General Public License
 |  along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package net.sf.webcat.ui;

import net.sf.webcat.ui.util.DojoUtils;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import er.extensions.appserver.ERXResponseRewriter;

// ------------------------------------------------------------------------
/**
 * An element whose content is a fragment of JavaScript code that should be
 * inserted in some special way into the page content. The {@code scriptName}
 * binding determines where the fragment should be inserted.
 * <p>
 * Currently the only supported value for {@code scriptName} is
 * "{@code onLoad}", which causes the script code to be appended to a global
 * {@code dojo.addOnLoad} block (creating it if it does not yet exist). If the
 * value of {@code scriptName} is anything else, or null, then the script will
 * be appended at the end of the &lt;head&gt; tag content.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class WCScriptFragment extends WODynamicGroup
{
    //~ Constructor ...........................................................
    
    // ----------------------------------------------------------
    /**
     * Creates a new instance of the WCScriptFragment class.
     * 
     * @param name 
     * @param someAssociations 
     * @param template 
     */
    public WCScriptFragment(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);
        
        _scriptName = someAssociations.objectForKey("scriptName");
    }
    

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the value of the scriptName binding in the specified context.
     * 
     * @param context the context
     * @return the value of the scriptName binding
     */
    protected String scriptNameInContext(WOContext context)
    {
        if (_scriptName != null)
            return _scriptName.valueInComponent(context.component()).toString();
        else
            return null;
    }
    
    
    // ----------------------------------------------------------
    /**
     * Overriden to cause the component content (which should be JavaScript
     * code) to be inserted into the script specified by the "scriptName"
     * association.
     * 
     * @param response the response
     * @param context the context
     */
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        WOResponse contentResponse = new WOResponse();
        super.appendToResponse(contentResponse, context);
        
        String scriptName = scriptNameInContext(context);
        String script = contentResponse.contentString();
        
        if ("onLoad".equalsIgnoreCase(scriptName))
        {
            DojoUtils.addScriptCodeToOnLoad(response, context, script);
        }
        else
        {
            ERXResponseRewriter.addScriptCodeInHead(response, context, script);
        }
    }
    
    
    //~ Static/instance variables .............................................

    protected WOAssociation _scriptName;
}
