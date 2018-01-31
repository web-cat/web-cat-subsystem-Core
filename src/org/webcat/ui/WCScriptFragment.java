/*==========================================================================*\
 |  $Id: WCScriptFragment.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;
import er.extensions.appserver.ERXResponseRewriter;

// ------------------------------------------------------------------------
/**
 * <p>
 * An element whose content is a fragment of JavaScript code that should be
 * inserted in some special way into the page content. The {@code location}
 * binding determines where the fragment should be inserted.
 * </p>
 * <h2>Bindings</h2>
 * <dl>
 * <dt>location</dt>
 * <dd>The location in the page at which to insert the script; this is only
 * relevant when the element is used on a new page load, not as part of an
 * Ajax response. Valid choices are "onLoad", which inserts the script into the
 * page's <tt>onLoad</tt> script, and "head", which inserts the script into the
 * head tag. Any other value, or null, inserts the script at the location of
 * the WCScriptFragment element.</dd>
 * <dt>type</dt>
 * <dd>The MIME type of the script. In most cases this can be omitted, as the
 * default value of "text/javascript" is appropriate.</dd>
 * <dt>filename</dt>
 * <dd>The path of the script, relative to the framework's WebServerResources
 * directory.</dd>
 * <dt>framework</dt>
 * <dd>The framework that contains the script resource, or null to use the
 * framework of the hosting component.</dd>
 * </dl>
 *
 * @author Tony Allevato
 * @version $Id: WCScriptFragment.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class WCScriptFragment extends WOHTMLDynamicElement
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

        _location = someAssociations.objectForKey("location");
        _type = someAssociations.objectForKey("type");
        _filename = someAssociations.objectForKey("filename");
        _framework = someAssociations.objectForKey("framework");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the value of the location binding in the specified context.
     *
     * @param context the context
     * @return the value of the scriptName binding
     */
    protected String locationInContext(WOContext context)
    {
        if (_location != null)
            return _location.valueInComponent(context.component()).toString();
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the type binding in the specified context.
     *
     * @param context the context
     * @return the value of the type binding
     */
    protected String typeInContext(WOContext context)
    {
        if (_type != null)
            return _type.valueInComponent(context.component()).toString();
        else
            return "text/javascript";
    }


    // ----------------------------------------------------------
    /**
     * Gets the resource URL of the script if the filename and (optional)
     * framework bindings are specified.
     *
     * @param aContext the context of the request
     * @return the resource URL of the script, or null if the filename is not
     *     specified
     */
    protected String _scriptURL(WOContext aContext)
    {
        if (_filename == null)
        {
            return null;
        }

        WOComponent aComponent = aContext.component();

        String filename = (String)_filename.valueInComponent(aComponent);
        String frameworkName = _frameworkNameInComponent(
                _framework, aComponent);
        String scriptURL = aContext._urlForResourceNamed(filename,
                frameworkName, true);

        if (scriptURL == null)
        {
            scriptURL = WOApplication.application().resourceManager().
                errorMessageUrlForResourceNamed(filename, frameworkName);
        }

        return scriptURL;
    }


    // ----------------------------------------------------------
    /**
     * Overriden to cause the component content (which should be JavaScript
     * code) to be inserted into the script specified by the "location"
     * association.
     *
     * @param response the response
     * @param context the context
     */
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        WOResponse contentResponse = new WOResponse();
        super.appendChildrenToResponse(contentResponse, context);

        String location = locationInContext(context);
        String type = typeInContext(context);
        String scriptURL = _scriptURL(context);
        String script = contentResponse.contentString();

        if ("onLoad".equalsIgnoreCase(location))
        {
            DojoUtils.addScriptCodeToOnLoad(response, context, script);
        }
        else if ("head".equalsIgnoreCase(location))
        {
            if (_filename != null)
            {
                String frameworkName = _frameworkNameInComponent(
                        _framework, context.component());

                ERXResponseRewriter.addScriptResourceInHead(response, context,
                        frameworkName,
                        (String) _filename.valueInComponent(
                                context.component()));
            }

            if (script.trim().length() > 0)
            {
                ERXResponseRewriter.addScriptCodeInHead(
                        response, context, script);
            }
        }
        else
        {
            response.appendContentString("<script type=\"");
            response.appendContentString(type);
            response.appendContentString("\"");

            if (scriptURL != null)
            {
                response.appendContentString(" src=\"");
                response.appendContentString(scriptURL);
                response.appendContentString("\"></script>\n");
            }
            else
            {
                response.appendContentString(">\n");
                response.appendContentString(script);
                response.appendContentString("\n</script>\n");
            }
        }
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _location;
    protected WOAssociation _type;
    protected WOAssociation _filename;
    protected WOAssociation _framework;
}
