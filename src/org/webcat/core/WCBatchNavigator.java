/*==========================================================================*\
 |  $Id: WCBatchNavigator.java,v 1.3 2010/10/05 00:40:37 stedwar2 Exp $
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

package org.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import er.extensions.eof.ERXConstant;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.Session;
import org.webcat.core.User;
import org.webcat.core.WCBatchNavigator;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 *  A custom version of a batch navigator that has a different look than
 *  the one in WOExtensions.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.3 $, $Date: 2010/10/05 00:40:37 $
 */
public class WCBatchNavigator
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WOBatchNavigationBar object.
     *
     * @param aContext the context
     */
    public WCBatchNavigator(WOContext aContext)
    {
        super(aContext);
    }


    //~ KVC Properties ........................................................

    public AuthenticationDomain authDomain;
    public boolean open;


    //~ Methods ...............................................................


    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (newBatchSize != null)
        {
            setNewNumberOfObjectsPerBatch();
        }
        else if (hasBinding( "persistentKey") && hasSession())
        {
            String key = (String)valueForBinding("persistentKey");
            Object o = ((Session)session()).user().preferences()
                .valueForKey(key);
            if (o != null && o instanceof Integer)
            {
                log.debug("appendToResponse(): key " + key + " = " + o);
                setNumberOfObjectsPerBatchIfNecessary(
                    ((Integer)o).intValue());
            }
        }
        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Set the page number (batch number) to display.
     *
     * @param index The page number
     */
    public void setBatchIndex(Integer index)
    {
        log.debug("setBatchIndex(" + index + ")");
        int batchIndex;

        //Treat a null index as a 0 index. Negative numbers are handled
        //by the display group.
        batchIndex = (index != null)
            ? index.intValue()
            : 0;
        group().setCurrentBatchIndex(batchIndex);
    }


    // ----------------------------------------------------------
    private void setNumberOfObjectsPerBatchIfNecessary(int number)
    {
        WODisplayGroup group = group();
        int curSize = group.numberOfObjectsPerBatch();
        log.debug("setNumberOfObjectsPerBatchIfNecessary(" + number
            + "), was " + curSize);
        if (curSize != number)
        {
            // index is the one-based index of the first object shown in
            // the current batch
            int index = (group.currentBatchIndex() - 1) * curSize + 1;
            if (number <= 0)
            {
                group.setNumberOfObjectsPerBatch(number);
            }
            else
            {
                // newPage is the one-based batch number that will show
                // the object at the given index
                int newBatch = index / number + 1;
                group.setNumberOfObjectsPerBatch(number);
                group.setCurrentBatchIndex(newBatch);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Set the number of objects shown on each page/batch.
     *
     * @param number The number of objects to show
     */
    public void setNumberOfObjectsPerBatch(Integer number)
    {
        newBatchSize = number;
    }


    // ----------------------------------------------------------
    /**
     * Set the number of objects shown on each page/batch.
     *
     * @param number The number of objects to show
     */
    private void setNewNumberOfObjectsPerBatch()
    {
        log.debug("setNewNumberOfObjectsPerBatch() = " + newBatchSize);
        int num;

        //If a negative number is provided we default the number
        //of objects per batch to 0.
        num = (newBatchSize != null  &&  newBatchSize.intValue() > 0)
            ? newBatchSize.intValue()
            : 0;
        setNumberOfObjectsPerBatchIfNecessary(num);
        if (hasBinding("persistentKey") && hasSession())
        {
            String key = (String)valueForBinding("persistentKey");
            User user = ((Session)session()).user();
            log.debug("setNewNumberOfObjectsPerBatch(): key " + key + " <- "
                + num + "(" + newBatchSize + ")");
            user.preferences().takeValueForKey(
                (newBatchSize == null)
                    ? ERXConstant.integerForInt(num)
                    : newBatchSize,
                key);
            user.savePreferences();
        }
    }


    // ----------------------------------------------------------
    /**
     * Access the bound display group's current batch position.
     *
     * @return The current batch index
     */
    public int batchIndex()
    {
        int result = group().currentBatchIndex();
        return result;
    }


    // ----------------------------------------------------------
    /**
     * The current number of objects displayed for each page/batch.
     *
     * @return The number of objects per batch
     */
    public int numberOfObjectsPerBatch()
    {
        int result = newBatchSize == null
            ? group().numberOfObjectsPerBatch()
            : newBatchSize.intValue();
        return result;
    }


    // ----------------------------------------------------------
    public WOComponent filter()
    {
        open = true;
        WODisplayGroup dg = group();
        if (dg != null)
        {
            if (hasUserFilter())
            {
                // Save an unused tag in the operator dictionary so we can
                // tell that this display group is being actively filtered
                // and what kind of entity it contains
                dg.queryOperator().takeValueForKey(
                    User.ENTITY_NAME, "entityType");
                dg.qualifyDataSource();
            }
        }
        return go();
    }


    // ----------------------------------------------------------
    /**
     * Action for the "go" button in the batch navigator, which simply
     * returns true to reload the current page.  It also takes care of
     * storing the batch navigation settings if an appropriate key is
     * defined.
     * @return null, to reload the current page
     */
    public WOComponent go()
    {
        if (log.isDebugEnabled())
        {
            log.debug("go(): batch = " + batchIndex() + ", size = "
                + numberOfObjectsPerBatch());
        }
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Action for the "fewer" button in the batch navigator, which simply
     * returns true to reload the current page.  It also takes care of
     * setting the batch size to less than the total number of objects
     * displayed, to force paging.
     * @return null, to reload the current page
     */
    public WOComponent fewer()
    {
        int num = group().allObjects().count() / 2;
        if (num == 0)
        {
            num++;
        }
        setNumberOfObjectsPerBatch(ERXConstant.integerForInt(num));
        if (log.isDebugEnabled())
        {
            log.debug("fewer(): now, batch = " + batchIndex() + ", size = "
                + numberOfObjectsPerBatch());
        }
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Determine if the batch navigator should be able to show a button
     * for reducing the batch size.
     * @return true if the associated display group can show more than
     * 1 object
     */
    public boolean canShowFewer()
    {
        WODisplayGroup dg = group();
        boolean result = dg.allObjects().count() > 1;
        log.debug("canShowFewer(): " + result);
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Check to see if a filter for User objects should be displayed.
     *
     * @return True if this batch control operates on a group of User objects
     */
    public boolean hasUserFilter()
    {
        WODisplayGroup dg = group();
        boolean result = dg != null
            // If this display group is known to contain users (but might
            // be filtered so none are showing right now!)
            && (User.ENTITY_NAME.equals(
                dg.queryOperator().valueForKey("entityType"))

            // Or if this display group's first object is a user
                 || (dg.allObjects().count() > 0
                     && dg.allObjects().objectAtIndex(0) instanceof User));
        log.debug("hasUserFilter() = " + result);
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Reset the display group's queryMatch binding.
     * @return null, to reload the current page
     */
    public WOComponent clearFilter()
    {
        WODisplayGroup dg = group();
        if (dg != null)
        {
            dg.queryMatch().removeAllObjects();
            dg.queryOperator().removeAllObjects();
            dg.qualifyDataSource();
        }
        selectedAuthDomain = null;
        open = false;
        return null;
    }


    // ----------------------------------------------------------
    public NSArray<AuthenticationDomain> authDomains()
    {
        return AuthenticationDomain.authDomains();
    }


    // ----------------------------------------------------------
    public boolean multipleInstitutions()
    {
        return true; // AuthenticationDomain.authDomains().count() > 1;
    }


    // ----------------------------------------------------------
    public AuthenticationDomain selectedAuthDomain()
    {
        if (selectedAuthDomain == null)
        {
            WODisplayGroup dg = group();
            if (dg != null)
            {
                String prop = (String)dg.queryMatch().valueForKey(
                    "authenticationDomain.propertyName");
                if (prop != null)
                {
                    NSArray<AuthenticationDomain> domains =
                        AuthenticationDomain.authDomains();
                    for (AuthenticationDomain ad : domains)
                    {
                        if (prop.equals(ad.propertyName()))
                        {
                            selectedAuthDomain = ad;
                            break;
                        }
                    }
                }
            }
        }
        return selectedAuthDomain;
    }


    // ----------------------------------------------------------
    public void setSelectedAuthDomain(AuthenticationDomain value)
    {
        selectedAuthDomain = value;
        if (value != null)
        {
            WODisplayGroup dg = group();
            if (dg != null)
            {
                dg.queryMatch().takeValueForKey(value.propertyName(),
                    "authenticationDomain.propertyName");
            }
        }
    }


    // ----------------------------------------------------------
    private WODisplayGroup group()
    {
        return (WODisplayGroup)valueForBinding("displayGroup");
    }


    //~ Instance/static variables .............................................

    private AuthenticationDomain selectedAuthDomain;
    private Integer newBatchSize;
    static Logger log = Logger.getLogger( WCBatchNavigator.class );
}
