package com.k_int.gen.AsnUseful;

import java.math.BigInteger;
import com.k_int.codec.runtime.*;
import com.k_int.codec.util.*;

public class EXTERNAL_codec extends base_codec
{
  public static EXTERNAL_codec me = null;

  private ObjectDescriptor_codec i_objectdescriptor_codec = ObjectDescriptor_codec.getCodec();
  private encoding_inline0_codec i_encoding_inline0_codec = encoding_inline0_codec.getCodec();
  private OID_codec i_oid_codec = OID_codec.getCodec();
  private Integer_codec i_integer_codec = Integer_codec.getCodec();

  public synchronized static EXTERNAL_codec getCodec()
  {
    if ( me == null )
    {
      me = new EXTERNAL_codec();
    }
    return me;
  }

  public Object serialize(SerializationManager sm,
                          Object type_instance,
                          boolean is_optional,
                          String type_name) throws java.io.IOException
  {
    EXTERNAL_type retval = (EXTERNAL_type)type_instance;

    sm.implicit_settag(0, 8);
    if ( sm.sequenceBegin() )
    {

      if ( sm.getDirection() == SerializationManager.DIRECTION_DECODE )
      {
          retval = new EXTERNAL_type();
      }

      retval.direct_reference = (int[])i_oid_codec.serialize(sm, retval.direct_reference,true, "direct_reference");
      retval.indirect_reference = (BigInteger)i_integer_codec.serialize(sm, retval.indirect_reference,true, "indirect_reference");
      retval.data_value_descriptor = (java.lang.String)i_objectdescriptor_codec.serialize(sm, retval.data_value_descriptor,true, "data_value_descriptor");

      if ( null != retval.direct_reference )
      {
        OIDRegister reg = OIDRegister.getRegister(); 
        OIDRegisterEntry e = reg.lookupByOID(retval.direct_reference);

        if ( null != e )
        {
          // We know what should be in the Any that follows as a part of the encoding... Excellent.
          sm.setHintCodec((base_codec)(e.getHandler()));
        }
      }

      retval.encoding = (encoding_inline0_type)i_encoding_inline0_codec.serialize(sm, retval.encoding,false, "encoding");
      sm.sequenceEnd();
    }

    return retval;
  }

}
