/**
 *
 * SerializationManager
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: SerializationManager.java,v 1.1.1.1 2003/07/17 17:21:52 ianibbo Exp $
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

import java.math.BigInteger;
import java.util.Vector;
import java.util.Stack;

public interface SerializationManager
{
  public static final Integer TAGMODE_NONE = new Integer(-1);
  public static final Integer IMPLICIT = new Integer(0);
  public static final Integer EXPLICIT = new Integer(1);

  public static final int UNIVERSAL = 0;

  public static final int BOOLEAN = 1;
  public static final int INTEGER = 2;
  public static final int BITSTRING = 3;
  public static final int OCTETSTRING = 4;
  public static final int NULL = 5;
  public static final int OID = 6;
  public static final int OBJECT_DESCRIPTOR = 7;
  public static final int EXTERNAL = 8;
  public static final int REAL = 9;
  public static final int ENUMERATED = 10;
  public static final int SEQUENCEOF = 16;
  public static final int SEQUENCE = 16;
  public static final int SET = 17;
  public static final int NUMERIC_STRING = 18;
  public static final int PRINTABLE_STRING = 19;
  public static final int GENERALIZED_TIME = 24;
  public static final int GRAPHIC_STRING = 25;
  public static final int VISIBLE_STRING = 26;
  public static final int GENERAL_STRING = 27;


  // Er, not sure what I'm doing here
  public static final int ANY = 100;

  public static final int DIRECTION_ENCODE = 0;
  public static final int DIRECTION_DECODE = 1;
  public static final int DIRECTION_PRINT = 2;

  public int getDirection();

  // Tag returns the length of the encoded contents 
  public int tag_codec(boolean is_constructed) throws java.io.IOException;
  public byte[] octetstring_codec(Object instance, boolean is_constructed) throws java.io.IOException;
  public Boolean boolean_codec(Object instance, boolean is_constructed) throws java.io.IOException;
  public BigInteger integer_codec(Object instance, boolean is_constructed) throws java.io.IOException;
  public int[] oid_codec(Object instance, boolean is_constructed) throws java.io.IOException;
  public byte[] any_codec(Object instance, boolean is_constructed) throws java.io.IOException;
  public AsnBitString bitstring_codec(Object instance, boolean is_constructed) throws java.io.IOException;
  public AsnNull null_codec(Object instance, boolean is_constructed) throws java.io.IOException;
  // public Object choice(Object current_instance, Object[][] choice_info, int which, String name) throws java.io.IOException;
  public Object choice(Object current_instance, Object[][] choice_info, String name) throws java.io.IOException;
  public boolean sequenceBegin() throws java.io.IOException;
  public boolean sequenceEnd() throws java.io.IOException;
  public boolean constructedBegin(int tagclass, int tagnumber)  throws java.io.IOException;
  public boolean constructedEnd() throws java.io.IOException;

  // The next tag to be encoded will be : 
  // public int tag_class = -1;
  // public int tag_value = -1;
  // public boolean is_constructed = false;

  public Object implicit_tag(base_codec c, Object current_instance, int tag_class, int tag_number, boolean is_optional, String name) throws java.io.IOException;
  public Object explicit_tag(base_codec c, Object current_instance, int tag_class, int tag_number, boolean is_optional, String name) throws java.io.IOException;
  public Vector sequenceOf(Vector v, base_codec codec) throws java.io.IOException;
  public void implicit_settag(int tagclass, int tagvalue);

  public base_codec getHintCodec();
  public void setHintCodec(base_codec c);

  public String getCharsetEncoding();
}
