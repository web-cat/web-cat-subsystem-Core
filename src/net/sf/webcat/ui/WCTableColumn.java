package net.sf.webcat.ui;

import net.sf.webcat.ui.util.DojoRemoteHelper;
import net.sf.webcat.ui.util.WCTableLayoutBuilder;
import net.sf.webcat.ui.util.WCTablePropertiesHelper;
import org.jfree.util.Log;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;

public class WCTableColumn extends WOHTMLDynamicElement
{
    public WCTableColumn(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);
        
        _name = _associations.removeObjectForKey("name");
        _componentName = _associations.removeObjectForKey("componentName");
        _keyPath = _associations.removeObjectForKey("keyPath");
        _expression = _associations.removeObjectForKey("expression");
        _sortingKeyPath = _associations.removeObjectForKey("sortingKeyPath");
        
        _propertiesHelper = new WCTablePropertiesHelper(_associations);
    }

    
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        WCTableLayoutBuilder layout = WCTable.currentTableLayout();

        if (layout == null)
        {
            Log.error("WCDataTableColumn must occur as an immediate child "
                    + "of a WCDataTable element. Your layout will not work "
                    + "as expected.");
        }
        else
        {
            String name = nameInContext(context);
            String componentName = componentNameInContext(context);
            String keyPath = keyPathInContext(context);
            String expression = expressionInContext(context);
            String sortingKeyPath = sortingKeyPathInContext(context);
            NSDictionary<String, Object> properties =
                _propertiesHelper.propertiesInContext(context);

            if (expression != null)
            {
                keyPath = "~" + expression;
            }

            layout.add(name, componentName, keyPath, sortingKeyPath,
                    properties);
        }

        WCTable.commitTableLayoutChanges();

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
    protected String componentNameInContext(WOContext context)
    {
        String name = null;
        
        if(_componentName != null)
        {
            name = _componentName.valueInComponent(
                    context.component()).toString();
        }
        
        return name;
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


    // ----------------------------------------------------------
    protected String expressionInContext(WOContext context)
    {
        String expression = null;
        
        if(_expression != null)
        {
            expression = _expression.valueInComponent(
                    context.component()).toString();
        }
        
        return expression;
    }


    // ----------------------------------------------------------
    protected String sortingKeyPathInContext(WOContext context)
    {
        String keyPath = null;
        
        if(_sortingKeyPath != null)
        {
            keyPath = _sortingKeyPath.valueInComponent(
                    context.component()).toString();
        }
        
        return keyPath;
    }

    
    protected WOAssociation _name;
    protected WOAssociation _componentName;
    protected WOAssociation _keyPath;
    protected WOAssociation _expression;
    protected WOAssociation _sortingKeyPath;
    protected WCTablePropertiesHelper _propertiesHelper;
}
