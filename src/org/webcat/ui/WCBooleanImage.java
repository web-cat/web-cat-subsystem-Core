package org.webcat.ui;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.appserver._private.WOHTMLURLValuedElement;
import com.webobjects.appserver._private.WOImageInfo;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSLog.Logger;
import com.webobjects.foundation.NSMutableDictionary;

//-------------------------------------------------------------------------
/**
 * A dynamic element that acts like WOImage, but allows for selecting from one
 * of two images based on a boolean value.
 *
 * <h2>Bindings</h2>
 * <dl>
 * <dt>value</dt>
 * <dd>A boolean value that indicates whether the {@code trueFilename} or the
 * {@code falseFilename} should be chosen as the image to display.</dd>
 * <dt>framework</dt>
 * <dd>The name of the framework from which to retrieve the image.</dd>
 * <dt>trueFilename</dt>
 * <dd>The filename of the image to use if the {@code value} binding is true,
 * or if the {@code falseFilename} binding is not provided.</dd>
 * <dt>falseFilename</dt>
 * <dd>The filename of the image to use if the {@code value} binding is false,
 * or if the {@code trueFilename} binding is not provided.</dd>
 * <dt>width</dt>
 * <dd>The width of the images, in pixels. It is assumed that both images have
 * the same size.</dd>
 * <dt>height</dt>
 * <dd>The height of the images, in pixels. It is assumed that both images have
 * the same size.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/01/20 15:34:11 $
 */
public class WCBooleanImage extends WOHTMLDynamicElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCBooleanImage(String aName,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("img", someAssociations.mutableClone(), template);

        _value = _associations.removeObjectForKey("value");
        _framework = _associations.removeObjectForKey("framework");
        _trueFilename = _associations.removeObjectForKey("trueFilename");
        _falseFilename = _associations.removeObjectForKey("falseFilename");
        _width = _associations.removeObjectForKey("width");
        _height = _associations.removeObjectForKey("height");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    protected boolean hasContent()
    {
        return false;
    }


    // ----------------------------------------------------------
    @SuppressWarnings("null")
    protected String _imageNameInContext(WOContext context)
    {
        boolean value = false;

        if (_value != null)
        {
            value = _value.booleanValueInComponent(context.component());
        }

        WOAssociation filenameAssoc = null;

        if (_trueFilename != null && _falseFilename != null)
        {
            filenameAssoc = value ? _trueFilename : _falseFilename;
        }
        else if (_trueFilename != null)
        {
            filenameAssoc = _trueFilename;
        }
        else if (_falseFilename != null)
        {
            filenameAssoc = _falseFilename;
        }

        return (String) filenameAssoc.valueInComponent(
                context.component());
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
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(getClass().getName());
        sb.append(" " + super.toString());
        sb.append(" framework=" + this._framework);
        sb.append(" trueFilename=" + this._trueFilename);
        sb.append(" falseFilename=" + this._falseFilename);
        sb.append(" width=" + this._width);
        sb.append(" height=" + this._height);
        sb.append(">");
        return sb.toString();
    }


    //~ Static/instance variables .............................................

    private WOAssociation _value;
    private WOAssociation _framework;
    private WOAssociation _trueFilename;
    private WOAssociation _falseFilename;
    private WOAssociation _width;
    private WOAssociation _height;
}
