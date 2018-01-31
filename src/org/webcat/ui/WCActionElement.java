package org.webcat.ui;

import org.apache.log4j.Logger;
import org.webcat.ui._base.DojoActionFormElement;
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

//-------------------------------------------------------------------------
/**
 * An element whose tag name can be anything you want (using an
 * {@code elementName} binding similar to {@code WOGenericElement}), but which
 * can also execute actions (standard and remote) when clicked. This is useful
 * when you want to perform an action through the {@code onClick} handler of an
 * element but you don't want to use a button or a link, and you don't want to
 * deal with the hassle of creating a {@code WCActionFunction} and binding it
 * manually to the {@code onClick} handler of an element.
 *
 * @author Tony Allevato
 * @author Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/02 16:18:29 $
 */
public class WCActionElement extends WOHTMLDynamicElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public WCActionElement(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);

        _otherQueryAssociations = _NSDictionaryUtilities
                .extractObjectsForKeysWithPrefix(_associations, "?", true);

        if (_otherQueryAssociations == null
                || _otherQueryAssociations.count() <= 0)
        {
            _otherQueryAssociations = null;
        }

        _elementName = _associations.removeObjectForKey("elementName");
        _action = _associations.removeObjectForKey("action");
        _disabled = _associations.removeObjectForKey("disabled");
        _actionClass = _associations.removeObjectForKey("actionClass");
        _directActionName = _associations
                .removeObjectForKey("directActionName");
        _pageName = _associations.removeObjectForKey("pageName");
        _onClick = _associations.removeObjectForKey("onClick");
        _ignoreForm = _associations.removeObjectForKey("ignoreForm");
        _remoteHelper = new DojoRemoteHelper(_associations);

        if (_action == null && _pageName == null && _directActionName == null
                && _actionClass == null)
        {
            throw new WODynamicElementCreationException("<"
                    + getClass().getName()
                    + "> Missing required attribute: 'action' or or "
                    + "'pageName' or 'directActionName' or 'actionClass'");
        }

        if ((_action != null && _pageName != null)
                || (_action != null && _directActionName != null)
                || (_pageName != null && _directActionName != null)
                || (_action != null && _actionClass != null))
        {
            throw new WODynamicElementCreationException("<"
                    + getClass().getName()
                    + "> At least two of these conflicting attributes are "
                    + "present: 'action', 'pageName', "
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
    protected void _appendOpenTagToResponse(WOResponse response,
            WOContext context)
    {
        response.appendContentCharacter('<');
        response.appendContentString(elementNameInContext(context));

        appendAttributesToResponse(response, context);

        if (!(hasContent()))
        {
            response.appendContentString(" /");
        }
        response.appendContentCharacter('>');
    }


    // ----------------------------------------------------------
    protected void _appendCloseTagToResponse(WOResponse response,
            WOContext context)
    {
        if (hasContent())
        {
            response.appendContentString("</");
            response.appendContentString(elementNameInContext(context));
            response.appendContentCharacter('>');
        }
    }


    // ----------------------------------------------------------
    protected void _appendCGIActionURLToResponse(WOResponse response,
            WOContext context, boolean htmlEscapeURL)
    {
        String actionPath = computeActionStringInContext(_actionClass,
                _directActionName, context);

        NSDictionary<String, Object> queryDict = computeQueryDictionaryInContext(
                actionPath, null /* FIXME */, _otherQueryAssociations, true,
                context);

        response.appendContentString(context._directActionURL(actionPath,
                queryDict, secureInContext(context), 0, htmlEscapeURL));
    }


    // ----------------------------------------------------------
    protected void _appendComponentActionURLToResponse(WOResponse response,
            WOContext context, boolean escapeHTML)
    {
        String actionURL = context.componentActionURL(WOApplication
                .application().componentRequestHandlerKey(),
                secureInContext(context));

        response.appendContentString(actionURL);
    }


    // ----------------------------------------------------------
    protected void _appendOpeningHrefToResponse(WOResponse response,
            WOContext context)
    {
        response.appendContentCharacter(' ');
        response.appendContentString("href");
        response.appendContentCharacter('=');
        response.appendContentCharacter('"');
    }


    // ----------------------------------------------------------
    protected void _appendClosingHrefToResponse(WOResponse response,
            WOContext context)
    {
        response.appendContentCharacter('"');
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
            /*else
            {
                _appendOpeningHrefToResponse(response, context);

                if (_actionClass != null || _directActionName != null)
                {
                    _appendCGIActionURLToResponse(response, context, true);
                }
                else if (_action != null || _pageName != null)
                {
                    _appendComponentActionURLToResponse(response, context, true);
                }

                _appendClosingHrefToResponse(response, context);
            }*/
        }

        appendOnClickAttributeToResponse(response, context);
    }


    // ----------------------------------------------------------
    protected String elementNameInContext(WOContext context)
    {
        if (_elementName != null)
        {
            Object elementName = _elementName.valueInComponent(context
                    .component());
            return (elementName == null) ? null : elementName.toString();
        }
        else
        {
            return null;
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
            if (formName == null)
            {
                log.warn("Rendered a WCActionElement with a non-remote action "
                        + "that isn't in a form; the action will not execute.");
            }

            if (!isIgnoreFormInContext(context) && formName != null)
            {
                String senderID = context.elementID();

                if (onClick == null)
                {
                    onClick = "";
                }

                onClick += "; webcat.fullSubmit('" + formName + "','"
                        + senderID + "');";
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
    protected void appendXhrGetToResponse(WOResponse response, WOContext context)
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

        response.appendContentString(_remoteHelper.remoteSubmitCall("this",
                requestOptions, context));
    }


    // ----------------------------------------------------------
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        if (AjaxUtils.isAjaxRequest(request)
                && AjaxUtils.shouldHandleRequest(request, context, null))
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
        return (context.elementID().equals(context.senderID()))
                || (context.wasFormSubmitted()
                        && context.isMultipleSubmitForm() && request
                        .formValueForKey(context.elementID()) != null);
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
                    Object nextPageValue = _pageName
                            .valueInComponent(component);

                    if (nextPageValue != null)
                    {
                        nextPageName = nextPageValue.toString();
                    }
                }

                if (_action != null)
                {
                    invokedElement = (WOActionResults) _action
                            .valueInComponent(component);
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
                        invokedElement = WOApplication.application()
                                .pageWithName(nextPageName, context);
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

    protected WOAssociation _elementName;
    protected WOAssociation _action;
    protected WOAssociation _pageName;
    protected WOAssociation _disabled;
    protected WOAssociation _actionClass;
    protected WOAssociation _directActionName;
    protected WOAssociation _onClick;
    protected WOAssociation _ignoreForm;
    protected NSDictionary<String, WOAssociation> _otherQueryAssociations;

    protected DojoRemoteHelper _remoteHelper;

    private static final Logger log = Logger.getLogger(WCActionElement.class);
}
