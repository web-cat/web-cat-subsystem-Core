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
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import java.util.Properties;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * A page that allows the user to e-mail a feedback message to the
 * administrator.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class FeedbackPage
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new FeedbackPage object.
     *
     * @param context The page's context
     */
    public FeedbackPage( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    /** Holds the comments the user has entered. */
    public String comments;

    /** A list of subject categories for the user's feedback. */
    public NSMutableArray categories;

    /** The selected subject category. */
    public String selectedCategory;

    /** A flag indicating when the message has been successfully sent. */
    public boolean sent = false;

    /** The title of the page where the feedback request originated (can be
     *  null). */
    public String pageTitle;
    
    /** A string of addiitonal information generated for a feedback request. */
    public Object extraInfo;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        if ( categories == null )
        {
            categories = baseCategories.mutableClone();
            if ( pageTitle != null )
            {
                categories.insertObjectAtIndex( "Page titled: " + pageTitle,
                                                0 );
            }
        }
        super.appendToResponse(response, context);
        if ( sent )
        {
            // Reset the page for the next go around
            comments = null;
        }
    }


    // ----------------------------------------------------------
    public WOComponent sendFeedback()
    {
        StringBuffer body = new StringBuffer();
        body.append(   "User     :  " );
        body.append( wcSession().primeUser().nameAndUid() );
        body.append( "\nSubject  :  " );
        body.append( selectedCategory );
        body.append( "\nPage     :  " );
        body.append( pageTitle );
        body.append( "\nDate/Time:  " );
        body.append( new NSTimestamp() );
        body.append( "\n\n" );
        body.append( comments );
        body.append( "\n\n" );
        if ( extraInfo != null )
        {
            body.append( extraInfo );
            body.append( "\n" );
        }
        Application.sendAdminEmail( null, null, true,
            "Feedback: " + selectedCategory,
            body.toString(),
            null );
        sent = true;
        return null;
    }


    //~ Instance/static variables .............................................

    private static final NSArray baseCategories = new NSArray( new String[]{
        "A general comment about Web-CAT",
        "A comment regarding a specific class assignment",
        "A bug to report",
        "A feature request",
        "A suggestion for improvement",
        "Page layout or web design",
        "Quality of help pages"
        } );

    static Logger log = Logger.getLogger( FeedbackPage.class );
}
