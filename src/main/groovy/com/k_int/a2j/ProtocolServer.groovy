package com.k_int.a2j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProtocolServer<RootCodecClass, RootTypeClass> {

  private int socket_timeout = 300000;  // 300 second default timeout
  private int server_status = 0; // 0=Idle, 1=Started, 2=Stop Requested, 3=Stopped
  private int server_port = 0;
  private Runnable server = null;
  private RootCodecClass root_codec;
  private ServerSocket server_socket = null;

  final static Logger logger = LoggerFactory.getLogger(ProtocolServer.class);

  /**
   * The factory we will use to create new protocol associations. By default we create a vanialla
   * protocol association that does nothing special.
   */
  private ProtocolAssociationFactory paf = null;

  public ProtocolServer(int port, 
                        RootCodecClass root_codec) throws IOException {
    this.root_codec = root_codec;
    this.server_port = port;

    // Construct a default protocol association factory, no subclassing of APDU handling etc
    this.paf = new ProtocolAssociationFactory() {
      public ProtocolAssociation create(Socket socket,
                                        RootCodecClass pa_root_codec,
                                        String association_name) {
        return new ProtocolAssociation<RootCodecClass,RootTypeClass>(socket,pa_root_codec,'ServerAssociation')
      }

    }
  }

  public ProtocolServer(int port,
                        RootCodecClass root_codec,
                        ProtocolAssociationFactory paf) throws IOException {
    this.root_codec = root_codec;
    this.server_port = port;
    this.paf = paf;
  }

  public void start() throws Exception {

    logger.debug("ProtocolServer::start");

    server = new Runnable() {
      @Override
      public void run() {
        try {
          server_status = 1;
          logger.debug("About to call startServer");
          startServer();
          logger.debug("startServer done");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };

    new Thread(server).start()

    logger.debug("ProtocolServer::start returning");
  }

  public void stop(boolean wait_for_finish) {

    logger.debug("stop requested, currently "+server_status);

    if ( server_status == 1 ) {
      server_status=2;

      if ( server_socket)
        server_socket.close();

      if ( wait_for_finish ) {
        while(server_status!=3) {
          logger.debug('Wait for server status 3, currently '+server_status);
          synchronized(this) {
            this.wait();
          }
        }
        logger.debug('Server reached status 3 - Socket accept thread terminating');
      }
    }
  }

  // create server channel  
  private void startServer() throws IOException {
    server_socket = new ServerSocket(server_port);
    try {
      while ( server_status==1 ) {
        logger.debug("Waiting for connection");
        Socket socket = (Socket)server_socket.accept();

        logger.debug("Accepting new server socket");

        if ( socket_timeout > 0 )
          socket.setSoTimeout(socket_timeout);

        logger.debug("Create new protocol association for new socket connection");
        // ProtocolAssociation pa = new ProtocolAssociation<RootCodecClass,RootTypeClass>(socket,root_codec,'ServerAssociation')
        ProtocolAssociation pa = this.paf.create(socket,root_codec,'ServerAssociation');

        assert pa != null

        logger.debug("Starting protocol association (server) and returning to accept loop");
        pa.start()
      }
    }
    catch ( java.net.SocketException se) {
      if ( se.message?.equals('Socket closed') ) {
        logger.debug('Server socket closed, shutown');
      }
      else {
        logger.info("Socket exception",se);
      }
    }
    catch (java.io.IOException e) {
      logger.error("Unexpected IO Exception",e);
    }
    catch ( Exception e ) {
      logger.error("Unexpected Exception",e);
    }

    server_status = 3;
    synchronized(this) {
      this.notifyAll();
    }
  }

}

