/*==========================================================================*\
 |  $Id: LongResponseTaskWithProgress.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import er.extensions.concurrency.ERXLongResponseTask;

//-------------------------------------------------------------------------
/**
 * A version of the {@link ERXLongResponseTask} that includes hooks for
 * displaying a progress bar.
 *
 * @author Stephen Edwards
 * @version $Id: LongResponseTaskWithProgress.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public abstract class LongResponseTaskWithProgress
    extends ERXLongResponseTask.DefaultImplementation
{
    // ----------------------------------------------------------
    /**
     * For progress bar displays, find out how many "units" there are
     * for this entire task's total amount of work.  This method should
     * return zero if no progress measure is available for the task.
     * @return The total number of steps to use for this task's progress bar
     */
    public int totalNumberOfSteps()
    {
        return 0;
    }


    // ----------------------------------------------------------
    /**
     * This mutator does nothing--it is just to make KVC access happy.
     * Concrete subclasses can implement it if desired.
     * @param newValue
     */
    public void setTotalNumberOfSteps( int newValue )
    {
        // Nothing to do here
    }


    // ----------------------------------------------------------
    /**
     * For progress bar displays, find out how many "units" have been
     * completed so far.  This default implementation just returns zero
     * (which is appropriate for tasks that have no progress measure).
     * Tasks that override {@link #totalNumberOfSteps()} should also
     * override this method.
     * @return The number of steps that have been completed for this task's
     * progress bar
     */
    public int stepsCompletedSoFar()
    {
        return 0;
    }


    // ----------------------------------------------------------
    /**
     * This mutator does nothing--it is just to make KVC access happy.
     * Concrete subclasses can implement it if desired.
     * @param newValue
     */
    public void setStepsCompletedSoFar( int newValue )
    {
        // Nothing to do here
    }


    // ----------------------------------------------------------
    public boolean isCancelled()
    {
        return super.isCancelled();
    }


    // ----------------------------------------------------------
    public Object result()
    {
        return super.result();
    }


    // ----------------------------------------------------------
    /**
     * Perform any post-processing or clean up actions associated with
     * resources created as part of producing {@link #performAction()}'s
     * result.  This placeholder does nothing, but subclasses can override
     * it as necessary.  The intent is that, if the result value produced
     * by this long-running task involves any EOs, then the task will have
     * its own editing context (and possibly other resources).  This method
     * should <b>always</b> be called by clients that create tasks, once the
     * result value is no longer needed, so that any internal editing
     * context(s) can be released.  Subclasses should implement this method
     * so that it releases any internally held resources.  Clients may call
     * this method multiple times, so subclasses should handle this.
     */
    public void resultNoLongerNeeded()
    {
        // Nothing to do as the default; subclasses can override as necessary
    }
}
