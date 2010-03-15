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

package net.sf.webcat.core.vfs;

import java.util.regex.Pattern;
import net.sf.webcat.core.FileUtilities;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSComparator.ComparisonException;

//-------------------------------------------------------------------------
/**
 * Represents a file-like object in a virtual file system. A "file-like object"
 * can be an actual file or folder, an archive that can be treated as both
 * kinds of objects, or a "virtual" directory that does not correspond to an
 * actual file system location but merely acts as a container for other
 * file-like objects.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public abstract class WCFile
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the WCFile class.
     *
     * @param parent the parent of this file-like object, or null if it is a
     *     root
     */
    public WCFile(WCFile parent)
    {
        this.parent = parent;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether this WCFile object points to the same
     * location on its virtual file system as the specified object.
     *
     * @param obj the object to compare this WCFile object to
     * @return true if obj is a WCFile and it points to the same location as
     *     this WCFile object, otherwise false
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof WCFile)
        {
            return equals((WCFile) obj);
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether this WCFile object points to the same
     * location on its virtual file system as the specified object.
     *
     * @param otherFile the file to compare this WCFile object to
     * @return true if this WCFile object and the passed object point to the
     *     same location on the virtual file system, otherwise false
     */
    public abstract boolean equals(WCFile otherFile);


    // ----------------------------------------------------------
    /**
     * Gets a string representation of this object useful for debugging.
     *
     * @return a string representation of this file object
     */
    @Override
    public String toString()
    {
        return "<" + getClass().getSimpleName() + ": " + path() + ">";
    }


    // ----------------------------------------------------------
    /**
     * Gets the name of this file-like object.
     *
     * @return the name of the file-like object
     */
    public abstract String name();


    // ----------------------------------------------------------
    /**
     * Gets the path to this file-like object, relative to its root.
     *
     * @return the path to this file-like object
     */
    public final String path()
    {
        if (isRoot())
        {
            return SEPARATOR + name();
        }
        else
        {
            return parent().path() + SEPARATOR + name();
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the string used to separate path components in a virtual file
     * system path (the forward slash symbol, "/").
     *
     * @return the path separator string
     */
    public static String pathSeparator()
    {
        return SEPARATOR;
    }


    // ----------------------------------------------------------
    /**
     * Splits a path string into an array of path components.
     *
     * @param path the path string
     * @return an array of path components
     */
    public static NSArray<String> splitPathComponents(String path)
    {
        if (path.startsWith(SEPARATOR))
        {
            path = path.substring(SEPARATOR.length());
        }

        NSMutableArray<String> components = new NSMutableArray<String>();
        String[] componentArray = path.split(Pattern.quote(SEPARATOR));

        for (String component : componentArray)
        {
            components.addObject(component);
        }

        return components;
    }


    // ----------------------------------------------------------
    /**
     * Gets the array of path components for the path of this file. This is
     * equivalent to calling <tt>WCFile.splitPathComponents(someFile.path())</tt>.
     *
     * @return an array of path components
     */
    public NSArray<String> pathComponents()
    {
        return splitPathComponents(path());
    }


    // ----------------------------------------------------------
    /**
     * Combines an array of path components into a path string.
     *
     * @param components the array of path components
     * @return the path string
     */
    public static String combinePathComponents(NSArray<String> components)
    {
        StringBuffer buffer = new StringBuffer(48);

        for (String component : components)
        {
            buffer.append(SEPARATOR);
            buffer.append(component);
        }

        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the specified path string is an absolute
     * or relative path.
     *
     * @param path a path string
     * @return true if the path string is an absolute path, otherwise false
     */
    public static boolean isPathAbsolute(String path)
    {
        return path.startsWith(SEPARATOR);
    }


    // ----------------------------------------------------------
    /**
     * Gets the length of this file-like object in bytes. Objects that do not
     * have a meaningful length, like folders, should return
     * {@link #LENGTH_NOT_APPLICABLE}.
     *
     * @return the length of the file-like object in bytes
     */
    public abstract long length();


    // ----------------------------------------------------------
    /**
     * Gets the last-modified time of this file-like object. This method may
     * return null if the concept of a last-modified time does not apply to a
     * type of file-like object.
     *
     * @return the last-modified time of the file-like object, or null
     */
    public abstract NSTimestamp lastModified();


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets a reference to an adapter interface that can be used to perform
     * additional operations that this file object supports. If the object does
     * not support the specified adapter interface, this method returns null,
     * so this can be used to test that operations are available.
     * </p><p>
     * The following adapter interfaces are currently supported:
     * </p>
     * <ul>
     * <li>{@link IReadableFile}: this object is a file from which data can be
     * read</li>
     * <li>{@link IWritableFile}: this object is a file to which data can be
     * written</li>
     * <li>{@link IReadWritableFile}: a combination of the above two adapters
     * </li>
     * <li>{@link IModifiableContainer}: a container that can have other files
     * and containers added to it
     * </ul>
     * <p>
     * Implementors should call the superclass implementation if the adapter
     * type is not one that they recognize.
     * </p>
     *
     * @param <T> the interface type that this object should be adapted to
     * @param adapterType the Class object for the interface type that this
     *     object should be adapted to
     * @return the adapter reference, or null if the object cannot be adapted
     *     to the requested type
     */
    public <T> T adaptTo(Class<? extends T> adapterType)
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the file represented by this object is
     * backed directly by an actual file or folder on the file system. For
     * example, children of a zip archive are not directly on the file system.
     *
     * @return true if this object is backed directly by a file on the file
     *     system
     */
    public abstract boolean isPhysicalFile();


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets a java.io.File object that can be used to get a path that external
     * processes can use to access the file.
     * </p><p><b>
     * This is a work in progress. Eventually it would be used to provide
     * automatic extraction of files inside zip files so that they can be
     * accessed just as regular files on the file system would be used.
     * </b></p>
     *
     * @return a java.io.File object that can be used to access the file by
     *     external processes
     */
    public abstract java.io.File externalizeFile();


    // ----------------------------------------------------------
    /**
     * Gets the parent of this file-like object, or null if it is a root.
     *
     * @return the parent of this file-like object, or null
     */
    public final WCFile parent()
    {
        return parent;
    }


    // ----------------------------------------------------------
    /**
     * Gets the root of the virtual file system upon which this file-like
     * object is located.
     *
     * @return the root of the virtual file system upon which this file-like
     *     object is located
     */
    public final WCFile root()
    {
        WCFile current = this;
        while (!current.isRoot())
        {
            current = current.parent();
        }

        return current;
    }


    // ----------------------------------------------------------
    /**
     * Visits the children of this file-like object, calling the specified
     * visitor's {@link IChildVisitor#visit} method on each child. The
     * visitation should cease immediately if the {@link IChildVisitor#visit}
     * method returns false.
     *
     * @param visitor the object that will handle each child as it is visited
     */
    protected abstract void visitChildren(IChildVisitor visitor);


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets a value indicating whether this object can contain files and other
     * containers.
     * </p><p>
     * Note that an object being a container does not necessarily mean that new
     * files and folders can be <b>created</b> in the container; for example,
     * a ZIP archive is a container but it cannot be modified. To determine
     * writability, you should ask the container for its
     * {@link IModifiableContainer} adapter.
     * </p>
     *
     * @return true if this object is a container that can hold other files and
     *     containers, otherwise false
     */
    public abstract boolean isContainer();


    // ----------------------------------------------------------
    /**
     * Gets the children of this file-like object. If the concept of children
     * is not appropriate for a type of virtual file (such as a native file on
     * the physical file system), it should return an empty array.
     *
     * @return an NSArray containing WCFile instances
     */
    public final NSArray<WCFile> children()
    {
        final NSMutableArray<WCFile> children = new NSMutableArray<WCFile>();

        visitChildren(new IChildVisitor() {
            public boolean visit(WCFile file)
            {
                children.addObject(file);
                return true;
            }
        });

        try
        {
            children.sortUsingComparator(new NSComparator() {
                @Override
                public int compare(Object lhs_, Object rhs_)
                throws ComparisonException
                {
                    WCFile lhs = (WCFile) lhs_;
                    WCFile rhs = (WCFile) rhs_;
                    return NSComparator
                        .AscendingCaseInsensitiveStringComparator
                        .compare(lhs.name(), rhs.name());
                }
            });
        }
        catch (ComparisonException e)
        {
            // Do nothing.
        }

        return children;
    }


    // ----------------------------------------------------------
    public final WCFile childWithName(String name)
    {
        ChildWithNameVisitor visitor = new ChildWithNameVisitor();
        visitor.name = name;

        visitChildren(visitor);

        return visitor.file;
    }


    // ----------------------------------------------------------
    public WCFile fileWithPath(String path)
    {
        WCFile current = isPathAbsolute(path) ? root() : this;

        NSArray<String> pathComponents = splitPathComponents(path);
        for (String component : pathComponents)
        {
            current = current.childWithName(component);

            if (current == null)
            {
                break;
            }
        }

        return current;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether this file-like object represents the
     * root of its hierarchy.
     *
     * @return true if this file-like object is the root of its hierarchy
     */
    public boolean isRoot()
    {
        return parent == null;
    }


    // ----------------------------------------------------------
    /**
     * Gets a string that represents the human-readable length of the file;
     * that is, it is shown as a fraction of the largest applicable unit (byte,
     * kilobyte, or megabyte).
     *
     * @return the human-readable length of the file, or the empty string if
     *     this file-like object does not have an applicable length
     */
    public String humanReadableLength()
    {
        long length = length();

        return (length == LENGTH_NOT_APPLICABLE) ? "" :
            FileUtilities.fileSizeAsString(length());
    }


    // ----------------------------------------------------------
    /**
     * Gets the name of the framework that should be used to retrieve the image
     * pointed to by {@link #iconURL}.
     *
     * @return the name of the framework containing the icon image
     */
    public String iconFramework()
    {
        return "Core";
    }


    // ----------------------------------------------------------
    /**
     * Gets the WebServerResources-relative URL to an image that can be used as
     * the icon for this file-like object when displaying it in the user
     * interface. This URL should be relative to the <tt>WebServerResources</tt>
     * folder in the framework returned by {@link #iconFramework}.
     *
     * @return the URL to the file's icon
     */
    public abstract String iconURL();


    //~ Protected interfaces ..................................................

    // ----------------------------------------------------------
    /**
     * Implemented in order to visit the children of a file-like object.
     */
    protected interface IChildVisitor
    {
        //~ Methods ...........................................................

        // ----------------------------------------------------------
        /**
         * Called when a file-like object is visited.
         *
         * @param file the file-like object being visited
         * @return true to continue the visitation, false to stop immediately
         */
        boolean visit(WCFile file);
    }


    //~ Private classes .......................................................

    // ----------------------------------------------------------
    /**
     * A visitor that stops visiting when a file with a specified name has been
     * found, and stores the corresponding {@link WCFile} reference so that it
     * can be retrieved later.
     */
    private class ChildWithNameVisitor implements IChildVisitor
    {
        //~ Methods ...........................................................

        // ----------------------------------------------------------
        public boolean visit(WCFile file)
        {
            if (file.name().equalsIgnoreCase(name))
            {
                this.file = file;
                return false;
            }
            else
            {
                return true;
            }
        }


        //~ Static/instance variables .........................................

        public String name;
        public WCFile file = null;
    }


    //~ Static/instance variables .............................................

    public static final long LENGTH_NOT_APPLICABLE = -1L;

    private static final String SEPARATOR = "/";

    private WCFile parent;
}
