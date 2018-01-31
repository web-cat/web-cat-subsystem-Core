/*==========================================================================*\
 |  $Id: CourseOffering.java,v 1.6 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

package org.webcat.core;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import er.extensions.eof.ERXKey;
import er.extensions.foundation.ERXArrayUtilities;
import java.io.File;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Represents a single offering of a course (i.e., one section in a given
 * semester).
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.6 $, $Date: 2012/03/28 13:48:08 $
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
     * @param theCrn The CRN to look up
     * @return The course offering, or null if no such CRN exists
     */
    public static CourseOffering offeringForCrn(
        EOEditingContext ec, String theCrn)
    {
        CourseOffering offering = null;
        NSArray<CourseOffering> results = objectsMatchingQualifier(
            ec, CourseOffering.crn.eq(theCrn));
        if (results != null && results.count() > 0)
        {
            offering = results.objectAtIndex(0);
        }
        return offering;
    }


    //~ Constants (for key names) .............................................

    // Derived Attributes ---
    public static final String COURSE_NUMBER_KEY =
        COURSE_KEY + "." + Course.NUMBER_KEY;
    public static final ERXKey<Integer> courseNumber =
        new ERXKey<Integer>(COURSE_NUMBER_KEY);


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Returns the list of students in this course offering, sorted by
     * user name.
     *
     * @return the sorted list of enrolled students
     */
    public NSArray<User> studentsSortedByPID()
    {
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(
               students(),
               User.userName.ascInsensitives());
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
            String myLabel = label();
            if ( myLabel == null || myLabel.equals( "" ) )
            {
                myLabel = crn();
            }
            if ( course() == null )
            {
                // !!!
                log.error(
                    "course offering with no associated course: " + crn()
                    + ((label() == null) ? "" : ("(" + label() + ")")));
                // don't cache!
                return "null (" + myLabel + ")";
            }
            else
            {
                cachedCompactName = course().deptNumber() + " (" + myLabel + ")";
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
     * Returns true if the given user is an instructor of this
     * course offering.
     *
     * @param user     The user to check
     * @return true if the user is an instructor of the offering
     */
    public boolean isInstructor(User user)
    {
        NSArray<User> myInstructors = instructors();
        return (myInstructors.indexOfObject(user) != NSArray.NotFound);
    }


    // ----------------------------------------------------------
    /**
     * Returns true if the given user is a grader (TA) for this
     * course offering.
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
     * course offering.
     *
     * @param user     The user to check
     * @return true if the user is a grader for the offering
     */
    public boolean isGrader( User user )
    {
        NSArray<User> tas = graders();
        return ( ( tas.indexOfObject( user ) ) != NSArray.NotFound );
    }


    // ----------------------------------------------------------
    /**
     * Returns true if the given user is a member of the staff (an
     * instructor or grader) for this course offering.
     *
     * @param user     The user to check
     * @return true if the user is staff for the offering
     */
    public boolean isStaff(User user)
    {
        return isInstructor(user) || isGrader(user);
    }


    // ----------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accessibleByUser(User user)
    {
        return isStaff(user);
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
    /**
     * Gets the array of users that includes all course staff (instructors or
     * graders).
     *
     * @return the array of users
     */
    public NSArray<User> staff()
    {
        NSMutableArray<User> staff = instructors().mutableClone();
        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(
            staff, graders());
        return staff;
    }


    // ----------------------------------------------------------
    /**
     * Gets the array of users that includes students enrolled in this
     * course offering together with all course staff (instructors or
     * graders).
     *
     * @return the array of users
     */
    public NSArray<User> studentsAndStaff()
    {
        NSMutableArray<User> studentsAndStaff = students().mutableClone();
        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(
            studentsAndStaff, instructors());
        ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates(
            studentsAndStaff, graders());
        return studentsAndStaff;
    }


    // ----------------------------------------------------------
    /**
     * Gets the array of students enrolled in this course offering, not
     * including any course staff (instructors or graders).
     *
     * @return the array of students
     */
    public NSArray<User> studentsWithoutStaff()
    {
        NSMutableArray<User> studentsWithoutStaff = students().mutableClone();
        studentsWithoutStaff.removeObjectsInArray(instructors());
        studentsWithoutStaff.removeObjectsInArray(graders());
        return studentsWithoutStaff;
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see org.webcat.core._CourseOffering#setCourse(org.webcat.core.Course)
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
        NSArray<CourseOffering> others = objectsMatchingQualifier(
            editingContext(), crn.is(value.toString()));
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
                NSArray<AuthenticationDomain> domains =
                    AuthenticationDomain.authDomains();
                for (AuthenticationDomain domain : domains)
                {
                    StringBuffer dir = domain.submissionBaseDirBuffer();
                    dir.append(subdirToDelete);
                    File courseDir = new File(dir.toString());
                    if (courseDir.exists())
                    {
                        FileUtilities.deleteDirectory(courseDir);
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
            .fetchSpecificationNamed(
                "offeringsForCourseOffering", "AssignmentOffering");
        NSMutableDictionary<String, Object> bindings =
            new NSMutableDictionary<String, Object>();
        bindings.setObjectForKey( this, "courseOffering" );
        spec = spec.fetchSpecificationWithQualifierBindings( bindings );

        NSArray<?> result =
            editingContext().objectsWithFetchSpecification( spec );
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
        NSArray<AuthenticationDomain> domains =
            AuthenticationDomain.authDomains();
        String msgs = null;
        for (AuthenticationDomain domain : domains)
        {
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
        if (crnDirNeedingRenaming == null
            || semesterDirNeedingRenaming == null)
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
