/*==========================================================================*\
 |  $Id: DojoActionFormElement.java,v 1.4 2011/03/23 15:07:45 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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

package org.webcat.ui._base;

import org.apache.log4j.Logger;
import org.webcat.ui.WCForm;
import org.webcat.ui.util.DojoRemoteHelper;
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.components._private.ERXWOForm;

//------------------------------------------------------------------------
/**
 * <p>
 * The base class from which all form widgets that can execute an action
 * (WCButton, WCMenuItem, and so forth) inherit. (Note that WCLink implements
 * this functionality separately since it is not a form element.) This class
 * provides a common set of bindings for executing server-side actions, both in
 * the form of ordinary page-load submits and via an Ajax interface.
 * </p><p>
 * Important note: Some uses of these elements (such as remote actions) will
 * involve the generation of an <tt>onClick</tt> event handler for the element.
 * If you wish to include your own <tt>onClick</tt> handling as well, use a
 * script with <tt>type="dojo/connect"</tt> rather than <tt>dojo/method</tt> to
 * ensure that both handlers are called correctly. In all cases, user-supplied
 * <tt>onClick</tt> handlers will be executed <i>before</i> the action handler.
 * </p>
 *
 * <h2>Bindings</h2>
 * <table>
 * <tr>
 * <td>{@code action}</td>
 * <td>The action method to invoke when this element is activated.</td>
 * </tr>
 * <tr>
 * <td>{@code directActionName}</td>
 * <td>The name of the direct action method (minus the "Action" suffix) to
 * invoke when this element is activated. Defaults to "default".</td>
 * </tr>
 * <tr>
 * <td>{@code actionClass}</td>
 * <td>The name of the class in which the method designated in
 * <tt>directActionName</tt> can be found. Defaults to DirectAction.</td>
 * </tr>
 * <tr>
 * <td>{@code remote}</td>
 * <td>If <tt>false</tt> or unspecified, the action is a traditional
 * synchronous action. If <tt>true</tt>, the action is executed via an Ajax
 * request. This defaults to false unless any of the other "remote.*" bindings
 * are specified, in which case this is assumed to be true. Therefore, it is
 * not necessary to explicitly use this binding unless you want an Ajax request
 * that does not use any of the other "remote.*" bindings.</td>
 * </tr>
 * <tr>
 * <td>{@code remote.responseType}</td>
 * <td>A string indicating how the action response should be treated, and in
 * which format it will be passed to the callback functions. Choices are "text"
 * (the default), where the response will be sent to the callback as a string;
 * "javascript", where the response is JavaScript code that will be executed
 * before the callback is called; "json", "json-comment-optional",
 * "json-comment-filtered", where the response is a JSON string that will be
 * evaluated and passed to the callback as an object; and "xml", where the
 * response is XML text that is parsed and passed to the callback as a Document
 * DOM object.</td>
 * </tr>
 * <tr>
 * <td>{@code remote.synchronous}</td>
 * <td>A boolean value indicating that the request should be synchronous
 * instead of asynchronous. Use this with caution as a long-running server-side
 * action will cause the browser to appear hung.
 * </td>
 * </tr>
 * </table>
 *
 * <h2>Events</h2>
 * <table>
 * <tr>
 * <td>{@code onRemoteLoad(response, ioArgs)}</td>
 * <td>Called upon a successful HTTP response code. The argument "response" is
 * the response returned from the action (see <tt>remote.responseType</tt>),
 * and "ioArgs" is defined as in dojo.xhr. <b>This function should return the
 * response for proper callback chaining.</b></td>
 * </tr>
 * <tr>
 * <td>{@code onRemoteError(response, ioArgs)}</td>
 * <td>Called upon an unsuccessful HTTP response code. The argument "response"
 * is the response returned from the action (see <tt>remote.responseType</tt>),
 * and "ioArgs" is defined as in dojo.xhr. <b>This function should return the
 * response for proper callback chaining.</b></td>
 * </tr>
 * <tr>
 * <td>{@code onRemoteEnd(response, ioArgs)}</td>
 * <td>Called upon the end of the request, regardless of the HTTP response code.
 * This handler will be called <b>after</b> <tt>onRemoteLoad</tt> and
 * <tt>onRemoteError</tt> if either of those is also specified. The argument
 * "response" is the response returned from the action (see
 * <tt>remote.responseType</tt>), and "ioArgs" is defined as in dojo.xhr.
 * <b>This function should return the response for proper callback chaining.
 * </b></td>
 * </tr>
 * </table>
 *
 * @author Tony Allevato
 * @version $Id: DojoActionFormElement.java,v 1.4 2011/03/23 15:07:45 aallowat Exp $
 */
