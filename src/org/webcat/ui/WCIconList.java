package org.webcat.ui;

import com.webobjects.appserver.WOContext;
import org.webcat.core.WCComponent;

//-------------------------------------------------------------------------
/**
 * Represents a list of items with an optional header and items that are
 * {@code WCIconListItem}s.
 *
 * TODO Currently the assumption is that the icons used in the list will be
 * 16px wide. This should be made controllable on a per-list basis.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCIconList extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCIconList(WOContext context)
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
    public String heading()
    {
        return (String) valueForBinding("heading");
    }
}
