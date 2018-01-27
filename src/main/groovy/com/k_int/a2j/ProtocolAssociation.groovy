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
import com.k_int.codec.runtime.BERInputStream
import com.k_int.codec.runtime.BEROutputStream
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProtocolAssociation<RootCodecClass, RootTypeClass> extends Thread {

  private int server_status = 0; // 0=Idle, 1=Started, 2=Stop Requested, 3=Stopped
  private Runnable server = null;
  private Socket socket = null;
  private RootCodecClass root_codec;
  private InputStream incoming_data = null;
  private OutputStream outgoing_data = null;

  /** The default buffer size for BER send and recieve streams */
  private static final int DEFAULT_BUFF_SIZE = 32768;

  public static final String US_ASCII_ENCODING = "US-ASCII";
  public static final String UTF_8_ENCODING = "UTF-8";
  public static final String UTF_16_ENCODING = "UTF-16";
  private String charset_encoding = US_ASCII_ENCODING;

  final static Logger logger = LoggerFactory.getLogger(ProtocolAssociation.class);


  
  public ProtocolAssociation(Socket socket, RootCodecClass root_codec) throws IOException {
    this.root_codec = root_codec;
    this.socket = socket;
    try {
      incoming_data = socket.getInputStream();
      outgoing_data = socket.getOutputStream();
    }
    catch( java.io.IOException e ) {
      cat.error("Error constructing TargetEndpoint",e);
    }

  }

  public void run() {

    while(server_status==1) {
      BERInputStream bds = new BERInputStream(incoming_data,charset_encoding);
      try {
        RootTypeClass apdu = null;
        apdu = (RootTypeClass)root_codec.serialize(bds, apdu, false, "PDU");
        receive(apdu);
      }
      catch ( java.io.InterruptedIOException iioe ) {
        cat.error("Processing java.io.InterruptedIOException, shut down association",iioe);
        running = false;
        try {
          //sendClose(0, "Session Timeout");
        }
        catch ( java.io.IOException ioe ) {
          // Don't worry, the peer might just have got the close PDU and shut things down ahead of us...
        }
        // notifyClose();
        // No need to close socket, but we should notify all listeners...
      }
      catch ( java.io.IOException ioe ) {
        // Client snapped connection somehow...
        if(ioe.getMessage().equals("Connection Closed")) {
          cat.debug("Connection Closed");
        }
        else {
          cat.error("Processing java.io.IOException, shut down association", ioe);
        }
        running = false;
        notifyClose();
      }
      catch ( Exception e ) {
        cat.error("Processing exception : ",e);
        e.printStackTrace();
        running=false;
      }
    }
  }

  public void send(RootTypeClass apdu) {
    logger.debug("send");
    BEROutputStream encoder = new BEROutputStream(DEFAULT_BUFF_SIZE,charset_encoding);
    root_codec.serialize(encoder, apdu, false, "PDU");
    encoder.flush();
    encoder.writeTo(outgoing_data);
    outgoing_data.flush();
    logger.debug("Sending APDU complete");
  }

  public receive(RootTypeClass apdu) {
    logger.debug("incoming APDU");
  }
}

