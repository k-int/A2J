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
      logger.debug("Create New protocol server");

      ProtocolServer ps = new ProtocolServer<Integer_codec, BigInteger>(8999, Integer_codec.getCodec());

      logger.debug("Start New protocol server");
      ps.start();

      logger.debug("Wait for setup");
      synchronized(this) { Thread.sleep(2000); }

      logger.debug("ok - carry on");

    then:
      logger.debug("Create client");
      java.net.Socket client_socket = new java.net.Socket(java.net.InetAddress.getByName('localhost'),8999);
      ProtocolAssociation client = new ProtocolAssociation<Integer_codec, BigInteger>(client_socket,Integer_codec.getCodec(),'ClientAssociation');

      // Start client thread
      client.start()

      logger.debug("Send int value 1002");
      client.send(new BigInteger(1002));

      logger.debug("Close client");
      client.close();

      synchronized(this) { Thread.sleep(2000); }

      logger.debug("Shutdown protocol server");
      ps.stop(true);

    expect:
      1==1
  }
}
