/*==========================================================================*\
 |  $Id: IQueryAssistant.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
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

package org.webcat.core.objectquery;

import com.webobjects.eocontrol.EOQualifier;

//-------------------------------------------------------------------------
/**
 * Component classes that implement a reporter query assistant must implement
 * this interface. A query assistant can use whatever internal representation
 * it wishes to maintain the state of the query being constructed; this
 * interface defines how it transforms that state into an EOQualifier that can
 * be used in a fetch specification.
 *
 * @author aallowat
 * @version $Id: IQueryAssistant.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public interface IQueryAssistant
{
	// ------------------------------------------------------------------------
	/**
	 * Gets a qualifier that represents the current internal state of this
	 * query assistant.
	 *
	 * @return an EOQualifier object
	 */
	EOQualifier qualifierFromState();


	// ------------------------------------------------------------------------
	/**
	 * Converts the specified qualifier to whatever internal state
	 * representation that this query assistant uses to maintain the query
	 * begin constructed.
	 *
	 * @param q the EOQualifier to obtain the state from
	 */
	void takeStateFromQualifier(EOQualifier q);
}
