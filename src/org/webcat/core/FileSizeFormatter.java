/*==========================================================================*\
 |  $Id: FileSizeFormatter.java,v 1.2 2012/05/09 14:24:09 stedwar2 Exp $
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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

//-------------------------------------------------------------------------
/**
 * A formatter that formats numbers intended to represent sizes of files.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/05/09 14:24:09 $
 */
public class FileSizeFormatter extends Format
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo,
            FieldPosition pos)
    {
        if (obj instanceof Number)
        {
            long size = ((Number) obj).longValue();

            if (size < 1024L)
            {
                toAppendTo.append(size);
                toAppendTo.append(" bytes");
            }
            else if (size < 1048576L)
            {
                double sz = size / 1024.0;
                DecimalFormat fmt = new DecimalFormat("0.0");
                fmt.format(sz, toAppendTo,
                        new FieldPosition(DecimalFormat.FRACTION_FIELD));
                toAppendTo.append(" kB");
            }
            else if (size < 1073741824L)
            {
                double sz = size / 1048576.0;
                DecimalFormat fmt = new DecimalFormat("0.0");
                fmt.format(sz, toAppendTo,
                        new FieldPosition(DecimalFormat.FRACTION_FIELD));
                toAppendTo.append(" MB");
            }
            else
            {
                double sz = size / 1073741824.0;
                DecimalFormat fmt = new DecimalFormat("0.0");
                fmt.format(sz, toAppendTo,
                        new FieldPosition(DecimalFormat.FRACTION_FIELD));
                toAppendTo.append(" GB");
            }
        }
        else
        {
            throw new IllegalArgumentException("The object formatted by a "
                    + "FileSizeFormatter must be a numeric type.");
        }

        return toAppendTo;
    }


    // ----------------------------------------------------------
    @Override
    public Object parseObject(String source, ParsePosition pos)
    {
        throw new UnsupportedOperationException("FileSizeFormatter does not "
                + "support parsing.");
    }
}
