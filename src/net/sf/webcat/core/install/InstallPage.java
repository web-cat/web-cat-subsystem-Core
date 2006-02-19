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

package net.sf.webcat.core.install;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import net.sf.webcat.core.*;


// -------------------------------------------------------------------------
/**
 * A basic interface implemented by all InstallPages.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public abstract class InstallPage
    extends WCComponentWithErrorMessages
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new InstallPage object.
     * 
     * @param context The context to use
     */
    public InstallPage( WOContext context )
    {
        super( context );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse request, WOContext context )
    {
        setDefaultConfigValues( Application.configurationProperties() );
        super.appendToResponse( request, context );
    }

    // ----------------------------------------------------------
    public abstract int stepNo();


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        // Default does nothing, but subclasses can override this
    }


    // ----------------------------------------------------------
    public void setConfigDefault( WCConfigurationFile configuration,
                                  String              key,
                                  String              value )
    {
        if ( value == null ) return;
        String oval = configuration.getProperty( key );
        if ( oval == null || oval.equals( "" ) )
        {
            configuration.setProperty( key, value );
        }
    }


    // ----------------------------------------------------------
    public void validationFailedWithException(
        Throwable e,
        Object    value,
        String    keypath )
    {
        errorMessage( e.getMessage() );
    }


    // ----------------------------------------------------------
    public void takeFormValues( NSDictionary formValues )
    {
        // Default does nothing, but subclasses can override this
    }


    // ----------------------------------------------------------
    public void takeValuesFromRequest( WORequest request, WOContext context )
    {
        takeFormValues( request.formValues() );
        super.takeValuesFromRequest( request, context );
    }


    // ----------------------------------------------------------
    /**
     * Extract a given key value from a set of form values, and
     * store the corresponding information in the application's
     * configuration.
     * @param formValues The form value dictionary from the request
     * @param formKey    The key to look up
     * @param errMsgIfEmpty An error message to generate if the given
     *     value is empty.  If this parameter is null, empty parameters
     *     are allowed without generating any error message.
     * @return The value transferred, or null otherwise.
     */
    public String storeFormValueToConfig( NSDictionary formValues,
                                          String       formKey,
                                          String       errMsgIfEmpty )
    {
        return storeFormValueToConfig(
            formValues, formKey, formKey, errMsgIfEmpty );
    }


    // ----------------------------------------------------------
    /**
     * Extract a given key value from a set of form values, and
     * store the corresponding information in the application's
     * configuration.
     * @param formValues The form value dictionary from the request
     * @param formKey    The key to look up
     * @param configKey  The key to use when storing the result in the
     *                   application's configuration
     * @param errMsgIfEmpty An error message to generate if the given
     *     value is empty.  If this parameter is null, empty parameters
     *     are allowed without generating any error message.
     * @return The value transferred, or null otherwise.
     */
    public String storeFormValueToConfig( NSDictionary formValues,
                                          String       formKey,
                                          String       configKey,
                                          String       errMsgIfEmpty )
    {
        String value = extractFormValue( formValues, formKey );
        try
        {
            if ( value != null )
            {
                value = validateValueForKey( value, formKey ).toString();
                if ( ( value == null || value.equals( "" ) )
                     && errMsgIfEmpty != null )
                {
                    errorMessage( errMsgIfEmpty );
                    value = null;
                }
                else
                {
                    Application.configurationProperties().setProperty(
                        configKey, value );
                }
            }
            else if ( errMsgIfEmpty != null )
            {
                errorMessage( errMsgIfEmpty );
                value = null;
            }
        }
        catch ( NSValidation.ValidationException e )
        {
            errorMessage( e.getMessage() );
            value = null;
        }
        return value;
    }


    // ----------------------------------------------------------
    /**
     * Extract a given key value from a set of form values.  If multiple
     * values are associated with the given key, the first one is returned.
     * @param formValues The form value dictionary from the request
     * @param key        The key to look up
     * @return The first value associated with the key, or null
     */
    public String extractFormValue( NSDictionary formValues, String key )
    {
        NSArray values = (NSArray)formValues.objectForKey( key );
        if ( values != null && values.count() > 0 )
        {
            return values.objectAtIndex( 0 ).toString();
        }
        else
        {
            return null;
        }
    }

}
