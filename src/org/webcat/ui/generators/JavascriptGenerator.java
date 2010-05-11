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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webcat.core.Application;
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * This class is the main entry point for server-side Javascript generation
 * that is used to manipulate a page upon returning from a remote (Ajax) action
 * invocation. This class implements WOActionResults so it can be returned
 * directly from an action; the {@link #generateResponse()} method creates the
 * appropriate WOResponse with the content type set to "text/javascript" so
 * that it can be evaluated on return.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public class JavascriptGenerator implements WOActionResults
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new JavascriptGenerator object.
     */
    public JavascriptGenerator()
    {
        lines = new ArrayList<String>();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Generates the server response for this Javascript fragment.
     *
     * @return a WOResponse object containing the Javascript code
     */
    public WOResponse generateResponse()
    {
        WOResponse response = new WOResponse();
        response.setHeader("text/javascript", "content-type");
        response.setContent(toString());
        return response;
    }


    // ----------------------------------------------------------
    /**
     * A convenience method to quickly construct a Map from a list of key-value
     * pairs.
     *
     * @param keysAndValues alternating keys and values that will be inserted
     *     into the map
     * @return a Map containing the keys and values
     */
    public static Map<String, Object> newHash(Object... keysAndValues)
    {
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            String key = (String) keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (value != null)
            {
                map.put(key, value);
            }
        }

        return map;
    }


    // ----------------------------------------------------------
    /**
     * Gets the generated Javascript code as a string.
     *
     * @return the generated Javascript code
     */
    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        for (String line : lines)
        {
            buffer.append(line);
            if (line.charAt(line.length() - 1) != ';')
            {
                buffer.append(';');
            }
            buffer.append('\n');
        }

        if (isDevelopmentMode())
        {
            return "try {\n"
                + buffer.toString()
                + "\n} catch (e) {"
                + " alert('Error in JavaScriptGenerator generated code:\\n\\n'"
                + " + e.toString()); }";
        }
        else
        {
            return buffer.toString();
        }
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Adds a CSS class to a DOM element.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.addClass">dojo.addClass</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param cssClass the CSS class to add
     * @return this generator, for chaining
     */
    public JavascriptGenerator addClass(String id, String cssClass)
    {
        return call("dojo.addClass", id, cssClass);
    }


    // ----------------------------------------------------------
    /**
     * Displays a themed modal alert dialog.
     *
     * @param title the title of the dialog
     * @param message the message inside the dialog
     * @return this generator, for chaining
     */
    public JavascriptGenerator alert(String title, String message)
    {
        return alert(title, message, null, null);
    }


    // ----------------------------------------------------------
    /**
     * Displays a themed modal alert dialog and executes the specified function
     * when it is dismissed.
     *
     * @param title the title of the dialog
     * @param message the message inside the dialog
     * @param onClose the function to execute when the dialog is dismissed
     * @return this generator, for chaining
     */
    public JavascriptGenerator alert(String title, String message,
            JavascriptFunction onClose)
    {
        return alert(title, message, null, onClose);
    }


    // ----------------------------------------------------------
    /**
     * Displays a themed modal alert dialog and executes the specified function
     * when it is dismissed.
     *
     * @param title the title of the dialog
     * @param message the message inside the dialog
     * @param okLabel the label to display for the OK button
     * @param onClose the function to execute when the dialog is dismissed
     * @return this generator, for chaining
     */
    public JavascriptGenerator alert(String title, String message,
            String okLabel, JavascriptFunction onClose)
    {
        JSHash options = new JSHash();
        options.put("title", title);
        options.put("message", message);
        options.put("okLabel", okLabel);
        options.put("onClose", onClose);

        return alert(options);
    }


    // ----------------------------------------------------------
    /**
     * Displays a themed modal alert dialog with the specified options.
     *
     * @param options the options for this alert
     * @return this generator, for chaining
     */
    public JavascriptGenerator alert(JSHash options)
    {
        JSHash options_ = options.clone();

        JavascriptFunction onClose = options_.get("onClose",
                JavascriptFunction.class);

        if (onClose != null)
        {
            options_.put("onClose", JSHash.code(javascriptObjectFor(onClose)));
        }

        return call("webcat.alert", options_);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for a DOM element. The animation generated here
     * will be played automatically; there is no need to chain a call to
     * {@link AnimationProxy#play} onto the result of this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param properties the animation properties
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(String id, Map<String, Object> properties)
    {
        return anim(id, properties, null, null, null, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for a DOM element. The animation generated here
     * will be played automatically; there is no need to chain a call to
     * {@link AnimationProxy#play} onto the result of this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(String id, Map<String, Object> properties,
            Integer duration)
    {
        return anim(id, properties, duration, null, null, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for a DOM element. The animation generated here
     * will be played automatically; there is no need to chain a call to
     * {@link AnimationProxy#play} onto the result of this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(String id, Map<String, Object> properties,
            Integer duration, JavascriptFunction easing)
    {
        return anim(id, properties, duration, easing, null, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for a DOM element. The animation generated here
     * will be played automatically; there is no need to chain a call to
     * {@link AnimationProxy#play} onto the result of this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @param onEnd a function to be called when the animation ends
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(String id, Map<String, Object> properties,
            Integer duration, JavascriptFunction easing,
            JavascriptFunction onEnd)
    {
        return anim(id, properties, duration, easing, onEnd, null);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for a DOM element. The animation generated here
     * will be played automatically; there is no need to chain a call to
     * {@link AnimationProxy#play} onto the result of this method.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.anim">dojo.anim</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @param onEnd a function to be called when the animation ends
     * @param delay the number of milliseconds to delay beginning the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy anim(String id, Map<String, Object> properties,
            Integer duration, JavascriptFunction easing,
            JavascriptFunction onEnd, Integer delay)
    {
        return new AnimationProxy(this, "dojo.anim(" +
                argumentsForCall(id, properties, duration,
                        easing, onEnd, delay) + ")");
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for a DOM element. The animation generated here
     * will <b>not</b> be played automatically; you must chain a call to
     * {@link AnimationProxy#play} onto the result of this method in order to
     * play it.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.animateProperty">dojo.animateProperty</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param properties the animation properties
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy animateProperty(String id,
            Map<String, Object> properties, Integer duration,
            JavascriptFunction easing)
    {
        return animateProperty(newHash(
                "node", id,
                "properties", properties,
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation for a DOM element. The animation generated here
     * will <b>not</b> be played automatically; you must chain a call to
     * {@link AnimationProxy#play} onto the result of this method in order to
     * play it.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.animateProperty">dojo.animateProperty</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy animateProperty(Map<String, Object> args)
    {
        return new AnimationProxy(this, "dojo.animateProperty(" +
                argumentsForCall(args) + ")");
    }


    // ----------------------------------------------------------
    /**
     * Appends a fragment of literal Javascript code to this generator.
     *
     * @param javascript a Javascript fragment
     * @return this generator, for chaining
     */
    public JavascriptGenerator append(String javascript)
    {
        lines.add(javascript);
        return this;
    }


    // ----------------------------------------------------------
    /**
     * Assigns a value to a variable.
     *
     * @param variable the variable
     * @param value the value
     * @return this generator, for chaining
     */
    public JavascriptGenerator assign(String variable, Object value)
    {
        record(variable + " = " + javascriptObjectFor(value));
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Sets the value of an attribute on a DOM element.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.attr">dojo.attr</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param attribute the attribute to set
     * @param value the value of the attribute
     * @return this generator, for chaining
     */
    public JavascriptGenerator attr(String id, String attribute, Object value)
    {
        return call("dojo.attr", id, attribute, value);
    }


    // ----------------------------------------------------------
    /**
     * Blocks one or more DOM elements by covering them with transparent
     * overlays and animated busy-spinners.
     *
     * @param ids the DOM element IDs
     * @return this generator, for chaining
     */
    public JavascriptGenerator block(String... ids)
    {
        return loopOnMultipleArgs("webcat.block", ids);
    }


    // ----------------------------------------------------------
    /**
     * Generates a call to a Javascript function.
     *
     * @param function the name of the function to call
     * @param arguments arguments to pass to the function
     * @return this generator, for chaining
     */
    public JavascriptGenerator call(String function, Object... arguments)
    {
        record(function + "(" + argumentsForCall(arguments) + ")");
        return this;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets a widget by its Dijit ID.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dijit.byId">dijit.byId</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the Dijit widget ID
     * @return a {@link DijitProxy} object for chaining calls to this widget
     */
    public DijitProxy dijit(String id)
    {
        return new DijitProxy(this, id);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will fade a node from its current opacity to
     * fully opaque.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.fadeIn">dojo.fadeIn</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy fadeIn(String id, Integer duration,
            JavascriptFunction easing)
    {
        return fadeIn(newHash(
                "node", id,
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will fade a node from its current opacity to
     * fully opaque.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.fadeIn">dojo.fadeIn</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy fadeIn(Map<String, Object> args)
    {
        return new AnimationProxy(this, "dojo.fadeIn(" +
                argumentsForCall(args) + ")");
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will fade a node from its current opacity to
     * fully transparent.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.fadeOut">dojo.fadeOut</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy fadeOut(String id, Integer duration,
            JavascriptFunction easing)
    {
        return fadeOut(newHash(
                "node", id,
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will fade a node from its current opacity to
     * fully transparent.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.fadeOut">dojo.fadeOut</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy fadeOut(Map<String, Object> args)
    {
        return new AnimationProxy(this, "dojo.fadeOut(" +
                argumentsForCall(args) + ")");
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Selects a set of nodes based on a CSS selector string.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.query">dojo.query</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param pattern the CSS selector string used to match nodes
     * @return a {@link NodeListProxy} object for chaining calls to this node
     *     list
     */
    public NodeListProxy query(String pattern)
    {
        return new NodeListProxy(this, pattern);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Selects a set of nodes based on a CSS selector string.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.query">dojo.query</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param pattern the CSS selector string used to match nodes
     * @param root the ID of a DOM node to use as the search scope
     * @return a {@link NodeListProxy} object for chaining calls to this node
     *     list
     */
    public NodeListProxy query(String pattern, String root)
    {
        return new NodeListProxy(this, pattern, root);
    }


    // ----------------------------------------------------------
    /**
     * Redirects the browser to the given location.
     *
     * @param url the url to load
     * @return this generator, for chaining
     */
    public JavascriptGenerator redirectTo(String url)
    {
        return assign("window.location.href", url);
    }


    // ----------------------------------------------------------
    /**
     * Redirects the browser to the location determined by the specified direct
     * action.
     *
     * @param actionName the direct action name
     * @param queryParams query parameters to pass to the action, or null
     * @return this generator, for chaining
     */
    public JavascriptGenerator redirectTo(String actionName,
            NSDictionary<String, Object> queryParams)
    {
        String url = ERXWOContext.currentContext().directActionURLForActionNamed(
                actionName, queryParams);

        return assign("window.location.href", url);
    }


    // ----------------------------------------------------------
    /**
     * Refreshes one or more content panes.
     *
     * @param ids the Dijit IDs of the content panes
     * @return this generator, for chaining
     */
    public JavascriptGenerator refresh(String... ids)
    {
        return loopOnMultipleArgs("webcat.refresh", ids);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Removes an attribute from a DOM element.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.removeAttr">dojo.removeAttr</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param attribute the attribute to remove
     * @return this generator, for chaining
     */
    public JavascriptGenerator removeAttr(String id, String attribute)
    {
        return call("dojo.removeAttr", id, attribute);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Removes a CSS class from a DOM element.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.removeClass">dojo.removeClass</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param cssClass the CSS class to remove
     * @return this generator, for chaining
     */
    public JavascriptGenerator removeClass(String id, String cssClass)
    {
        return call("dojo.removeClass", id, cssClass);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Sets the value of a CSS style on a DOM element.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.style">dojo.style</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param style the CSS style to set
     * @param value the value of the CSS style
     * @return this generator, for chaining
     */
    public JavascriptGenerator style(String id, String style, String value)
    {
        return call("dojo.style", id, style, value);
    }


    // ----------------------------------------------------------
    /**
     * Initiates a submit action for a form, performing the same action as if
     * the user had physically clicked on the specified button.
     *
     * @param formName the name of the form to be submitted
     * @param buttonName the name of the button to be clicked
     * @return this generator, for chaining
     */
    public JavascriptGenerator submit(String formName, String buttonName)
    {
        return call("webcat.fullSubmit", formName, buttonName);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Adds a CSS class from a DOM element if it does not exist or removes it
     * if it exists.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.toggleClass">dojo.toggleClass</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param cssClass the CSS class to toggle
     * @return this generator, for chaining
     */
    public JavascriptGenerator toggleClass(String id, String cssClass)
    {
        return call("dojo.toggleClass", id, cssClass);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Adds or removes a CSS class from a DOM element.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.toggleClass">dojo.toggleClass</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param cssClass the CSS class to toggle
     * @param condition true to add the class, or false to remove it
     * @return this generator, for chaining
     */
    public JavascriptGenerator toggleClass(String id, String cssClass,
            Boolean condition)
    {
        return call("dojo.toggleClass", id, cssClass, condition);
    }


    // ----------------------------------------------------------
    /**
     * Unblocks one or more DOM elements by removing their transparent
     * overlays and animated busy-spinners.
     *
     * @param ids the DOM element IDs
     * @return this generator, for chaining
     */
    public JavascriptGenerator unblock(String... ids)
    {
        return loopOnMultipleArgs("webcat.unblock", ids);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will expand a node from its current height to
     * its natural height.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.fx.wipeIn">dojo.fx.wipeIn</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy wipeIn(String id, Integer duration,
            JavascriptFunction easing)
    {
        return wipeIn(newHash(
                "node", id,
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will expand a node from its current height to
     * its natural height.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.fx.wipeIn">dojo.fx.wipeIn</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy wipeIn(Map<String, Object> args)
    {
        return new AnimationProxy(this, "dojo.wipeIn(" +
                argumentsForCall(args) + ")");
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will contract a node from its natural height
     * to make it appear hidden.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.fx.wipeOut">dojo.fx.wipeOut</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param id the DOM element id
     * @param duration the number of milliseconds the animation should run
     * @param easing an easing function to calculate acceleration/deceleration
     *     of the animation
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy wipeOut(String id, Integer duration,
            JavascriptFunction easing)
    {
        return wipeOut(newHash(
                "node", id,
                "duration", duration,
                "easing", easing));
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Creates an animation that will contract a node from its natural height
     * to make it appear hidden.
     * </p><p>
     * See <a href="http://api.dojotoolkit.org/jsdoc/HEAD/dojo.fx.wipeOut">dojo.fx.wipeOut</a>
     * for details on this method's parameters and operation.
     * </p>
     *
     * @param args the animation arguments; see the link to the Dojo API above
     * @return an {@link AnimationProxy} object for chaining calls to this
     *     animation
     */
    public AnimationProxy wipeOut(Map<String, Object> args)
    {
        return new AnimationProxy(this, "dojo.wipeOut(" +
                argumentsForCall(args) + ")");
    }


    // ----------------------------------------------------------
    /**
     * Records a line of code in this Javascript generator.
     *
     * @param line the line of code to record
     */
    /*package*/ void record(String line)
    {
        lines.add(line.replaceAll("\\;\\z", ""));
    }


    // ----------------------------------------------------------
    /**
     * Gets the Javascript literal representation of the specified object.
     *
     * @param object the object
     * @return the Javascript representation of the object
     */
    @SuppressWarnings("unchecked")
    /*package*/ String javascriptObjectFor(Object object)
    {
        if (object instanceof JavascriptFunction)
        {
            JavascriptFunction function = (JavascriptFunction) object;

            StringBuffer buffer = new StringBuffer();
            buffer.append("function(");
            buffer.append(function.args());
            buffer.append(") {\n");
            JavascriptGenerator generator = new JavascriptGenerator();
            function.generate(generator);
            buffer.append(generator.toString());
            buffer.append("}");
            return buffer.toString();
        }
        else if (object instanceof JSHash)
        {
            return object.toString();
        }
        else if (object.getClass().isArray())
        {
            try
            {
                return new JSONArray(object).toString();
            }
            catch (JSONException e)
            {
                return object.toString();
            }
        }
        else if (object instanceof List)
        {
            return new JSONArray((List) object).toString();
        }
        else if (object instanceof Map)
        {
            return new JSONObject((Map) object).toString();
        }
        else if (object instanceof String)
        {
            return JSONObject.quote(object.toString());
        }
        else
        {
            return object.toString();
        }
    }


    // ----------------------------------------------------------
    /**
     * Generates the Javascript argument list string from a list of values.
     *
     * @param arguments the list of arguments
     * @return the argument list string
     */
    /*package*/ String argumentsForCall(Object... arguments)
    {
        if (arguments == null || arguments.length == 0)
        {
            return "";
        }

        StringBuffer buffer = new StringBuffer();

        if (arguments[0] != null)
        {
            buffer.append(javascriptObjectFor(arguments[0]));
        }

        for (int i = 1; i < arguments.length; i++)
        {
            if (arguments[i] != null)
            {
                buffer.append(", ");
                buffer.append(javascriptObjectFor(arguments[i]));
            }
        }

        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Executes a function on one or more IDs, generating a
     * <code>dojo.forEach</code> loop if necessary for multiple IDs.
     *
     * @param method the name of the function to call
     * @param ids the IDs to execute the function on
     * @return this generator, for chaining
     */
    /*package*/ JavascriptGenerator loopOnMultipleArgs(
            String method, String[] ids)
    {
        if (ids.length > 1)
        {
            record("dojo.forEach(" + javascriptObjectFor(ids) + ", "
                    + method + ")");
        }
        else
        {
            record(method + "(" + javascriptObjectFor(ids[0]) + ")");
        }

        return this;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the application is currently running
     * under development or deployment mode.  Scripts generated in development
     * mode are enclosed in a try/catch handler with extra debugging
     * information displayed in the event of an error.
     *
     * @return true if running in development mode, or false if in deployment
     *     mode
     */
    private boolean isDevelopmentMode()
    {
        return Application.isDevelopmentModeSafe();
    }


    //~ Static/instance variables .............................................

    /* package */ List<String> lines;

    // A shortcut for accessing a JavascriptGenerator that does nothing.
    public static final JavascriptGenerator NO_OP = new JavascriptGenerator();
}
