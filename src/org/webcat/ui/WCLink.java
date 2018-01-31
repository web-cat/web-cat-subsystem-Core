/*==========================================================================*\
 |  $Id: WCLink.java,v 1.3 2010/10/28 00:37:30 aallowat Exp $
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

package org.webcat.ui;

import org.apache.log4j.Logger;
import org.webcat.ui.util.DojoRemoteHelper;
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOPageNotFoundException;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOCGIFormValues;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.appserver._private.WONoContentElement;
import com.webobjects.appserver._private.WOStaticURLUtilities;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSDictionaryUtilities;
import er.ajax.AjaxUtils;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.components._private.ERXWOForm;

//------------------------------------------------------------------------
/**
 * <p>
 * A hyperlink that can execute an action, both in the form of an ordinary
 * page-load and via an Ajax interface. Another enhancement compared to
 * WOHyperlink is that WCLinks that are nested in a form cause that form to be
 * submitted (causing bindings on the page to be synchronized, just as with
 * submit buttons).
 * </p><p>
 * Unlike the other action elements such as WCButton, WCMenuItem, etc., the
 * WCLink is an ordinary HTML anchor tag and <b>not</b> a Dojo widget. This
 * means that, unlike those other elements, you cannot use embedded script tags
 * with <tt>type="dojo/connect"</tt> to hook up event handlers. Instead, use
 * the attributes/bindings <tt>onRemoteLoad</tt>, <tt>onRemoteError</tt>, and
 * <tt>onRemoteEnd</tt> for this functionality.
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
 * <td>{@code pageName}</td>
 * <td>The name of a WebObjects component that should be instantiated and
 * returned.</td>
 * </tr>
 * <tr>
 * <td>{@code ignoreForm}</td>
 * <td>Normally, links inside forms cause that form to be submitted so that
 * bindings are synchronized before the action is executed. If this is set to
 * true, the link will not submit the form (duplicating the functionality of
 * built-in WOHyperlinks).</td>
 * </tr>
 * <tr>
 * <td>{@code onClick}</td>
 * <td>JavaScript code that is executed in the browser when the element is
 * clicked, before the action is invoked.</td>
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
 * <td>@{code remote.synchronous}</td>
 * <td>A boolean value indicating that the request should be synchronous
 * instead of asynchronous. Use this with caution as a long-running server-side
 * action will cause the browser to appear hung.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code onRemoteLoad}</td>
 * <td>A two-argument JavaScript function(response, ioArgs) called upon a
 * successful HTTP response code. The argument "response" is the response
 * returned from the action (see <tt>remote.responseType</tt>), and "ioArgs" is
 * defined as in dojo.xhr. <b>This function should return the response for
 * proper callback chaining.</b></td>
 * </tr>
 * <tr>
 * <td>{@code onRemoteError}</td>
 * <td>A two-argument JavaScript function(response, ioArgs) called upon an
 * unsuccessful HTTP response code. The argument "response" is the response
 * returned from the action (see <tt>remote.responseType</tt>), and "ioArgs" is
 * defined as in dojo.xhr. <b>This function should return the response for
 * proper callback chaining.</b></td>
 * </tr>
 * <tr>
 * <td>{@code onRemoteEnd}</td>
 * <td>A two-argument JavaScript function(response, ioArgs) called upon the end
 * of the request, regardless of the HTTP response code. This function will be
 * called <b>after</b> <tt>remote.onLoad</tt> or <tt>remote.onError</tt> if
 * either of those is also specified. The argument "response" is the response
 * returned from the action (see <tt>remote.responseType</tt>), and "ioArgs" is
 * defined as in dojo.xhr. <b>This function should return the response for
 * proper callback chaining.</b></td>
 * </tr>
 * </table>
 *
 * @author Tony Allevato
 * @version $Id: WCLink.java,v 1.3 2010/10/28 00:37:30 aallowat Exp $
 */
