/*==========================================================================*\
 |  Copyright (C) 2011-2021 Virginia Tech
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

package org.webcat.woextensions;

import er.extensions.eof.ERXEC;

//-------------------------------------------------------------------------
/**
 * <p>Represents an action with an associated editing context, that has
 * built-in support for locking, unlocking, and disposing of the editing
 * context.
 * </p><p>
 * Basic usage is:</p>
 * <pre>
 * import org.webcat.woextensions.ECAction;
 * import static org.webcat.woextensions.ECAction.run;
 *
 * ...
 *
 * run(new ECAction() { public void action() {
 *
 *     // place your actions here
 *     // use "ec" to refer to the action's editing context
 *
 * }});
 * </pre>
 * <p>
 * When used like this, a new editing context is created and locked just
 * for this action, and then unlocked and disposed when the action
 * completes.  If you have an existing editing context that you wish
 * to reuse for multiple actions, you can pass it into the constructor,
 * along with a boolean that indicates whether or not to dispose of the
 * context when the action completes:
 * </p>
 * <pre>
 * run(new ECAction(myEC, false) { public void action() {
 *
 *     // place your actions here
 *     // use "ec" to refer to the action's editing context, which equals myEC
 *
 * }});
 * </pre>
 * <p>
 * Inside classes that define their own run() methods, you cannot statically
 * import run(), as in the examples above.  In those cases, you can use
 * this form instead:
 * </p>
 * <pre>
 * new ECAction() { public void action() {
 * ...
 * }}.run();
 * </pre>
 *
 *  @author  Stephen Edwards
 */
public abstract class ECAction
    implements Runnable
{
    // This class should extend ECActionWithResult<Void>, but the
    // differences between Void and void make the action() method
    // clumsy.  Instead, here we repeat the same code (yuck).

    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object with a default EC that will be destroyed when
     * the action completes.
     */
    public ECAction()
    {
        this(null);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new object with a "tolerant" EC that automatically
     * resolves optimistic locking failures during saveChanges().
     * The EC will be destroyed when the action completes.
     * Read more about tolerant EC behavior in the ERXEC documentation.
     * @param retry If true, saveChanges() will auto-retry after
     *              resolving the failure.
     * @param merge If true, saveChanges() will automatically merge
     *              any pending changes with those already written to
     *              the database in order to resolve optimistic locking
     *              failures.
     */
    public ECAction(boolean retry, boolean merge)
    {
        this();
        ((ERXEC)ec).setOptions(true, retry, merge);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new object hooked to a pre-existing EC.  The EC is
     * assumed to be owned externally, so it will <em>not</em>
     * be disposed when the action completes.
     * @param context The existing EC to use in the action.
     */
    public ECAction(WCEC context)
    {
        this(context, false);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new object hooked to a pre-existing EC, with user
     * control over whether that EC is disposed after the action completes.
     * @param context The existing EC to use in the action.
     * @param ownsEC  If true, the given EC will be disposed after the
     *                action completes.  If false, the EC will not be
     *                disposed.
     */
    public ECAction(WCEC context, boolean ownsEC)
    {
        ec = context;
        this.ownsEC = ownsEC;
        if (ec == null)
        {
            ec = WCEC.newEditingContext();
            this.ownsEC = true;
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Subclasses should override this method to define the body of the
     * action to perform.  Inside the overriding definition of this method,
     * clients can use the name "ec" like a local variable to refer to
     * this action's editing context ("ec" is actually an instance field
     * of this object).  The action's EC is automatically locked before this
     * method is invoked, and unlocked after this method exits.
     */
    public abstract void action();


    // ----------------------------------------------------------
    /**
     * An instance method that locks the EC, invokes the action()
     * method, unlocks the EC, and if necessary, disposes the
     * EC.
     */
    public void run()
    {
        try
        {
            ec.lock();
            action();
        }
        finally
        {
            ec.unlock();
            if (ownsEC)
            {
                ec.dispose();
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * A static version of {@link #action()} that can be statically
     * imported and used when you want the run() call to be at the start
     * of a statement, rather than at the very end.  It simply calls
     * the run() method on the given object.
     *
     * @param action The action to perform.
     */
    public static void run(ECAction action)
    {
        action.run();
    }


    //~ Instance/static variables .............................................

    /** This action's editing context, for use in {@link #action()}. */
    protected WCEC ec;
    /** True if this object will dispose of ec at the end of run(). */
    protected boolean ownsEC;
}
