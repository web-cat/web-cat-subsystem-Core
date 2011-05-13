package org.webcat.core;

import org.eclipse.jgit.lib.Constants;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class RepositoryFileLabel extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public RepositoryFileLabel(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String noFilePlaceholder;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public Object value()
    {
        return value;
    }


    // ----------------------------------------------------------
    public void setValue(Object value)
    {
        this.value = value;

        if (value == null)
        {
            fileItem = null;
        }
        else if (value instanceof String)
        {
            fileItem = RepositoryEntryRef.fromOldStylePath((String) value);
        }
        else
        {
            fileItem = RepositoryEntryRef.fromDictionary(
                    (NSDictionary<String, Object>) value);
        }
    }


    // ----------------------------------------------------------
    public boolean hasFile()
    {
        return fileItem != null;
    }


    // ----------------------------------------------------------
    public String repositoryAndPath()
    {
        return fileItem.repositoryName() + "/" + fileItem.path();
    }


    // ----------------------------------------------------------
    public String iconPath()
    {
        return fileItem.iconPath();
    }


    // ----------------------------------------------------------
    public String branch()
    {
        String branch = fileItem.branch();

        if (branch.startsWith(Constants.R_HEADS))
        {
            return branch.substring(Constants.R_HEADS.length());
        }
        else if (branch.startsWith(Constants.R_TAGS))
        {
            return branch.substring(Constants.R_TAGS.length());
        }
        else
        {
            return branch;
        }
    }


    // ----------------------------------------------------------
    public boolean isTag()
    {
        return fileItem.branch().startsWith(Constants.R_TAGS);
    }


    //~ Static/instance variables .............................................

    private Object value;
    private RepositoryEntryRef fileItem;
}
