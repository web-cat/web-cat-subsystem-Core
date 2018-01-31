/*==========================================================================*\
 |  $Id: AnimationProxy.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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
 * <p>
 * A proxy object that provides methods available on a Dojo animation. See
 * <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.Animation">dojo.Animation</a>
 * for more information, as well as the documentation for each of the
 * individual methods below.
 * </p><p>
 * This class is not intended to and cannot be instantiated by users. These
 * proxy objects are created by {@link JavascriptGenerator} when chaining
 * calls together that involve different types of objects.
 * </p>
 *
 * @author  Tony Allevato
 * @version $Id: AnimationProxy.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class AnimationProxy extends JavascriptProxy
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the AnimationProxy class.
     *
     * @param generator the generator to use
     * @param root the root expression of this proxy
     */
    /*package*/ AnimationProxy(JavascriptGenerator generator, String root)
    {
        super(generator, root);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Plays this animation.
     *
     * @return this proxy object, for chaining
     */
    public AnimationProxy play()
    {
        return play(null, null);
    }


    // ----------------------------------------------------------
    /**
     * Plays this animation.
     *
     * @param delay the number of milliseconds to delay starting this animation
     * @return this proxy object, for chaining
     */
    public AnimationProxy play(Integer delay)
    {
        return play(delay, null);
    }


    // ----------------------------------------------------------
    /**
     * Plays this animation.
     *
     * @param delay the number of milliseconds to delay starting this animation
     * @param gotoStart true to start this animation over from the beginning
     * @return this proxy object, for chaining
     */
    public AnimationProxy play(Integer delay, Boolean gotoStart)
    {
        appendToFunctionChain("play(" +
                generator.argumentsForCall(delay, gotoStart) + ")");
        return this;
    }
}
