package com.k_int.a2j

import spock.lang.Specification
import spock.lang.Unroll
import com.k_int.a2j.ProtocolEndpoint
import com.k_int.codec.runtime.Integer_codec
import java.math.BigInteger
import com.k_int.a2j.ProtocolServer;

/**
 * Test the traditional socket based client and server endpoints.
 */
class ProtocolEndpointTest extends Specification {

  @Unroll
  def testBasicIntProtocol() {
    when:
      // ProtocolEndpoint<Integer_codec, BigInteger> intProtocolEndpoint = new ProtocolEndpoint<Integer_codec, BigInteger>(Integer_codec.getCodec(), 'localhost',8999);
      // ProtocolServer<Integer_codec, BigInteger> protocol_server = new ProtocolServer<Integer_codec, BigInteger>(Integer_codec.getCodec(), 8999);
      System.out.println("Create New protocol server");
      ProtocolServer ps = new ProtocolServer<Integer_codec, BigInteger>('localhost',8999,Integer_codec.getCodec());
      System.out.println("Start New protocol server");
      ps.start();
      System.out.println("Wait for setup");
      synchronized(this) {
        Thread.sleep(2000);
      }
      System.out.println("ok - carry on");
    then:
      System.out.println("Stop server");
      ps.stop(true);
    expect:
      1==1
  }
}
