package com.k_int.a2j

import spock.lang.Specification
import spock.lang.Unroll
// import com.k_int.a2j.ProtocolEndpoint

class EncodingTest extends Specification {

    @Unroll
    def testBasicIntEncoding() {
      when:
        com.k_int.codec.runtime.BEROutputStream ber_os = new com.k_int.codec.runtime.BEROutputStream();
        def expected_encoding = [1] as byte[]
      then:
        ber_os.encodeLength(1);
        byte[] encoding = ber_os.toByteArray();
      expect:
        encoding == expected_encoding;
    }

    @Unroll
    def testLongIntEncoding() {
      when:
        com.k_int.codec.runtime.BEROutputStream ber_os = new com.k_int.codec.runtime.BEROutputStream();
        def expected_encoding = [-125, 15, 66, 63] as byte[]
      then:
        ber_os.encodeLength(999999 as int);
        byte[] encoding = ber_os.toByteArray();
      expect:
        encoding == expected_encoding;
    }

    @Unroll
    def testBigIntegerEncoding() {
      when:
        com.k_int.codec.runtime.BEROutputStream ber_os = new com.k_int.codec.runtime.BEROutputStream();
        def expected_encoding = [3, 18, -42, -121] as byte[]
      then:
        ber_os.integer_codec(new BigInteger(1234567),false)
        byte[] encoding = ber_os.toByteArray();
      expect:
        encoding == expected_encoding;
    }


}
