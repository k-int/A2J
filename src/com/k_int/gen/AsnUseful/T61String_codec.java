package com.k_int.gen.AsnUseful;

// For logging
import java.math.BigInteger;
import com.k_int.codec.runtime.*;

public class T61String_codec extends base_codec
{
  public static T61String_codec me = null;
  private OctetString_codec i_octetstring_codec = OctetString_codec.getCodec();

  public synchronized static T61String_codec getCodec()
  {
    if ( me == null )
    {
      me = new T61String_codec();
    }
    return me;
  }

  public Object serialize(SerializationManager sm,
                          Object type_instance,
                          boolean is_optional,
                          String type_name) throws java.io.IOException
  {
    String retval = (String)type_instance;
 
    boolean is_constructed = false;
 
    if ( sm.getDirection() == SerializationManager.DIRECTION_ENCODE )
    {
      sm.implicit_tag(i_octetstring_codec, 
                      retval != null ? retval.getBytes(sm.getCharsetEncoding()) : null,
                      0, 20, is_optional, "T61String");
    }
    else if ( sm.getDirection() == SerializationManager.DIRECTION_DECODE )
    {
      byte[] octet_str = (byte[]) sm.implicit_tag(i_octetstring_codec, null, 0, 20, is_optional, "T61String");
      if ( octet_str != null )
        retval = new String(octet_str, sm.getCharsetEncoding());
    }
 
    if ( ( retval == null ) && ( ! is_optional ) )
      throw new java.io.IOException("Missing mandatory member: "+type_name);
 
    return retval;
  }
}
