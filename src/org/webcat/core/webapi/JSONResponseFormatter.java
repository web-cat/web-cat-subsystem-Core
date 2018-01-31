package org.webcat.core.webapi;

import java.io.IOException;
import java.io.Writer;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.webcat.core.EOBase;
import com.webobjects.appserver.WOMessage;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * A web API formatter that sends its response in JSON format. This formatter
 * is the default if no formatting specifier is included in the URL.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2012/06/22 16:23:17 $
 */
public class JSONResponseFormatter extends ResponseFormatter
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void formatToWriter(Writer writer) throws IOException
    {
        this.writer = new JSONWriter(writer);

        super.formatToWriter(writer);
    }


    // ----------------------------------------------------------
    public String mimeType()
    {
        return "application/json";
    }


    // ----------------------------------------------------------
    @Override
    protected void format(Object object) throws IOException
    {
        try
        {
            if (object instanceof NSArray)
            {
                writer.array();

                for (Object child : (NSArray) object)
                {
                    format(child);
                }

                writer.endArray();
            }
            else if (object instanceof NSDictionary)
            {
                writer.object();

                NSDictionary<?, ?> map = (NSDictionary<?, ?>) object;

                for (Object key : map.allKeys())
                {
                    writer.key(key.toString());
                    format(map.objectForKey(key));
                }

                writer.endObject();
            }
            else if (object instanceof Boolean)
            {
                writer.value(((Boolean) object).booleanValue());
            }
            else if (object instanceof Number)
            {
                Number number = (Number) object;

                if (object instanceof Float || object instanceof Double)
                {
                    writer.value(number.doubleValue());
                }
                else
                {
                    writer.value(number.longValue());
                }
            }
            else if (object instanceof EOBase)
            {
                writer.value(((EOBase) object).apiId());
            }
            else if (object instanceof WebAPIError)
            {
                WebAPIError error = (WebAPIError) object;

                writer.object();

                if (error.code() != 0)
                {
                    writer.key("code").value(error.code());
                }

                if (error.message() != null && error.message().length() > 0)
                {
                    writer.key("message").value(error.message());
                }

                writer.endObject();
            }
            else
            {
                writer.value(object);
            }
        }
        catch (JSONException e)
        {
            throw new IOException(e);
        }
    }


    //~ Static/instance variables .............................................

    private JSONWriter writer;
}
