/*==========================================================================*\
 |  $Id: FilePickerDelegate.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

import org.webcat.ui.generators.JavascriptGenerator;

//-------------------------------------------------------------------------
/**
 * A delegate that modifies the behavior of the {@link FilePickerDialog} by
 * filtering entries and is called when a file is selected.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public interface FilePickerDelegate
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean fileCanBeSelected(RepositoryEntryRef file, boolean isDirectory);


    // ----------------------------------------------------------
    /**
     * Called when a file is selected.
     *
     * @param file information about the file
     * @return a JavascriptGenerator that modifies the page to reflect the
     *     selection
     */
    public JavascriptGenerator fileWasSelected(RepositoryEntryRef file);
}
