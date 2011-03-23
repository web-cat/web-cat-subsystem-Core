package org.webcat.ui;

import org.webcat.core.WCComponent;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXValueUtilities;

//-------------------------------------------------------------------------
/**
 * A list item in a {@code WCIconList} that supports an icon and can have an
 * id attached for easy refreshing.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCIconListItem extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCIconListItem(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    public String id()
    {
        return (String) valueForBinding("id");
    }


    // ----------------------------------------------------------
    public String framework()
    {
        String framework = (String) valueForBinding("framework");

        if (framework == null)
        {
            framework = parent().frameworkName();
        }

        return framework;
    }


    // ----------------------------------------------------------
    public String filename()
    {
        return (String) valueForBinding("filename");
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
        return ERXComponentUtilities.booleanValueForBinding(
                this, "remote", false);
    }
}
