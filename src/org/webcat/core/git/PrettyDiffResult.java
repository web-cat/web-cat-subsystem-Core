/*==========================================================================*\
 |  $Id: PrettyDiffResult.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010-2012 Virginia Tech
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

import org.eclipse.jgit.diff.RawText;
import com.webobjects.appserver.WOMessage;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

public class PrettyDiffResult
    implements NSKeyValueCodingAdditions
{
    // ----------------------------------------------------------
    public PrettyDiffResult()
    {
        this.content = new StringBuffer();
        this.aLineNumbers = new StringBuffer();
        this.bLineNumbers = new StringBuffer();
        lineDifference = 0;
    }


    // ----------------------------------------------------------
    public String content()
    {
        return content.toString();
    }


    // ----------------------------------------------------------
    public String aLineNumbers()
    {
        return aLineNumbers.toString();
    }


    // ----------------------------------------------------------
    public String bLineNumbers()
    {
        return bLineNumbers.toString();
    }


    // ----------------------------------------------------------
    public int additionCount()
    {
        return additionCount;
    }


    // ----------------------------------------------------------
    public int deletionCount()
    {
        return deletionCount;
    }


    // ----------------------------------------------------------
    void appendAddedLine(RawText text, int line)
    {
        content.append("<div class=\"diff-added\">+");
        content.append(
                WOMessage.stringByEscapingHTMLString(text.getString(line)));
        content.append("</div>");

        aLineNumbers.append("<div>&nbsp;</div>");

        lineDifference++;

        bLineNumbers.append("<div>");
        bLineNumbers.append(Integer.toString(line + 1));
        bLineNumbers.append("</div>");

        additionCount++;
    }


    // ----------------------------------------------------------
    void appendContextLine(RawText text, int line)
    {
        content.append("<div class=\"diff-context\"> ");
        content.append(
                WOMessage.stringByEscapingHTMLString(text.getString(line)));
        content.append("</div>");

        aLineNumbers.append("<div>");
        aLineNumbers.append(Integer.toString(line + 1));
        aLineNumbers.append("</div>");

        bLineNumbers.append("<div>");
        bLineNumbers.append(Integer.toString(line + 1 + lineDifference));
        bLineNumbers.append("</div>");
    }


    // ----------------------------------------------------------
    void appendHunkHeader(int aStartLine, int aEndLine,
            int bStartLine, int bEndLine)
    {
        content.append("<div class=\"diff-header\">");
        content.append("@@");
        appendRange('-', aStartLine + 1, aEndLine - aStartLine);
        appendRange('+', bStartLine + 1, bEndLine - bStartLine);
        content.append(" @@");
        content.append("</div>");

        aLineNumbers.append("<div>...</div>");
        bLineNumbers.append("<div>...</div>");
    }


    // ----------------------------------------------------------
    private void appendRange(char prefix, int begin, int cnt)
    {
        content.append(' ');
        content.append(prefix);

        switch (cnt)
        {
            case 0:
                content.append(Integer.toString(begin - 1));
                content.append(',');
                content.append('0');
                break;

            case 1:
                content.append(Integer.toString(begin));
                break;

            default:
                content.append(Integer.toString(begin));
                content.append(',');
                content.append(Integer.toString(cnt));
                break;
        }
    }


    // ----------------------------------------------------------
    void appendRemovedLine(RawText text, int line)
    {
        content.append("<div class=\"diff-removed\">-");
        content.append(
                WOMessage.stringByEscapingHTMLString(text.getString(line)));
        content.append("</div>");

        lineDifference--;

        aLineNumbers.append("<div>");
        aLineNumbers.append(Integer.toString(line + 1));
        aLineNumbers.append("</div>");

        bLineNumbers.append("<div>&nbsp;</div>");

        deletionCount++;
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(
                this, value, keyPath);
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(
                this, keyPath);
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
                this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }


    private StringBuffer content;
    private StringBuffer aLineNumbers;
    private StringBuffer bLineNumbers;
    private int lineDifference;

    private int additionCount;
    private int deletionCount;
}
