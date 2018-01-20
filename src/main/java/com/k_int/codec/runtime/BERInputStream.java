/**
 *
 * BERInputStream : An implementation of the SerializationManager class that takes
 *                  an InputStream and can then be used as a parameter to a codec instance.
 *                  The data from the input stream will then be decoded according to the 
 *                  basic encoding rules.
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: BERInputStream.java,v 1.1.1.1 2003/07/17 17:21:50 ianibbo Exp $
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
 *
 */

package com.k_int.codec.runtime;

import java.math.BigInteger;
import java.util.Vector;
import java.util.Stack;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 

public class BERInputStream implements SerializationManager
{
  Stack encoding_info = new Stack();
  public int tag_class = -1;
  public int tag_value = -1;

  public int next_tag_class = -1;
  public int next_tag_number = -1;
  public boolean next_is_constructed = false;
  public boolean next_is_indefinite = false;
  public int next_length = -1;

  public boolean is_constructed = false;
  BufferedInputStream in = null;

  private base_codec codec_hint = null;  

  public String encoding = "US-ASCII";
  // private String encoding = "UTF-8";

  // Remember that logging statements in this module must be controlled by
  // the DebugFlag class in this package!
  private Log log = LogFactory.getLog(this.getClass());

  public BERInputStream(InputStream from)
  {
    this.in = new BufferedInputStream(from);
  }

  public BERInputStream(InputStream from, String encoding)
  {
    this.in = new BufferedInputStream(from);
    this.encoding=encoding;
  }

  public BERInputStream(InputStream from, String encoding, int buffsize)
  {
    this.in = new BufferedInputStream(from,buffsize);
    this.encoding=encoding;
  }

  // Methods from SerializationManager

  public int getDirection()
  {
    return DIRECTION_DECODE;
  }


  // Return length of tag encoding
  public int tag_codec(boolean is_constructed) throws java.io.IOException
  {
    // We expect to find the tag (tag_class, tag_value) on the input stream, if so, decode the length
    // octets also, if not, return -1

    if ( next_tag_class < 0 )
    {
      // First thing to to is to check that there is actually some more data in this PDU
      // So, if we are in the middle of decoding something.
      // The problem now is that if the last thing in a constructed type is an ASN NULL, then
      // there will be no data left in the constructed type after we have read the next tag and
      // length....
      if ( encoding_info.size() > 0 )
      {
        // But there is no more data
        if ( !moreData() )
        {
          if ( DebugFlag.debug_enabled )
           log.debug("tag_codec returning -1 whilst expecting ("+tag_class+","+tag_value+") because there is no more data in the constructed type");
          tag_class = -1;
          return -1;
        }
      }

      // Peek at the next tag
      decodeNextTag();
    }

    // debug("Looking for "+tag_class+" "+tag_value+" next = "+next_tag_class+" "+next_tag_number+"\n");

    if ( ( next_tag_class == tag_class ) &&
         ( next_tag_number == tag_value ) )
    {
      if ( DebugFlag.debug_enabled )
        log.debug("["+next_tag_class+","+next_tag_number+"] cons="+next_is_constructed+" len="+next_length+" is indef:"+next_is_indefinite);

      // Set up to read next tag class
      next_tag_class = -1;
      tag_class = -1;

      // We have a match, so return enc len
      return next_length;
    }
    else
    {
      // Did not find the expected tag, reset next tag
      tag_class = -1;
    }
    
    return -1; 
  }

  private void decodeNextTag() throws java.io.IOException
  {
    byte c = (byte)read();

    c &= 0xFF;
    next_tag_class = c & 0xC0;
    next_is_constructed = (c & 0x20) != 0;
 
    // System.err.println("First byte of tag is "+c);
    next_tag_number = c & 0x1F;
 
    // If there are multiple octets to encode the tag
    if (next_tag_number == 0x1F)
    {
      next_tag_number = 0;
      do {
        c = (byte)read();

        // Shift value 7 bits left
        next_tag_number = next_tag_number << 7;
 
        // Merge with the octets we just got
        next_tag_number = ( next_tag_number | ( c & 0x7F ) );
      } while ((c & 0x80) != 0);
    } 

    next_length = decodeLengthOctets();
  
    if ( DebugFlag.debug_enabled )
    {
      log.debug("[class:"+next_tag_class+" tag:"+next_tag_number+" len:"+next_length+"]");
    }
  }

