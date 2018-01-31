/*==========================================================================*\
 |  $Id: JavascriptFunction.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2009 Virginia Tech
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

package org.webcat.ui.generators;

//-------------------------------------------------------------------------
/**
 * Represents a Javascript function during server-side JS generation.
 *
 * @author  Tony Allevato
 * @version $Id: JavascriptFunction.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public abstract class JavascriptFunction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new Javascript function that takes no arguments.
     */
    public JavascriptFunction()
    {
        this("");
    }


    // ----------------------------------------------------------
    /**
     * Initializes a new Javascript function that takes arguments specified as
     * a comma-separated list.
     *
     * @param args the arguments taken by the function
     */
    public JavascriptFunction(String args)
    {
        this.args = args;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * A convenience method that generates a Javascript function from a literal
     * Javascript code fragment.
     *
     * @param args the comma-separated list of arguments
     * @param code the Javascript code
     * @return a JavascriptFunction object that can be passed as an argument to
     *     another generator call
     */
    public static JavascriptFunction fromCode(String args, final String code)
    {
        return new JavascriptFunction(args) {
            @Override
            public void generate(JavascriptGenerator g)
            {
                g.append(code);

                if (!code.trim().endsWith(";"))
                {
                    g.append(";");
                }
            }
        };
    }


    // ----------------------------------------------------------
    /**
     * Gets the argument list of this function.
     *
     * @return the comma-separated list of arguments
     */
    public String args()
    {
        return args;
    }


    // ----------------------------------------------------------
    /**
     * Anonymous instances of this class must override this method to provide
     * the body of the function. The JavascriptGenerator passed into this
     * method can be used to generate this function.
     *
     * @param g the generator used to generate the body of this function
     */
    public abstract void generate(JavascriptGenerator g);


    //~ Static/instance variables .............................................

    private String args;
}
