package com.k_int.gen.AsnUseful;

// For logging
import java.math.BigInteger;
import com.k_int.codec.runtime.*;

public class encoding_inline0_codec extends base_codec
{
  public static encoding_inline0_codec me = null;


  private static Object[][] choice_info = { 
    { SerializationManager.EXPLICIT,new Integer(128), new Integer(0), Any_codec.getCodec() , "single_ASN1_type", new Integer(0) },
    { SerializationManager.IMPLICIT, new Integer(128), new Integer(1), OctetString_codec.getCodec() , "octet_aligned", new Integer(1) },
    { SerializationManager.IMPLICIT, new Integer(128), new Integer(2), BitString_codec.getCodec() , "arbitrary", new Integer(2) }
  };

  public synchronized static encoding_inline0_codec getCodec()
  {
    if ( me == null )
    {
      me = new encoding_inline0_codec();

    }
    return me;
  }

  public Object serialize(SerializationManager sm,
                          Object type_instance,
                          boolean is_optional,
                          String type_name) throws java.io.IOException
  {
    encoding_inline0_type retval = (encoding_inline0_type)type_instance;

      if ( sm.getDirection() == SerializationManager.DIRECTION_DECODE )
      {
        retval = new encoding_inline0_type();
        retval = (encoding_inline0_type)sm.choice(retval, choice_info, type_name);
      }
      else
      {
        if ( retval != null )
          sm.choice(retval, choice_info, type_name);
      }

      if ( ( retval==null ) && ( !is_optional ) )
      {
        throw new RuntimeException("Missing mandatory choice for "+type_name);
      }

    return retval;
  }

}
