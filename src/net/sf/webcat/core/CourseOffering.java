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
import com.webobjects.foundation.NSValidation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import java.io.File;
import org.apache.log4j.Logger;


// -------------------------------------------------------------------------
/**
 * Represents a single offering of a course (i.e., one section in a given
 * semester).
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class CourseOffering
    extends _CourseOffering
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new CourseOffering object.
     */
    public CourseOffering()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * Look up a CourseOffering by CRN.  Assumes the editing
     * context is appropriately locked.
     * @param ec The editing context to use
     * @param crn The CRN to look up
     * @return The course offering, or null if no such CRN exists
     */
    public static CourseOffering offeringForCrn(
        EOEditingContext ec, String crn )
    {
        CourseOffering offering = null;
        NSArray results = EOUtilities.objectsMatchingKeyAndValue( ec,
            ENTITY_NAME, CRN_KEY, crn );
        if ( results != null && results.count() > 0 )
        {
            offering = (CourseOffering)results.objectAtIndex( 0 );
        }
        return offering;
    }


    //~ Constants (for key names) .............................................

    // Derived Attributes ---
    public static final String COURSE_NUMBER_KEY  =
        COURSE_KEY + "." + Course.NUMBER_KEY;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Creates a string containing a comma-separated list of
     * instructor e-mail addresses.  Returns null if no instructors
     * for this course offering have been defined.
     *
     * @return the e-mail address(es) as a string
     */
    public NSArray studentsSortedByPID()
    {
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(
               students(),
               new NSArray( new Object[]{
                   new EOSortOrdering(
                           User.USER_NAME_KEY,
                           EOSortOrdering.CompareCaseInsensitiveAscending
                       )
               })
           );
    }


    // ----------------------------------------------------------
    /**
     * Returns the course's department abbreviation combined with
     * the course's number (e.g., "CS 1705").
     * @return the department abbreviation and the course number
     */
    public String compactName()
    {
        if ( cachedCompactName == null )
        {
            String label = label();
            if ( label == null || label.equals( "" ) )
            {
                label = crn();
            }
            if ( course() == null )
            {
                // !!!
                log.error(
                    "course offering with no associated course: " + crn()
                    + ((label() == null) ? "" : ("(" + label() + ")")));
                // don't cache!
                return "null(" + label + ")";
            }
            else
            {
                cachedCompactName = course().deptNumber() + "(" + label + ")";
            }
        }
        return cachedCompactName;
    }


    // ----------------------------------------------------------
    /**
     * Returns the course's department abbreviation combined with
     * the course's number (e.g., "CS 1705").
     * @return the department abbreviation and the course number
     */
    public String deptNumberAndName()
    {
        if ( cachedDeptNumberAndName == null )
        {
            cachedDeptNumberAndName = compactName() + ": " + course().name();
        }
        return cachedDeptNumberAndName;
    }


    // ----------------------------------------------------------
    /**
     * Get a short (no longer than 60 characters) description of this coursse
     * offering, which currently returns {@link #compactName()}.
     * @return the description
     */
    public String userPresentableDescription()
    {
        return compactName();
    }


    // ----------------------------------------------------------
    /**
     * Get a human-readable representation of this course offering, which is
     * the same as {@link #userPresentableDescription()}.
     * @return this course offering's short name
     */
    public String toString()
    {
        return userPresentableDescription();
    }


    // ----------------------------------------------------------
    /**
     * Returns true if the given user is an instructor of this
     * course offering
     *
     * @param user     The user to check
     * @return true if the user is an instructor of the offering
     */
    public boolean isInstructor( User user )
    {
        NSArray instructors = instructors();
        return ( ( instructors.indexOfObject( user ) ) != NSArray.NotFound );
    }


    // ----------------------------------------------------------
    /**
     * Returns true if the given user is a grader (TA) for this
     * course offering
     *
     * @param user     The user to check
     * @return true if the user is a grader for the offering
     * 
     * @deprecated Use the {@link #isGrader(User)} method instead.
     */
    @Deprecated
    public boolean isTA( User user )
    {
        return isGrader(user);
    }
    
    
    // ----------------------------------------------------------
    /**
     * Returns true if the given user is a grader (TA) for this
     * course offering
     *
     * @param user     The user to check
     * @return true if the user is a grader for the offering
     */
    public boolean isGrader( User user )
    {
        NSArray tas = graders();
        return ( ( tas.indexOfObject( user ) ) != NSArray.NotFound );
    }


    // ----------------------------------------------------------
    /**
     * Gets the array of graders (TAs) for this course offering.
     * 
     * @return the array of users who are designated as graders for this
     *     course offering
     *     
     * @deprecated Use the {@link #graders()} method instead.
     */
    @Deprecated
    public NSArray<User> TAs()
    {
        return graders();
    }
    
    
    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see net.sf.webcat.core._CourseOffering#setCourse(net.sf.webcat.core.Course)
     */
    public void setCourse( Course value )
    {
        cachedCompactName = null;
        cachedDeptNumberAndName = null;
        super.setCourse( value );
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>crn</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setCrn( String value )
    {
        saveOldDirComponents();
        cachedSubdirName = null;
        cachedCompactName = null;
        cachedDeptNumberAndName = null;
        super.setCrn( value.trim() );
    }


    // ----------------------------------------------------------
    public Object validateCrn( Object value )
    {
        if ( value == null || value.equals("") )
        {
            throw new ValidationException(
                "Please provide a unique CRN to identify your course "
                + "offering." );
        }
        NSArray others = EOUtilities.objectsMatchingKeyAndValue(
            editingContext(), ENTITY_NAME, CRN_KEY, value );
        if (others.count() > 1
            || (others.count() == 1
                && others.objectAtIndex(0) != this))
        {
            throw new ValidationException(
                "Another course offering with that CRN already exists." );
        }
        return value;
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>label</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setLabel( String value )
    {
        cachedCompactName = null;
        cachedDeptNumberAndName = null;
        super.setLabel( value );
    }


    // ----------------------------------------------------------
    public String crnSubdirName()
    {
        if ( cachedSubdirName == null )
        {
            String name = crn();
            cachedSubdirName = AuthenticationDomain.subdirNameOf( name );
            log.debug( "trimmed name '" + name + "' to '"
                       + cachedSubdirName + "'" );
        }
        return cachedSubdirName;
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>semester</code>
     * relationship (DO NOT USE--instead, use
     * <code>setSemesterRelationship()</code>.
     * This method is provided for WebObjects use.
     *
     * @param value The new entity to relate to
     */
    public void setSemester( Semester value )
    {
        log.debug("setSemester(" + value + ")");
        saveOldDirComponents();
        cachedSubdirName = null;
        super.setSemester(value);
    }


    // ----------------------------------------------------------
    public void takeValueForKey( Object value, String key )
    {
        log.debug("takeValueForKey(" + value + ", " + key + ")");
        if (SEMESTER_KEY.equals(key))
        {
            saveOldDirComponents();
            cachedSubdirName = null;
        }
        super.takeValueForKey( value, key );
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see er.extensions.eof.ERXGenericRecord#didUpdate()
     */
    public void didUpdate()
    {
        super.didUpdate();
        if ( crnDirNeedingRenaming != null || semesterDirNeedingRenaming != null)
        {
            renameSubdirs(
                semesterDirNeedingRenaming,
                ( semester() == null ? null : semester().dirName() ),
                crnDirNeedingRenaming,
                crnSubdirName() );
            crnDirNeedingRenaming = null;
            semesterDirNeedingRenaming = null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the name of the subdirectory where all submissions for this
     * course offering are stored.  This subdirectory is relative to
     * the base submission directory for some authentication domain, such
     * as the value returned by
     * {@link #submissionBaseDirName(AuthenticationDomain)}.
     * @param dir the string buffer to add the requested subdirectory to
     *        (a / is added to this buffer, followed by the subdirectory name
     *        generated here)
     * @param course the course whose subdir should be added (may not be null).
     */
    public void addSubdirTo( StringBuffer dir )
    {
        dir.append( '/' );
        dir.append( semester().dirName() );
        dir.append( '/' );
        dir.append( crnSubdirName() );
    }


    // ----------------------------------------------------------
    @Override
    public void mightDelete()
    {
        log.debug("mightDelete()");
        if (isNewObject()) return;
        if (hasAssignmentOfferings())
        {
            log.debug("mightDelete(): offering has assignments");
            throw new ValidationException("You may not delete a course "
                + "offering that has assignment offerings.");
        }
        StringBuffer buf = new StringBuffer("/");
        addSubdirTo(buf);
        subdirToDelete = buf.toString();
        super.mightDelete();
    }


    // ----------------------------------------------------------
    @Override
    public boolean canDelete()
    {
        boolean result = (course() == null
            || editingContext() == null
            || !hasAssignmentOfferings());
        log.debug("canDelete() = " + result);
        return result;
    }


    // ----------------------------------------------------------
    @Override
    public void didDelete( EOEditingContext context )
    {
        log.debug("didDelete()");
        super.didDelete( context );
        // should check to see if this is a child ec
        EOObjectStore parent = context.parentObjectStore();
        if (parent == null || !(parent instanceof EOEditingContext))
        {
            if (subdirToDelete != null)
            {
                NSArray domains = AuthenticationDomain.authDomains();
                for ( int i = 0; i < domains.count(); i++ )
                {
                    AuthenticationDomain domain =
                        (AuthenticationDomain)domains.objectAtIndex( i );
                    StringBuffer dir = domain.submissionBaseDirBuffer();
                    dir.append(subdirToDelete);
                    File courseDir = new File(dir.toString());
                    if (courseDir.exists())
                    {
                        net.sf.webcat.archives.FileUtilities.deleteDirectory(
                            courseDir);
                    }
                }
            }
        }
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private boolean hasAssignmentOfferings()
    {
        if (isNewObject()) return false;
        // This method introduces some minor conceptual coupling with
        // the Grader subsystem.  However, it avoids all binary dependencies
        // and it is necessary to preserve the integrity of the data
        // model across the subsystems.

        // This code is basically the same as that in
        // _AssignmentOffering.objectsForCourseOffering()
        EOFetchSpecification spec = EOFetchSpecification
            .fetchSpecificationNamed( "courseOffering", "AssignmentOffering" );
        NSMutableDictionary bindings = new NSMutableDictionary();
        bindings.setObjectForKey( this, "courseOffering" );
        spec = spec.fetchSpecificationWithQualifierBindings( bindings );

        NSArray result = editingContext().objectsWithFetchSpecification( spec );
        if (log.isDebugEnabled())
        {
            log.debug("hasAssignmentOfferings(): fetch = " + result);
        }
        return result.count() > 0;
    }


    // ----------------------------------------------------------
    private void renameSubdirs(
        String oldSemesterSubdir, String newSemesterSubdir,
        String oldCrnSubdir,      String newCrnSubdir )
    {
        NSArray domains = AuthenticationDomain.authDomains();
        String msgs = null;
        for ( int i = 0; i < domains.count(); i++ )
        {
            AuthenticationDomain domain =
                (AuthenticationDomain)domains.objectAtIndex( i );
            StringBuffer dir = domain.submissionBaseDirBuffer();
            dir.append('/');
            int baseDirLen = dir.length();
            dir.append(oldSemesterSubdir);
            dir.append('/');
            dir.append( oldCrnSubdir );
            File oldDir = new File( dir.toString() );
            log.debug("Checking for: " + oldDir);
            if ( oldDir.exists() )
            {
                dir.delete( baseDirLen, dir.length() );
                dir.append(newSemesterSubdir);

                // First, make sure that the new dir exists!
                File newDir = new File( dir.toString() );
                if (!newDir.exists())
                {
                    newDir.mkdirs();
                }

                dir.append('/');
                dir.append( newCrnSubdir );
                newDir = new File( dir.toString() );

                // Do the renaming
                log.debug("Renaming: " + oldDir + " => " + newDir);
                if (!oldDir.renameTo( newDir ))
                {
                    msgs = (msgs == null ? "" : (msgs + "  "))
                        + "Failed to rename directory: "
                        + oldDir + " => " + newDir;
                }
            }
        }
        if (msgs != null)
        {
            throw new RuntimeException(msgs);
        }
    }


    // ----------------------------------------------------------
    private void saveOldDirComponents()
    {
        if (crnDirNeedingRenaming == null || semesterDirNeedingRenaming == null)
        {
            if ( crnDirNeedingRenaming == null && crn() != null )
            {
                crnDirNeedingRenaming = crnSubdirName();
            }
            if ( semesterDirNeedingRenaming == null && semester() != null )
            {
                semesterDirNeedingRenaming = semester().dirName();
            }
        }
    }


    //~ Instance/static variables .............................................

    private String cachedSubdirName           = null;
    private String cachedCompactName          = null;
    private String cachedDeptNumberAndName    = null;
    private String semesterDirNeedingRenaming = null;
    private String crnDirNeedingRenaming      = null;
    private String subdirToDelete;

    static Logger log = Logger.getLogger( CourseOffering.class );
}