public class WCLink extends WOHTMLDynamicElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public WCLink(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("a", someAssociations, template);

        _otherQueryAssociations =
            _NSDictionaryUtilities.extractObjectsForKeysWithPrefix(
                    _associations, "?", true);

        if (_otherQueryAssociations == null
                || _otherQueryAssociations.count() <= 0)
        {
            _otherQueryAssociations = null;
        }

        _action = _associations.removeObjectForKey("action");
        _string = _associations.removeObjectForKey("string");
        _href = _associations.removeObjectForKey("href");
        _disabled = _associations.removeObjectForKey("disabled");
        _queryDictionary = _associations.removeObjectForKey("queryDictionary");
        _actionClass = _associations.removeObjectForKey("actionClass");
        _directActionName =
            _associations.removeObjectForKey("directActionName");
        _pageName = _associations.removeObjectForKey("pageName");
        _fragmentIdentifier =
            _associations.removeObjectForKey("fragmentIdentifier");
        _escapeHTML = _associations.removeObjectForKey("escapeHTML");
        _onClick = _associations.removeObjectForKey("onClick");
        _ignoreForm = _associations.removeObjectForKey("ignoreForm");
        _remoteHelper = new DojoRemoteHelper(_associations);

        if (_action == null && _href == null && _pageName == null
                && _directActionName == null && _actionClass == null)
        {
            throw new WODynamicElementCreationException("<"
                    + getClass().getName()
                    + "> Missing required attribute: 'action' or 'href' or "
                    + "'pageName' or 'directActionName' or 'actionClass'");
        }

        if ((_action != null && _href != null)
                || (_action != null && _pageName != null)
                || (_href != null && _pageName != null)
                || (_action != null && _directActionName != null)
                || (_href != null && _directActionName != null)
                || (_pageName != null && _directActionName != null)
                || (_action != null && _actionClass != null))
        {
            throw new WODynamicElementCreationException("<"
                    + getClass().getName()
                    + "> At least two of these conflicting attributes are "
                    + "present: 'action', 'href', 'pageName', "
                    + "'directActionName', 'actionClass'.");
        }

        if (_action != null && _action.isValueConstant())
        {
            throw new WODynamicElementCreationException("<"
                    + getClass().getName() + "> 'action' is a constant.");
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected void _appendOpenTagToResponse(WOResponse response,
            WOContext context)
    {
        if(!isDisabledInContext(context))
        {
            super._appendOpenTagToResponse(response, context);
        }
        else
        {
            response.appendContentString("<span");
            appendAttributesToResponse(response, context);
            response.appendContentCharacter('>');
        }
    }


    // ----------------------------------------------------------
    @Override
    protected void _appendCloseTagToResponse(WOResponse response,
            WOContext context)
    {
        if(!isDisabledInContext(context))
        {
            super._appendCloseTagToResponse(response, context);
        }
        else
        {
            response.appendContentString("</span>");
        }
    }


    // ----------------------------------------------------------
    protected void _appendQueryStringToResponse(WOResponse response,
            WOContext context, String aRequestHandlerPath,
            boolean htmlEscapeURL, boolean defaultIncludeSessionID)
    {
        NSDictionary<String, Object> queryDict =
            computeQueryDictionaryInContext(
                    aRequestHandlerPath == null ? "" : aRequestHandlerPath,
                            _queryDictionary, _otherQueryAssociations,
                            defaultIncludeSessionID, context);

        if (queryDict.count() > 0)
        {
            String queryString =
                WOCGIFormValues.getInstance().encodeAsCGIFormValues(
                        queryDict, htmlEscapeURL);

            if (queryString.length() > 0)
            {
                int questionMarkIndex = aRequestHandlerPath == null ? -1 :
                    aRequestHandlerPath.indexOf("?");

                if (questionMarkIndex > 0)
                {
                    response.appendContentString(htmlEscapeURL ? "&amp;" : "&");
                }
                else
                {
                    response.appendContentCharacter('?');
                }

                response.appendContentString(queryString);
            }
        }
    }


    // ----------------------------------------------------------
    protected void _appendFragmentToResponse(WOResponse response,
            WOContext context)
    {
        String fragmentIdentifier = fragmentIdentifierInContext(context);

        if (fragmentIdentifier.length() > 0)
        {
            response.appendContentCharacter('#');
            response.appendContentString(fragmentIdentifier);
        }
    }


    // ----------------------------------------------------------
    protected void _appendCGIActionURLToResponse(WOResponse response,
            WOContext context, boolean htmlEscapeURL)
    {
        String actionPath = computeActionStringInContext(_actionClass,
                _directActionName, context);

        NSDictionary<String, Object> queryDict =
            computeQueryDictionaryInContext(actionPath, _queryDictionary,
                    _otherQueryAssociations, true, context);

        response.appendContentString(context._directActionURL(actionPath,
                queryDict, secureInContext(context), 0, htmlEscapeURL));

        _appendFragmentToResponse(response, context);
    }


    // ----------------------------------------------------------
    protected void _appendComponentActionURLToResponse(WOResponse response,
            WOContext context, boolean escapeHTML)
    {
        String actionURL = context.componentActionURL(
                WOApplication.application().componentRequestHandlerKey(),
                secureInContext(context));

        response.appendContentString(actionURL);

        _appendQueryStringToResponse(response, context, actionURL, escapeHTML,
                true);
        _appendFragmentToResponse(response, context);
    }


    // ----------------------------------------------------------
    protected void _appendStaticURLToResponse(WOResponse response,
            WOContext context, boolean escapeHTML)
    {
        String staticURL = hrefInContext(context);

        if (WOStaticURLUtilities.isRelativeURL(staticURL) &&
                !WOStaticURLUtilities.isFragmentURL(staticURL))
        {
            String resourceURL =
                context._urlForResourceNamed(staticURL, null, false);

            if (resourceURL != null)
            {
                response.appendContentString(resourceURL);
                staticURL = resourceURL;
            }
            else
            {
                response.appendContentString(context.component().baseURL());
                response.appendContentCharacter('/');
                response.appendContentString(staticURL);
            }
        }
        else
        {
            response.appendContentString(staticURL);
        }

        _appendQueryStringToResponse(response, context, staticURL, escapeHTML,
                false);
        _appendFragmentToResponse(response, context);
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        if (isDisabledInContext(context))
        {
            return;
        }

        if (_remoteHelper.isRemoteInContext(context))
        {
            response.appendContentString(" href=\"javascript:void(0);\"");
        }
        else
        {
            String formName = ERXWOForm.formName(context, null);
            if (!isIgnoreFormInContext(context) && formName != null)
            {
                response.appendContentString(" href=\"javascript:void(0);\"");
            }
            else
            {
                _appendOpeningHrefToResponse(response, context);

                if (_actionClass != null || _directActionName != null)
                {
                    _appendCGIActionURLToResponse(response, context, true);
                }
                else if(_action != null || _pageName != null)
                {
                    _appendComponentActionURLToResponse(response, context, true);
                }
                else if(_href != null)
                {
                    _appendStaticURLToResponse(response, context, true);
                }
                else if(_fragmentIdentifier != null
                        && fragmentIdentifierInContext(context).length() > 0)
                {
                    _appendQueryStringToResponse(response, context, "", true, true);
                    _appendFragmentToResponse(response, context);
                }

                _appendClosingHrefToResponse(response, context);
            }
        }

        appendOnClickAttributeToResponse(response, context);
    }


    // ----------------------------------------------------------
    protected void _appendOpeningHrefToResponse(WOResponse response,
            WOContext context)
    {
        response.appendContentCharacter(' ');
        response.appendContentString("href");
        response.appendContentCharacter('=');
        response.appendContentCharacter('"');

        String prefix = prefixInContext(context);
        if (prefix.length() > 0)
        {
            response.appendContentString(prefix);
        }
    }


    // ----------------------------------------------------------
    protected void _appendClosingHrefToResponse(WOResponse response,
            WOContext context)
    {
        String suffix = suffixInContext(context);
        if (suffix.length() > 0)
        {
            response.appendContentString(suffix);
        }

        response.appendContentCharacter('"');
    }


    // ----------------------------------------------------------
    public void appendContentStringToResponse(WOResponse response,
            WOContext context)
    {
        if (_string != null)
        {
            WOComponent component = context.component();
            Object val = _string.valueInComponent(component);

            if (val != null)
            {
                String valueToAppend = val.toString();
                boolean shouldEscapeHTML = true;

                if (_escapeHTML != null)
                {
                    shouldEscapeHTML =
                        _escapeHTML.booleanValueInComponent(component);
                }

                if (shouldEscapeHTML)
                {
                    response.appendContentHTMLString(valueToAppend);
                }
                else
                {
                    response.appendContentString(valueToAppend);
                }
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public void appendChildrenToResponse(WOResponse response, WOContext context)
    {
        super.appendChildrenToResponse(response, context);

        appendContentStringToResponse(response, context);
    }


    // ----------------------------------------------------------
    protected String hrefInContext(WOContext context)
    {
        Object value = null;

        if (_href != null)
        {
            value = _href.valueInComponent(context.component());
        }

        if (value != null)
        {
            return value.toString();
        }
        else
        {
            return "";
        }
    }


    // ----------------------------------------------------------
    protected String fragmentIdentifierInContext(WOContext context)
    {
       Object value = null;

        if (_fragmentIdentifier != null)
        {
            value = _fragmentIdentifier.valueInComponent(context.component());
        }

        if (value != null)
        {
            return value.toString();
        }
        else
        {
            return "";
        }
    }


    // ----------------------------------------------------------
    protected boolean isDisabledInContext(WOContext context)
    {
        if (!isRenderedInContext(context))
        {
            return true;
        }

        if (_disabled != null)
        {
            return _disabled.booleanValueInComponent(context.component());
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    protected boolean isIgnoreFormInContext(WOContext context)
    {
        if (_ignoreForm != null)
        {
            return _ignoreForm.booleanValueInComponent(context.component());
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    protected void appendOnClickAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String onClick = null;

        if (_onClick != null)
        {
            onClick = _onClick.valueInComponent(context.component()).toString();
        }

        if (_remoteHelper.isRemoteInContext(context))
        {
            response.appendContentString(" onclick=\"");

            if (onClick != null)
            {
                response.appendContentString(onClick);

                if (!onClick.endsWith(";"))
                {
                    response.appendContentCharacter(';');
                }
            }

            appendXhrGetToResponse(response, context);
            response.appendContentString("\"");
        }
        else
        {
            String formName = ERXWOForm.formName(context, null);
            if (!isIgnoreFormInContext(context) && formName != null)
            {
                String senderID = context.elementID();

                if (onClick == null)
                {
                    onClick = "";
                }

                onClick += "; webcat.fullSubmit('"
                    + formName + "','" + senderID + "');";
            }

            if (onClick != null)
            {
                response.appendContentString(" onclick=\"");
                response.appendContentString(onClick);
                response.appendContentString("\"");
            }
        }
    }


    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected void appendXhrGetToResponse(WOResponse response,
            WOContext context)
    {
        WOComponent component = context.component();

        String actionUrl = null;

        if (_directActionName != null)
        {
            actionUrl = context.directActionURLForActionNamed(
                    (String) _directActionName.valueInComponent(component),
                    ERXComponentUtilities.queryParametersInComponent(
                            _associations, component)).replaceAll("&amp;", "&");
        }
        else
        {
            actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
        }

        JSHash requestOptions = new JSHash();
        requestOptions.put("url", actionUrl);

        // If we're inside a form, include the name of the element as the
        // sender. This is unnecessary for buttons, but required for other
        // types of action elements, like WCMenuItem.

        String formName = ERXWOForm.formName(context, null);
        if (!isIgnoreFormInContext(context) && formName != null)
        {
            requestOptions.put("sender", context.elementID());
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
    protected boolean shouldHandleAction(WORequest request, WOContext context)
    {
        return (context.elementID().equals(context.senderID())) ||
                (context.wasFormSubmitted() && context.isMultipleSubmitForm()
                        && request.formValueForKey(context.elementID()) != null);
    }


    // ----------------------------------------------------------
    public WOActionResults invokeStandardAction(WORequest aRequest,
            WOContext context)
    {
        String nextPageName = null;
        WOActionResults invokedElement = null;
        WOComponent component = context.component();

        if (shouldHandleAction(aRequest, context))
        {
            if (_disabled == null
                    || !_disabled.booleanValueInComponent(component))
            {
                context.setActionInvoked(true);

                if (_pageName != null)
                {
                    Object nextPageValue =
                        _pageName.valueInComponent(component);

                    if (nextPageValue != null)
                    {
                        nextPageName = nextPageValue.toString();
                    }
                }

                if (_action != null)
                {
                    invokedElement =
                        (WOActionResults)_action.valueInComponent(component);
                }
                else
                {
                    if (_pageName == null)
                    {
                        throw new IllegalStateException("<"
                                + getClass().getName()
                                + "> : Missing page name.");
                    }

                    if (nextPageName != null)
                    {
                        invokedElement =
                            WOApplication.application().pageWithName(
                                    nextPageName, context);
                    }
                    else
                    {
                        throw new WOPageNotFoundException("<"
                                + getClass().getName()
                                + "> : cannot find page.");
                    }
                }
            }
            else
            {
                invokedElement = new WONoContentElement();
            }

            if (invokedElement == null)
            {
                invokedElement = context.page();
            }
        }

        return invokedElement;
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


    //~ Static/instance variables .............................................

    protected WOAssociation _action;
    protected WOAssociation _string;
    protected WOAssociation _pageName;
    protected WOAssociation _href;
    protected WOAssociation _disabled;
    protected WOAssociation _fragmentIdentifier;
    protected WOAssociation _escapeHTML;
    protected WOAssociation _queryDictionary;
    protected WOAssociation _actionClass;
    protected WOAssociation _directActionName;
    protected WOAssociation _onClick;
    protected WOAssociation _ignoreForm;
    protected NSDictionary<String, WOAssociation> _otherQueryAssociations;

    protected DojoRemoteHelper _remoteHelper;

    private static final Logger log = Logger.getLogger(WCLink.class);
}
