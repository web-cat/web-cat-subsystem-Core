package net.sf.webcat.core;

import net.sf.webcat.ui.AbstractTreeModel;
import net.sf.webcat.ui.generators.JavascriptFunction;
import net.sf.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXS;

public class WidgetTestPage extends WCComponent
{
    public WidgetTestPage(WOContext context)
    {
        super(context);
        formValues.put("plainTextBox", "plain text box");
        formValues.put("numberSpinner", new Integer(10));
    }


    public AutoIncrementingCounterSet counters =
        new AutoIncrementingCounterSet();

    public NSMutableDictionary<String, Object> formValues =
        new NSMutableDictionary<String, Object>();

    public TreeModel treeModel = new TreeModel();
    public NSMutableArray<String> checkedTreeItems;


    //~ Public Nested Classes .................................................

    public class TreeModel extends AbstractTreeModel
    {
        //~ Public Methods ....................................................

        // ----------------------------------------------------------
        @Override
        protected NSArray<?> childrenOfItem(Object parentItem)
        {
            if (parentItem == null)
            {
                NSMutableArray<String> items = new NSMutableArray<String>();
                items.addObject("1");
                items.addObject("2");
                items.addObject("3");
                return items;
            }
            else if (parentItem instanceof String)
            {
                String parent = (String) parentItem;
                if (parent.split("\\.").length < 4)
                {
                    NSMutableArray<String> items = new NSMutableArray<String>();
                    items.addObject(parent + ".1");
                    items.addObject(parent + ".2");
                    items.addObject(parent + ".3");
                    return items;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }


        // ----------------------------------------------------------
        @Override
        protected boolean itemHasChildren(Object parentItem)
        {
            if (parentItem instanceof String)
            {
                String parent = (String) parentItem;
                return (parent.split("\\.").length < 4);
            }
            else
            {
                return false;
            }
        }
    }


    public String validateTextBox()
    {
        String value = (String) formValues.valueForKey("validatedTextBox");

        if (value == null)
        {
            return "String must not be null.";
        }
        else if (value.length() % 2 == 0)
        {
            return "String length must not be a multiple of 2.";
        }
        else
        {
            return null;
        }
    }


    public String validatePassword()
    {
        String value = (String) formValues.valueForKey("validatedPassword");

        if (value == null)
        {
            return "Password must not be empty.";
        }

        boolean valid = true;
        for (int i = 0; i < value.length(); i++)
        {
            char ch = value.charAt(i);
            if (!Character.isLetterOrDigit(ch))
            {
                valid = false;
                break;
            }
        }

        if (!valid)
        {
            return "Password may only contain letters and digits.";
        }
        else
        {
            return null;
        }
    }


    public String validateConfirmPassword()
    {
        String password = (String) formValues.valueForKey("validatedPassword");
        String confirm = (String) formValues.valueForKey("validatedConfirmPassword");

        if (password == null && confirm == null)
        {
            return null;
        }
        else if (password != null && confirm != null && !password.equals(confirm))
        {
            return "Passwords must be identical.";
        }
        else
        {
            return null;
        }
    }


    public JavascriptGenerator submitForm()
    {
        JavascriptGenerator page = new JavascriptGenerator();

        StringBuilder buffer = new StringBuilder();
        appendFormValue(buffer, "checkbox");
        appendFormValue(buffer, "numberSpinner");

        page.call("alert", buffer.toString());

        return page;
    }


    public JavascriptGenerator displayDialog()
    {
        JavascriptGenerator page = new JavascriptGenerator();
        page.alert("Test alert",
                "If you click on the OK button, you'll be redirected to Google.",
                null,
                new JavascriptFunction() {
                    @Override
                    public void generate(JavascriptGenerator g)
                    {
                        g.redirectTo("http://www.google.com");
                    }
                });

        return page;
    }


    public WOActionResults validateForm()
    {
        return new ValidatingAction(this) {
            @Override
            protected WOActionResults performStandardAction()
            {
                return null;
            }
        };
    }


    private void appendFormValue(StringBuilder buffer, String key)
    {
        buffer.append(key);
        buffer.append(" = ");
        buffer.append(formValues.objectForKey(key));
        buffer.append("\n");
    }


    private class AutoIncrementingCounterSet
    implements NSKeyValueCoding
    {
        public Object valueForKey(String key)
        {
            if (!counters.containsKey(key))
            {
                counters.setObjectForKey(Integer.valueOf(0), key);
            }

            int value = counters.objectForKey(key) + 1;
            counters.setObjectForKey(value, key);
            return value;
        }


        public void takeValueForKey(Object arg0, String arg1)
        {
            // Do nothing.
        }

        private NSMutableDictionary<String, Integer> counters =
            new NSMutableDictionary<String, Integer>();
    }
}
