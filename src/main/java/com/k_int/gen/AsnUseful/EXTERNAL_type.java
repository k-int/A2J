package com.k_int.gen.AsnUseful;

import java.math.BigInteger;
import java.util.Vector;
import com.k_int.codec.runtime.*;
import java.io.Serializable;

public class EXTERNAL_type implements Serializable
{
    public int[] direct_reference = null;
    public BigInteger indirect_reference = null;
    public java.lang.String data_value_descriptor = null;
    public encoding_inline0_type encoding = null;

    public EXTERNAL_type(int[] direct_reference,
                         BigInteger indirect_reference,
                         java.lang.String data_value_descriptor,
                         encoding_inline0_type encoding)
    {
      this.direct_reference = direct_reference;
      this.indirect_reference = indirect_reference;
      this.data_value_descriptor = data_value_descriptor;
      this.encoding = encoding;
    }

    public EXTERNAL_type() {}
}
