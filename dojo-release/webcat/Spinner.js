/*==========================================================================*\
 |  $Id: Spinner.js,v 1.3 2010/04/19 15:21:59 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

dojo.provide("webcat.Spinner");

dojo.require("dojo.cache");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

//=========================================================================
/**
 * The webcat.Spinner widget displays a progress spinner to indicate that a
 * long-running operation is occurring in the background. The spinner is
 * implemented not as an animated GIF, but using a timer to cycle through the
 * frames of a PNG CSS-sprite image, so that the spinner will look correct on
 * any background.
 */
dojo.declare("webcat.Spinner",
        [ dijit._Widget, dijit._Templated ],
{
    //~ Properties ............................................................

    /**
     * The size of the spinner. Three options are currently available:
     * "small" (18px), "medium" (36px), and "large" (72px).
     */
    size: "small",

    /**
     * Indicates whether the spinner is currently spinning. This can also be
     * set when the spinner is created to start it spinning initially.
     */
    isSpinning: false,

    templateString: dojo.cache("webcat", "templates/Spinner.html"),


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    postMixInProperties: function()
    {
        if (this.size == "medium")
        {
            this._sizeClass = "webcatSpinnerMedium";
            this._pixelSize = 36;
        }
        else if (this.size == "large")
        {
            this._sizeClass = "webcatSpinnerLarge";
            this._pixelSize = 72;
        }
        else
        {
            this._sizeClass = "webcatSpinnerSmall";
            this._pixelSize = 18;
        }
    },


    // ----------------------------------------------------------
    startup: function()
    {
        this.inherited(arguments);

        if (this.isSpinning)
        {
            this.start();
        }
    },


    // ----------------------------------------------------------
    /**
     * Ensures that the widget is removed from the animation cache when it is
     * destroyed (this can happen if stop() is never called).
     */
    uninitialize: function()
    {
        webcat._removeSpinner(this);
    },


    // ----------------------------------------------------------
    /**
     * Makes the spinner visible and starts its animation.
     */
    start: function()
    {
        this.isSpinning = true;
        webcat._addSpinner(this);
        dojo.removeClass(this.domNode, "webcatSpinnerHidden");
    },


    // ----------------------------------------------------------
    /**
     * Hides the spinner and stops its animation.
     */
    stop: function()
    {
        this.isSpinning = false;
        dojo.addClass(this.domNode, "webcatSpinnerHidden");
        webcat._removeSpinner(this);
    },


    // ----------------------------------------------------------
    /**
     * Gets the pixel size of this spinner.
     */
    getPixelSize: function()
    {
        return this._pixelSize;
    },


    // ----------------------------------------------------------
    /**
     * Sets the current frame of the animation by moving the viewport window
     * on the CSS sprite.
     */
    _setFrameIndex: function(index)
    {
        dojo.style(this.domNode,
                "background-position-x",
                "-" + (this._pixelSize * index) + "px");
    }
});

/**
 * The code below is used by the Spinner class and is not intended to be called
 * by clients.
 */
(function() {
    var spinnerFrame = 0;
    var spinnerCache = { };
    var spinnerCacheSize = 0;
    var spinnerInterval = null;

    dojo.mixin(webcat, {

    // ----------------------------------------------------------
    /**
     * This function is essentially the "thread" that updates all of the
     * spinner animation frames that are currently visible. By using a single
     * callback for every spinner, performance is (presumably) better and all
     * of the spinners stay visually synchronized.
     */
    _spinnerCallback: function()
    {
        spinnerFrame = (spinnerFrame + 1) % 12;

        for (var id in spinnerCache)
        {
            if (spinnerCache.hasOwnProperty(id))
            {
                var widget = spinnerCache[id];

                // Sanity check if something bad happened and the widget was
                // destroyed without being removed from the cache.
                if (widget == null || widget.domNode == null)
                {
                    delete spinnerCache[id];
                }
                else
                {
                    widget._setFrameIndex(spinnerFrame);
                }
            }
        }
    },


    // ----------------------------------------------------------
    /**
     * Adds a spinner to the set that will be animated. Called by Spinner.show
     * above.
     */
    _addSpinner: function(spinner)
    {
        if (!spinnerCache[spinner.id])
        {
            spinnerCache[spinner.id] = spinner;
            spinnerCacheSize++;

            if (!spinnerInterval)
            {
                spinnerInterval = setInterval(webcat._spinnerCallback, 100);
            }
        }
    },


    // ----------------------------------------------------------
    /**
     * Removes a spinner from the update set. Called by Spinner.hide above.
     */
    _removeSpinner: function(spinner)
    {
        if (spinnerCache[spinner.id])
        {
            spinnerCacheSize--;
            delete spinnerCache[spinner.id];

            if (spinnerCacheSize == 0)
            {
                clearInterval(spinnerInterval);
                spinnerInterval = null;
            }
        }
    }

    });
})();
