/*==========================================================================*\
 |  $Id: GitPerson.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

package org.webcat.core.git;

import org.eclipse.jgit.lib.PersonIdent;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Wraps a JGit {@PersonIdent} object with KVC support and some other
 * convenience methods.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class GitPerson
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitPerson(PersonIdent ident)
    {
        this.ident = ident;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String email()
    {
        try
        {
            return ident.getEmailAddress();
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String name()
    {
        try
        {
            return ident.getName();
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public NSTimestamp timestamp()
    {
        try
        {
            return new NSTimestamp(ident.getWhen());
        }
        catch (Exception e)
        {
            return null;
        }
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


    //~ Static/instance variables .............................................

    private PersonIdent ident;
}
