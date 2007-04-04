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

import er.extensions.*;

//-------------------------------------------------------------------------
/**
 * A basic, CSS-style-controlled progress bar component.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class ProgressBar
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Default constructor.
     * @param context The page's context
     */
    public ProgressBar( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public double valueMin = 0.0;
    public double valueMax = 100.0;
    public double value    = 0.0;
    
    public static final int WIDTH = 200;

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get the width of this progress bar's active portion in pixels.
     * @return The width of the filled portion of the bar
     */
    public int width()
    {
        return (int)(WIDTH * fraction() + 0.5 );
    }


    // ----------------------------------------------------------
    /**
     * Get the nearest integer percent value (from 0-100) for this bar's
     * active portion.
     * @return The width of the filled portion of the bar as an integer
     * percent value
     */
    public int percent()
    {
        return (int)( fraction() * 100.0 + 0.5 );
    }


    // ----------------------------------------------------------
    /**
     * Get a string-formatted version of the {@link #percent()} result,
     * including a percent sign (%).
     * @return The percent as a string
     */
    public String percentLabel()
    {
        return "" + percent() + "%";
    }


    // ----------------------------------------------------------
    /**
     * Get the fractional value (from 0.0-1.0) for this bar's
     * active portion.
     * @return The width of the filled portion of the bar as a real
     * number between 0.0-1.0
     */
    public double fraction()
    {
        double result = ( value - valueMin )/( valueMax - valueMin );
        if ( result < 0.0 )
        {
            result = 0.0;
        }
        else if ( result > 1.0 )
        {
            result = 1.0;
        }
        return result;
    }
}
