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

import com.webobjects.foundation.*;
import er.extensions.ERXProperties;
import er.extensions.ERXValueUtilities;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * An enhanced subclass of Properties that self-loads from a file
 * name and provides more type conversions.  Based heavily on
 * org.w3c.util.ObservableProperties, but without the notification
 * support.
 * <p>
 * This subclass also supports auto-expansion of property references
 * within property values.  References use the form ${key}.  Recursive
 * references are supported up to 256 levels deep. For example,
 * <p>
 * <pre>
 * A = 12345678
 * B = ${A}90
 * C = ${B} plus more
 * </pre>
 * <p>
 * will result in <code>getProperty("C")</code>
 * returning the value "1234567890 plus more".  The property expansion
 * support is based on code written by Chris Mair.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class WCProperties
    extends java.util.Properties
    implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WCProperties object, initializing its contents from
     * a property file.
     *
     * @param filename The file to load from
     * @param defaults The defaults
     */
    public WCProperties( String filename, Properties defaults )
    {
        super();
        this.defaults = defaults;
        load( filename );
    }


    // ----------------------------------------------------------
    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param defaults The defaults
     */
    public WCProperties( Properties defaults )
    {
        super();
        this.defaults = defaults;
    }


    // ----------------------------------------------------------
    /**
     * Creates an empty property list with no default values.
     */
    public WCProperties()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Load properties from the specified file if it exists.  The
     * full file name of the file will be added to the property
     * defined by PROPERTYFILES_LOADED if successful.
     *
     * @param fileName The property file to load
     */
    public void attemptToLoad( String fileName )
    {
        File file = new File( fileName );
        if ( file.exists() )
        {
            String propertyfiles_loaded = getProperty( PROPERTYFILES_LOADED );
            try
            {
                InputStream is = new FileInputStream( file );
                log.info( "Loading properties from "
                          + file.getAbsolutePath() );
                load( is );
                if ( propertyfiles_loaded == null )
                {
                    propertyfiles_loaded = file.getAbsolutePath();
                }
                else
                {
                    propertyfiles_loaded += ";" + file.getAbsolutePath();
                }
                setProperty( PROPERTYFILES_LOADED, propertyfiles_loaded );
                is.close();
            }
            catch ( IOException e )
            {
                log.error( "Error loading properties from "
                           + file.getAbsolutePath()
                           + ":",
                           e );
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Loads the contents of the specified file.  This function first
     * looks for the file relative to "Contents/Resources" from the
     * current directory (where WO "Resources" files are stored).  If
     * found, properties from this file are loaded.  Then it looks for
     * the same file name relative to the user's home directory.  If found,
     * this property file is loaded as well (overriding any previously
     * defined properties).  Finally, it looks relative to the current
     * directory (which also catches absolute pathnames for the
     * <code>fileName</code> parameter).  If successful, this property
     * file is loaded last.
     *
     * @param fileName The property file to load
     */
    public void load( String fileName )
    {
        // First, load from WO subdirectory
        attemptToLoad(  "Contents/Resources/" + fileName );

        // Now, load from user's home directory
        String dataDir = System.getProperty( "user.home" );
        if( dataDir != null )
        {
            if( ! dataDir.endsWith( File.separator ) )
            {
                dataDir += File.separator;
            }
            attemptToLoad( dataDir + fileName );
        }

        // Finally, load from current dir or absolute path
        attemptToLoad( fileName );
    }


    // ----------------------------------------------------------
    /**
     * Add all properties from the given dictionary to this object.
     * All keys and values in the dictionary are converted to strings
     * in order to be added to this property map.
     *
     * @param dictionary The map containing properties to add
     */
    public void addPropertiesFromDictionary( NSDictionary dictionary )
    {
        if ( dictionary == null ) return;
        Enumeration e = dictionary.keyEnumerator();
        while ( e.hasMoreElements() )
        {
            Object key   = e.nextElement();
            Object value = dictionary.objectForKey( key );
            setProperty( key.toString(), value.toString() );
        }
    }


    // ----------------------------------------------------------
    /**
     * Add all properties from the given dictionary to this object.
     * All keys and values in the dictionary are converted to strings
     * in order to be added to this property map.
     *
     * @param dictionary The map containing properties to add
     */
    public void addPropertiesFromDictionaryIfNotDefined(
                    NSDictionary dictionary )
    {
        if ( dictionary == null ) return;
        Enumeration e = dictionary.keyEnumerator();
        while ( e.hasMoreElements() )
        {
            Object key       = e.nextElement();
            String keyString = key.toString();
            if ( getProperty( keyString ) == null )
            {
                Object value = dictionary.objectForKey( key );
                setProperty( key.toString(), value.toString() );
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Get this property value as a float.  If the property
     * does not exist, the default value 0.0 will be returned.
     *
     * @param key  The name of the property to be fetched
     * @return     Its float value
     */
    public double floatForKey( String key )
    {
        String v = getProperty( key );
        float result = 0.0F;
        if ( v != null )
        {
            try
            {
                result = Float.parseFloat( v );
            }
            catch ( NumberFormatException e )
            {
                log.error( "property " + key + ": '" + v
                           + "' : float format exception" );
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get this property value as a double.  If the property
     * does not exist, the default value 0.0 will be returned.
     *
     * @param key The name of the property to be fetched
     * @return     Its double value
     */
    public double doubleForKey( String key )
    {
        String v = getProperty( key );
        double result = 0.0;
        if ( v != null )
        {
            try
            {
                result = Double.parseDouble( v );
            }
            catch ( NumberFormatException e )
            {
                log.error( "property " + key + ": '" + v
                           + "' : double format exception" );
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get this property value as a File.  If the property does
     * not exist, null is returned.
     *
     * @param name The name of the property to be fetched
     * @return     A File instance
     */
    public File getFile( String name )
    {
        String v = getProperty( name );
        File result = null;
        if ( v != null )
        {
            result = new File( v );
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning an NSArray for a
     * given property.
     * @param s property key
     * @return array de-serialized from the string in
     *      this properties mapping
     */
    public NSArray arrayForKey( String s )
    {
        return arrayForKeyWithDefault( s, null );
    }

    // ----------------------------------------------------------
    /**
     * Cover method for returning an NSArray for a
     * given property and set a default value if not given.
     * @param s property key
     * @param defaultValue default value
     * @return array de-serialized from the string in
     *      the properties or default value
     */
    public NSArray arrayForKeyWithDefault( String s, NSArray defaultValue )
    {
        return ERXValueUtilities.arrayValueWithDefault( getProperty( s ),
                                                        defaultValue );
    }

    // ----------------------------------------------------------
    /**
     * Cover method for returning a boolean for a
     * given property. This method uses the
     * method <code>booleanValue</code> from
     * {@link ERXValueUtilities}.
     * @param s property key
     * @return boolean value of the string in the
     *      properties
     */
    public boolean booleanForKey( String s )
    {
        return booleanForKeyWithDefault( s, false );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning a boolean for a
     * given property or a default value. This method uses the
     * method <code>booleanValue</code> from
     * {@link ERXValueUtilities}.
     * @param s property key
     * @param defaultValue default value
     * @return boolean value of the string in the
     *      properties
     */
    public boolean booleanForKeyWithDefault( String s, boolean defaultValue )
    {
        return ERXValueUtilities.booleanValueWithDefault( getProperty( s ),
                                                          defaultValue );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning an NSDictionary for a
     * given property.
     * @param s property key
     * @return dictionary de-serialized from the string in
     *      the properties
     */
    public NSDictionary dictionaryForKey( String s )
    {
        return dictionaryForKeyWithDefault( s, null );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning an NSDictionary for a
     * given property or the default value.
     * @param s property key
     * @param defaultValue default value
     * @return dictionary de-serialized from the string in
     *      the properties
     */
    public NSDictionary dictionaryForKeyWithDefault(
        String s, NSDictionary defaultValue )
    {
        return ERXValueUtilities.dictionaryValueWithDefault(
                        getProperty( s ), defaultValue );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning an int for a
     * given property.
     * @param s property key
     * @return int value of the property or 0
     */
    public int intForKey( String s )
    {
        return intForKeyWithDefault( s, 0 );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning a long for a
     * given property.
     * @param s property key
     * @return long value of the property or 0
     */
    public long longForKey( String s )
    {
        return longForKeyWithDefault( s, 0 );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning a BigDecimal for a
     * given property. This method uses the
     * method <code>bigDecimalValueWithDefault</code> from
     * {@link ERXValueUtilities}.
     * @param s property key
     * @return bigDecimal value of the string in the properties.  Scale is
     *         controlled by the string, ie "4.400" will have a scale of 3.
     */
    public BigDecimal bigDecimalForKey( String s )
    {
        return bigDecimalForKeyWithDefault( s, null );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning a BigDecimal for a
     * given property or a default value. This method uses the
     * method <code>bigDecimalValueWithDefault</code> from
     * {@link ERXValueUtilities}.
     * @param s property key
     * @param defaultValue default value
     * @return BigDecimal value of the string in the properties. Scale is
     *         controlled by the string, ie "4.400" will have a scale of 3.
     */
    public BigDecimal bigDecimalForKeyWithDefault(
        String s, BigDecimal defaultValue )
    {
        return ERXValueUtilities.bigDecimalValueWithDefault(
                        getProperty( s ), defaultValue );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning an int for a
     * given property with a default value.
     * @param s property key
     * @param defaultValue default value
     * @return int value of the property or the default value
     */
    public int intForKeyWithDefault( String s, int defaultValue )
    {
        return ERXValueUtilities.intValueWithDefault(
                        getProperty( s ), defaultValue );
    }


    // ----------------------------------------------------------
    /**
     * Cover method for returning a long for a
     * given property with a default value.
     * @param s property key
     * @param defaultValue default value
     * @return long value of the property or the default value
     */
    public long longForKeyWithDefault( String s, long defaultValue )
    {
        return ERXValueUtilities.longValueWithDefault(
                        getProperty( s ), defaultValue );
    }


    // ----------------------------------------------------------
    /**
     * Returning an string for a given
     * property. This is a cover method of
     * {@link java.util.Properties#getProperty(String)}
     * @param s property
     * @return string value of the property or null
     */
    public String stringForKey( String s )
    {
        return stringForKeyWithDefault( s, null );
    }


    // ----------------------------------------------------------
    /**
     * Returning an string for a given
     * property. This is a cover method of
     * {@link java.util.Properties#getProperty(String,String)}
     * @param s property key
     * @param defaultValue default value
     * @return string value of the property or default value
     */
    public String stringForKeyWithDefault( String s, String defaultValue )
    {
        String s1 = getProperty( s );
        return s1 != null
            ? s1
            : maybeSubstitutePropertyReferences( defaultValue );
    }


    // ----------------------------------------------------------
    /**
     * Sets an array in the properties for a particular key.
     * @param array to be set in the properties
     * @param key to be used to get the value
     */
    public void setArrayForKey( NSArray array, String key )
    {
        setStringForKey(
            NSPropertyListSerialization.stringFromPropertyList( array ), key );
    }


    // ----------------------------------------------------------
    /**
     * Sets a dictionary in the properties for a particular key.
     * @param dictionary to be set in the properties
     * @param key to be used to get the value
     */
    public void setDictionaryForKey( NSDictionary dictionary, String key )
    {
        setStringForKey(
            NSPropertyListSerialization.stringFromPropertyList( dictionary ),
            key );
    }


    // ----------------------------------------------------------
    /**
     * Sets a string in the properties for another string.
     * @param string to be set in the properties
     * @param key to be used to get the value
     */
    public void setStringForKey( String string, String key )
    {
        setProperty( key, string );
    }


    // ----------------------------------------------------------
    /**
     * Caches the application name for appending to the key.
     * Note that for a period when the application is starting up
     * application() will be null and name() will be null.
     * @return application name used for appending, for example ".ERMailer"
     */
    protected String applicationNameForAppending()
    {
        if ( applicationNameForAppending == null )
        {
            applicationNameForAppending =
                com.webobjects.appserver.WOApplication.application() != null
                ? com.webobjects.appserver.WOApplication.application().name()
                : null;
            if ( applicationNameForAppending != null )
            {
                applicationNameForAppending = "." + applicationNameForAppending;
            }
        }
        return applicationNameForAppending;
    }


    // ----------------------------------------------------------
    /**
     * Substitutes property references in the given string.  Any reference
     * of the form ${key} is processed by looking up "key" and replacing
     * the entire reference with the key's value.
     * <p>
     * If "key" is not defined in this property map (or any of its ancestors),
     * then the reference is left unchanged.
     * <p>
     * If the value for "key" contains other property references, they are also
     * expanded (up to a maximum recursive depth of 256, which is just to
     * prevent stack overflow).  If this recursive depth is exceeded while
     * expanding property references, the property reference is replaced
     * by the string "${key:RECURSION-TO-DEEP}".
     * @param value the string to perform substitution on
     * @return The value, with all property references substituted
     */
    public String substitutePropertyReferences( String value )
    {
        return substitutePropertyReferences( value, MAX_RECURSIVE_DEPTH );
    }


    // ----------------------------------------------------------
    /**
     * Just like {@link #substitutePropertyReferences(String)}, but
     * only performs substitution if
     * {@link #willPerformPropertySubstitution()}.
     * @param value the string to perform substitution on
     * @return The value, with all property references substituted
     */
    public String maybeSubstitutePropertyReferences( String value )
    {
        if ( willPerformPropertySubstitution )
        {
            return substitutePropertyReferences( value, MAX_RECURSIVE_DEPTH );
        }
        else
        {
            return value;
        }
    }


    // ----------------------------------------------------------
    /**
     * Performs the real substitution work for
     * {@link #substitutePropertyReferences(String)}.  Based on code
     * published by Chris Mair.
     * @param value the string to perform substitution on
     * @param maxDepth the limit on the number of levels of recursion that
     * can be used in substituting properties
     * @return The value, with all property references substituted
     */
    private String substitutePropertyReferences( String value, int maxDepth )
    {
        if ( value == null || value.length() == 0 )
        {
            return value;
        }

        // Get the index of the first constant, if any
        StringBuffer buffer = new StringBuffer( value.length() );
        int beginIndex = 0;
        int startName = value.indexOf( REFERENCE_START, beginIndex );

        while ( startName >= 0 )
        {
            int endName = value.indexOf( REFERENCE_END, startName );
            if ( endName == -1 )
            {
                // Terminating symbol not found
                // Return the value as is
                break;
            }

            if ( startName > beginIndex )
            {
                buffer.append( value.substring( beginIndex, startName ) );
                beginIndex = startName;
            }

            String constName  =
                value.substring( startName + REFERENCE_START.length(), endName);
            String constValue = ( maxDepth > 0 )
                ? getProperty(constName, true, maxDepth - 1)
                : REFERENCE_START + constName + ":RECURSION-TOO-DEEP"
                  + REFERENCE_END;

            if ( constValue == null )
            {
                // Property name not found
                buffer.append( value.substring( beginIndex,
                                endName + REFERENCE_END.length() ) );
            }
            else
            {
                // Insert the constant value into the
                // original property value
                buffer.append( constValue );
            }

            beginIndex = endName + REFERENCE_END.length();
            // Look for the next constant
            startName = value.indexOf( REFERENCE_START, beginIndex );
        }

        buffer.append( value.substring(beginIndex, value.length() ) );
        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Overriding the default setProperty method to check for and handle
     * the NO_SUBSTITUTION_PREFIX on the key.
     * @param key to check
     * @return property value
     */
    public synchronized Object setProperty( String key, String value )
    {
        if ( key != null && key.startsWith( NO_SUBSTITUTION_PREFIX ) )
        {
            key = key.substring( NO_SUBSTITUTION_PREFIX.length() );
        }
        return super.setProperty( key, value );
    }


    // ----------------------------------------------------------
    /**
     * Overriding the default getProperty method to first check:
     * key.<ApplicationName> before checking for key.  If nothing
     * is found then key.  Default is checked.
     * @param key to check
     * @return property value
     */
    public String getProperty( String key )
    {
        return getProperty(
            key, willPerformPropertySubstitution, MAX_RECURSIVE_DEPTH );
    }


    // ----------------------------------------------------------
    /**
     * Overriding the default getProperty method to first check:
     * key.<ApplicationName> before checking for key.  If nothing
     * is found then key.  Default is checked.
     * @param key to check
     * @param performSubstitution if true, any property references within
     * the resulting value will be expanded through substitution
     * @param maxDepth the limit on the number of levels of recursion that
     * can be used in substituting properties
     * @return property value
     */
    public String getProperty(
                    String key, boolean performSubstitution, int maxDepth )
    {
        String property = null;
        String application = applicationNameForAppending();
        if ( key != null && key.startsWith( NO_SUBSTITUTION_PREFIX ) )
        {
            key = key.substring( NO_SUBSTITUTION_PREFIX.length() );
            performSubstitution = false;
        }
        if ( application != null )
        {
            property = super.getProperty( key + application );
        }
        if ( property == null )
        {
            property = super.getProperty( key );
            if ( property == null )
            {
                property =
                    super.getProperty( key + ERXProperties.DefaultString );
            }
            // This behavior from ERXProperties makes it hard to dynamically
            // reset properties easily, so turn it off here.
            // // We go ahead and set the value to increase the lookup the
            // // next time the property is accessed.
            // if ( property != null && application != null )
            // {
            //     setProperty( key + application, property );
            // }
        }
        if ( performSubstitution )
        {
            property = substitutePropertyReferences( property, maxDepth );
        }
        return property;
    }


    // ----------------------------------------------------------
    /**
     * Sets the receiver's value for the property identified by key
     * to value.  If the value is null, the property is removed
     * from the current property object.  This will <b>not</b>
     * remove inherited default values.
     *
     * @param value The value to set
     * @param key   The name of the property to set
     */
    public void takeValueForKey( Object value, String key )
    {
        if ( value == null || value == NSKeyValueCoding.NullValue )
        {
            remove( key );
        }
        else
        {
            setProperty( key, value.toString() );
        }
    }


    // ----------------------------------------------------------
    /**
     * Sets the receiver's value for the property identified by key path
     * to value.  If the key path exists as a distinct key in the dictionary,
     * this method set the corresponding object in the dictionary by invoking
     * takeValueForKey. Otherwise, it recursively invokes valueForKey for
     * each segment of the keyPath on the result of the previous segment,
     * and then takeValueForKey on the last object in the path.
     *
     * @param value   The value to set
     * @param keyPath The path to the property to set
     */
    public void takeValueForKeyPath( Object value, String keyPath )
    {
        log.debug( "takeValueForKeyPath(" + keyPath + ")" );

        if ( getProperty( keyPath ) != null )
        {
            takeValueForKey( value, keyPath );
        }
        else
        {
            int pos = keyPath.indexOf( KeyPathSeparator );
            if ( pos < 0 )
            {
                takeValueForKey( value, keyPath );
            }
            else
            {
                String prefix = keyPath.substring( 0, pos );
                if ( getProperty( prefix ) != null )
                {
                    NSKeyValueCodingAdditions.DefaultImplementation
                    .takeValueForKeyPath( this, value, keyPath );
                }
                else
                {
                    takeValueForKey( value, keyPath );
                }
            }
        }
//
//
//
//
//        if ( value == null || value == NSKeyValueCoding.NullValue )
//        {
//            remove( key );
//        }
//        else
//        {
//            setProperty( key, value.toString() );
//        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the value bound to the given key.
     *
     * @param key   The name of the property to fetch
     * @return      Its value, or null if there is no binding
     */
    public Object valueForKey( String key )
    {
        return getProperty( key );
    }


    // ----------------------------------------------------------
    /**
     * Returns the value bound to the given key path.
     * If the key path exists as a distinct key in the dictionary,
     * this method returns the corresponding object in the dictionary by
     * invoking valueForKey. Otherwise, it recursively invokes valueForKey
     * for each segment of the keyPath on the result of the previous segment.
     *
     * @param keyPath The path to the property to fetch
     * @return        Its value, or null if there is no binding
     */
    public Object valueForKeyPath( String keyPath )
    {
        log.debug( "valueForKeyPath(" + keyPath + ")" );
        Object result = getProperty( keyPath );
        if ( result != null )
        {
            return result;
        }
        else
        {
            return NSKeyValueCodingAdditions.DefaultImplementation
                .valueForKeyPath( this, keyPath );
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the set of locally defined properties.
     * @return the set of locally defined properties
     * @see java.util.Map.Entry
     */
    public Set localEntrySet()
    {
        return entrySet();
    }


    // ----------------------------------------------------------
    /**
     * Returns the set of default-defined properties.
     * @return the set of default-defined properties
     * @see java.util.Map.Entry
     */
    public Set inheritedEntrySet()
    {
        return defaults.entrySet();
    }


    // ----------------------------------------------------------
    /**
     * Find out if property references (in ${property} form) within
     * property values will be substituted when properties are retrieved.
     * @return true if substitution will be performed
     */
    public boolean willPerformPropertySubstitution()
    {
        return willPerformPropertySubstitution;
    }


    // ----------------------------------------------------------
    /**
     * Determine if property references (in ${property} form) within
     * property values will be substituted when properties are retrieved.
     * @param value true if substitution is desired, false if it should be
     * turned off
     */
    public void setWillPerformPropertySubstitution( boolean value )
    {
        willPerformPropertySubstitution = value;
    }


    //~ Instance/static variables .............................................

    /** caches the application name that is appended to the key for lookup */
    protected String applicationNameForAppending;
    protected boolean willPerformPropertySubstitution = true;

    private final String REFERENCE_START = "${";
    private final String REFERENCE_END = "}";
    private final int    MAX_RECURSIVE_DEPTH = 256;

    static Logger log = Logger.getLogger( WCProperties.class );
    static public String PROPERTYFILES_LOADED = "propertyfiles.loaded";
    static public String NO_SUBSTITUTION_PREFIX = "NOSUB.";
}
