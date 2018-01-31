package org.webcat.core.webapi;

import java.io.IOException;
import java.io.Writer;
import org.webcat.core.EOBase;
import com.webobjects.appserver.WOMessage;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * A web API response formatter that sends its response in XML format.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2012/06/22 16:23:17 $
 */
public class XmlResponseFormatter extends ResponseFormatter
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void formatToWriter(Writer writer) throws IOException
    {
        this.writer = writer;

        writer.append("<?xml version=\"1.0\" ?>\n");
        super.formatToWriter(writer);
    }


    // ----------------------------------------------------------
    public String mimeType()
    {
        return "application/xml";
    }


    // ----------------------------------------------------------
    public void format(Object object) throws IOException
    {
        if (object instanceof NSArray)
        {
            indent(writer);
            writer.write("<array>\n");

            depth++;
            for (Object child : (NSArray) object)
            {
                formatWithTag(child, "item");
            }
            depth--;

            indent(writer);
            writer.write("</array>\n");
        }
        else if (object instanceof NSDictionary)
        {
            indent(writer);
            writer.write("<map>\n");

            NSDictionary<?, ?> map = (NSDictionary<?, ?>) object;

            depth++;
            for (Object key : map.allKeys())
            {
                indent(writer);
                formatWithTag(map.objectForKey(key), key.toString());
                writer.write("\n");
            }
            depth--;

            indent(writer);
            writer.write("</map>\n");
        }
        else if (object instanceof EOBase)
        {
            writer.write(((EOBase) object).apiId());
        }
        else if (object instanceof WebAPIError)
        {
            WebAPIError error = (WebAPIError) object;

            if (error.code() != 0)
            {
                writer.write("<code>");
                format(error.code());
                writer.write("</code>");
            }

            if (error.message() != null && error.message().length() > 0)
            {
                writer.write("<message>");
                format(error.message());
                writer.write("</message>");
            }
        }
        else
        {
            writer.write(
                    WOMessage.stringByEscapingHTMLString(object.toString()));
        }
    }


    // ----------------------------------------------------------
    private void formatWithTag(Object object, String tag) throws IOException
    {
        writer.write("<");
        writer.write(tag);
        writer.write(">");

        format(object);

        writer.write("</");
        writer.write(tag);
        writer.write(">");
    }


    // ----------------------------------------------------------
    private void indent(Writer writer) throws IOException
    {
        for (int i = 0; i < depth; i++)
        {
            writer.write("    ");
        }
    }


    //~ Instance/static variables .............................................

    private Writer writer;
    private int depth;
}
