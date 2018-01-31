/*==========================================================================*\
 |  $Id: WCTransitionalBasePage.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSMutableArray;
import java.net.URL;
import org.apache.log4j.Logger;

// ------------------------------------------------------------------------
/**
 * <p>
 * The base class for any page that uses the Dojo toolkit. This component
 * defines the base HTML content and also manages the stylesheets and scripts
 * used by Dojo.
 * </p><p>
 * WCBasePage also provides the ability to automatically import CSS and
 * JavaScript resources for the particular component page that contains this
 * WCBasePage component. That is, if you have a component named FooPage defined
 * as follows:
 * <pre>
 * &lt;wo:WCBasePage&gt;
 * your content...
 * &lt;/wo:WCBasePage&gt;
 * </pre>
 * then this component will automatically search in the WebServerResources
 * directory of the framework that contains FooPage to find
 * "stylesheets/FooPage.css" and "javascript/FooPage.js". If they are found,
 * they will be imported.
 * </p><p>
 * The logic described above only applies to the page component itself. If
 * nested components need to import their own scripts, they should make use of
 * {@link WCScriptFragment}.
 * </p>
 *
 * <h2>Bindings</h2>
 * <table>
 * <tr>
 * <td>{@code extraRequires}</td>
 * <td>A semicolon-separated list of additional module names that should be
 * <tt>dojo.require</tt>d in the page header.</td>
 * </tr>
 * </table>
 *
 * @author Tony Allevato
 * @version $Id: WCTransitionalBasePage.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class WCTransitionalBasePage extends WOComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Dojo page.
     *
     * @param context the context
     */
    public WCTransitionalBasePage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String title;
    public String extraBodyCssClass;
    public String extraRequires;
    public boolean includePageWrapping = true;

    /** Used to refer to a single item in a repetition on the page. */
    public String oneExtraRequire;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        log.debug( "nowrap = "
                   + context.request().stringFormValueForKey( "nowrap" ) );
        includePageWrapping =
            ( context.request().stringFormValueForKey( "nowrap" ) == null );
        response.appendHeader("no-cache", "pragma");
        response.appendHeader("no-cache", "cache-control");
        super.appendToResponse( response, context );
    }


    // ----------------------------------------------------------
    /**
     * Returns the HTML page's title string.  This is the title
     * string that will show as the "page title" in the browser.
     * This generic implementation returns "Web-CAT", which is the
     * title that will be used for pages that do not provide one.
     * Ideally, subsystems will override this default.
     *
     * @return The page title
     */
    public String pageTitle()
    {
        return ( title == null )
                ? "Web-CAT"
                : ( "Web-CAT: " + title );
    }


    // ----------------------------------------------------------
    public NSArray<String> extraRequiresArray()
    {
        if (extraRequires == null)
        {
            return null;
        }

        NSMutableArray<String> array = new NSMutableArray<String>();

        String[] requires = extraRequires.split(";");
        for (String require : requires)
        {
            if (require != null && require.length() > 0)
            {
                array.addObject(require);
            }
        }

        return array;
    }


    // ----------------------------------------------------------
    /**
     * Returns true if a page-specific stylesheet exists for the component
     * containing this WCBasePage instance.
     *
     * @return true if a page-specific stylesheet exists for the component,
     *     otherwise false
     */
    public boolean doesPageSpecificStylesheetExist()
    {
        return doesPageSpecificResourceExist(STYLESHEETS_RESOURCE_DIR,
                STYLESHEETS_RESOURCE_EXT);
    }


    // ----------------------------------------------------------
    /**
     * Returns true if a page-specific JavaScript file exists for the component
     * containing this WCBasePage instance.
     *
     * @return true if a page-specific JavaScript file exists for the component,
     *     otherwise false
     */
    public boolean doesPageSpecificJavascriptExist()
    {
        return doesPageSpecificResourceExist(JAVASCRIPT_RESOURCE_DIR,
                JAVASCRIPT_RESOURCE_EXT);
    }


    // ----------------------------------------------------------
    /**
     * Returns true if a page-specific resource for the component containing
     * this WCBasePage instance.
     *
     * @param directory the WebServerResources subdirectory containing the
     *     resource to look for
     * @param extension the extension of the resource to look for
     *
     * @return true if the page-specific resource exists for the component,
     *     otherwise false
     */
    protected boolean doesPageSpecificResourceExist(String directory,
            String extension)
    {
        String resName = pageSpecificResourcePath(directory, extension);
        WOResourceManager manager =
            WOApplication.application().resourceManager();

        URL url = manager.pathURLForResourceNamed(resName, pageFramework(),
                context()._languages());

        return (url != null);
    }


    // ----------------------------------------------------------
    /**
     * Gets the name of the framework that contains the component containing
     * this WCBasePage instance.
     *
     * @return the name of the framework that contains the component
     */
    public String pageFramework()
    {
        return NSBundle.bundleForClass(parent().getClass()).name();
    }


    // ----------------------------------------------------------
    /**
     * Returns the path (relative to WebServerResources) of the page-specific
     * stylesheet for the component containing this WCBasePage instance.
     *
     * @return the WebServerResources-relative path of the page-specific
     *     stylesheet
     */
    public String pageSpecificStylesheetPath()
    {
        return pageSpecificResourcePath(STYLESHEETS_RESOURCE_DIR,
                STYLESHEETS_RESOURCE_EXT);
    }


    // ----------------------------------------------------------
    /**
     * Returns the path (relative to WebServerResources) of the page-specific
     * Javascript file for the component containing this WCBasePage instance.
     *
     * @return the WebServerResources-relative path of the page-specific
     *     JavaScript file
     */
    public String pageSpecificJavascriptPath()
    {
        return pageSpecificResourcePath(JAVASCRIPT_RESOURCE_DIR,
                JAVASCRIPT_RESOURCE_EXT);
    }


    // ----------------------------------------------------------
    public String bodyCssClass()
    {
        return (extraBodyCssClass == null)
            ? "nihilo"
            : ("nihilo " + extraBodyCssClass);
    }


    // ----------------------------------------------------------
    /**
     * Returns true if a page-specific resource for the component containing
     * this WCBasePage instance.
     *
     * @param directory the WebServerResources subdirectory containing the
     *     resource to look for
     * @param extension the extension of the resource to look for
     *
     * @return the WebServerResources-relative path of the page-specific
     *     resource
     */
    protected String pageSpecificResourcePath(String directory,
            String extension)
    {
        return directory + "/" + parent().getClass().getSimpleName() + "." +
            extension;
    }


    //~ Static/instance variables .............................................

    private static final long serialVersionUID = 1L;

    private static final String JAVASCRIPT_RESOURCE_DIR = "javascript";
    private static final String JAVASCRIPT_RESOURCE_EXT = "js";

    private static final String STYLESHEETS_RESOURCE_DIR = "stylesheets";
    private static final String STYLESHEETS_RESOURCE_EXT = "css";

    static Logger log = Logger.getLogger( WCTransitionalBasePage.class );
}
