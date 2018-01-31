package org.webcat.ui;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOComponentReference;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSUtilities;

//-------------------------------------------------------------------------
/**
 * <p>
 * An experimental component that gives more control over how nested component
 * content is processed.
 * </p>
 * <h3>Page.wo</h3>
 * <pre>
 * [wo:TestContainer]
 *     [wo:TestElement1]Element 1-1[/wo:TestElement1]
 *     [wo:TestElement1]Element 1-2[/wo:TestElement1]
 *     [wo:TestElement2]Element 2-1[/wo:TestElement2]
 *     [wo:TestElement2]Element 2-2[/wo:TestElement2]
 * [/wo:TestContainer]</pre>
 *
 * <h3>TestContainer.wo</h3>
 * <pre>
 * [div]
 *     [div style="background-color: red"]
 *         [wo:WCChildElements componentName="TestElement2"/]
 *     [/div]
 *     [div style="background-color: green"]
 *         [wo:WCChildElements componentName="TestElement1"/]
 *     [/div]
 *     [div style="background-color: blue"]
 *         [wo:WCChildElements componentName="TestElement2"/]
 *     [/div]
 * [/div]</pre>
 * would effectively produce:
 * <pre>
 * [div]
 *     [div style="background-color: red"]
 *         [wo:TestElement1]Element 1-1[/wo:TestElement1]
 *         [wo:TestElement1]Element 1-2[/wo:TestElement1]
 *     [/div]
 *     [div style="background-color: green"]
 *         [wo:TestElement2]Element 2-1[/wo:TestElement2]
 *         [wo:TestElement2]Element 2-2[/wo:TestElement2]
 *     [/div]
 *     [div style="background-color: blue"]
 *         [wo:TestElement1]Element 1-1[/wo:TestElement1]
 *         [wo:TestElement1]Element 1-2[/wo:TestElement1]
 *     [/div]
 * [/div]</pre>
 * with TestElement1 and TestElement2 being processed as normal.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/10/28 00:37:30 $
 */
public class WCComponentContentSelector extends WODynamicElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCComponentContentSelector(String aName,
            NSDictionary<String, WOAssociation> associations, WOElement template)
    {
        super(null, null, null);

        _componentName = associations.objectForKey("componentName");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    protected String componentNameInContext(WOContext context)
    {
        return (String) _componentName.valueInComponent(context.component());
    }


    // ----------------------------------------------------------
    protected boolean shouldElementBeProcessed(WOElement element,
                                               WOContext context)
    {
        if (element instanceof WOComponentReference)
        {
            WOComponentReference ref = (WOComponentReference) element;

            String compName = componentNameInContext(context);
            Class<?> compClass = _NSUtilities.classWithName(compName);
            Class<?> refClass = _NSUtilities.classWithName(ref._name);

            if (compClass.equals(refClass))
            {
                return true;
            }
        }

        return false;
    }


    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        WOComponent component = context.component();
        WOElement contentElement = component._childTemplate();

        if (contentElement != null && contentElement instanceof WODynamicGroup)
        {
            WODynamicGroup group = (WODynamicGroup) contentElement;

            for (WOElement child : group.childrenElements())
            {
                if (shouldElementBeProcessed(child, context))
                {
                    context._setCurrentComponent(component.parent());
                    child.appendToResponse(response, context);
                    context._setCurrentComponent(component);
                }
            }
        }
    }


    // ----------------------------------------------------------
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        WOComponent component = context.component();
        WOElement contentElement = component._childTemplate();

        if (contentElement != null && contentElement instanceof WODynamicGroup)
        {
            WODynamicGroup group = (WODynamicGroup) contentElement;

            for (WOElement child : group.childrenElements())
            {
                if (shouldElementBeProcessed(child, context))
                {
                    context._setCurrentComponent(component.parent());
                    child.takeValuesFromRequest(request, context);
                    context._setCurrentComponent(component);
                }
            }
        }
    }


    // ----------------------------------------------------------
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        WOActionResults result = null;
        WOComponent component = context.component();
        WOElement contentElement = component._childTemplate();

        if (contentElement != null && contentElement instanceof WODynamicGroup)
        {
            WODynamicGroup group = (WODynamicGroup) contentElement;

            for (WOElement child : group.childrenElements())
            {
                if (shouldElementBeProcessed(child, context))
                {
                    context._setCurrentComponent(component.parent());
                    result = child.invokeAction(request, context);
                    context._setCurrentComponent(component);
                }
            }
        }

        return result;
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _componentName;
}
