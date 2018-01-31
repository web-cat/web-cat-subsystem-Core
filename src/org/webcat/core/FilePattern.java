/*==========================================================================*\
 |  $Id: FilePattern.java,v 1.1 2010/10/18 15:32:19 aallowat Exp $
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

package org.webcat.core;

import java.io.File;
import java.util.regex.Pattern;

//--------------------------------------------------------------------------
/**
 * This class internally converts a DOS-style wildcard pattern into a regular
 * expression that can be used to match filenames.
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 * @author  latest changes by: $Author: aallowat $
 * @version $Revision: 1.1 $ $Date: 2010/10/18 15:32:19 $
 */
public class FilePattern
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new file pattern matcher.
     *
     * @param pattern the DOS-style wildcard pattern to match
     */
    public FilePattern(String pattern)
    {
        int length = pattern.length();

        StringBuffer buffer = new StringBuffer(length);

        for (int i = 0; i < length; i++)
        {
            char c = pattern.charAt(i);

            if (!Character.isLetterOrDigit(c))
            {
                switch (c)
                {
                    case '?': // Fall through
                    case '*':
                        buffer.append('.');
                        break;

                    default:
                        buffer.append('\\');
                        break;
                }
            }

            buffer.append(c);
        }

        this.pattern = Pattern.compile(buffer.toString());
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Determines if the specified path matches the pattern.
     *
     * @param path the path to match
     * @return true if the path matches the pattern; otherwise, false
     */
    public boolean matches(String path)
    {
        return pattern.matcher(path).matches();
    }


    // ----------------------------------------------------------
    /**
     * Determines if the specified File object matches the pattern.
     *
     * @param file the File to match.
     * @return true if the File matches the pattern; otherwise, false
     */
    public boolean matches(File file)
    {
        return matches(file.getAbsolutePath());
    }


    // ----------------------------------------------------------
    /**
     * Gets a human-readable description of the object.
     *
     * @return a human-readable description of the object
     */
    @Override
    public String toString()
    {
        return "<FilePattern: " + pattern + ">";
    }


    //~ Static/instance variables .............................................

    /* The regular expression that represents this file pattern. */
    private Pattern pattern;
}
