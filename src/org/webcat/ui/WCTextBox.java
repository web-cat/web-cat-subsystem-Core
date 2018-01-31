/*==========================================================================*\
 |  $Id: WCTextBox.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

import java.math.BigDecimal;
import java.text.Format;
import java.text.ParseException;
import org.webcat.ui._base.DojoFormElement;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOFormatterRepository;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSValidation;

//------------------------------------------------------------------------
/**
 * A basic text box that automatically takes on Dojo styling.
 *
 * <h2>Bindings</h2>
 * <dl>
 * <dt>value</dt>
 * <dd>The text entered into the text box.</dd>
 * </dl>
 *
 * @author Tony Allevato
 * @version $Id: WCTextBox.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class WCTextBox extends DojoFormElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTextBox(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("span", someAssociations, template);

        _formatter = _associations.removeObjectForKey("formatter");
        _dateFormat = _associations.removeObjectForKey("dateformat");
        _numberFormat = _associations.removeObjectForKey("numberformat");
        _useDecimalNumber = _associations.removeObjectForKey("useDecimalNumber");
        _remoteValidator = _associations.removeObjectForKey("remoteValidator");

        if (_dateFormat != null && _numberFormat != null)
            throw new WODynamicElementCreationException("<" +
                    getClass().getName() +
                    "> Cannot have 'dateFormat' and 'numberFormat' " +
                    "attributes at the same time.");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        if (_remoteValidator != null)
        {
            return "dijit.form.ValidationTextBox";
        }
        else
        {
            return "dijit.form.TextBox";
        }
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
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        if (_remoteValidator != null)
        {
            WCForm.addValidatorToCurrentForm(context.elementID(),
                    _remoteValidator);
        }

        return super.invokeAction(request, context);
    }


    // ----------------------------------------------------------
    @Override
    protected Object objectForStringValue(String stringValue, WOContext context)
    {
        WOComponent component = context.component();
        Object objectValue = stringValue;

        if (stringValue != null)
        {
            Format formatter = null;

            if (stringValue.length() != 0)
            {
                formatter = WOFormatterRepository.formatterForComponent(
                        component, _dateFormat, _numberFormat, _formatter);
            }

            if (formatter != null)
            {
                try
                {
                    Object firstPass = formatter.parseObject(stringValue);
                    String formattedValue = formatter.format(firstPass);
                    objectValue = formatter.parseObject(formattedValue);
                }
                catch (ParseException e1)
                {
                    String keyPath = _value.keyPath();

                    NSValidation.ValidationException exception =
                        new NSValidation.ValidationException(
                            e1.getMessage(), stringValue, keyPath);

                    component.validationFailedWithException(exception,
                            stringValue, keyPath);

                    return null;
                }

                if (objectValue != null && _useDecimalNumber != null
                        && _useDecimalNumber.booleanValueInComponent(component))
                {
                    objectValue = new BigDecimal(objectValue.toString());
                }
            }
            else if (objectValue.toString().length() == 0)
            {
                objectValue = null;
            }
        }

        return objectValue;
    }


    // ----------------------------------------------------------
    @Override
    protected String stringValueForObject(Object value, WOContext context)
    {
        if (value != null)
        {
            WOComponent component = context.component();
            String stringValue = null;

            Format formatter = WOFormatterRepository.formatterForInstance(
                    value, component, _dateFormat, _numberFormat, _formatter);

            if (formatter != null)
            {
                try
                {
                    String formattedValue = formatter.format(value);
                    Object parsedValue = formatter.parseObject(formattedValue);
                    stringValue = formatter.format(parsedValue);
                }
                catch (Exception e)
                {
                    stringValue = null;
                }
            }

            if (stringValue == null)
            {
                stringValue = value.toString();
            }

            return stringValue;
        }

        return null;
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _formatter;
    protected WOAssociation _dateFormat;
    protected WOAssociation _numberFormat;
    protected WOAssociation _useDecimalNumber;
    protected WOAssociation _remoteValidator;
}
