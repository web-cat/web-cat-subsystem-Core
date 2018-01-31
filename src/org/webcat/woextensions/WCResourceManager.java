/*==========================================================================*\
 |  $Id: WCResourceManager.java,v 1.1 2011/10/25 12:51:37 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

package org.webcat.woextensions;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import er.extensions.appserver.ERXResourceManager;
import er.extensions.foundation.ERXMutableURL;
import org.webcat.core.Application;
import org.webcat.core.Subsystem;
import org.webcat.core.SubsystemManager;
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
 *  @author  Stephen Edwards
 *  @author  Latest changes by: $Author: stedwar2 $
 *  @version $Revision: 1.1 $, $Date: 2011/10/25 12:51:37 $
 */
public class WCResourceManager
    extends ERXResourceManager
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
    @SuppressWarnings("unchecked")
    public String urlForResourceNamed(
        String    aResourceName,
        String    aFrameworkName,
        NSArray   aLanguageList,
        WORequest aRequest)
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
            int pos = aResourceName.indexOf(FRAMEWORK_SUFFIX);
            if (pos >= 0)
            {
                aFrameworkName = aResourceName.substring(0, pos);
                aResourceName = aResourceName.substring(
                    pos + FRAMEWORK_SUFFIX.length());
            }
        }
        return super.urlForResourceNamed(
            aResourceName, aFrameworkName, aLanguageList, aRequest);
    }


    // ----------------------------------------------------------
    public static String resourceURLFor(
        String     aResourceName,
        String     aFrameworkName,
        NSArray<?> aLanguageList,
        WORequest  aRequest)
    {
        if (aRequest == null)
        {
            aRequest = exemplarRequest;
        }
        return ((WCResourceManager)Application.application()
            .resourceManager()).urlForResourceNamed(
                aResourceName, aFrameworkName, aLanguageList, aRequest);
    }


    // ----------------------------------------------------------
    public static String versionlessResourceURLFor(
        String     aResourceName,
        String     aFrameworkName,
        NSArray<?> aLanguageList,
        WORequest  aRequest)
    {
        String result = resourceURLFor(
            aResourceName, aFrameworkName, aLanguageList, aRequest);
        if (result != null)
        {
            int pos = result.lastIndexOf('?');
            if (pos >= 0)
            {
                result = result.substring(0, pos);
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public static String resourceURLFor(
        String aResourceName, WORequest aRequest)
    {
        if (aRequest == null)
        {
            aRequest = exemplarRequest;
        }
        return ((WCResourceManager)Application.application()
            .resourceManager()).urlForResourceNamed(
                aResourceName, null, null, aRequest);
    }


    // ----------------------------------------------------------
    /**
     * Controls resource version numbers via version build datestamps
     * stored for each subsystem, and appends the query parameter
     * "?xxx" to WebServerResource URLs.
     */
    public static class WCVersionManager
        implements IVersionManager
    {
        // ----------------------------------------------------------
        /**
         * Returns the variant of the given resource URL adjusted to include
         * version information.
         * @param resourceUrl
         *            the original resource URL
         * @param name
         *            the name of the resource being loaded
         * @param bundleName
         *            the name of the bundle that contains the resource
         * @param languages
         *            the languages requested
         * @param request
         *            the request
         * @return a versioned variant of the resourceUrl
         */
        @SuppressWarnings("unchecked")
        public String versionedUrlForResourceNamed(
            String resourceUrl,
            String name,
            String bundleName,
            NSArray languages,
            WORequest request)
        {
            resourceUrl = standardizeURL(resourceUrl);
            String version = versionFor(bundleName);
            if (version != null)
            {
                try
                {
                    ERXMutableURL url = new ERXMutableURL(resourceUrl);
                    url.addQueryParameter("", version);
                    resourceUrl = url.toExternalForm();
                }
                catch (MalformedURLException e)
                {
                    log.error("Failed to construct URL from '"
                        + resourceUrl + "'.", e);
                }
            }
            return resourceUrl;
        }


        // ----------------------------------------------------------
        private String defaultVersion()
        {
            if (defaultVersion == null)
            {
                defaultVersion = Application.wcApplication().version();
                int pos = defaultVersion.lastIndexOf('.');
                if (pos >= 0)
                {
                    defaultVersion = defaultVersion.substring(pos + 1);
                }
            }
            return defaultVersion;
        }


        // ----------------------------------------------------------
        private String versionFor(String bundleName)
        {
            if (bundleName == null
                || Application.wcApplication().needsInstallation())
            {
                return defaultVersion();
            }

            String version = bundleVersion.get(bundleName);
            if (version == null)
            {
                SubsystemManager manager = Application.wcApplication()
                    .subsystemManager();
                Subsystem bundle = manager.subsystem(bundleName);
                if (bundle == null)
                {
                    // Could be a framework that is nested inside a subsystem
                    for (Subsystem sub : manager.subsystems())
                    {
                        // First, cache the version for this subsystem, if
                        // we haven't already
                        if (bundleVersion.get(sub.name()) == null)
                        {
                            bundleVersion.put(
                                sub.name(), sub.descriptor().versionDate());
                        }

                        // Now check for dependencies
                        String alsoContains = sub.descriptor()
                            .getProperty("alsoContains");
                        if (alsoContains != null)
                        {
                            alsoContains =
                                alsoContains.replace(".framework", "");
                            String[] contains = alsoContains.split(",\\s*");
                            for (String subsub : contains)
                            {
                                bundleVersion.put(
                                    subsub, sub.descriptor().versionDate());
                                if (bundleName.equals(subsub))
                                {
                                    bundle = sub;
                                }
                            }
                        }
                        if (bundle != null)
                        {
                            break;
                        }
                    }
                }
                if (bundle != null)
                {
                    version = bundle.descriptor().versionDate();
                    bundleVersion.put(bundleName, version);
                }
            }
            if (version == null)
            {
                version = defaultVersion();
                bundleVersion.put(bundleName, version);
            }
            return version;
        }


        //~ Instance/static variables .........................................

        private String defaultVersion;
        private Map<String, String> bundleVersion =
            new HashMap<String, String>();
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private static String standardizeURL(String url)
    {
        String result = url;
        int pos = result.indexOf(':');
        if (pos > 0
            && pos < result.length() - 2
            && result.charAt( pos + 1 ) == '/'
            && result.charAt( pos + 2 ) != '/')
        {
            result = result.substring(0, pos + 1) + "/"
                + result.substring(pos + 1);
        }
        return result.replaceAll("[/]([A-Za-z])%3A","$1:")
            .replaceAll("[+][%]26[+]", " & ");
    }


    //~ Instance/static variables .............................................

    private static final String FRAMEWORK_SUFFIX =
        ".framework/WebServerResources/";

    private static WORequest exemplarRequest;
    private static boolean wantExemplar = false;

    static Logger log = Logger.getLogger(WCResourceManager.class);
}
