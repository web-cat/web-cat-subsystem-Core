/*==========================================================================*\
 |  $Id$
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
 * A proxy object that provides methods available on Dijit widget. See
 * <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dijit._Widget">dijit._Widget</a>
 * for more information, as well as the documentation for each of the
 * individual methods below.
 * </p><p>
 * This class is not intended to and cannot be instantiated by users. These
 * proxy objects are created by {@link JavascriptGenerator} when chaining
 * calls together that involve different types of objects.
 * </p>
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public class DijitProxy extends JavascriptProxy
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the DijitProxy class.
     *
     * @param generator the generator to use
     * @param id the Dijit ID of the widget
     */
    /*package*/ DijitProxy(JavascriptGenerator generator, String id)
    {
        super(generator, "dijit.byId('" + id + "')");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * <p>
     * Sets the value of a named property on a widget.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dijit._Widget.attr">dijit._Widget.attr</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param attribute the named property to set
     * @param value the value of the property
     * @return this proxy object, for chaining
     */
    public DijitProxy attr(String attribute, Object value)
    {
        appendToFunctionChain("attr('" + attribute + "', "
                + generator.javascriptObjectFor(value) + ")");

        return this;
    }


    // ----------------------------------------------------------
    /**
     * A convenience method to disable this widget. Equivalent to calling
     * <code>attr("disabled", true)</code>.
     *
     * @return this proxy object, for chaining
     */
    public DijitProxy disable()
    {
        return attr("disabled", true);
    }


    // ----------------------------------------------------------
    /**
     * A convenience method to enable this widget. Equivalent to calling
     * <code>attr("disabled", false)</code>.
     *
     * @return this proxy object, for chaining
     */
    public DijitProxy enable()
    {
        return attr("disabled", false);
    }
}
