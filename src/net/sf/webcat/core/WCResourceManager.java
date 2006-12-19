/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
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
    extends WOResourceManager
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     */
    public WCResourceManager()
    {
        super();
        // Force evaluation of this field
        developmentBaseURL();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public String urlForResourceNamed(
        String    aResourceName,
        String    aFrameworkName,
        NSArray   aLanguageList,
        WORequest aRequest )
    {
        return urlForResourceNamed(
            false, aResourceName, aFrameworkName, aLanguageList, aRequest );
    }


    // ----------------------------------------------------------
    public String urlForStaticHtmlResourceNamed(
        String    aResourceName,
        String    aFrameworkName,
        NSArray   aLanguageList,
        WORequest aRequest )
    {
        return urlForResourceNamed(
            true, aResourceName, aFrameworkName, aLanguageList, aRequest );
    }


    // ----------------------------------------------------------
    public String urlForFrameworkPrefixedResourceNamed(
        String aResourceName, WORequest aRequest )
    {
        if ( Application.application()._rapidTurnaroundActiveForAnyProject() )
        {
            // Skip all caching and other processing here.  This should
            // only arise when (a) requesting CSS and Javascript-style
            // resources, when (b) no base.url has been set yet (e.g.,
            // only during the self-installation wizard).  In that case,
            // we want to use absolute external URLs, since internal
            // URLs based on /wr requests will not allow relative references
            // within CSS or Javascript files to be resolved correctly.
            // The default developmentBaseURL should provide this for us
            // until static HTML resources have been initialized correctly
            // after the self-installation wizard completes.
            int pos = aResourceName.indexOf( FRAMEWORK_SUFFIX );
            if ( pos >= 0 )
            {
                aResourceName = aResourceName.substring(
                    pos + FRAMEWORK_SUFFIX.length() );
            }
            return developmentBaseURL + aResourceName;
        }
        String result =
            (String)frameworkPrefixedCache.valueForKey( aResourceName );
        if ( result == null )
        {
            int pos = aResourceName.indexOf( FRAMEWORK_SUFFIX );
            if ( pos >= 0 )
            {
                String resourceName = aResourceName.substring(
                    pos + FRAMEWORK_SUFFIX.length() );
                String frameworkName = aResourceName.substring( 0, pos );
                result = urlForStaticHtmlResourceNamed(
                    resourceName, frameworkName, null, aRequest );
            }
            else
            {
                result = urlForStaticHtmlResourceNamed(
                    aResourceName, null, null, aRequest );
            }
            frameworkPrefixedCache.takeValueForKey( result, aResourceName );
        }
        return result;
    }


    // ----------------------------------------------------------
    public static String resourceURLFor(
        String    aResourceName,
        String    aFrameworkName,
        NSArray   aLanguageList,
        WORequest aRequest )
    {
        return ( (WCResourceManager) Application.application()
            .resourceManager() ).urlForStaticHtmlResourceNamed(
                aResourceName, aFrameworkName, aLanguageList, aRequest );
    }


    // ----------------------------------------------------------
    public static String frameworkPrefixedResourceURLFor(
        String aResourceName, WORequest aRequest )
    {
        return ( (WCResourceManager) Application.application()
            .resourceManager() ).urlForFrameworkPrefixedResourceNamed(
                aResourceName, aRequest );
    }


    // ----------------------------------------------------------
    public static String developmentBaseURL()
    {
        if ( developmentBaseURL == null )
        {
            developmentBaseURL = Application.configurationProperties()
                .getProperty( "WCResourceManager.developmentBaseURL",
                    "http://web-cat.cs.vt.edu/wcstatic/");
            if ( !developmentBaseURL.endsWith( "/" ) )
            {
                developmentBaseURL += "/";
            }
        }
        return developmentBaseURL;
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see com.webobjects.appserver.WOResourceManager#flushDataCache()
     */
    public void flushDataCache()
    {
        frameworkCache = new NSMutableDictionary();
        appCache = new NSMutableDictionary();
        frameworkPrefixedCache = new NSMutableDictionary();
        super.flushDataCache();
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private String urlForResourceNamed(
        boolean   useDevelopmentURLsIfNecessary,
        String    aResourceName,
        String    aFrameworkName,
        NSArray   aLanguageList,
        WORequest aRequest )
    {
        NSMutableDictionary cache = appCache;
        if ( aFrameworkName != null )
        {
            Object framework = frameworkCache.valueForKey( aFrameworkName );
            if ( framework == null )
            {
//                log.debug( "adding framework cache for " + aFrameworkName );
                cache = new NSMutableDictionary();
                frameworkCache.takeValueForKey( cache, aFrameworkName );
            }
            else
            {
                cache = (NSMutableDictionary)framework;
            }
        }
        String result = (String)cache.valueForKey( aResourceName );
        if ( result == null )
        {
//            log.debug( "adding cached URL for " + aFrameworkName + "/"
//                + aResourceName );
            if ( useDevelopmentURLsIfNecessary &&
                 net.sf.webcat.WCServletAdaptor.getInstance() == null )
            {
                result = developmentBaseURL + aResourceName;
            }
            else
            {
                result = standardizeURL( super.urlForResourceNamed(
                    aResourceName, aFrameworkName, aLanguageList, aRequest ) );
            }
            cache.takeValueForKey( result, aResourceName );
        }
//        if ( log.isDebugEnabled() )
//        {
//            log.debug( "urlForResourceNamed( " + aResourceName + ", "
//                + aFrameworkName + ", " + aLanguageList + ", request ) = "
//                + result );
//        }
        return result;
    }


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
//        log.debug( "standardizeURL( " + url + ") = " + result );
        return result;
    }


    //~ Instance/static variables .............................................

    private NSMutableDictionary frameworkCache = new NSMutableDictionary();
    private NSMutableDictionary appCache = new NSMutableDictionary();
    private NSMutableDictionary frameworkPrefixedCache =
        new NSMutableDictionary();

    private static String developmentBaseURL;
    private static final String FRAMEWORK_SUFFIX =
        ".framework/WebServerResources/";

    static Logger log = Logger.getLogger( WCResourceManager.class );
}
