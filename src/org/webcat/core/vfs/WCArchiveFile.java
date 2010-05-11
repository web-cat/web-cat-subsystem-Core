package org.webcat.core.vfs;

import java.io.File;
import java.io.IOException;
import org.webcat.archives.ArchiveManager;
import org.webcat.archives.IArchiveEntry;

/*package*/ class WCArchiveFile extends WCNativeFile
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCArchiveFile(WCFile parent, File fileObj)
    {
        super(parent, fileObj);

        ArchiveManager manager = ArchiveManager.getInstance();

        try
        {
            archiveEntries = manager.getContents(fileObj);
        }
        catch (IOException e)
        {
            archiveEntries = new IArchiveEntry[0];
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected void visitChildren(IChildVisitor visitor)
    {
        for (IArchiveEntry entry : archiveEntries)
        {
            if (!visitor.visit(new WCArchiveEntry(this, entry)))
            {
                break;
            }
        }
    }


    //~ Static/instance variables .............................................

    private IArchiveEntry[] archiveEntries;
}