  private int decodeLengthOctets() throws java.io.IOException
  {
    int datalen;
    byte lenpart = (byte)read();

    // System.err.println("First len octet is "+lenpart);

    if ((lenpart & 0x80) == 0)  // If bit 8 is 0
    {
      // Single octet length encoding
      // System.err.println("Single octet length encoding");
      datalen = lenpart;
      next_is_indefinite=false;
    }
    else if ( ( lenpart & 0x7F ) == 0 ) // Otherwise we are multiple octets (Maybe 0, which = indefinite)
    {
      // System.err.println("Indefinite length encoding");
      next_is_indefinite=true;
      datalen=0;
    }
    else
    {
      next_is_indefinite=false;
      // System.err.println("Multiple octet length encoding ("+(lenpart & 0x7F )+"octets)");
      lenpart &= 0x7F;
 
      datalen = 0;
      while (lenpart-- > 0)
        datalen = (datalen << 8) | ((byte)read() & 0xFF);
    }
 
    return datalen; 
  }

  public byte[] octetstring_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    byte[] retval = null;

    // Indefinite length encoding only allowed for constructed types, primitive length 0 must mean 
    // 0 contents octets
    if ( ( next_length == 0 ) && ( next_is_constructed ) )
    {
      if ( DebugFlag.debug_enabled )
        log.debug("Indefinite length encoding of octetstring");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      // StringWriter w = new StringWriter();

      byte current_octet = (byte)read();
      byte next_octet = (byte)read();

      while ( ( current_octet != 0 ) && ( next_octet != 0 ) )
      {
        baos.write(current_octet);

        current_octet = next_octet;
        next_octet = (byte)read();
      }

      retval = baos.toByteArray();
    }
    else
    {
      if ( DebugFlag.debug_enabled )
        log.debug("definite length encoding of octetstring ("+next_length+")");

      byte[] data = new byte[next_length];
      int bytes_left_to_read = next_length;
      int offset = 0;

      // We may need to call read repeatedly until we have all the data.
      while ( bytes_left_to_read > 0 )
      {
          int bytes_read = read(data,offset,bytes_left_to_read);
          bytes_left_to_read -= bytes_read;
          offset += bytes_read;
          if ( DebugFlag.debug_enabled )
            log.debug("Read "+bytes_read+" of "+next_length+" leaving "+bytes_left_to_read+" Next bytes will be at "+offset);
      }

      retval = data;
    }

    if ( DebugFlag.debug_enabled )
      log.debug("octetstring_codec returns byte array of length="+retval.length);

