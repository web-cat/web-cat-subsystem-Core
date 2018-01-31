/*==========================================================================*\
 |  $Id: WCFileUpload.java,v 1.2 2011/03/07 18:45:12 stedwar2 Exp $
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

package org.webcat.ui;

import org.apache.log4j.Logger;
import org.webcat.ui._base.DojoElement;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A replacement for WOFileUpload that uploads files immediately, in the
 * background, instead of waiting for a full page submit. Javascript callbacks
 * can be used to determine when the upload is completed.
 *
 * <h2>Bindings</h2>
 *
 * <dl>
 *
 * <dt>clearAfterUpload</dt>
 * <dd>If false, the name of the uploaded file will remain displayed in the
 * control when the file upload is complete. If true, the control will be
 * cleared. False is most appropriate when the control is being used to upload
 * a single file and the user is expected to be able to change their selection
 * later; true is most appropriate when the control is used as a sink for
 * multiple file uploads, and feedback about which files have been uploaded is
 * provided elsewhere on the page. Defaults to false.</dd>
 *
 * <dt>filePath</dt>
 * <dd>This binding will receive the name of the file that was uploaded.</dd>
 *
 * <dt>data</dt>
 * <dd>This binding will receive the data for the file that was uploaded.</dd>
 *
 * <dt>disabled</dt>
 * <dd>True to disable the control, otherwise false.</dd>
 *
 * <dt>mimeType</dt>
 * <dd>This binding will receive the MIME type of the file that is uploaded.
 * </dd>
 *
 * <dt>copyData</dt>
 * <dd>True if the data stored in the <b>data</b> binding should be copied;
 * false if it should use the same data object that makes up the request data,
 * which is only useful if you do not need to keep the data around beyond the
 * current request/response cycle because the data buffer is recycled. Defaults
 * to true.
 * </dd>
 *
 * <dt>action</dt>
 * <dd>The action that should be invoked when the file upload is complete. At
 * this point <b>filePath</b>, <b>data</b>, and <b>mimeType</b> can be assumed
 * to be set. The response to this action is like that of a remote action in
 * the sense that it should either be null (to do nothing further on the client
 * side), or a JavascriptGenerator that describes how to manipulate the page
 * DOM once the upload is complete.</dd>
 *
 * <dt>remoteValidator</dt>
 * <dd>A validator method used to validate the appropriateness of the uploaded
 * content before the form containing this element is submitted. This can be
 * used to ensure that that a file is uploaded before the user continues to the
 * next page, for instance. This method should take no arguments and return
 * null if validation is successful, or a String containing an error to relay
 * back to the user.</dd>
 *
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2011/03/07 18:45:12 $
 */
