/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.core.vfs;

//-------------------------------------------------------------------------
/**
 * A convenience interface for when both read and write access are required at
 * the same time for a file. File-like objects that are both readable and
 * writable should implement this interface <b>instead of</b> implementing
 * {@link IReadableFile} and {@link IWritableFile} separately, so that client
 * code can successfully cast to {@link IReadWritableFile}.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public interface IReadWritableFile extends IReadableFile, IWritableFile
{
    // This interface does not declare any methods of its own.
}
