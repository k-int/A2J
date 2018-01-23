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

public class ProtocolServer {

  private Selector selector;
  private Map<SocketChannel,List> dataMapper;
  private InetSocketAddress listenAddress;
  private int server_status = 0; // 0=Idle, 1=Started, 2=Stop Requested, 3=Stopped
  private Runnable server = null;
    
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

  public ProtocolServer(String address, int port) throws IOException {
    listenAddress = new InetSocketAddress(address, port);
    dataMapper = new HashMap<SocketChannel,List>();
  }

  // create server channel  
  private void startServer() throws IOException {

    System.out.println("startServer()");

    this.selector = Selector.open();
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking(false);

    // retrieve server socket and bind to port
    serverChannel.socket().bind(listenAddress);
    serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

    System.out.println("Server started...");

    while (server_status==1) {
      // wait for events
      System.out.println("selector.select()");
      this.selector.select();

      //work on selected keys
      Iterator keys = this.selector.selectedKeys().iterator();
      while (keys.hasNext()) {
        SelectionKey key = (SelectionKey) keys.next();

        // this is necessary to prevent the same key from coming up 
        // again the next time around.
        keys.remove();

        if (!key.isValid()) {
          continue;
        }

        if (key.isAcceptable()) {
          this.accept(key);
        }
        else if (key.isReadable()) {
          this.read(key);
        }
      }
    }
    System.out.println("startServer complete");
    synchronized (this) {
      server_status=3;
      notifyAll();
    }
  }

  //accept a connection made to this channel's socket
  private void accept(SelectionKey key) throws IOException {
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
    SocketChannel channel = serverChannel.accept();
    channel.configureBlocking(false);
    Socket socket = channel.socket();
    SocketAddress remoteAddr = socket.getRemoteSocketAddress();
    System.out.println("Connected to: " + remoteAddr);

    // register channel with selector for further IO
    dataMapper.put(channel, new ArrayList());
    channel.register(this.selector, SelectionKey.OP_READ);
  }
    
  //read from the socket channel
  private void read(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    int numRead = -1;
    numRead = channel.read(buffer);

    if (numRead == -1) {
      this.dataMapper.remove(channel);
      Socket socket = channel.socket();
      SocketAddress remoteAddr = socket.getRemoteSocketAddress();
      System.out.println("Connection closed by client: " + remoteAddr);
      channel.close();
      key.cancel();
      return;
    }

    byte[] data = new byte[numRead];
    System.arraycopy(buffer.array(), 0, data, 0, numRead);
    System.out.println("Got: " + new String(data));
  }
}

