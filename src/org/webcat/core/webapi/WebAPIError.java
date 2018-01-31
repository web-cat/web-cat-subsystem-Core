package org.webcat.core.webapi;

//-------------------------------------------------------------------------
/**
 * <p>
 * An error message that can be returned from web API controller actions to
 * indicate that a preventable error occurred during the API call. An error can
 * consist of a numeric error code, a textual error message, or both.
 * </p><p>
 * Error codes are action-defined. Zero should not be used because it will not
 * be transmitted back to the client. Similarly, null or empty error messages
 * will also not be transmitted.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2012/06/22 16:23:17 $
 */
public class WebAPIError
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new {@code WebAPIError} with the specified error code.
     *
     * @param code the error code
     */
    public WebAPIError(int code)
    {
        this(code, null);
    }


    // ----------------------------------------------------------
    /**
     * Initializes a new {@code WebAPIError} with the specified error message.
     *
     * @param message the error message
     */
    public WebAPIError(String message)
    {
        this(0, message);
    }


    // ----------------------------------------------------------
    /**
     * Initializes a new {@code WebAPIError} with the specified error code and
     * message.
     *
     * @param code the error code
     * @param message the error message
     */
    public WebAPIError(int code, String message)
    {
        this.code = code;
        this.message = message;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public int code()
    {
        return code;
    }


    // ----------------------------------------------------------
    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String message()
    {
        return message;
    }


    //~ Static/instance variables .............................................

    private int code;
    private String message;
}
