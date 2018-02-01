package com.k_int.a2j

import spock.lang.Specification
import spock.lang.Unroll
import com.k_int.a2j.ProtocolEndpoint
import com.k_int.codec.runtime.Integer_codec
import java.math.BigInteger
import com.k_int.a2j.ProtocolServer;
import com.k_int.a2j.ProtocolAssociation;
import com.k_int.a2j.ProtocolAssociationObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the traditional socket based client and server endpoints.
 */
class ProtocolEndpointTest extends Specification {

  final static Logger logger = LoggerFactory.getLogger(ProtocolEndpointTest.class);

  @Unroll
  def testBasicIntProtocol() {

    List received_apdus = new java.util.ArrayList();

    setup:
      logger.debug("Create New protocol server");

  
      // Create a new protocol association observer that will collect all incoming APDUs (BigInts in this case)
      ProtocolAssociationObserver pao = new ProtocolAssociationObserver<BigInteger>() {
        public void notify(ProtocolAssociation pa, BigInteger apdu) {
          logger.debug("**** Incoming ${apdu} from ${pa}");
          synchronized(received_apdus) {
            received_apdus.add(apdu)
            received_apdus.notifyAll()
          }
        }
      }

      // Override the default protocol association with one that notifies our observer above
      ProtocolAssociationFactory paf = new ProtocolAssociationFactory<Integer_codec,BigInteger>() {
        public ProtocolAssociation create(Socket socket,
                                          Integer_codec root_codec,
                                          String association_name) {
          ProtocolAssociation result = null;
          result = new ProtocolAssociation<Integer_codec,BigInteger>(socket,root_codec,'ServerAssociation')
          result.setObserver(pao);
          return result;
        }
      }

      // Create a new protocol server listening on port 8999i for encoded integer values, 
      // and customise the protocol association factory
      // so that we pass our observer to all associations.
      ProtocolServer ps = new ProtocolServer<Integer_codec, BigInteger>(
               8999, 
               Integer_codec.getCodec(),
               paf);

      logger.debug("Start New protocol server");
      ps.start();
      logger.debug("ok - carry on Size of received_apdus is ${received_apdus.size()}");

    when:
      // Create a client capable of transmitting big integers and send the value 1002 to the server using
      // that client.
      logger.debug("Create client");
      java.net.Socket client_socket = new java.net.Socket(java.net.InetAddress.getByName('localhost'),8999);
      ProtocolAssociation client = new ProtocolAssociation<Integer_codec, BigInteger>(client_socket,Integer_codec.getCodec(),'ClientAssociation');

      // Start client thread
      client.start()

      logger.debug("Send int value 1002");
      client.send(new BigInteger(1002));

      // All done, close client
      logger.debug("Close client");
      client.close();

    then:
      // Wait to see if the value arrives on the server
      synchronized(received_apdus) {
        logger.debug("Waiting for received_apdus to be 1: ${received_apdus.size()}");
        while ( received_apdus.size() != 1 ) {
          received_apdus.sleep(10000);
        }
      }
      logger.debug("There is 1 APDU waiting.. Check it has the correct value");

    expect:
      // That the value we received is the value we sent
      received_apdus.get(0).equals(new BigInteger(1002));

    cleanup:
      logger.debug("Shutdown protocol server (And wait for it to complete)");
      ps.stop(true);
  }
}
