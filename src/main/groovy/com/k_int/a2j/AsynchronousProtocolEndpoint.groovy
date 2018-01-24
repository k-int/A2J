package com.k_int.a2j;

// for OID Register
import com.k_int.codec.util.*;

// Basic imports
import java.io.*;
import java.net.*;
import java.util.*;
import com.k_int.gen.AsnUseful.*;
import java.math.BigInteger;
import com.k_int.codec.runtime.*;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;


public class AsynchronousProtocolEndpoint<RootCodecClass, RootTypeClass>  extends Thread {

  private RootCodecClass codec = null;
  private OIDRegister reg = OIDRegister.getRegister();
  private SocketChannel protocol_association = null;


  private static final int ASSOC_STATUS_IDLE=0;
  private static final int ASSOC_STATUS_CONNECTING=1;
  private static final int ASSOC_STATUS_CONNECTED=2;
  private static final int ASSOC_STATUS_PERM_FAILURE=3;

  byte[] data = null;

  public AsynchronousProtocolEndpoint(RootCodecClass codec, SocketChannel s) {
    this.codec = codec;
    protocol_association = s;
    // bds = new BERInputStream(incoming_data, charset_encoding,DEFAULT_BUFF_SIZE);
  }

  public synchronized int notifyIncomingData() {
    ByteBuffer buffer = ByteBuffer.allocate(16384);
    int numRead = -1;
    numRead = protocol_association.read(buffer);

    if (numRead == -1) {
      this.dataMapper.remove(channel);
      Socket socket = channel.socket();
      SocketAddress remoteAddr = socket.getRemoteSocketAddress();
      System.out.println("Connection closed by client: " + remoteAddr);
      channel.close();
    }
    else {

      if ( data == null ) {
        // Add the data to the current byte buffer.
        data = new byte[numRead];
      }
      else {
        // Need to extend the current data and append the newly read data
      }

      System.arraycopy(buffer.array(), 0, data, 0, numRead);
      System.out.println("Got: " + new String(data));
      // CompleteAPUQueue.append(data)
      // if CompleteAPDUQueue.hasCompleteAPDU()
      // 
      
      
    }

    return numRead;
  }
}