public abstract class DojoActionFormElement extends DojoFormElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public DojoActionFormElement(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);

        _action = _associations.removeObjectForKey("action");
        _actionClass = _associations.removeObjectForKey("actionClass");
        _directActionName =
            _associations.removeObjectForKey("directActionName");

        _remoteHelper = new DojoRemoteHelper(_associations);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    private String _actionClassAndName(WOContext context)
    {
        return computeActionStringInContext(_actionClass,
                _directActionName, context);
    }


    // ----------------------------------------------------------
    protected boolean hasActionInContext(WOContext context)
    {
        WOComponent component = context.component();

        boolean hasAction = (_action != null);
        boolean hasActionClass = (_actionClass != null &&
                _actionClass.valueInComponent(component) != null);
        boolean hasDirectActionName = (_directActionName != null &&
                _directActionName.valueInComponent(component) != null);

        return hasAction || hasActionClass || hasDirectActionName;
    }


    // ----------------------------------------------------------
    @Override
    public boolean hasContent()
    {
        return true;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * If the action element is implemented as a button or a hyperlink, then
     * that element itself suffices to perform a standard action. If the
     * element is represented by, for example, a div where the onclick
     * attribute is the only method of interaction, then that element's class
     * should override this and return true to use a "fake full submit" that
     * dynamically creates a button and uses it to submit the form.
     * </p><p>
     * Important: Make sure to use WCForm (or, at the very least, ERXWOForm) so
     * that the auto-detection of the containing form works correctly.
     * </p>
     *
     * @return true if the element needs a fake form submit to simulate a
     *     page-load submit; false if the element itself can perform the submit
     */
    protected boolean usesFakeFullSubmit()
    {
        return false;
    }


    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        super.appendToResponse(response, context);

        if (_directActionName != null || _actionClass != null)
        {
            response._appendContentAsciiString(
                    "<input type=\"hidden\" name=\"WOSubmitAction\"");
            response._appendTagAttributeAndValue("value",
                    _actionClassAndName(context), false);
            response.appendContentString(" />");
        }
    }


    // ----------------------------------------------------------
    protected void appendNameAttributeToResponse(WOResponse response,
            WOContext context)
    {
        if(_directActionName != null || _actionClass != null)
        {
            response._appendTagAttributeAndValue("name",
                    _actionClassAndName(context), false);
        }
        else
        {
            super.appendNameAttributeToResponse(response, context);
        }
    }


    // ----------------------------------------------------------
    protected void appendOnClickScriptToResponse(WOResponse response,
            WOContext context)
    {
        if (_remoteHelper.isRemoteInContext(context))
        {
            response.appendContentString("<script type=\"dojo/connect\" "
                    + "event=\"onClick\" args=\"evt\">\n");

            appendXhrGetToResponse(response, context);

            response.appendContentString("\n</script>\n");
        }
        else if (usesFakeFullSubmit())
        {
            response.appendContentString("<script type=\"dojo/connect\" "
                    + "event=\"onClick\" args=\"evt\">\n");

            response.appendContentString(WCForm.scriptToPerformFullSubmit(
                    context, nameInContext(context)));

            response.appendContentString("\n</script>\n");
        }
    }


    // ----------------------------------------------------------
    @Override
    public void appendChildrenToResponse(WOResponse response,
            WOContext context)
    {
        super.appendChildrenToResponse(response, context);

        if (dojoType() != null && hasActionInContext(context))
        {
            appendOnClickScriptToResponse(response, context);
        }
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        if (_remoteHelper.isRemoteInContext(context)
                && dojoType() == null && hasActionInContext(context))
        {
            WOResponse xhrResponse = new WOResponse();
            appendXhrGetToResponse(xhrResponse, context);
            xhrResponse.appendContentString(" return false;");

            response._appendTagAttributeAndValue("onclick",
                    xhrResponse.contentString(), true);
        }
    }


    // ----------------------------------------------------------
    protected String shadowButtonIdInContext(WOContext context)
    {
        return "__dae_shadow_button__" +
            ERXWOContext.safeIdentifierName(context, false);
    }


    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected void appendXhrGetToResponse(WOResponse response,
            WOContext context)
    {
        WOComponent component = context.component();
        JSHash requestOptions = new JSHash();

        if (_directActionName != null)
        {
            // FIXME may need work, untested

            String actionUrl = context.directActionURLForActionNamed(
                    (String) _directActionName.valueInComponent(component),
                    ERXComponentUtilities.queryParametersInComponent(
                            _associations, component)).replaceAll("&amp;", "&");

            requestOptions.put("url", actionUrl);
        }

        // If we're inside a form, include the name of the element as the
        // sender. This is unnecessary for buttons, but required for other
        // types of action elements, like WCMenuItem.

        String formName = ERXWOForm.formName(context, null);
        if (formName != null)
        {
            requestOptions.put("sender", nameInContext(context));
        }

        response.appendContentString(_remoteHelper.remoteSubmitCall(
                "this", requestOptions, context));
    }


    // ----------------------------------------------------------
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        if (AjaxUtils.isAjaxRequest(request) &&
                AjaxUtils.shouldHandleRequest(request, context, null))
        {
            return invokeRemoteAction(request, context);
        }
        else
        {
            return invokeStandardAction(request, context);
        }
    }


    // ----------------------------------------------------------
    protected WOActionResults invokeRemoteAction(WORequest request,
            WOContext context)
    {
        WOActionResults result = null;

        WOComponent component = context.component();

        AjaxUtils.createResponse(request, context);
        AjaxUtils.mutableUserInfo(request);

        context.setActionInvoked(true);
        if (_action != null)
        {
            result = (WOActionResults) _action.valueInComponent(component);
        }

        AjaxUtils.updateMutableUserInfoWithAjaxInfo(context);

        if (result == context.page())
        {
            log.warn("An Ajax request attempted to return the page, which "
                    + "is almost certainly an error.");

            result = null;
        }

        if (result == null)
        {
            result = AjaxUtils.createResponse(request, context);
        }

        return result;
    }


    // ----------------------------------------------------------
    protected WOActionResults invokeStandardAction(WORequest request,
            WOContext context)
    {
        WOActionResults actionResult = null;
        WOComponent component = context.component();

        if(!isDisabledInContext(context) && context.wasFormSubmitted())
        {
            if(context.isMultipleSubmitForm())
            {
                if(request.formValueForKey(nameInContext(context)) != null)
                {
                    context.setActionInvoked(true);

                    if(_action != null)
                    {
                        actionResult = (WOActionResults)
                            _action.valueInComponent(component);
                    }

                    if(actionResult == null)
                    {
                        actionResult = context.page();
                    }
                }
            }
            else
            {
                context.setActionInvoked(true);

                if(_action != null)
                {
                    actionResult = (WOActionResults)
                        _action.valueInComponent(component);
                }

                if(actionResult == null)
                {
                    actionResult = context.page();
                }
            }
        }

        return actionResult;
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _action;
    protected WOAssociation _actionClass;
    protected WOAssociation _directActionName;

    protected DojoRemoteHelper _remoteHelper;

    private static final Logger log =
        Logger.getLogger(DojoActionFormElement.class);
}
