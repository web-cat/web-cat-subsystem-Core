/*==========================================================================*\
 |  $Id: PathMatcher.java,v 1.1 2011/03/23 15:06:48 aallowat Exp $
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

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

//--------------------------------------------------------------------------
/**
 * <p>
 * This class is used to determine if a path matches a pattern that uses
 * Ant-style wildcards ("*", "**", and "?").
 * </p><p>
 * This class was partially adapted from the one provided by the Spring
 * framework, with some behavior modifications that make it more appropriate
 * for the submitter engine. The following wildcards are supported:
 * <ul>
 * <li>? matches one character</li>
 * <li>* matches zero or more characters</li>
 * <li>** matches zero or more directories in a path</li>
 * </ul>
 * </p><p>
 * Some examples:
 * <ul>
 * <li><code>src/*.java</code> - matches all <code>.java</code> files in the
 * <code>src</code> directory, but not in any subdirectories</li>
 * <li><code>src/&#42;&#42;/*.java</code> - matches all <code>.java</code> in
 * the <code>src</code> directory or any subdirectory of <code>src</code>
 * </ul>
 * </p><p>
 * However, relative paths (those not starting with "/", "&#42;/", or
 * "&#42;&#42;/" are treated as a special case, for simplicity for the
 * end-user. For example, "foo.java" is interpreted as
 * "&#42;&#42;/foo.java", to match the file "foo.java" in any directory in the
 * tree and not just the top-level directory. "/foo.java" would only match
 * "foo.java" in the top-level directory.
 * </p>
 *
 * @author  Tony Allevato (Virginia Tech Computer Science)
 * @author  latest changes by: $Author: aallowat $
 * @version $Revision: 1.1 $ $Date: 2011/03/23 15:06:48 $
 */
public class PathMatcher
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new PathMatcher with the specified pattern.
     *
     * @param pattern the Ant-style pattern associated with the matcher
     */
    public PathMatcher(String pattern)
    {
        if (!(pattern.startsWith("/") || pattern.startsWith("*/")
                || pattern.startsWith("**/")))
        {
            this.pattern = "**/" + pattern;
        }
        else
        {
            this.pattern = pattern;
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Splits a path string into its path components, as separated by the
     * path separator character ("/").
     *
     * @param string the path
     * @return an array of path components
     */
    private String[] getPathComponents(String string)
    {
        if (string == null)
        {
            return null;
        }

        StringTokenizer tokenizer =
            new StringTokenizer(string, PATH_SEPARATOR);

        ArrayList<String> tokens = new ArrayList<String>();

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken().trim();
            if (token.length() > 0)
            {
                tokens.add(token);
            }
        }

        return tokens.toArray(new String[tokens.size()]);
    }


    // ----------------------------------------------------------
    /**
     * Match the specified path against this matcher's pattern.
     *
     * @param path the path string to test
     * @return true if the path matched, otherwise false
     */
    public boolean matches(String path)
    {
        if (path.startsWith(PATH_SEPARATOR) != pattern.startsWith(PATH_SEPARATOR)
                && !pattern.startsWith("**/"))
        {
            return false;
        }

        String[] patternComponents = getPathComponents(pattern);
        String[] pathComponents = getPathComponents(path);

        int patternIdxStart = 0;
        int patternIdxEnd = patternComponents.length - 1;
        int pathIdxStart = 0;
        int pathIdxEnd = pathComponents.length - 1;

        // Match all elements up to the first "**".

        while (patternIdxStart <= patternIdxEnd
                && pathIdxStart <= pathIdxEnd)
        {
            String patternDir = patternComponents[patternIdxStart];

            if ("**".equals(patternDir))
            {
                break;
            }

            if (!matchStrings(patternDir, pathComponents[pathIdxStart]))
            {
                return false;
            }

            patternIdxStart++;
            pathIdxStart++;
        }

        if (pathIdxStart > pathIdxEnd)
        {
            // The path is exhausted, only match if the rest of the pattern is
            // a single "*" or multiple "**"s.

            if (patternIdxStart > patternIdxEnd)
            {
                return (pattern.endsWith(PATH_SEPARATOR) ?
                        path.endsWith(PATH_SEPARATOR) :
                            !path.endsWith(PATH_SEPARATOR));
            }

            if (patternIdxStart == patternIdxEnd
                    && patternComponents[patternIdxStart].equals("*")
                    && path.endsWith(PATH_SEPARATOR))
            {
                return true;
            }

            for (int i = patternIdxStart; i <= patternIdxEnd; i++)
            {
                if (!patternComponents[i].equals("**"))
                {
                    return false;
                }
            }

            return true;
        }
        else if (patternIdxStart > patternIdxEnd)
        {
            // The path is not exhausted, but the pattern is. Failure.

            return false;
        }

        // up to last '**'
        while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd)
        {
            String patDir = patternComponents[patternIdxEnd];

            if (patDir.equals("**"))
            {
                break;
            }

            if (!matchStrings(patDir, pathComponents[pathIdxEnd]))
            {
                return false;
            }

            patternIdxEnd--;
            pathIdxEnd--;
        }

        if (pathIdxStart > pathIdxEnd)
        {
            // Path is exhausted.

            for (int i = patternIdxStart; i <= patternIdxEnd; i++)
            {
                if (!patternComponents[i].equals("**"))
                {
                    return false;
                }
            }

            return true;
        }

        while (patternIdxStart != patternIdxEnd && pathIdxStart <= pathIdxEnd)
        {
            int patIdxTmp = -1;

            for (int i = patternIdxStart + 1; i <= patternIdxEnd; i++)
            {
                if (patternComponents[i].equals("**"))
                {
                    patIdxTmp = i;
                    break;
                }
            }

            if (patIdxTmp == patternIdxStart + 1)
            {
                // '**/**' situation, so skip one
                patternIdxStart++;
                continue;
            }

            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patternIdxStart - 1);
            int strLength = (pathIdxEnd - pathIdxStart + 1);
            int foundIdx = -1;

            strLoop:
                for (int i = 0; i <= strLength - patLength; i++)
                {
                    for (int j = 0; j < patLength; j++)
                    {
                        String subPat = patternComponents[patternIdxStart + j + 1];
                        String subStr = pathComponents[pathIdxStart + i + j];
                        if (!matchStrings(subPat, subStr))
                        {
                            continue strLoop;
                        }
                    }

                    foundIdx = pathIdxStart + i;
                    break;
                }

            if (foundIdx == -1)
            {
                return false;
            }

            patternIdxStart = patIdxTmp;
            pathIdxStart = foundIdx + patLength;
        }

        for (int i = patternIdxStart; i <= patternIdxEnd; i++)
        {
            if (!patternComponents[i].equals("**"))
            {
                return false;
            }
        }

        return true;
    }


    // ----------------------------------------------------------
    /**
     * Performs the DOS-style pattern matching for each of the individual
     * path components. Essentially the {@link #matches(String)} method
     * handles the "**" portions of the pattern, and this method handles the
     * "*" and "?" wildcards.
     *
     * @param subpattern subpattern to match against
     * @param str string which must be matched against the pattern
     * @return true if the string matches against the pattern, otherwise false
     */
    private boolean matchStrings(String subpattern, String str)
    {
        StringBuffer buffer = new StringBuffer();
        int length = subpattern.length();

        for (int i = 0; i < length; i++)
        {
            char c = subpattern.charAt(i);

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

        return Pattern.matches(buffer.toString(), str);
    }


    //~ Static/instance variables .............................................

    /* The path separator used by the matcher. */
    private static final String PATH_SEPARATOR = "/";

    /* The pattern used by the matcher. */
    private String pattern;
}
