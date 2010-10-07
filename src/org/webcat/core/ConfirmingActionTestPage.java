package org.webcat.core;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

public class ConfirmingActionTestPage extends WCComponent
{
    // ----------------------------------------------------------
    public ConfirmingActionTestPage(WOContext context)
    {
        super(context);
    }


    public boolean checkBoxChecked;
    public String textBoxValue;


    // ----------------------------------------------------------
    public WOActionResults processThings()
    {
        return new ConfirmingAction(this) {
            @Override
            protected String confirmationMessage()
            {
                return "Confirming the values <b>" + checkBoxChecked + "</b> and <b>"
                    + textBoxValue + "</b>?";
            }

            @Override
            protected WOActionResults performStandardAction()
            {
                return null;
            }
        };
    }
}
