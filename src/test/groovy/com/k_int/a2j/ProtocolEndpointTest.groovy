package com.k_int.a2j

import spock.lang.Specification
import spock.lang.Unroll
import com.k_int.a2j.ProtocolEndpoint
import com.k_int.codec.runtime.Integer_codec
import java.math.BigInteger
import com.k_int.a2j.ProtocolServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the traditional socket based client and server endpoints.
 */
class ProtocolEndpointTest extends Specification {

  final static Logger logger = LoggerFactory.getLogger(ProtocolEndpointTest.class);

  @Unroll
  def testBasicIntProtocol() {
    when:
      // ProtocolEndpoint<Integer_codec, BigInteger> intProtocolEndpoint = new ProtocolEndpoint<Integer_codec, BigInteger>(Integer_codec.getCodec(), 'localhost',8999);
      // ProtocolServer<Integer_codec, BigInteger> protocol_server = new ProtocolServer<Integer_codec, BigInteger>(Integer_codec.getCodec(), 8999);
      logger.debug("Create New protocol server");
      ProtocolServer ps = new ProtocolServer<Integer_codec, BigInteger>(8999,Integer_codec.getCodec());
      logger.debug("Start New protocol server");
      ps.start();
      logger.debug("Wait for setup");
      synchronized(this) {
        Thread.sleep(2000);
      }
      logger.debug("ok - carry on");
    then:
      logger.debug("Stop server");
      ps.stop(true);
    expect:
      1==1
  }
}
