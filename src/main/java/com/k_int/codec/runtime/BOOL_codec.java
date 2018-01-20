/**
 *
 * bool_codec
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: BOOL_codec.java,v 1.1.1.1 2003/07/17 17:21:51 ianibbo Exp $
 *
 * Copyright:   Copyright (C) 2000, Knowledge Integration Ltd.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the license, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite
 * 330, Boston, MA  02111-1307, USA.
 *
 */

package com.k_int.codec.runtime;

public class BOOL_codec extends base_codec
{
  private static BOOL_codec me = null;

  public static BOOL_codec getCodec()
  {
    if ( me == null )
      me = new BOOL_codec();

    return me;
  }

  public Object serialize(SerializationManager sm,
                          Object type_instance,
                          boolean is_optional,
                          String type_name) throws java.io.IOException
  {
    Boolean retval = (Boolean)type_instance;
    boolean is_constructed = false;     
 
    if ( sm.getDirection() == SerializationManager.DIRECTION_ENCODE )
    {
      if ( null != retval )
      {
        sm.implicit_settag(SerializationManager.UNIVERSAL, SerializationManager.BOOLEAN);
        int len = sm.tag_codec(false);
        if ( len >= 0 )
          retval = sm.boolean_codec(retval, is_constructed);
      }
    }
    else if ( sm.getDirection() == SerializationManager.DIRECTION_DECODE )
    {
      sm.implicit_settag(SerializationManager.UNIVERSAL, SerializationManager.BOOLEAN);
      int len = sm.tag_codec(false);
      if ( len >= 0 )
        retval = sm.boolean_codec(retval, is_constructed);
    }
 
    if ( ( retval == null ) && ( ! is_optional ) )
      throw new java.io.IOException("Missing mandatory member: "+type_name);
 
    return (Object)retval;
  }
}
