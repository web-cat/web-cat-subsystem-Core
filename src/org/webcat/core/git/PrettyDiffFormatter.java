/*==========================================================================*\
 |  $Id: PrettyDiffFormatter.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011-2012 Virginia Tech
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

package org.webcat.core.git;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.commons.io.output.NullOutputStream;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSComparator.ComparisonException;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class PrettyDiffFormatter
    extends DiffFormatter
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public PrettyDiffFormatter()
    {
        super(new NullOutputStream());

        prettyDiffs = new NSMutableDictionary<String, PrettyDiffResult>();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void format(AnyObjectId b) throws IOException
    {
        format(scan(b));
    }


    // ----------------------------------------------------------
    public List<DiffEntry> scan(AnyObjectId b) throws IOException
    {
        RevWalk rw = new RevWalk(reader());
        return scan(rw.parseTree(b));
    }


    // ----------------------------------------------------------
    public List<DiffEntry> scan(RevTree b) throws IOException
    {
        EmptyTreeIterator aParser = new EmptyTreeIterator();
        CanonicalTreeParser bParser = new CanonicalTreeParser();

        bParser.reset(reader(), b);

        return scan(aParser, bParser);
    }


    // ----------------------------------------------------------
    public ObjectReader reader()
    {
        try
        {
            Field field = getClass().getSuperclass().getDeclaredField("reader");
            field.setAccessible(true);
            return (ObjectReader) field.get(this);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public NSArray<String> modifiedPaths()
    {
        if (sortedPaths == null)
        {
            try
            {
                sortedPaths = prettyDiffs.allKeys().sortedArrayUsingComparator(
                        new NSComparator() {
                    @Override
                    public int compare(Object _lhs, Object _rhs)
                    {
                        String lhs = (String) _lhs;
                        String rhs = (String) _rhs;
                        return lhs.compareToIgnoreCase(rhs);
                    }
                });
            }
            catch (ComparisonException e)
            {
                sortedPaths = prettyDiffs.allKeys();
            }
        }

        return sortedPaths;
    }


    // ----------------------------------------------------------
    public PrettyDiffResult prettyDiffForPath(String path)
    {
        return prettyDiffs.objectForKey(path);
    }


    // ----------------------------------------------------------
    public int totalAdditionCount()
    {
        return totalAdditionCount;
    }


    // ----------------------------------------------------------
    public int totalDeletionCount()
    {
        return totalDeletionCount;
    }


    // ----------------------------------------------------------
    public void format(FileHeader head, RawText a, RawText b)
            throws IOException
    {
        currentDiff = new PrettyDiffResult();
        String path = head.getPath(Side.NEW);
        prettyDiffs.setObjectForKey(currentDiff, path);

        super.format(head, a, b);
    }


    // ----------------------------------------------------------
    protected void writeAddedLine(RawText text, int line) throws IOException
    {
        currentDiff.appendAddedLine(text, line);
        totalAdditionCount++;
    }


    // ----------------------------------------------------------
    protected void writeContextLine(RawText text, int line) throws IOException
    {
        currentDiff.appendContextLine(text, line);
    }


    // ----------------------------------------------------------
    protected void writeHunkHeader(int aStartLine, int aEndLine,
            int bStartLine, int bEndLine) throws IOException
    {
        currentDiff
                .appendHunkHeader(aStartLine, aEndLine, bStartLine, bEndLine);
    }


    // ----------------------------------------------------------
    protected void writeLine(char prefix, RawText text, int line)
            throws IOException
    {
        super.writeLine(prefix, text, line);
    }


    // ----------------------------------------------------------
    protected void writeRemovedLine(RawText text, int line) throws IOException
    {
        currentDiff.appendRemovedLine(text, line);
        totalDeletionCount++;
    }


    //~ Static/instance variables .............................................

    private NSMutableDictionary<String, PrettyDiffResult> prettyDiffs;
    private NSArray<String> sortedPaths;
    private PrettyDiffResult currentDiff;

    private int totalAdditionCount;
    private int totalDeletionCount;
}
