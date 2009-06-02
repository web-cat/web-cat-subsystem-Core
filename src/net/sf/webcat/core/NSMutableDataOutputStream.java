package net.sf.webcat.core;

import java.io.IOException;
import java.io.OutputStream;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSRange;

public class NSMutableDataOutputStream extends OutputStream
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public NSMutableDataOutputStream()
    {
        data = new NSMutableData();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public NSData data()
    {
        return data;
    }


    // ----------------------------------------------------------
    @Override
    public void write(int b) throws IOException
    {
        data.appendByte((byte) b);
    }
    

    // ----------------------------------------------------------
    @Override
    public void write(byte b[], int off, int len) throws IOException
    {
        data.appendBytes(b, new NSRange(off, len));
    }


    //~ Static/instance variables .............................................

    private NSMutableData data;
}
