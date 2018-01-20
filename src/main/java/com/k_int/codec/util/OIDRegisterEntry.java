/*
 *
 * OIDRegisterEntry 
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: OIDRegisterEntry.java,v 1.1.1.1 2003/07/17 17:21:49 ianibbo Exp $
 *
 * Copyright:   Copyright (C) 2000, Knowledge Integration Ltd (See the file LICENSE for details.)
 *
 */

package com.k_int.codec.util;

import java.util.StringTokenizer;

public class OIDRegisterEntry
{
    private String name = null;
    private String string_value = null;
    private int[] value = null;
    private String description = null;
    private Object handler = null;

    public OIDRegisterEntry(String name, String value, String description, Object handler)
    {
        this.name=name;
        this.string_value=value;
        this.description=description;
        this.handler=handler;
        parseValueString();
    }

    public String getName()
    {
        return name;
    }

    public int[] getValue()
    {
        return value;
    }

    public String getDescription()
    {
        return description;
    }

    public String getStringValue()
    {
        return string_value;
    }

    public Object getHandler()
    {
        return handler;
    }

    private void parseValueString()
    {
        if ( null != string_value )
        {
            // log.debug("Parsing value string "+string_value);
            StringTokenizer st = new StringTokenizer(string_value, "{,.}", false);
            this.value = new int[st.countTokens()];

            int counter = 0;
            while ( st.hasMoreTokens() )
            {
                this.value[counter++] = Integer.parseInt(st.nextToken());
            }
        }
    }

    public String toString()
    {
      return name;
    }
}
