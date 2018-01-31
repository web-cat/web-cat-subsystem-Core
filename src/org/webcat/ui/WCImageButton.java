/*==========================================================================*\
 |  $Id: WCImageButton.java,v 1.2 2011/04/19 16:31:36 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

import org.webcat.ui._base.DojoActionFormElement;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.appserver._private.WOImageInfo;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;

//------------------------------------------------------------------------
/**
 * A simple clickable image. Based on {@code WOImageButton}, but also supports
 * remote actions.
 *
 * <h2>Bindings</h2>
 * See also the bindings for {@link org.webcat.ui._base.DojoActionFormElement}.
 *
 * <dl>
 * <dt>framework</dt>
 * <dd>The name of the framework from which to retrieve the image.</dd>
 * <dt>filename</dt>
 * <dd>The filename of the image, relative to the WebServerResources folder of
 * the specified framework.</dd>
 * <dt>width</dt>
 * <dd>The width of the images, in pixels.</dd>
 * <dt>height</dt>
 * <dd>The height of the images, in pixels.</dd>
 * </table>
 *
 * @author Tony Allevato
 * @version $Id: WCImageButton.java,v 1.2 2011/04/19 16:31:36 aallowat Exp $
 */
public class WCImageButton extends DojoActionFormElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public WCImageButton(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("input", someAssociations, template);

        _framework = _associations.removeObjectForKey("framework");
        _filename = _associations.removeObjectForKey("filename");
        _width = _associations.removeObjectForKey("width");
        _height = _associations.removeObjectForKey("height");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return null;
    }


    // ----------------------------------------------------------
    @Override
    public String inputTypeInContext(WOContext context)
    {
        return "image";
    }


    // ----------------------------------------------------------
    @Override
    public boolean hasContent()
    {
        return false;
    }


    // ----------------------------------------------------------
    protected String _imageNameInContext(WOContext context)
    {
        if (_filename != null)
        {
            return (String) _filename.valueInComponent(
                    context.component());
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    protected void _appendImageAttributesToResponse(WOResponse response,
            WOContext context)
    {
        WOResourceManager resourceManager = WOApplication.application()
                .resourceManager();
        WOComponent component = context.component();

        String imageName = _imageNameInContext(context);
        String frameworkName = WOHTMLDynamicElement._frameworkNameInComponent(
                _framework, component);
        String imageURL = context._urlForResourceNamed(imageName,
                frameworkName, true);

        if (imageURL != null)
        {
            String width = null;
            String height = null;
            boolean shouldComputeWidth = false;
            boolean shouldComputeHeight = false;

            if (_width != null || _height != null)
            {
                if (_width != null)
                {
                    Object widthValue = _width.valueInComponent(component);
                    width = (widthValue == null) ? null : widthValue.toString();
                    shouldComputeWidth = (width == null) || (width.equals("*"));
                }

                if (_height != null)
                {
                    Object heightValue = _height.valueInComponent(component);
                    height = (heightValue == null) ? null : heightValue.toString();
                    shouldComputeHeight = (height == null) || (height.equals("*"));
                }
            }
            else
            {
                shouldComputeWidth = true;
                shouldComputeHeight = true;
            }

            if (shouldComputeWidth || shouldComputeHeight)
            {
                WOImageInfo imageInfo = resourceManager._imageInfoForUrl(
                        imageURL, imageName, frameworkName,
                        context._languages());

                if (imageInfo != null)
                {
                    if (shouldComputeWidth)
                    {
                        width = imageInfo.widthString();
                    }

                    if (shouldComputeHeight)
                    {
                        height = imageInfo.heightString();
                    }
                }
                else
                {
                    NSLog.err.appendln(
                            "<WOImage>: could not get height/width "
                            + "information for image at " + imageURL + " / "
                            + imageName + " / " + frameworkName);
                }
            }

            response._appendTagAttributeAndValue("src", imageURL, false);

            if (width != null)
            {
                response._appendTagAttributeAndValue("width", width, false);
            }

            if (height != null)
            {
                response._appendTagAttributeAndValue("height", height, false);
            }
        }
        else
        {
            imageURL = resourceManager.errorMessageUrlForResourceNamed(
                    imageName, frameworkName);
            response._appendTagAttributeAndValue("src", imageURL, false);
        }
    }


    // ----------------------------------------------------------
    public void appendAttributesToResponse(WOResponse response,
                                           WOContext context)
    {
        _appendImageAttributesToResponse(response, context);
        super.appendAttributesToResponse(response, context);
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
                String name = nameInContext(context);
                String value = (String) request.formValueForKey(name);
                String ptX = (String) request.formValueForKey(name + ".x");
                String ptY = (String) request.formValueForKey(name + ".y");

                if (value != null || (ptX != null && ptY != null))
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


    // ----------------------------------------------------------
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(getClass().getName());
        sb.append(" " + super.toString());
        sb.append(" framework=" + this._framework);
        sb.append(" filename=" + this._filename);
        sb.append(" width=" + this._width);
        sb.append(" height=" + this._height);
        sb.append(">");
        return sb.toString();
    }


    //~ Static/instance variables .............................................

    private WOAssociation _framework;
    private WOAssociation _filename;
    private WOAssociation _width;
    private WOAssociation _height;
}
