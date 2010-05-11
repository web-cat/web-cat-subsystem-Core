package org.webcat.ui;

import org.jfree.util.Log;
import org.webcat.ui.util.DojoRemoteHelper;
import org.webcat.ui.util.WCTableFilterBuilder;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;

public class WCTableFilter extends WOHTMLDynamicElement
{
    public WCTableFilter(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);
        
        _name = _associations.removeObjectForKey("name");
        _keyPath = _associations.removeObjectForKey("keyPath");
        _type = _associations.removeObjectForKey("type");
//        _choices = _associations.removeObjectForKey("choices");
    }

    
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        WCTableFilterBuilder filters = WCTable.currentFilters();

        if (filters == null)
        {
            Log.error("WCDataTableFilter must occur as an immediate child "
                    + "of a WCDataTable element. Your filter will not work "
                    + "as expected.");
        }
        else
        {
            String name = nameInContext(context);
            String type = typeInContext(context);
            String keyPath = keyPathInContext(context);

            filters.add(name, keyPath, type);
        }

        WCTable.commitFilterChanges();

        // Do not call super; this element generates no content.
    }
    

    // ----------------------------------------------------------
    protected String nameInContext(WOContext context)
    {
        String name = null;
        
        if(_name != null)
        {
            name = _name.valueInComponent(context.component()).toString();
        }
        
        return name;
    }

    
    // ----------------------------------------------------------
    protected String typeInContext(WOContext context)
    {
        String type = null;
        
        if(_type != null)
        {
            type = _type.valueInComponent(context.component()).toString();
        }
        
        return type;
    }
    

    // ----------------------------------------------------------
    protected String keyPathInContext(WOContext context)
    {
        String keyPath = null;
        
        if(_keyPath != null)
        {
            keyPath = _keyPath.valueInComponent(context.component()).toString();
        }
        
        return keyPath;
    }

    
    protected WOAssociation _name;
    protected WOAssociation _keyPath;
    protected WOAssociation _type;
}
