package org.webcat.ui;

import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCDropDownItem extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCDropDownItem(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    public boolean isSelectable()
    {
        return this.valueForBooleanBinding("isSelectable", true);
    }


    // ----------------------------------------------------------
    public boolean hasAction()
    {
        return hasBinding("action");
    }


    // ----------------------------------------------------------
    public WOActionResults action()
    {
        return (WOActionResults) valueForBinding("action");
    }


    // ----------------------------------------------------------
    public boolean remote()
    {
        return valueForBooleanBinding("remote", false);
    }


    // ----------------------------------------------------------
    public JavascriptGenerator selectItem()
    {
        return WCDropDownList.currentDropDownList().selectCurrentItem();
    }


    // ----------------------------------------------------------
    public WOActionResults clickItem()
    {
        WCDropDownList.currentDropDownList().selectCurrentItem();
        return action();
    }


    // ----------------------------------------------------------
    public boolean isRenderingTitleArea()
    {
        return WCDropDownList.currentDropDownList().isRenderingTitleArea();
    }


    // ----------------------------------------------------------
    public String hideMenuScript()
    {
        WCDropDownList list = WCDropDownList.currentDropDownList();

        JavascriptGenerator js = new JavascriptGenerator();
        js.append("var event=window.event;");
        js.append(list.hideMenu(true));

        return js.toString(true);
    }


    // ----------------------------------------------------------
    public String hideMenuAndUpdateSelectionScript()
    {
        WCDropDownList list = WCDropDownList.currentDropDownList();

        JavascriptGenerator js = new JavascriptGenerator();
        js.append("var event=window.event;");
        js.append(list.hideMenu(true));

        if (list.title == null)
        {
            String selectionId = list.selectionId();

            js.call("webcat.dropDownList.updateSelection", selectionId,
                    JSHash.code("this"));
        }

        return js.toString(true);
    }
}
