/*==========================================================================*\
 |  $Id: FeedbackPage.java,v 1.2 2011/03/07 18:44:37 stedwar2 Exp $
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

package org.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import org.webcat.core.Application;
import org.webcat.core.FeedbackPage;
import org.webcat.core.WCComponent;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * A page that allows the user to e-mail a feedback message to the
 * administrator.
 *
 * @author Stephen Edwards
 * @author Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2011/03/07 18:44:37 $
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
    public NSMutableArray<String> categories;

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
        body.append(" <");
        body.append( wcSession().primeUser().email() );
        body.append("> from ");
        body.append( wcSession().primeUser().authenticationDomain()
            .displayableName() );
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

        Application.sendAdminEmail(
            "Feedback: " + selectedCategory,
            body.toString() );
        sent = true;

        return null;
    }


    //~ Instance/static variables .............................................

    private static final NSArray<String> baseCategories =
        new NSArray<String>( new String[] {
            "A general comment about Web-CAT",
            "A comment regarding a specific class assignment",
            "A bug to report",
            "A feature request",
            "A suggestion for improvement",
            "Page layout or web design",
            "Quality of help pages"
        } );

    static Logger log = Logger.getLogger(FeedbackPage.class);
}
