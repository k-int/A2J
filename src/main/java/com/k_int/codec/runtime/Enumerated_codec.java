/**
 *
 * Integer_codec : Basic Enumerated (integer) codec
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: Enumerated_codec.java,v 1.1.1.1 2003/07/17 17:21:51 ianibbo Exp $
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

/**
 *
 * At some point, we may want to make an enumerated base type in here
 * and derive a class that has static final int values for each of the enumerated
 * items... I don't need that for the moment though.
 *
 */

package com.k_int.codec.runtime;

import java.math.BigInteger;

public class Enumerated_codec extends base_codec
{
  private static Enumerated_codec me = null;

  public static Enumerated_codec getCodec()
  {
    if ( me == null )
      me = new Enumerated_codec();

    return me;
  }

  public Object serialize(SerializationManager sm,
                          Object type_instance,
                          boolean is_optional,
                          String type_name) throws java.io.IOException
  {
    BigInteger retval = (BigInteger)type_instance;

    boolean is_constructed = false;

    if ( sm.getDirection() == SerializationManager.DIRECTION_ENCODE )
    {
      if ( null != retval )
      {
        sm.implicit_settag(SerializationManager.UNIVERSAL, SerializationManager.ENUMERATED);

        int len = sm.tag_codec(false);

        if ( len == 0 )
           throw new java.io.IOException("Error encoding length");
    
        retval = sm.integer_codec(retval, is_constructed);
      }
    }
    else if ( sm.getDirection() == SerializationManager.DIRECTION_DECODE )
    {
      sm.implicit_settag(SerializationManager.UNIVERSAL, SerializationManager.ENUMERATED);
      int len = sm.tag_codec(false);
      if ( len >= 0 )
      {
        retval = sm.integer_codec(retval, is_constructed);
      }
    }
 
    if ( ( retval == null ) && ( ! is_optional ) )
      throw new java.io.IOException("Missing mandatory Enumeration member: "+type_name);

    return (Object)retval;
  }
}
