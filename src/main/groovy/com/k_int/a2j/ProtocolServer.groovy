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

public class ProtocolServer<RootCodecClass, RootTypeClass> {

  private int socket_timeout = 300000;  // 300 second default timeout
  private int server_status = 0; // 0=Idle, 1=Started, 2=Stop Requested, 3=Stopped
  private int server_port = 0;
  private Runnable server = null;
  private RootCodecClass root_codec;

  public ProtocolServer(int port, RootCodecClass root_codec) throws IOException {
    root_codec = root_codec;
    this.server_port = port;
  }

  public void start() throws Exception {

    System.out.println("ProtocolServer::start");

    server = new Runnable() {
      @Override
      public void run() {
        try {
          server_status = 1;
          System.out.println("About to call startServer");
          startServer();
          System.out.println("startServer done");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };

    new Thread(server).start()

    System.out.println("ProtocolServer::start returning");
  }

  public void stop(boolean wait_for_finish) {

    System.out.println("stop requested, currently "+server_status);

    if ( server_status == 1 ) {
      server_status=2;

      if ( this.selector ) {
        System.out.println("wake up selector");
        this.selector.wakeup();
      }

      if ( wait_for_finish ) {
        System.out.println("Wait for server status 3, currently "+server_status);
        while(server_status!=3) {
          synchronized(this) {
            this.wait();
          }
        }
      }
    }
  }

  // create server channel  
  private void startServer() throws IOException {
    server_socket = new ServerSocket(server_port);
    try {
      while ( server_status==1 ) {
        cat.debug("Waiting for connection");
        Socket socket = (Socket)server_socket.accept();

        if ( socket_timeout > 0 )
          socket.setSoTimeout(socket_timeout);

        ProtocolAssociation pa = new ProtocolAssociation<RootCodecClass,RootTypeClass>(socket,root_codec)
      }

      server_socket.close();
    }
    catch (java.io.IOException e)
    {
      e.printStackTrace();
    }

  }

}