public class WCFileUpload extends DojoElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCFileUpload(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("span", someAssociations, template);

        _name = _associations.removeObjectForKey("name");
        _disabled = _associations.removeObjectForKey("disabled");
        _filePath = _associations.removeObjectForKey("filePath");
        _data = _associations.removeObjectForKey("data");
        _mimeType = _associations.removeObjectForKey("mimeType");
        _copyData = _associations.removeObjectForKey("copyData");
        _action = _associations.removeObjectForKey("action");
        _remoteValidator = _associations.removeObjectForKey("remoteValidator");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "webcat.FileInput";
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the "type" attribute that should be written along with
     * the tag, if it is an &lt;input&gt; element.
     * @param context TODO
     *
     * @return the value of the element's "type" attribute
     */
    protected String inputTypeInContext(WOContext context)
    {
        return "file";
    }


    // ----------------------------------------------------------
    /**
     * Gets the name of the element in the specified context.
     *
     * @param context
     *            the context
     * @return the name of the element
     */
    public String nameInContext(WOContext context)
    {
        if (_name != null)
        {
            Object value = _name.valueInComponent(context.component());

            if (value != null)
            {
                return value.toString();
            }
        }

        Object elementID = context.elementID();

        if (elementID != null)
        {
            return elementID.toString();
        }
        else
        {
            throw new IllegalStateException("<" + getClass().getName() + "> "
                    + "Cannot evaluate 'name' attribute, and context element "
                    + "ID is null.");
        }
    }


    // ----------------------------------------------------------
    protected boolean isDisabledInContext(WOContext context)
    {
        if (_disabled != null)
            return _disabled.booleanValueInComponent(context.component());
        else
            return false;
    }


    // ----------------------------------------------------------
    protected boolean copyDataInContext(WOContext context)
    {
        if (_copyData != null)
            return _copyData.booleanValueInComponent(context.component());
        else
            return true;
    }


    // ----------------------------------------------------------
    protected void appendInputTypeAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String inputType = inputTypeInContext(context);

        if (inputType != null && inputType.length() > 0)
        {
            _appendTagAttributeAndValueToResponse(response, "type", inputType,
                    false);
        }
    }


    // ----------------------------------------------------------
    protected void appendNameAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String name = nameInContext(context);

        if (name != null && name.length() > 0)
        {
            _appendTagAttributeAndValueToResponse(response, "name", name,
                    false);
        }
    }


    // ----------------------------------------------------------
    protected void appendDisabledAttributeToResponse(WOResponse response,
            WOContext context)
    {
        if (isDisabledInContext(context))
        {
            _appendTagAttributeAndValueToResponse(response, "disabled",
                    "disabled", false);
        }
    }


    // ----------------------------------------------------------
    protected void appendUrlAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String actionUrl = context.componentActionURL();

        _appendTagAttributeAndValueToResponse(response, "url",
                actionUrl, false);
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        appendInputTypeAttributeToResponse(response, context);
        appendDisabledAttributeToResponse(response, context);
        appendNameAttributeToResponse(response, context);
        appendUrlAttributeToResponse(response, context);
    }


    // ----------------------------------------------------------
    @Override
    public void appendChildrenToResponse(WOResponse response, WOContext context)
    {
        super.appendChildrenToResponse(response, context);

        if (_remoteValidator != null)
        {
            WCForm.appendValidatorScriptToResponse(response, context);
        }
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        if (!isDisabledInContext(context))
        {
            String nameString = nameInContext(context);
            Object aFilePath = null;

            if (_filePath != null)
            {
                aFilePath = request.formValueForKey(nameString + ".filename");

                if ((aFilePath instanceof String) &&
                        ((String) aFilePath).length() > 0)
                {
                    _filePath.setValue(aFilePath, context.component());
                }
                else
                {
                    aFilePath = null;
                }
            }

            if (_data != null && aFilePath != null)
            {
                NSArray<?> aValue = request.formValuesForKey(nameString);

                if (aValue != null)
                {
                    NSData dataCopy = null;
                    try
                    {
                        dataCopy = (NSData) aValue.objectAtIndex(0);
                    }
                    catch (ClassCastException cce)
                    {
                        throw new ClassCastException(
                                "<WCFileUpload>: Value in request was of type '"
                                + aValue.objectAtIndex(0).getClass().getName()
                                + "' instead of NSData. Verify that the WOForm's "
                                + "'enctype' binding is set to 'multipart/form-data'");
                    }

                    if (copyDataInContext(context))
                    {
                        dataCopy = new NSData(dataCopy);
                    }

                    _data.setValue(dataCopy, context.component());
                }

                if (_mimeType != null)
                {
                    Object aMimeType = request.formValueForKey(
                            nameString + ".mimetype");

                    if ((aMimeType instanceof String) &&
                            ((String) aMimeType).length() > 0)
                    {
                        _mimeType.setValue(aMimeType, context.component());
                    }
                }
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        WOActionResults result = null;

        if (!isDisabledInContext(context))
        {
            if (_remoteValidator != null)
            {
                WCForm.addValidatorToCurrentForm(context.elementID(),
                        _remoteValidator);
            }

            String nameString = nameInContext(context);
            Object aValue = request.formValueForKey(nameString);

            // Only invoke the action if this element is the file input being
            // submitted.

            if (aValue != null)
            {
                if (_action != null)
                {
                    result = (WOActionResults) _action.valueInComponent(
                            context.component());
                }

                WOResponse response = new WOResponse();
                appendActionResultToResponse(result, response);
                result = response;
            }
        }

        return result;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Appends the upload element's action results to a response, embedding
     * them in an HTML document with a textarea, as required by the Dojo iframe
     * I/O framework.
     * </p><p>
     * Future versions of this component may want to check the content type of
     * the action response and remove the embedding if it is HTML or XML
     * content, but for now we only support Javascript responses that are
     * executed client-side.
     * </p>
     *
     * @param result the action result
     * @param response the response
     */
    private void appendActionResultToResponse(
            WOActionResults result, WOResponse response)
    {
        response.appendContentString("<html><head></head><body><textarea>");

        if (result != null)
        {
            response.appendContentString(
                    result.generateResponse().contentString());
        }

        response.appendContentString("</textarea></body></html>");
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _disabled;
    protected WOAssociation _name;
    protected WOAssociation _filePath;
    protected WOAssociation _data;
    protected WOAssociation _mimeType;
    protected WOAssociation _copyData;
    protected WOAssociation _action;
    protected WOAssociation _remoteValidator;

    static final Logger log = Logger.getLogger(WCFileUpload.class);
}
