package net.sf.webcat.ui;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

public class WCContentSpan extends WCContentPane
{
    public WCContentSpan(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);
    }

    
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "webcat.ContentSpan";
    }


    // ----------------------------------------------------------
    @Override
    public String elementName()
    {
        return "span";
    }
}
