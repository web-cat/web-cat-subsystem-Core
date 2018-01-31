/*==========================================================================*\
 |  $Id: WCStylesheetFragment.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

package org.webcat.ui;

import org.webcat.ui.util.DojoUtils;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;

//--------------------------------------------------------------------------
/**
 * Allows subcomponents on a page to inject any CSS that they need into the
 * head of the page. The filename and framework bindings can be used to inject
 * a WebServerResources-relative stylesheet file; alternatively, component
 * content will be injected as inline CSS.
 * 
 * @author Tony Allevato
 * @version $Id: WCStylesheetFragment.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class WCStylesheetFragment extends WODynamicGroup
{
    //~ Constructor ...........................................................
    
    // ----------------------------------------------------------
    /**
     * Creates a new instance of the WCStylesheetFragment class.
     * 
     * @param name 
     * @param someAssociations 
     * @param template 
     */
    public WCStylesheetFragment(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);
        
        _filename = someAssociations.objectForKey("filename");
        _framework = someAssociations.objectForKey("framework");
        _media = someAssociations.objectForKey("media");
    }
    

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    protected String filenameInContext(WOContext context)
    {
        if (_filename != null)
            return _filename.valueInComponent(context.component()).toString();
        else
            return null;
    }
    
    
    // ----------------------------------------------------------
    protected String frameworkInContext(WOContext context)
    {
        if (_framework != null)
            return _framework.valueInComponent(context.component()).toString();
        else
            return null;
    }
    
    
    // ----------------------------------------------------------
    protected String mediaInContext(WOContext context)
    {
        if (_media != null)
            return _media.valueInComponent(context.component()).toString();
        else
            return null;
    }
    
    
    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        String filename = filenameInContext(context);
        if (filename != null)
        {
            String framework = frameworkInContext(context);
            
            if (framework == null)
            {
                framework = NSBundle.bundleForClass(
                        context.component().getClass()).name();
            }
            
            String media = mediaInContext(context);

            String url;
            if (filename.indexOf("://") != -1 || filename.startsWith("/"))
            {
                url = filename;
            }
            else
            {
                WOResourceManager rm =
                    WOApplication.application().resourceManager();
                NSArray<String> languages = null;
                if (context.hasSession())
                {
                    languages = context.session().languages();
                }

                url = rm.urlForResourceNamed(filename, framework, languages,
                        context.request());
            }

            DojoUtils.addStylesheetLinkInHead(response, context, url, media);
        }

        if (hasChildrenElements())
        {
            WOResponse contentResponse = new WOResponse();
            super.appendToResponse(contentResponse, context);
            
            String inlineCss = contentResponse.contentString();            
            DojoUtils.addInlineCssInHead(response, context, inlineCss);
        }
    }
    
    
    //~ Static/instance variables .............................................

    protected WOAssociation _filename;
    protected WOAssociation _framework;
    protected WOAssociation _media;
}
