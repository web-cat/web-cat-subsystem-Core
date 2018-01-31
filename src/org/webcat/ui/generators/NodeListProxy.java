/*==========================================================================*\
 |  $Id: NodeListProxy.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

import java.util.Map;

//-------------------------------------------------------------------------
/**
 * <p>
 * A proxy object that provides methods available on a NodeList. See
 * <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList">dojo.NodeList</a>
 * for more information, as well as the documentation for each of the
 * individual methods below.
 * </p><p>
 * This class is not intended to and cannot be instantiated by users. These
 * proxy objects are created by {@link JavascriptGenerator} when chaining
 * calls together that involve different types of objects.
 * </p>
 *
 * @author  Tony Allevato
 * @version $Id: NodeListProxy.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class NodeListProxy extends JavascriptProxy
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the NodeListProxy class.
     *
     * @param generator the generator to use
     * @param pattern the CSS selector to match
     */
    /*package*/ NodeListProxy(JavascriptGenerator generator, String pattern)
    {
        super(generator, "dojo.query(\"" + pattern + "\")");
    }


    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the NodeListProxy class.
     *
     * @param generator the generator to use
     * @param pattern the CSS selector to match
     * @param root the ID of the DOM element to use as the scope of the search
     */
    /*package*/ NodeListProxy(JavascriptGenerator generator, String pattern,
            String root)
    {
        super(generator, "dojo.query(\"" + pattern + "\", \"" + root + "\")");
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Adds a CSS class to each node in the list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.addClass">dojo.NodeList.addClass</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param cssClass the CSS class to add
     * @return this node list, for chaining
     */
    public NodeListProxy addClass(String cssClass)
    {
        appendToFunctionChain("addClass("
                + generator.argumentsForCall(cssClass) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Animate one or more CSS properties for all the nodes in this node list.
     * The animation generated here will be played automatically; there is no
     * need to chain a call to {@link AnimationProxy#play} onto the result of
     * this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.anim">dojo.NodeList.anim</a>
     * and <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param properties the animation properties
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(Map<String, Object> properties)
    {
        return anim(properties, null, null, null, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Animate one or more CSS properties for all the nodes in this node list.
     * The animation generated here will be played automatically; there is no
     * need to chain a call to {@link AnimationProxy#play} onto the result of
     * this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.anim">dojo.NodeList.anim</a>
     * and <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(Map<String, Object> properties,
            Integer duration)
    {
        return anim(properties, duration, null, null, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Animate one or more CSS properties for all the nodes in this node list.
     * The animation generated here will be played automatically; there is no
     * need to chain a call to {@link AnimationProxy#play} onto the result of
     * this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.anim">dojo.NodeList.anim</a>
     * and <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(Map<String, Object> properties,
            Integer duration, JavascriptFunction easing)
    {
        return anim(properties, duration, easing, null, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Animate one or more CSS properties for all the nodes in this node list.
     * The animation generated here will be played automatically; there is no
     * need to chain a call to {@link AnimationProxy#play} onto the result of
     * this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.anim">dojo.NodeList.anim</a>
     * and <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @param onEnd a function to be called when the animation ends
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(Map<String, Object> properties,
            Integer duration, JavascriptFunction easing,
            JavascriptFunction onEnd)
    {
        return anim(properties, duration, easing, onEnd, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Animate one or more CSS properties for all the nodes in this node list.
     * The animation generated here will be played automatically; there is no
     * need to chain a call to {@link AnimationProxy#play} onto the result of
     * this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.anim">dojo.NodeList.anim</a>
     * and <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @param onEnd a function to be called when the animation ends
     * @param delay the number of milliseconds to delay beginning the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(Map<String, Object> properties,
            Integer duration, JavascriptFunction easing,
            JavascriptFunction onEnd, Integer delay)
    {
        appendToFunctionChain("anim(" +
                generator.argumentsForCall(properties, duration,
                        easing, onEnd, delay) + ")");
        return new AnimationProxy(generator, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for each node in this list. The animation generated
     * here will <b>not</b> be played automatically; you must chain a call to
     * {@link AnimationProxy#play} onto the result of this method in order to
     * play it.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.animateProperty">dojo.NodeList.animateProperty</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy animateProperty(Map<String, Object> properties,
            Integer duration, JavascriptFunction easing)
    {
        return animateProperty(JavascriptGenerator.newHash(
                "properties", properties,
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for each node in this list. The animation generated
     * here will <b>not</b> be played automatically; you must chain a call to
     * {@link AnimationProxy#play} onto the result of this method in order to
     * play it.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.animateProperty">dojo.NodeList.animateProperty</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy animateProperty(Map<String, Object> args)
    {
        appendToFunctionChain("animateProperty(" +
                generator.argumentsForCall(args) + ")");
        return new AnimationProxy(generator, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns a node list comprised of items at the given index or indices in
     * this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.at">dojo.NodeList.at</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param indices the indices
     * @return this node list, for chaining
     */
    public NodeListProxy at(int... indices)
    {
        appendToFunctionChain("at(" +
                generator.argumentsForCall(indices) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Sets an HTML attribute on each node in the list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.attr">dojo.NodeList.attr</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param attribute the HTML attribute to set
     * @param value the value of the attribute
     * @return this node list, for chaining
     */
    public NodeListProxy attr(String attribute, Object value)
    {
        appendToFunctionChain("attr("
                + generator.argumentsForCall(attribute, value) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all of the immediate children of the nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.children">dojo.NodeList.children</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy children()
    {
        return children(null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all of the immediate children of the nodes in this node list,
     * filtered based on the specified query.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.children">dojo.NodeList.children</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy children(String query)
    {
        appendToFunctionChain("children(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the closest parent that matches the query, including the current
     * node if it also matches.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.closest">dojo.NodeList.closest</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy closest(String query)
    {
        appendToFunctionChain("closest(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns back to the previous node list that generated this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.end">dojo.NodeList.end</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy end()
    {
        appendToFunctionChain("end()");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the even nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.even">dojo.NodeList.even</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy even()
    {
        appendToFunctionChain("even()");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will fade in each node in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.fadeIn">dojo.NodeList.fadeIn</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy fadeIn(Integer duration, JavascriptFunction easing)
    {
        return fadeIn(JavascriptGenerator.newHash(
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will fade in each node in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.fadeIn">dojo.NodeList.fadeIn</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy fadeIn(Map<String, Object> args)
    {
        appendToFunctionChain("fadeIn(" +
                generator.argumentsForCall(args) + ")");
        return new AnimationProxy(generator, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will fade out each node in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.fadeOut">dojo.NodeList.fadeOut</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy fadeOut(Integer duration, JavascriptFunction easing)
    {
        return fadeOut(JavascriptGenerator.newHash(
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will fade out each node in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.fadeOut">dojo.NodeList.fadeOut</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy fadeOut(Map<String, Object> args)
    {
        appendToFunctionChain("fadeOut(" +
                generator.argumentsForCall(args) + ")");
        return new AnimationProxy(generator, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the first node in this list as its own node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.first">dojo.NodeList.first</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy first()
    {
        appendToFunctionChain("first()");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the last node in this list as its own node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.last">dojo.NodeList.last</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy last()
    {
        appendToFunctionChain("last()");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the next element for nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.next">dojo.NodeList.next</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy next()
    {
        return next(null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the next element for nodes in this node list, filtered based on
     * the specified query.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.next">dojo.NodeList.next</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy next(String query)
    {
        appendToFunctionChain("next(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all sibling elements that come after the nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.nextAll">dojo.NodeList.nextAll</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy nextAll()
    {
        return nextAll(null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all sibling elements that come after the nodes in this node
     * list, filtered based on the specified query.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.nextAll">dojo.NodeList.nextAll</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy nextAll(String query)
    {
        appendToFunctionChain("nextAll(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the odd nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.odd">dojo.NodeList.odd</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy odd()
    {
        appendToFunctionChain("odd()");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the immediate parent elements for nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.parent">dojo.NodeList.parent</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy parent()
    {
        return parent(null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the immediate parent elements for nodes in this node list,
     * filtered based on the specified query.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.parent">dojo.NodeList.parent</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy parent(String query)
    {
        appendToFunctionChain("parent(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all parent elements for nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.parents">dojo.NodeList.parents</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy parents()
    {
        return parents(null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all parent elements for nodes in this node list, filtered based
     * on the specified query.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.parents">dojo.NodeList.parents</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy parents(String query)
    {
        appendToFunctionChain("parents(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the previous element for nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.prev">dojo.NodeList.prev</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy prev()
    {
        return prev(null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns the previous element for nodes in this node list, filtered based
     * on the specified query.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.prev">dojo.NodeList.prev</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy prev(String query)
    {
        appendToFunctionChain("prev(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all sibling elements that come before the nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.prevAll">dojo.NodeList.prevAll</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy prevAll()
    {
        return prevAll(null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all sibling elements that come before the nodes in this node
     * list, filtered based on the specified query.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.prevAll">dojo.NodeList.prevAll</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy prevAll(String query)
    {
        appendToFunctionChain("prevAll(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns a new node list whose members match the specified query, using
     * each node in this node list as the root for each search.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.query">dojo.NodeList.query</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy query(String query)
    {
        appendToFunctionChain("query(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Removes a CSS class from each node in the list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.removeClass">dojo.NodeList.removeClass</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param cssClass the CSS class to remove
     * @return this node list, for chaining
     */
    public NodeListProxy removeClass(String cssClass)
    {
        appendToFunctionChain("removeClass("
                + generator.argumentsForCall(cssClass) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all sibling elements of the nodes in this node list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.siblings">dojo.NodeList.siblings</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @return this node list, for chaining
     */
    public NodeListProxy siblings()
    {
        return siblings(null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Returns all sibling elements of the nodes in this node list, filtered
     * based on the specified query.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.siblings">dojo.NodeList.siblings</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param query a single-expression CSS selector
     * @return this node list, for chaining
     */
    public NodeListProxy siblings(String query)
    {
        appendToFunctionChain("siblings(" +
                generator.argumentsForCall(query) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Sets a CSS style on each node in the list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.style">dojo.NodeList.style</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param attribute the CSS style to set
     * @param value the value of the attribute
     * @return this node list, for chaining
     */
    public NodeListProxy style(String attribute, Object value)
    {
        appendToFunctionChain("style("
                + generator.argumentsForCall(attribute, value) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Toggles a CSS class on each node in the list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.toggleClass">dojo.NodeList.toggleClass</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param cssClass the CSS class to toggle
     * @return this node list, for chaining
     */
    public NodeListProxy toggleClass(String cssClass)
    {
        return toggleClass(cssClass, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Adds/removes a CSS class to/from each node in the list.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.toggleClass">dojo.NodeList.toggleClass</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param cssClass the CSS class to toggle
     * @param condition true to add the class, or false to remove it
     * @return this node list, for chaining
     */
    public NodeListProxy toggleClass(String cssClass, Boolean condition)
    {
        appendToFunctionChain("toggleClass(" +
                generator.argumentsForCall(cssClass, condition) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will expand each node in this node list to its
     * natural height.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.wipeIn">dojo.NodeList.wipeIn</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy wipeIn(Integer duration, JavascriptFunction easing)
    {
        return wipeIn(JavascriptGenerator.newHash(
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will expand each node in this node list to its
     * natural height.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.wipeIn">dojo.NodeList.wipeIn</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy wipeIn(Map<String, Object> args)
    {
        appendToFunctionChain("wipeIn(" +
                generator.argumentsForCall(args) + ")");
        return new AnimationProxy(generator, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will contract each node in this node list so
     * that it appears hidden.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.wipeOut">dojo.NodeList.wipeOut</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy wipeOut(Integer duration, JavascriptFunction easing)
    {
        return wipeOut(JavascriptGenerator.newHash(
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will contract each node in this node list so
     * that it appears hidden.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.NodeList.wipeOut">dojo.NodeList.wipeOut</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy wipeOut(Map<String, Object> args)
    {
        appendToFunctionChain("wipeOut(" +
                generator.argumentsForCall(args) + ")");
        return new AnimationProxy(generator, null);
    }
}