    return retval;
  }

  public Boolean boolean_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    Boolean retval = null;

    byte val = (byte)read();
 
    if ( val != 0x00)
      retval = Boolean.TRUE;
    else
      retval = Boolean.FALSE;

    // debug("boolean_codec returns "+retval+" length="+next_length+"\n");

    return retval;
  }

  public BigInteger integer_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    byte[] data = new byte[next_length];

    int bytes_left_to_read = next_length;
    int offset = 0;
 
    // We may need to call read repeatedly until we have all the data.
    while ( bytes_left_to_read > 0 )
    {
      int bytes_read = read(data,offset,bytes_left_to_read);
      bytes_left_to_read -= bytes_read;
      offset += bytes_read;
    }         

    return new BigInteger(data);   
  }

  public int[] oid_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    // System.err.println("Decoding OID, length = "+next_length);
 
    int[] retval = new int[next_length+1];
    byte[] decode_buffer = new byte[next_length];
    int pos=2;
 
    int bytes_left_to_read = next_length;
    int offset = 0;
 
    // We may need to call read repeatedly until we have all the data.
    while ( bytes_left_to_read > 0 )
    {
      int bytes_read = read(decode_buffer,offset,bytes_left_to_read);
      bytes_left_to_read -= bytes_read;
      offset += bytes_read;
    }         
 
    ByteArrayInputStream bais = new ByteArrayInputStream(decode_buffer);
 
    byte octet = (byte)bais.read();
 
    if ( octet >= 80 )
    {
        retval[0]=2;
        retval[1]=octet-80;
    }
    else if ( octet >= 40 )
    {
        retval[0]=1;
        retval[1]=octet-40;
    }
    else
    {
        retval[0]=0;
        retval[1]=octet;
    }
 
    // Split first octet into first 2 elements of OID
 
    while ( bais.available() > 0 )
    {
      retval[pos++] = decodeBase128Int(bais);
    }
 
    int[] result = new int[pos];
    System.arraycopy(retval,0,result,0,pos);

    // debug("oid_codec returns "+result+" length="+next_length+"\n");
 
    return result;
  }

  public byte[] any_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    byte[] data = null;

    if ( ( next_length > 0 ) && ( next_is_constructed ) )
    {
      // debug("definite length encoding of octetstring ("+next_length+")\n");
      data = new byte[next_length];
      int bytes_left_to_read = next_length;
      int offset = 0;
 
      // We may need to call read repeatedly until we have all the data.
      while ( bytes_left_to_read > 0 )
      {
          int bytes_read = read(data,offset,bytes_left_to_read);
          bytes_left_to_read -= bytes_read;
          offset += bytes_read;
          // debug("Read "+bytes_read+" of "+next_length+" leaving "+bytes_left_to_read+" Next bytes will be at "+offset);
      }
    }
    else if ( next_length == 0 )
    {
      // Indefinite length encoding
      // debug("Indefinite length encoding of any data....");
      StringWriter w = new StringWriter();
 
      byte current_octet = (byte)read();
      byte next_octet = (byte)read();
 
      while ( ( current_octet != 0 ) && ( next_octet != 0 ) )
      {
        w.write(current_octet);
 
        current_octet = next_octet;
        next_octet = (byte)read();
      }

      data = w.toString().getBytes();
    }
    else
      throw new java.io.IOException("Problem decoding any");

    // debug("any returns ... length="+next_length+"\n");

    return data;
  }

  public AsnBitString bitstring_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    AsnBitString abs = (AsnBitString)instance;

    // debug("Bitstring codec...");

    int unused = read();

    if ( next_length > 0 )
    {
      byte[] data = new byte[next_length-1];

      data = new byte[next_length-1];
      int bytes_left_to_read = next_length-1;
      int offset = 0;
 
      // We may need to call read repeatedly until we have all the data.
      while ( bytes_left_to_read > 0 )
      {
          int bytes_read = read(data,offset,bytes_left_to_read);
          bytes_left_to_read -= bytes_read;
          offset += bytes_read;
      }         

      abs = new AsnBitString(data, unused);

      // debug("got "+next_length+" bytes of bitstring with "+unused+" unused bits at end\n");
    }

    // debug("bitstring returns ... length="+next_length+"\n");

    return abs;
  }

  public AsnNull null_codec(Object instance, boolean is_constructed) throws java.io.IOException
  {
    AsnNull retval = null;

    // No contents octets allowed for null encoding
    if ( next_length != 0 )
    {
      throw new java.io.IOException("Unexpected length encoding of null");
    }

    return new AsnNull();
  }

  public Object choice(Object current_instance, Object[][] choice_info, String name) throws java.io.IOException
  {
    ChoiceType retval = (ChoiceType)current_instance;
    if ( DebugFlag.debug_enabled )
      log.debug("1.... choice_codec ("+name+") current = "+retval+" number of options="+choice_info.length);
    Object result = null;

    for ( int i=0;  ( ( i< choice_info.length ) && ( result == null ) ) ;i++ )
    {
      Integer tagmode = (Integer)(choice_info[i][0]);
      Integer tagclass = (Integer)(choice_info[i][1]);
      Integer tagnumber = (Integer)(choice_info[i][2]);
      base_codec codec_to_use = ((base_codec)(choice_info[i][3]));

      if ( DebugFlag.debug_enabled )
        log.debug("choice Trying ["+i+"] : "+tagmode+" "+tagclass+" "+tagnumber+" "+codec_to_use);

      if (  tagmode.equals(SerializationManager.TAGMODE_NONE ) )
      {
        result = codec_to_use.serialize(this, result, true, ((String)(choice_info[i][4])));
      }
      else
      {
        if ( tagmode.equals(SerializationManager.IMPLICIT ) )
        {
          // Implicit Tagging
          if ( DebugFlag.debug_enabled )
            log.debug("implicit  tagging, so simply calling codec for "+((String)(choice_info[i][4])));

          result = implicit_tag(codec_to_use, result, tagclass.intValue(), tagnumber.intValue(), true,  ((String)(choice_info[i][4])));
        }
        else
        {
          if ( DebugFlag.debug_enabled )
            log.debug("explicit tagging, so simply calling codec for "+((String)(choice_info[i][4])));

          if ( constructedBegin(tagclass.intValue(), tagnumber.intValue()) )
          {
            result = codec_to_use.serialize(this, result, false, ((String)(choice_info[i][4])));
            constructedEnd();
          }
        }
      }

      if ( result != null )
      {
        if ( DebugFlag.debug_enabled )
          log.debug("Choice codec matched on choice "+i);
        retval.o = result;
        retval.which = i;
      }
    }

    if ( result == null )
      retval = null;

    return retval;
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

  public boolean sequenceEnd() throws java.io.IOException
  {
    return constructedEnd();
  }

  public boolean constructedBegin(int tagclass, int tagnumber)  throws java.io.IOException
  {
    if ( tag_class < 0 )
    {
      tag_class = tagclass;
      tag_value = tagnumber;
    }

    if ( tag_codec(true) >= 0 )
    {
      // Entering a constructed block
      if ( DebugFlag.debug_enabled )
      {
        log.debug("CONS ["+encoding_info.size()+"] ("+tag_class+","+tag_value+") "+tagclass+" len="+next_length);
        log.debug("{");
      } 
      CodecStackInfo csi = new CodecStackInfo();
      csi.content_length = next_length;
      csi.bytes_processed = 0;
      csi.is_constructed = next_is_constructed;
      csi.is_indefinite_length = next_is_indefinite;
      encoding_info.push(csi); 
      return true;
    }

    return false;
  }

  public boolean constructedEnd() throws java.io.IOException
  {
    CodecStackInfo csi = (CodecStackInfo)encoding_info.pop();
    // We now need to add any bytes read on to the bytes processed total for the next 
    // constructed item in the stack

    if ( DebugFlag.debug_enabled )
    {
      log.debug("}");
    } 

    // If we are closing an indefinite length encoding, consume the terminating octets
    // if ( csi.content_length == 0 )
    if ( csi.is_indefinite_length )
    {
      // debug("Reading indefinite length terminating octets\n");
      byte b1 = (byte)read();
      byte b2 = (byte)read();
      
      if ( ( b1 == 0 ) && ( b2 == 0 ) )
      {
        next_tag_class=-1;
        tag_class=-1;
      }
      else
      {
        throw new java.io.IOException("Expected indefinite length terminating octets for constructed type, found other values");
      }
    }

    if ( encoding_info.size() > 0 )
    {
      CodecStackInfo curr = (CodecStackInfo)encoding_info.peek();

      curr.bytes_processed += csi.bytes_processed;
    }

    // debug("Constructed End ["+encoding_info.size()+"] ("+csi.bytes_processed+" bytes)\n");
    return true;
  }

  public Object implicit_tag(base_codec c, Object current_instance, int tag_class, int tag_number, boolean is_optional, String name) throws java.io.IOException
  {
    Object retval = null;
 
    if ( DebugFlag.debug_enabled )
      log.debug("implicit_tag "+tag_class+","+tag_number+" "+name);

    implicit_settag(tag_class, tag_number);
    // if ( tag_codec(false) > 0 )
    // {
    retval = c.serialize(this, current_instance, is_optional, name);
    // }

    return retval;
  }

  public Object explicit_tag(base_codec c, Object current_instance, int tag_class, int tag_number, boolean is_optional, String name) throws java.io.IOException
  {
    Object retval = current_instance;

    // debug("explicit_tag "+tag_class+","+tag_number+" "+name+"\n");

    if ( constructedBegin(tag_class, tag_number) )
    {
      retval = c.serialize(this, retval, is_optional, name);
      constructedEnd();
    }

    return retval;
  }

  public Vector sequenceOf(Vector v, base_codec codec) throws java.io.IOException
  {
    if ( v != null )
    {
      while ( moreData() )
      {
        Object item_to_add =  codec.serialize(this, null, true, "SequenceOf item");
        if ( item_to_add == null )
          throw new java.io.IOException("Error expecting member of sequenceOf");
        v.add ( item_to_add );
      }
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

  // Override default read method with octet counting for constructed members
  public int read() throws java.io.IOException
  {
    int retval = in.read();

    if ( retval == -1 )
      throw new java.io.IOException("Connection Closed");

    if ( encoding_info.size() > 0 )
    {
      // Get head off stack and add 1 to bytes processed
       CodecStackInfo csi = (CodecStackInfo)encoding_info.peek();
       csi.bytes_processed += 1;
    }

    return retval;
  }

  public int read(byte[] buffer, int offset, int max) throws java.io.IOException
  {
    int retval = in.read(buffer, offset, max);

    if ( retval == -1 )
      throw new java.io.IOException("Connection Closed");

    if ( encoding_info.size() > 0 )
    {
      // Get head off stack and add retval to bytes processed
       CodecStackInfo csi = (CodecStackInfo) encoding_info.peek();
       csi.bytes_processed += retval;
    }

    return retval;
  }


  // This function should NEVER consume the 2 octets that terminate indefinite length encoding...
  // That should be left to the thing encoding the sequence... This func just takes a peek at the
  // next octets to see what's to come...
  public boolean moreData() throws java.io.IOException
  {
    // Get the current top of the encoding stack and compare it's content_length
    // with the current decode position
    // debug("moreData called, stack="+encoding_info.size()+"\n");

    if ( encoding_info.size() > 0 )
    {
      CodecStackInfo csi = (CodecStackInfo) encoding_info.peek();
 
      if ( DebugFlag.debug_enabled )
        log.debug ("moreData() Content length="+ csi.content_length+
                   ", bytes_processed="+csi.bytes_processed+
                   ", constructed="+csi.is_constructed+
                   ", indefinite length encoding ="+csi.is_indefinite_length);

      if ( csi.content_length > 0 )
      {
        // debug("MoreData comparing "+csi.bytes_processed+" < "+ csi.content_length+"\n");
        if ( csi.bytes_processed < csi.content_length )
          return true;
        else
          return false;
      }
      else if ( csi.is_indefinite_length )
      {
        // Indefinite length encodings are terminated by 00
        // debug("MoreData... Indefinite length encoding, so check for terminating octets\n");
        in.mark(5);
        int i1 = in.read();
        int i2 = in.read();
        in.reset();
        if ( ( i1 == 0 ) && ( i2 == 0 ) )
        {
          // debug("MoreData... false ( Next octets are 00 )\n");
          // csi.bytes_processed += 2;
          return false;
        }
        else
        {
          // debug("MoreData... true because next 2 octets are not 00: "+i1+" and "+i2+"\n");
          // in.reset();
          return true;
        }
      }
    }

    return false;
  }

  private int decodeBase128Int(InputStream ins) throws java.io.IOException
  {
    int retval = 0;
    byte octet = (byte)128;
    while ( ( octet & 128 ) == 128 )
    {
      octet = (byte)ins.read();
      retval = ( ( retval << 7 ) | ( octet & 127 ) );
    }
 
    return retval;
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
