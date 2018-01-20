/**
 *
 * BEROutputStream : An implementation of the SerializationManager 
 *                   that can be used to serialize a type to a ByteArrayOutputStream.
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: BEROutputStream.java,v 1.1.1.1 2003/07/17 17:21:51 ianibbo Exp $
 * @see    com.k_int.codec.runtime.SerializationManager
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
import java.util.Enumeration;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BEROutputStream extends ByteArrayOutputStream implements SerializationManager
{
  Stack encoding_info = new Stack();
  public int tag_class = -1;
  public int tag_value = -1;
  public boolean is_constructed = false;

  /**
    Encoding that will be used for translating strings, etc.
    "US-ASCII"  Seven-bit ASCII, a.k.a. ISO646-US
    "ISO-8859-1"   ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
    "UTF-8" Eight-bit Unicode Transformation Format
    "UTF-16BE" Sixteen-bit Unicode Transformation Format, big-endian byte order
    "UTF-16LE" Sixteen-bit Unicode Transformation Format, little-endian byte order
    "UTF-16" Sixteen-bit Unicode Transformation Format, byte order specified by a 
    mandatory initial byte-order mark (either order accepted on input, big-endian used on output)
  */
  // private String encoding = "UTF-8";
  public String encoding = "US-ASCII";

  private base_codec codec_hint = null;

  // Remember that logging statements in this module must be controlled by
  // the DebugFlag class in this package!
  private Log log = LogFactory.getLog(this.getClass());

  public BEROutputStream()
  {
    super();

    if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
      log.debug("new BEROutputStream, default string encoding : "+encoding);
  }

  public BEROutputStream(String encoding)
  {
    super();

    if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
      log.debug("new BEROutputStream, default string encoding : "+encoding);

    this.encoding = encoding;
  }

  public BEROutputStream(int size)
  {
    super(size);

    if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
      log.debug("new BEROutputStream, default string encoding : "+encoding);
  }


  public BEROutputStream(int size, String encoding)
  {
    super(size);

    if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
      log.debug("new BEROutputStream, default string encoding : "+encoding);

    this.encoding = encoding;
  }

  // Methods from SerializationManager

  public int getDirection()
  {
    return DIRECTION_ENCODE;
  }

  // Return length of tag encoding
  public int tag_codec(boolean is_constructed) throws java.io.IOException
  {
    if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
      log.debug("tag ["+tag_class+","+tag_value+"]");

    if ( tag_class == -1 )
      throw new java.io.IOException("Trying to write an un-initialized tag");

    int k = tag_class;
    if(is_constructed)
        k |= 0x20;
 
    if(tag_value < 31) // We can encode in a single octet
    {
        write(k | tag_value);
    }
    else // Multiple tag octets
    {
        // In multiple length tags, first octet gives class & cons, bits 1-5 are all 1
        write(k | 0x1f);
 
        // Followed by the integer encoding of the tag, base 128
        encodeBase128Int(tag_value);
    }

    tag_class = -1;
    tag_value = -1;
    return 1;
  }

  public byte[] octetstring_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    byte[] enc = (byte[])instance;
    encodeLength(enc.length);
    write(enc);
    return enc;
  }

  public Boolean boolean_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    Boolean retval = (Boolean)instance;
    encodeLength(1);
    write(retval.booleanValue() ? 1 : 0);

    return retval;
  }

  public BigInteger integer_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    // debug("integer_codec "+(BigInteger)instance);

    // Just write out the integer encoding
    BigInteger intval = (BigInteger)instance;
 
    if ( intval != null )
    {
      int i=0;

      byte abyte0[] = intval.toByteArray();
 
      if((abyte0[0] & 0x80) != 0)
        throw new IllegalArgumentException("Illegal internal encoding");
 
      // byte abyte1[];
 
      // Write the encoding
      // if(abyte0[0] != 0)
      // {
      //   abyte1 = abyte0;
      // }
      // else
      // {
      //   i = abyte0.length;
      //   abyte1 = new byte[i - 1];
      //   System.arraycopy(abyte0, 1, abyte1, 0, i-1);
      // }
      // encodeLength(abyte1.length);
      // write(abyte1);
 
      encodeLength(abyte0.length);
      write(abyte0);

      flush();
    }
 
    return intval;
  }

  public int[] oid_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    // debug("oid_codec");
    int[] oid = (int[])instance;

    int len = oid.length;

    int len_pos = count;
    // Assume OID's will be less than 127 octets in length
    write(0);

    if ( len > 1 )
    {
      // We encode by first doing ( 40 * array[0] ) + array[1]
      byte first_octet = (byte) ( ( oid[0] * 40 ) + oid[1] );
      write(first_octet);
 
      for ( int i=2; i<len; i++ )
      {
        encodeBase128Int(oid[i]);
      }

      rewriteLength(len_pos, count-(len_pos+1), 1);
    }

    return oid;
  }

  public byte[] any_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    byte[] b = (byte[])instance;

    encodeLength(b.length);
    write(b);
    // write(b,0,b.length);

    return b;
  }

  public AsnBitString bitstring_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    AsnBitString abs = (AsnBitString)instance;

    int len = ( abs.getValue().length ) + 1;  // +1 for number of unused bits octet

    // debug("bitstring_codec length of contents : "+len);

    // Encode the length
    encodeLength(len);

    write(0);  // Cheat and say that there are no unused octets

    write(abs.getValue());

    return abs;
  }

  public AsnNull null_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    AsnNull retval = (AsnNull)instance;
    encodeLength(0);       // Length of length encoding for a length of 0 is 0!
    // write(0);

    return retval;
  } 

  public Object choice(Object current_instance, Object[][] choice_info, String name) throws java.io.IOException
  {
    // Extract the which element from current_instance
    ChoiceType c = (ChoiceType)current_instance;
 
    Integer tagmode = (Integer)(choice_info[c.which][0]);
    Integer tagclass = (Integer)(choice_info[c.which][1]);
    Integer tagnumber = (Integer)(choice_info[c.which][2]);

    // debug("choice_codec ("+name+") tagmode:"+tagmode+"\n");
 
    base_codec codec_to_use = ((base_codec)(choice_info[c.which][3]));
 
    if ( tagmode.equals(SerializationManager.TAGMODE_NONE) )
    {
      // No tagging, just dive into the decoding
      // debug("  <no tagging, so simply calling codec for "+((String)(choice_info[c.which][4]))+">\n");
      codec_to_use.serialize(this, c.o, false, ((String)(choice_info[c.which][4])));
    }
    else
    {
      if (  tagmode.equals(SerializationManager.IMPLICIT ) )
      {
        // debug("  <implicit  tagging, so simply calling codec for "+((String)(choice_info[c.which][4]))+">\n");
        // Implicit Tagging
        implicit_settag(tagclass.intValue(), tagnumber.intValue());
        codec_to_use.serialize(this, c.o, false, ((String)(choice_info[c.which][4])));
      }
      else  // Explicit tagging
      {
        // debug("  <explicit  tagging, so simply calling codec for "+((String)(choice_info[c.which][4]))+">\n");
        constructedBegin(tagclass.intValue(), tagnumber.intValue());
        codec_to_use.serialize(this, c.o, false, ((String)(choice_info[c.which][4])));
        constructedEnd();
      }
    }
    return current_instance;
  }

  public boolean sequenceBegin() throws java.io.IOException
  {
    if ( tag_class < 0 )
    {
      tag_class = SerializationManager.UNIVERSAL;
      tag_value = SerializationManager.SEQUENCE;
    }
    return constructedBegin(tag_class, tag_value); 
  }

  public boolean sequenceEnd()
  {
    return constructedEnd();
  }

  public boolean constructedBegin(int tagclass, int tagnumber)  throws java.io.IOException
  {
    // debug("Constructed Begin\n");

    if ( tag_class < 0 )
    {
      tag_class = tagclass;
      tag_value = tagnumber;
    }

    if ( ( DebugFlag.debug_enabled ) && ( log.isDebugEnabled() ) )
    {
      log.debug("CONS [???] ("+tag_class+","+tag_value+") "+tagclass);
      log.debug("{");
    }


    tag_codec(true);

    CodecStackInfo csi = new CodecStackInfo();

    csi.len_offset = count;
    csi.lenlen = 4;

    // Write dummy length offset (I'm writing 4 octets here.. for no good reason);
    write((byte)0);
    write((byte)0);
    write((byte)0);
    write((byte)0);

    csi.contents_offset = count;
    // debug("Constructed length starts at "+csi.len_offset+" contents start at "+count+"\n");

    encoding_info.push(csi);
    return true;
  }

  public boolean constructedEnd()
  {
    CodecStackInfo csi = (CodecStackInfo)encoding_info.pop();

    // debug("Constructed contents end at "+count+"\n");

    int length_of_contents = count - csi.contents_offset;
    rewriteLength(csi.len_offset, length_of_contents, csi.lenlen);

    if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
    {
      log.debug("} CONS END ["+length_of_contents+"] ("+tag_class+","+tag_value+")");
    }
    return true;
  }

  public Object implicit_tag(base_codec c, Object current_instance, int tag_class, int tag_number, boolean is_optional, String name) throws java.io.IOException
  {
    if ( null != current_instance )
    {
      if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
        log.debug("implicit_tag "+tag_class+","+tag_number+" "+name);

      implicit_settag(tag_class, tag_number);
      c.serialize(this, current_instance, is_optional, name);
    }
    else
    {
      if ( !is_optional )
        throw new java.io.IOException("Missing mandatory member: "+name);
      else
      {
        if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
          log.debug("(missing optional) implicit_tag "+tag_class+","+tag_number+" "+name);
      }
    }

    return current_instance;
  }

  public Object explicit_tag(base_codec c, Object current_instance, int tag_class, int tag_number, boolean is_optional, String name) throws java.io.IOException
  {
    if ( null != current_instance )
    {
      if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
        log.debug("explicit_tag "+tag_class+","+tag_number+" "+name);
      constructedBegin(tag_class, tag_number);
      c.serialize(this, current_instance, is_optional, name);
      constructedEnd();
    }
    else
    {
      if ( !is_optional )
        throw new java.io.IOException("Missing mandatory member: "+name);
      else
      {
        if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
          log.debug("(missing optional) explicit_tag "+tag_class+","+tag_number+" "+name);
      }
    }

    return current_instance;
  }

  public Vector sequenceOf(Vector v, base_codec codec) throws java.io.IOException
  {
    if ( null != v )
    {
      if ( v.size() > 0 )
      {
        for ( Enumeration e = v.elements(); e.hasMoreElements(); ) 
        {
          Object o = e.nextElement();
          codec.serialize(this, o, true, "SequenceOfElement");
        }
      }
    }
    else
    {
      if ( DebugFlag.debug_enabled && log.isDebugEnabled() )
        log.debug("empty sequenceOf");
    }

    return v;
  }

  public void implicit_settag(int tagclass, int tagvalue)
  {
    if ( tag_class < 0 )
    {
      tag_class = tagclass;
      tag_value = tagvalue;
    }
  }

  private boolean tag()
  {
    return true;
  }

  private int encodeLength(int len)
  {
    // debug("Encoding length "+len+"\n");

    if(len >= 128)
    {
      byte byte0;
 
      if(len >= 0x1000000)
         byte0 = 4;
      else if(len >= 0x10000)
         byte0 = 3;
      else if(len >= 256)
         byte0 = 2;
      else
         byte0 = 1;
 
      write(0x80 | byte0);
      for(int j = (byte0 - 1) * 8; j >= 0; j -= 8)
        write(len >> j);

      return byte0;
    }
    else
    {
      // Single length octet
      write(len);
      return 1;
    }
  }

  private boolean rewriteLength(int pos, int len, int max_octets)
  {
    // debug("rewriting length..."+len+"\n");

    int len_offset = pos;

    if ( max_octets > 1 )
    {
      byte byte0 = (byte)(max_octets-1);  // -1 because max_octets includes length of length encoding

      // Encode length of length encoding
      buf[len_offset++]=(byte)(0x80 | byte0);

      // Length octets
      for(int j = (byte0 - 1) * 8; j >= 0; j -= 8)
        buf[len_offset++]=(byte)(len >> j);
    }
    else
    {
      if ( len > 127 ) 
      {
        // Really fatal error
        System.err.println("Asked to encode a value > 127 in only one length octet");
        System.exit(1);
      }
      else
      {
        buf[len_offset] = (byte)len;
      }
    }

    return true;
  }

  private int encodeTag()
  {
    return 1;
  }

  private void encodeBase128Int(int value) throws java.io.IOException
  {
    byte[] enc = new byte[10];
    int pos = 0;
 
    while ( ( value > 127 ) && ( pos < 9 ) )
    {
      enc[pos++] = (byte) ( value & 127 );
      value = value >> 7;
    }
    enc[pos] = (byte)value;
 
    for ( ;pos>=0;pos-- )
    {
      write( ( enc[pos] | ( pos == 0 ? 0 : 128 ) ) );
    }
  } 

  public base_codec getHintCodec()
  {
    return codec_hint;
  }

  public void setHintCodec(base_codec c)
  {
    codec_hint = c;
  }

  public String getCharsetEncoding()
  {
    return encoding;
  }
}
