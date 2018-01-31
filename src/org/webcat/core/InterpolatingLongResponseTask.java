/*==========================================================================*\
 |  $Id: InterpolatingLongResponseTask.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import org.webcat.core.LongResponseTaskWithProgress;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * A version of the {@link LongResponseTaskWithProgress} that supports
 * discrete "steps" in the action to be performed, automatically updates
 * progress bar information, and uses elapsed time for each step to
 * "interpolate" progress bar information between long-running steps.
 *
 * @author Stephen Edwards
 * @version $Id: InterpolatingLongResponseTask.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public abstract class InterpolatingLongResponseTask
    extends LongResponseTaskWithProgress
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new task.  Subclasses that use this constructor should
     * override {@link #setUpTask()} to provide the information
     * necessary to interpolate progress information.
     */
    public InterpolatingLongResponseTask()
    {
        // data members are initialized in their declarations below
    }


    // ----------------------------------------------------------
    /**
     * Create a new task.  If the parameters used here are not available
     * at object creation time, use the default constructor and override
     * {@link #setUpTask()} instead.
     * @param numberOfSteps the (unweighted) number of steps in this task
     * @param stepWeights the relative weights of each step, or null (which
     * causes all steps to be weighted equally)
     */
    public InterpolatingLongResponseTask( int numberOfSteps, int[] stepWeights )
    {
        setUnweightedNumberOfSteps( numberOfSteps );
        setStepWeights( stepWeights );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * For progress bar displays, find out how many "units" there are
     * for this entire task's total amount of work.  This actually returns
     * the <em>weighted</em> total, for better progress bar resolution.
     * @return The total number of steps to use for this task's progress bar
     */
    public synchronized int totalNumberOfSteps()
    {
        ensureCumulativeWeights();
        return cumulativeWeights[numberOfSteps];
    }


    // ----------------------------------------------------------
    /**
     * Set the total number of (unweighted) steps for this task.  This method
     * is usually called by subclass constructors.  Use the
     * {@link #setStepWeights(int[])} method to define the relative weights
     * for the steps.
     * @param steps The total number of steps to use for this task's progress
     * bar
     */
    public synchronized void setUnweightedNumberOfSteps( int steps )
    {
        numberOfSteps = steps;
        cumulativeWeights = null;
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
    public synchronized int stepsCompletedSoFar()
    {
        ensureCumulativeWeights();
        int result = cumulativeWeights[currentStep];
        if ( currentStep < numberOfSteps )
        {
            int myStepWeight = weightForStep( currentStep );
            if ( myStepWeight > 1 )
            {
                long timeNeededUntilNow = 0;
                if( timeCurrent != null && timeStarted != null )
                {
                    timeNeededUntilNow = timeCurrent.getTime() -
                        timeStarted.getTime();
                }
                long timePerWeightedUnit = 0;
                if ( currentStep > 0 )
                {
                    int denom =  cumulativeWeights[currentStep];
                    if ( denom == 0 ) { denom = 1; }
                    timePerWeightedUnit = timeNeededUntilNow /
                        cumulativeWeights[currentStep];
                }
                if ( timePerWeightedUnit < 1 )
                {
                    timePerWeightedUnit = 1000; // default is one second
                }
                long additionalTime = 0;
                if ( timeCurrent != null )
                {
                    additionalTime = System.currentTimeMillis() -
                        timeCurrent.getTime();
                }
                int additionalUnits =
                    (int)( additionalTime / timePerWeightedUnit );
                if ( additionalUnits < 0 )
                {
                    // should never happen, of course
                    additionalUnits = 0;
                }
                else if ( additionalUnits > myStepWeight )
                {
                    additionalUnits = myStepWeight - 1;
                }
                result += additionalUnits;
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Set the relative weights for the steps in this task.  By default,
     * all steps are considered to have a weight of "1".  Setting step weights
     * will change their relative proportions when generating the corresponding
     * progress bar.  This method is normally called from a subclass
     * constructor.
     * @param weights The weights, in an array indexed from 0 to
     * totalNumberOfSteps() - 1
     */
    public synchronized void setStepWeights( int[] weights )
    {
        stepWeights = weights;
        cumulativeWeights = null;
    }


    // ----------------------------------------------------------
    /**
     * Perform all the steps, updating the step counter as needed.  This
     * method repeatedly called {@link #nextStep(int,Object)} to perform
     * each step, updating internal bookkeeping information along the way.
     * @return the final result of the task
     */
    public Object performAction()
    {
        Object result = setUpTask();
        timeStarted = new NSTimestamp(); // remember when everything began...
        while ( currentStep < numberOfSteps && !isCancelled() )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "performAction(): calling nextStep("
                    + currentStep + " (of " + numberOfSteps + "), "
                    + result + ")");
            }
            result = nextStep( currentStep, result );
            timeCurrent = new NSTimestamp();
            currentStep++;
        }
        result = tearDownTask( result );
        return result;
    }


    //~ Protected methods .....................................................

    // ----------------------------------------------------------
    /**
     * Perform any time-consuming initialization for concrete subclasses.
     * Any initialization that takes longer than a second or two should go
     * here instead of in the subclass constructor.  This operation is
     * called as part of {@link #performAction()}, before any of the
     * lower-level calls to {@link #nextStep(int,Object)} begin.  It provides
     * an opportunity to adjust the number of steps or the relative weights
     * of steps before execution of the steps begin.  This base implementation
     * does nothing.  Subclasses that wish to use this feature can override
     * this method to do something appropriate.
     * @return The return value will be used to initialize the "result" for
     * the task, and will be passed into the first call to
     * {@link #nextStep(int,Object)}
     */
    protected Object setUpTask()
    {
        // Nothing to do as the default; subclasses can override as necessary
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Perform the next step of the task.  This abstract method should
     * be overridden in a concrete subclass to do "one unit" worth of
     * work for the specified step.
     * @param stepNumber The number of the step to execute
     * @param resultSoFar The working result returned by the previous call
     * to this method
     * @return The new working result; the return value for the final step
     * will be used as the return value for the entire action sequence
     */
    protected abstract Object nextStep( int stepNumber, Object resultSoFar );


    // ----------------------------------------------------------
    /**
     * Perform any post-processing or clean up actions for concrete subclasses.
     * This operation is called as part of {@link #performAction()}, after all
     * of the lower-level calls to {@link #nextStep(int,Object)} have been
     * made.  It provides an opportunity for finalization actions that are
     * not part of <code>nextStep()</code> to be executed as part of the
     * task.  This base implementation does nothing.  Subclasses that wish
     * to use this feature can override this method to do something
     * appropriate.
     * @param resultSoFar The working result returned by the last call
     * to nextStep()
     * @return The final result
     */
    protected Object tearDownTask( Object resultSoFar )
    {
        // Nothing to do as the default; subclasses can override as necessary
        return resultSoFar;
    }


    // ----------------------------------------------------------
    /**
     * Get the relative weight for a specific step.
     * @param stepNumber
     * @return The weight of the given step (default is 1)
     */
    public synchronized int weightForStep( int stepNumber )
    {
        return ( stepWeights == null || stepNumber > stepWeights.length )
            ? 1
            : stepWeights[stepNumber];
    }


    // ----------------------------------------------------------
    /**
     * Allocate and calculate the array of cumulative step weights, if
     * necessary.
     */
    protected void ensureCumulativeWeights()
    {
        if ( cumulativeWeights == null )
        {
            int cumulativeWeight = 0;
            cumulativeWeights = new int[numberOfSteps + 1];
            for ( int i = 0; i < numberOfSteps; i++ )
            {
                cumulativeWeights[i] = cumulativeWeight;
                cumulativeWeight += weightForStep( i );
            }
            cumulativeWeights[numberOfSteps] = cumulativeWeight;
        }
    }


    //~ Instance/static variables .............................................

    protected int numberOfSteps = 100;
    protected int currentStep = 0;
    protected int[] stepWeights = null;
    protected int[] cumulativeWeights = null;
    protected NSTimestamp timeStarted = null;
    protected NSTimestamp timeCurrent = null;
}
