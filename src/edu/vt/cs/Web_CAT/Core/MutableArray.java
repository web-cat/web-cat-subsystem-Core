/*==========================================================================*\
 |  $Id: MutableArray.java,v 1.5 2011/03/07 18:39:42 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

package edu.vt.cs.Web_CAT.Core;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.webcat.core.MutableContainer;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSRange;

//-------------------------------------------------------------------------
/**
 *  A customized subcass of NSArray that can be used as the value class
 *  for an EO attribute.  Based on the info in
 *  <a href="http://docs.info.apple.com/article.html?artnum=75173">http://docs.info.apple.com/article.html?artnum=75173</a>.
 *
 *  See the description in {@link MutableDictionary} for critical
 *  usage details.
 *
 *  @deprecated use the org.webcat.core version of this class instead.
 *  This version is only provided for database compatibility during the
 *  Spring 2006 semester.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.5 $, $Date: 2011/03/07 18:39:42 $
 */
@Deprecated
@SuppressWarnings("unchecked")
public class MutableArray
    extends er.extensions.foundation.ERXMutableArray
    implements MutableContainer
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates an empty array.
     */
    public MutableArray()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * Creates an empty mutable array with enough allocated memory to hold
     * the number of objects specified by capacity, a number greater than or
     * equal to 0.
     *
     * MutableArrays expand as needed, so capacity simply establishes the
     * object's initial capacity.
     *
     * @param capacity a size hint for the anticipated upper bound
     */
    public MutableArray( int capacity )
    {
       super( capacity );
    }


    // ----------------------------------------------------------
    /**
     * Creates a mutable array containing the single element object.
     * @param object the single element contained in the mutable array
     */
    public MutableArray( Object object )
    {
        super( object );
        resetChildParents( false );
    }


    // ----------------------------------------------------------
    /**
     * Creates a mutable array containing the objects from objects in the
     * range specified by range.
     *
     * After an immutable array has been initialized in this way, it can't
     * be modified.
     *
     * @param objects the objects contained in the mutable array
     * @param range   the range specified
     */
    public MutableArray( Object[] objects, NSRange range )
    {
        super( objects, range );
        resetChildParents( false );
    }


    // ----------------------------------------------------------
    /**
     * Creates a mutable array containing objects.
     * @param objects the objects contained in the mutable array
     */
    public MutableArray( Object[] objects )
    {
        super( objects );
        resetChildParents( false );
    }


    // ----------------------------------------------------------
    /**
     * One should use the mutableClone method instead. Creates a mutable
     * array containing the objects in otherArray.
     * @param otherArray contains the objects
     */
    public MutableArray( NSArray otherArray )
    {
        super( otherArray );
        resetChildParents( false );
    }


    // ----------------------------------------------------------
    /**
     * Creates a mutable array containing the objects from vector in the
     * range specified by range.
     *
     * The ignoreNull argument controls the method's behavior when it
     * encounters a value in the vector: if ignoreNull is true, the null
     * value is simply ignored.
     *
     * @param vector     mutable array contains objects from this
     * @param range      the specified range
     * @param ignoreNull the null value is ignored
     * @throws IllegalArgumentException if ignoreNull is false and a null
     * reference exists within the specified range of the vector.
     */
    public MutableArray( Vector vector, NSRange range, boolean ignoreNull )
    {
       super( vector, range, ignoreNull );
       resetChildParents( false );
    }

    // ----------------------------------------------------------
    /**
     * Constructs an array containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     * @param c the collection whose elements are to be placed into this list.
     */
    public MutableArray( Collection c )
    {
        super( c.toArray() );
        resetChildParents( false );
    }


    //~ Methods ...............................................................

    //----------------------------------------------------------
    /**
     * Creates a new, distinct dictionary that contains all the same
     * values as this one.  This method is necessary to support custom
     * value class usage correctly.
     * @return The duplicate object
     */
    public Object clone()
    {
        return new MutableArray(this);
    }


    // ----------------------------------------------------------
    /**
     * This is the conversion method that serializes this array for
     * storage in the database.  It uses java serialization to serialize
     * this object into bytes within an NSData object.  We're using this
     * instead of ERXMutableArray.toBlob() since it is slightly more
     * efficient and has better error checking.
     *
     * @return An NSData object containing the serialized bytes of this object.
     */
    public NSData archiveData()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(
            kOverheadAdjustment + count() * kCountBytesFactor );
        NSData result = null;
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream( bos );
            oos.writeObject( this );
            oos.flush();
            oos.close();
            result = new NSData( bos.toByteArray() );
        }
        catch ( IOException ioe )
        {
            if ( NSLog.debugLoggingAllowedForLevelAndGroups(
                     NSLog.DebugLevelCritical, NSLog.DebugGroupArchiving ) )
            {
                NSLog.debug.appendln( ioe );
            }

            if ( kThrowOnError )
            {
                throw new NSForwardException( ioe );
            }
        }

        return result;
    }


    // ----------------------------------------------------------
    /**
     * This is the factory method used to recreate an array from a
     * database attribute. It uses java Serialization to turn bytes from an
     * NSData into a reconstituted Object.  We're using this instead of
     * ERXMutableArray.fromBlob() since it is slightly more efficient
     * and has better error checking.
     *
     * @param data This is the NSData holding the previously serialized bytes.
     * @return The un-serialized Object.
     */
    public static MutableArray objectWithArchiveData( NSData data )
    {
        if ( data == null ) return new MutableArray();
        ByteArrayInputStream bis = new ByteArrayInputStream( data.bytes() );
        MutableArray result = null;
        Throwable exception = null;
        try
        {
            ObjectInputStream ois = new ObjectInputStream( bis );
            result = (MutableArray)ois.readObject();
        }
        catch ( IOException ioe )
        {
            exception = ioe;
        }
        catch ( ClassNotFoundException cnfe )
        {
            exception = cnfe;
        }

        if ( exception != null )
        {
            if ( NSLog.debugLoggingAllowedForLevelAndGroups(
                     NSLog.DebugLevelCritical, NSLog.DebugGroupArchiving ) )
            {
                NSLog.debug.appendln( exception );
            }

            if ( kThrowOnError )
            {
                throw new NSForwardException( exception );
            }
        }

        return result;
    }


    //----------------------------------------------------------
    /**
     * Create an array from a property list string.
     * @param plist the property list to read from
     * @return a new MutableArray containing the keys and values from
     * the property list (The signature returns the base class type to be
     * compatible with the method in the base class, but the actual object
     * belongs to this subclass)
     */
    public static er.extensions.foundation.ERXMutableArray fromPropertyList(
        String plist )
    {
        NSArray a = (NSArray)NSPropertyListSerialization.
            propertyListFromString( plist );
        return new MutableArray( a );
    }


    //----------------------------------------------------------
    /**
     * Test this object to see if it has been changed (mutated) since it
     * was last saved.
     * @return true if this dictionary has been changed
     */
    public boolean hasChanged()
    {
        log.debug( "hasChanged() = " + hasChanged );
        return hasChanged;
    }


    //----------------------------------------------------------
    /**
     * Mark this object as having changed (mutated) since it
     * was last saved.
     * @param value true if this dictionary has been changed
     */
    public void setHasChanged( boolean value )
    {
        log.debug( "setHasChanged() = " + value );
        hasChanged = value;
        if ( hasChanged )
        {
            if ( parent != null )
            {
                parent.setHasChanged( value );
            }
            if ( owner != null )
            {
                owner.mutableContainerHasChanged();
            }
        }
    }


    //----------------------------------------------------------
    /**
     * Set the enclosing container that holds this one, if any.
     * @param parent a reference to the enclosing container
     */
    public void setParent( MutableContainer parent )
    {
        this.parent = parent;
    }


    //----------------------------------------------------------
    /**
     * Set the owner of this container.
     * @param owner the owner of this container container
     */
    public void setOwner( MutableContainerOwner owner )
    {
        this.owner = owner;
    }


    //----------------------------------------------------------
    /**
     * Set the enclosing container that holds this one, if any.
     * Also, recursively cycle through all contained mutable containers,
     * resetting their parents to this object as well.
     * @param parent a reference to the enclosing container
     */
    public void setParentRecursively( MutableContainer parent )
    {
        this.parent = parent;
        for ( int i = 0; i < count(); i++ )
        {
            Object o = objectAtIndex( i );

            if ( o instanceof MutableContainer )
            {
                ( (MutableContainer)o ).setParentRecursively( this );
            }
        }
    }


    //----------------------------------------------------------
    /**
     * Examine all contained objects within the specified range for
     * mutable containers, and reset the parent relationships for any
     * that are found.  Any NS containers found will be converted to
     * mutable versions.
     * @param start the starting index of the range
     * @param end   one greater than the last index in the range
     * @param recurse if true, force the reset to cascade recursively down
     *                the tree, rather than just affecting this node's
     *                immediate children.
     */
    public void resetChildParents( int start, int end, boolean recurse )
    {
        for ( int i = start; i < end; i++ )
        {
            Object o = objectAtIndex( i );
            if ( o instanceof MutableContainer )
            {
                MutableContainer mc = (MutableContainer)o;
                mc.setParent( this );
                if ( recurse )
                {
                    mc.resetChildParents( recurse );
                }
            }
            else if ( o instanceof NSDictionary )
            {
                set( i, new MutableDictionary( (NSDictionary)o ) );
            }
            else if ( o instanceof NSArray )
            {
                set( i, new MutableArray( (NSArray)o ) );
            }
        }
    }


    //----------------------------------------------------------
    /**
     * Examine all contained objects for mutable containers, and reset
     * the parent relationships for any that are found.  Any NS containers
     * found will be converted to mutable versions.
     * @param recurse if true, force the reset to cascade recursively down
     *                the tree, rather than just affecting this node's
     *                immediate children.
     */
    public void resetChildParents( boolean recurse )
    {
        resetChildParents( 0, count(), recurse );
    }


    //----------------------------------------------------------
    /**
     * Examine all contained objects within the specified range for
     * mutable containers, and clear the parent relationships for any
     * that are found.
     * @param start the starting index of the range
     * @param end   one greater than the last index in the range
     */
    public void clearChildParents( int start, int end )
    {
        for ( int i = start; i < end; i++ )
        {
            setParentIfPossible( objectAtIndex( i ), null );
        }
    }


    //----------------------------------------------------------
    /**
     * Examine all contained objects for mutable containers, and clear
     * the parent relationships for any that are found.
     */
    public void clearChildParents()
    {
        clearChildParents( 0, count() );
    }


    //----------------------------------------------------------
    /**
     * Retrieve the enclosing container that holds this one, if any.
     * @return a reference to the enclosing container
     */
    public MutableContainer parent()
    {
        return parent;
    }


    // ----------------------------------------------------------
    protected Object setParentIfPossible( Object o, MutableContainer p )
    {
        if ( o instanceof MutableContainer )
        {
            ( (MutableContainer)o ).setParent( p );
        }
        return o;
    }


    // ----------------------------------------------------------
    protected Object convertToMutableIfPossible( Object o )
    {
        if ( o instanceof MutableContainer )
        {
            ( (MutableContainer)o ).setParent( this );
        }
        else if ( o instanceof NSDictionary )
        {
            MutableDictionary md = new MutableDictionary( (NSDictionary)o );
            md.setParent( this );
            o = md;
        }
        else if ( o instanceof NSArray )
        {
            MutableArray ma = new MutableArray( (NSArray)o );
            ma.setParent( this );
            o = ma;
        }
        return o;
    }


    // ----------------------------------------------------------
    public void add( int arg0, Object arg1 )
    {
        setHasChanged( true );
        super.add( arg0, convertToMutableIfPossible( arg1 ) );
    }


    // ----------------------------------------------------------
    public boolean add( Object arg0 )
    {
        setHasChanged( true );
        return super.add( convertToMutableIfPossible( arg0 ) );
    }


    // ----------------------------------------------------------
    public boolean addAll( Collection arg0 )
    {
        setHasChanged( true );
        boolean result = super.addAll( arg0 );
        resetChildParents( false );
        return result;
    }


    // ----------------------------------------------------------
    public boolean addAll( int arg0, Collection arg1 )
    {
        setHasChanged( true );
        boolean result = super.addAll( arg0, arg1 );
        resetChildParents( false );
        return result;
    }


    // ----------------------------------------------------------
    public void addObject( Object arg0 )
    {
        setHasChanged( true );
        super.addObject( convertToMutableIfPossible( arg0 ) );
    }


    // ----------------------------------------------------------
    public void addObjects( Object[] arg0 )
    {
        setHasChanged( true );
        for ( int i = 0; i < arg0.length; i++ )
        {
            arg0[i] = convertToMutableIfPossible( arg0[i] );
        }
        super.addObjects( arg0 );
    }


    // ----------------------------------------------------------
    public void addObjectsFromArray( NSArray arg0 )
    {
        setHasChanged( true );
        super.addObjectsFromArray( arg0 );
        resetChildParents( false );
    }


    // ----------------------------------------------------------
    public Object remove( int arg0 )
    {
        setHasChanged( true );
        Object result = super.remove( arg0 );
        setParentIfPossible( result, null );
        return result;
    }


    // ----------------------------------------------------------
    public boolean remove( Object o )
    {
        boolean result = false;
        if ( o == null )
        {
            result = super.remove( o );
        }
        else
        {
            for ( Iterator e = iterator(); e.hasNext(); )
            {
                Object next = e.next();
                if ( o.equals( next ) )
                {
                    setParentIfPossible( next, null );
                    e.remove();
                    result = true;
                    break;
                }
            }
        }
        if ( result )
        {
            setHasChanged( true );
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean removeAll( Collection arg0 )
    {
        setHasChanged( true );
        clearChildParents();
        boolean result = super.removeAll( arg0 );
        resetChildParents( false );
        return result;
    }


    // ----------------------------------------------------------
    public void removeAllObjects()
    {
        setHasChanged( true );
        clearChildParents();
        super.removeAllObjects();
    }


    // ----------------------------------------------------------
    public boolean removeIdenticalObject( Object arg0 )
    {
        boolean result = super.removeIdenticalObject( arg0 );
        if ( result )
        {
            setHasChanged( true );
            setParentIfPossible( arg0, null );
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean removeIdenticalObject( Object arg0, NSRange arg1 )
    {
        boolean result = super.removeIdenticalObject( arg0, arg1 );
        if ( result )
        {
            setHasChanged( true );
            setParentIfPossible( arg0, null );
        }
        return result;
    }


    // ----------------------------------------------------------
    public Object removeLastObject()
    {
        if ( count() > 0 )
        {
            setHasChanged( true );
            setParentIfPossible( objectAtIndex( count() - 1 ), null );
        }
        return super.removeLastObject();
    }


    // ----------------------------------------------------------
    public boolean removeObject( Object o )
    {
        boolean result = false;
        if ( o == null )
        {
            result = super.removeObject( o );
        }
        else
        {
            for ( Iterator e = iterator(); e.hasNext(); )
            {
                Object next = e.next();
                if ( o.equals( next ) )
                {
                    setParentIfPossible( next, null );
                    e.remove();
                    result = true;
                }
            }
        }
        if ( result )
        {
            setHasChanged( true );
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean removeObject( Object arg0, NSRange arg1 )
    {
        boolean result = false;
        clearChildParents( arg1.location(), arg1.location() + arg1.length() );
        result = super.removeObject( arg0, arg1 );
        if ( result )
        {
            setHasChanged( true );
        }
        resetChildParents(
            arg1.location(), arg1.location() + arg1.length(), false );
        return result;
    }


    // ----------------------------------------------------------
    public Object removeObjectAtIndex( int arg0 )
    {
        setHasChanged( true );
        Object result = super.removeObjectAtIndex( arg0 );
        setParentIfPossible( result, null );
        return result;
    }


    // ----------------------------------------------------------
    public void removeObjects( Object[] arg0 )
    {
        setHasChanged( true );
        clearChildParents();
        super.removeObjects( arg0 );
        resetChildParents( false );
    }


    // ----------------------------------------------------------
    public void removeObjectsInArray( NSArray arg0 )
    {
        setHasChanged( true );
        clearChildParents();
        super.removeObjectsInArray( arg0 );
        resetChildParents( false );
    }


    // ----------------------------------------------------------
    public void removeObjectsInRange( NSRange arg0 )
    {
        setHasChanged( true );
        clearChildParents( arg0.location(), arg0.location() + arg0.length() );
        super.removeObjectsInRange( arg0 );
    }


    // ----------------------------------------------------------
    protected void removeRange( int arg0, int arg1 )
    {
        setHasChanged( true );
        clearChildParents( arg0, arg1 );
        super.removeRange( arg0, arg1 );
    }


    // ----------------------------------------------------------
    public boolean retainAll( Collection arg0 )
    {
        setHasChanged( true );
        clearChildParents();
        boolean result = super.retainAll( arg0 );
        resetChildParents( false );
        return result;
    }


    // ----------------------------------------------------------
    public Object set( int arg0, Object arg1 )
    {
        setHasChanged( true );
        setParentIfPossible( objectAtIndex( arg0 ), null );
        return super.set( arg0, convertToMutableIfPossible( arg1 ) );
    }


    // ----------------------------------------------------------
    public void setArray( NSArray arg0 )
    {
        setHasChanged( true );
        clearChildParents();
        super.setArray( arg0 );
        resetChildParents( false );
    }


    //----------------------------------------------------------
    /**
     * Replace this container's contents by copying from another (and
     * assuming parent ownership over any subcontainers).  The container
     * is free to assume the argument is of a compatible container type.
     * @param other the container to copy from
     */
    public void copyFrom( MutableContainer other )
    {
        setArray( (NSArray)other );
    }


    // ----------------------------------------------------------
    public void takeValueForKey( Object arg0, String arg1 )
    {
        setHasChanged( true );
        super.takeValueForKey( convertToMutableIfPossible( arg0 ), arg1 );
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath( Object arg0, String arg1 )
    {
        setHasChanged( true );
        super.takeValueForKeyPath( convertToMutableIfPossible( arg0 ), arg1 );
    }


    //~ Instance/static variables .............................................

    /**
     * This helps create the ByteArrayOutputStream with a good space estimate.
     */
    private static final int kOverheadAdjustment = 512;

    /**
     * This also helps create the ByteArrayOutputStream with a good space
     * estimate.
     */
    private static final int kCountBytesFactor = 16;

    /**
     * This determines, when an error occurs, if we should throw an
     * NSForwardException or just return null.
     */
    private static final boolean kThrowOnError = true;

    private boolean          hasChanged = false;
    private MutableContainer parent     = null;
    private transient MutableContainerOwner owner = null;

    static final long serialVersionUID = 6119252187335542077L;
    static Logger log = Logger.getLogger( MutableArray.class );
}
