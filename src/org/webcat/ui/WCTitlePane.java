package org.webcat.ui;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOElementID;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import er.ajax.AjaxUtils;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXStringUtilities;

public class WCTitlePane extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTitlePane(WOContext context)
    {
        super(context);

        titlePaneBindings = new BindingsProxy();
    }


    //~ KVC attributes (must be public) .......................................

    public BindingsProxy titlePaneBindings;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * This component uses a template that is generated at runtime in order to
     * pass its bindings directly to the content pane that it contains.
     *
     * @return the template
     */
    @Override
    public WOElement template()
    {
        if (cachedTemplate == null)
        {
            cachedTemplate = WOComponent.templateWithHTMLString(
                    NSBundle.bundleForClass(getClass()).name(), name(),
                    TEMPLATE_HTML, declarationStringWithBindings(), null,
                    WOApplication.application().associationFactory(),
                    WOApplication.application().namespaceProvider());
        }

        return cachedTemplate;
    }


    // ----------------------------------------------------------
    @Override
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Returns true if the content of this title pane should take part in the
     * request/response cycle (that is, if it should be rendered or asked to
     * take values or invoke actions).
     *
     * @return true if the content should be rendered; otherwise, false
     */
    public boolean shouldProcessContent()
    {
        if (!wasContentRendered)
        {
            WOContext context = context();

            boolean isInitiallyOpen = valueForBooleanBinding("open", true);
            boolean isLoadingNow = false;

            if (context.elementID() != null && context.senderID() != null)
            {
                boolean isAjax = AjaxUtils.isAjaxRequest(context.request());

                WOElementID elementID = new WOElementID(context.elementID());
                elementID.deleteLastElementIDComponent();
                String parentID = elementID.toString();

                // ... if the request is an AJAX request that was sent by the
                // enclosing content pane (meaning the pane is trying to load
                // itself), or if the request is any request (AJAX or not) sent
                // by a child of the pane (meaning it must have been opened and
                // loaded for something inside it to send a request).

                isLoadingNow =
                    (isAjax && parentID.equals(context.senderID()));
            }

            wasContentRendered = isInitiallyOpen || isLoadingNow;
        }

        return wasContentRendered;
    }


    // ----------------------------------------------------------
    /**
     * Passes any bindings associated with this WCTitlePane component to the
     * WCContentPane that will be embedded inside it. This method essentially
     * just adds a set of "binding = binding" lines to the WOD declaration of
     * the template, and {@link #handleQueryWithUnboundKey(String)} will grab
     * the correct values from this component to give to the content pane.
     *
     * @return the WOD template string
     */
    public String declarationStringWithBindings()
    {
        StringBuffer buffer = new StringBuffer(32);

        for (String binding : bindingKeys())
        {
            buffer.append(binding);
            buffer.append(" = titlePaneBindings.");
            buffer.append(binding);
            buffer.append(";\n");
        }

        String wod = TEMPLATE_WOD.replaceFirst("\\$\\{otherBindings\\}",
                buffer.toString());

        return wod;
    }


    //~ Private classes .......................................................

    // ----------------------------------------------------------
    /**
     * Allows the title pane's bindings to pass through seamlessly to the
     * content pane contained in the template.
     */
    private class BindingsProxy implements NSKeyValueCoding
    {
        // ----------------------------------------------------------
        public void takeValueForKey(Object value, String key)
        {
            WCTitlePane pane = WCTitlePane.this;

            if (pane.hasBinding(key))
            {
                pane.setValueForBinding(value, key);
            }
            else
            {
                NSKeyValueCoding.DefaultImplementation.takeValueForKey(
                        this, value, key);
            }
        }


        // ----------------------------------------------------------
        public Object valueForKey(String key)
        {
            WCTitlePane pane = WCTitlePane.this;

            if (pane.hasBinding(key))
            {
                return pane.valueForBinding(key);
            }
            else
            {
                return NSKeyValueCoding.DefaultImplementation.valueForKey(
                        this, key);
            }
        }
    }


    //~ Static/instance variables .............................................

    private WOElement cachedTemplate;
    private boolean wasContentRendered;

    private static final String TEMPLATE_HTML =
        "<wo name=\"TitlePane\">" +
            "<wo name=\"ShouldProcessChildren\">" +
                "<wo name=\"ComponentContent\" />" +
            "</wo>" +
        "</wo>";

    private static final String TEMPLATE_WOD =
        "TitlePane : WCContentPane {\n" +
            "dojoType = \"webcat.TitlePane\";\n" +
            "${otherBindings}" +
        "}\n" +
        "ShouldProcessChildren : WOConditional {\n" +
            "condition = shouldProcessContent;\n" +
        "}\n" +
        "ComponentContent : WOComponentContent { }\n";
}
