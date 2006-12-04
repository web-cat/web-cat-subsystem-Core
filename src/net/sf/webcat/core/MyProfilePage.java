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

//-------------------------------------------------------------------------
/**
* Represents a standard Web-CAT page that has not yet been implemented
* (is "to be defined").
*
*  @author Stephen Edwards
*  @version $Id$
*/
public class MyProfilePage
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new TBDPage object.
     *
     * @param context The context to use
     */
    public MyProfilePage( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public String              newPassword1;
    public String              newPassword2;
    public WODisplayGroup      enrolledInDisplayGroup;
    public User                instructor;
    public WODisplayGroup      TADisplayGroup;
    public WODisplayGroup      teachingDisplayGroup;
    public CourseOffering      courseOffering;
    public int                 index;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        enrolledInDisplayGroup.setMasterObject( wcSession().user() );
        teachingDisplayGroup.setMasterObject( wcSession().user() );
        TADisplayGroup.setMasterObject( wcSession().user() );
        super.appendToResponse( response, context );
    }


    // ----------------------------------------------------------
    public boolean applyLocalChanges()
    {
        User u = wcSession().localUser();
        String lcPassword = ( newPassword1 == null )
            ? null
            : newPassword1.toLowerCase();
        if (  u.canChangePassword()
           && (  newPassword1 != null
              || newPassword2 != null ) )
        {
            if ( newPassword1 == null || newPassword2 == null )
            {
                error(
                    "To change your password, complete both password fields." );
            }
            else if ( !newPassword1.equals( newPassword2 ) )
            {
                error(
                    "The two password fields do not match.  "
                    + "Please re-enter your password." );
            }
            else if ( newPassword1.length() < 6 )
            {
                error(
                    "Your password must be at least six characters long." );
            }
            else if (  lcPassword.equals( u.userName().toLowerCase() )
                    || lcPassword.equals( u.firstName().toLowerCase() )
                    || lcPassword.equals( u.lastName().toLowerCase() ) )
            {
                error(
                    "You may not use your name as a password.  "
                    + "Please enter a different password." );
            }
            else if ( newPassword1.equals( u.password() ) )
            {
                error( "The password you have specified is already "
                       + "your current password." );
            }
            if ( !hasMessages() )
            {
                u.changePassword( newPassword1 );
                confirmationMessage( "Your password has been changed." );
            }
        }
        newPassword1 = null;
        newPassword2 = null;
        // TODO: include user validation here
//        wcSession().commitLocalChanges();
//        return pageWithName( wcSession().currentTab().selectDefaultSibling()
//                             .selectedPageName() );
        return super.applyLocalChanges();
    }
}
