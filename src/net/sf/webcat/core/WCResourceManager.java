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

package net.sf.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  A drop-in replacement for {@link WOResourceManager} that fixes a bug
 *  in WODeploymentBundle's urlForResource() method.  Unfortunately,
 *  WODeploymentBundle "standardizes" the URL the same way it would a
 *  local path name, and accidentally converts the double-slash at the
 *  start of an absolute URL to just a single slash, thereby changing
 *  its meaning.  This subclass looks for and restores such broken URLs,
 *  allowing absolute URLs to be used in a deployed application's
 *  frameworksBaseURL() setting.
 *
 *  @author  stedwar2
 *  @version $Id$
 */
public class WCResourceManager
    extends er.extensions.ERXResourceManager
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     */
    public WCResourceManager()
    {
        super();

        // This provides support for resource-manager-based URL generation
        // for HTML resources during development mode, even when no
        // request parameter is provided.  During deployment, this is
        // always disabled, since the frameworks base URL is always set
        // to something other than the default in that case.
        wantExemplar = Application.application().frameworksBaseURL().equals(
            "/WebObjects/Frameworks");
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public String urlForResourceNamed(
        String    aResourceName,
        String    aFrameworkName,
        NSArray   aLanguageList,
        WORequest aRequest )
    {
        if (wantExemplar && exemplarRequest == null && aRequest != null)
        {
            exemplarRequest = aRequest;
            log.debug("exemplar = " + exemplarRequest);
            log.debug("frameworks base = "
                + Application.application().frameworksBaseURL());
        }
        if (aFrameworkName == null)
        {
            int pos = aResourceName.indexOf( FRAMEWORK_SUFFIX );
            if ( pos >= 0 )
            {
                aFrameworkName = aResourceName.substring( 0, pos );
                aResourceName = aResourceName.substring(
                    pos + FRAMEWORK_SUFFIX.length() );
            }
        }
        return standardizeURL(super.urlForResourceNamed(
            aResourceName, aFrameworkName, aLanguageList, aRequest ));
    }


    // ----------------------------------------------------------
    public static String resourceURLFor(
        String    aResourceName,
        String    aFrameworkName,
        NSArray   aLanguageList,
        WORequest aRequest )
    {
        if (aRequest == null) aRequest = exemplarRequest;
        return ( (WCResourceManager) Application.application()
            .resourceManager() ).urlForResourceNamed(
                aResourceName, aFrameworkName, aLanguageList, aRequest );
    }


    // ----------------------------------------------------------
    public static String resourceURLFor(
        String aResourceName, WORequest aRequest )
    {
        if (aRequest == null) aRequest = exemplarRequest;
        return ( (WCResourceManager) Application.application()
            .resourceManager() ).urlForResourceNamed(
                aResourceName, null, null, aRequest );
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private String standardizeURL( String url )
    {
        String result = url;
        int pos = result.indexOf( ':' );
        if ( pos > 0
             && pos < result.length() - 2
             && result.charAt( pos + 1 ) == '/'
             && result.charAt( pos + 2 ) != '/'
           )
        {
            result = result.substring( 0, pos + 1 ) + "/"
                + result.substring( pos + 1 );
        }
        return result;
    }


    //~ Instance/static variables .............................................

    private static final String FRAMEWORK_SUFFIX =
        ".framework/WebServerResources/";

    private static WORequest exemplarRequest;
    private static boolean wantExemplar = false;

    static Logger log = Logger.getLogger( WCResourceManager.class );
}
