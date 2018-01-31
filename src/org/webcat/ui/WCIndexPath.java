/*==========================================================================*\
 |  $Id: WCIndexPath.java,v 1.1 2011/05/13 19:43:46 aallowat Exp $
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

package org.webcat.ui;

import java.util.Arrays;

//-------------------------------------------------------------------------
/**
 * An index path is a sequence of numerical indices that represent the
 * positions of children in a tree.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:43:46 $
 */
public class WCIndexPath implements Comparable<WCIndexPath>
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCIndexPath(int... indices)
    {
        this.indices = indices;
    }


    // ----------------------------------------------------------
    public WCIndexPath(String indices)
    {
        String[] components = indices.split("\\.");
        int[] intComponents = new int[components.length];
        for (int i = 0; i < components.length; i++)
        {
            intComponents[i] = Integer.parseInt(components[i]);
        }

        this.indices = intComponents;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int[] indices()
    {
        return indices.clone();
    }


    // ----------------------------------------------------------
    public int indexAtPosition(int position)
    {
        return indices[position];
    }


    // ----------------------------------------------------------
    public int length()
    {
        return indices.length;
    }


    // ----------------------------------------------------------
    public WCIndexPath indexPathByRemovingLastIndex()
    {
        if (length() == 0)
        {
            return this;
        }
        else
        {
            int[] prefix = new int[length() - 1];
            for (int i = 0; i < prefix.length; i++)
            {
                prefix[i] = indices[i];
            }

            return new WCIndexPath(prefix);
        }
    }


    // ----------------------------------------------------------
    public WCIndexPath indexPathByAddingIndex(int index)
    {
        int[] prefix = new int[length() + 1];
        for (int i = 0; i < length(); i++)
        {
            prefix[i] = indices[i];
        }
        prefix[length()] = index;

        return new WCIndexPath(prefix);
    }


    // ----------------------------------------------------------
    @Override
    public int hashCode()
    {
        return Arrays.hashCode(indices);
    }


    // ----------------------------------------------------------
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof WCIndexPath)
        {
            WCIndexPath otherPath = (WCIndexPath) other;
            return Arrays.equals(indices, otherPath.indices);
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    public int compareTo(WCIndexPath otherPath)
    {
        for (int i = 0; i < Math.min(length(), otherPath.length()); i++)
        {
            if (indices[i] != otherPath.indices[i])
            {
                return Integer.valueOf(indices[i]).compareTo(
                        otherPath.indices[i]);
            }
        }

        // If we reached this point, we have exhausted one of the index paths,
        // so it is a prefix of the other (or the same). Compare the lengths to
        // determine ordering.

        return Integer.valueOf(length()).compareTo(otherPath.length());
    }


    // ----------------------------------------------------------
    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        if (indices.length > 0)
        {
            buffer.append(indices[0]);

            for (int i = 1; i < indices.length; i++)
            {
                buffer.append('.');
                buffer.append(indices[i]);
            }
        }

        return buffer.toString();
    }


    //~ Static/instance variables .............................................

    private int[] indices;
}
