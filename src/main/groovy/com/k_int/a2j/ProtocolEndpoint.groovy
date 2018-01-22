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

public class ProtocolEndpoint<RootCodecClass, RootTypeClass>  extends Thread {

  private RootCodecClass codec = RootCodecClass.getCodec();
  private OIDRegister reg = OIDRegister.getRegister();
  private Socket protocol_association = null;
  private InputStream incoming_data = null;
  private OutputStream outgoing_data = null;
  private String target_hostname;
  private String target_port;
  private int assoc_status = ASSOC_STATUS_IDLE;
  private APDUObservable pdu_announcer = new APDUObservable();


  private static final int ASSOC_STATUS_IDLE=0;
  private static final int ASSOC_STATUS_CONNECTING=1;
  private static final int ASSOC_STATUS_CONNECTED=2;
  private static final int ASSOC_STATUS_PERM_FAILURE=3;


  public ProtocolEndpoint(String target_hostname, int target_port) {
    this.target_hostname=target_hostname;
    this.target_port=target_port;
  }

  public void connect() throws java.net.ConnectException, java.io.IOException {

    if ( ( null != target_hostname ) && ( null != target_port ) ) {
      cat.debug("Attempting to connect to "+target_hostname+":"+target_port);

      int timeout = 20000;
      String timeout_prop = props.getProperty("ConnectTimeout");
      protocol_association = new Socket(target_hostname, target_port);
      outgoing_data = z_assoc.getOutputStream();
      incoming_data = z_assoc.getInputStream();
    }
    else {
      throw new java.net.ConnectException("No ServiceHost and/or ServicePort");
    }

    cat.debug("Connect completed OK, send init request (nodelay="+protocol_association.getTcpNoDelay()+
                 ", timeout="+protocol_association.getSoTimeout()+
                 ", linger="+protocol_association.getSoLinger()+")");
  }

  public void shutdown() throws java.io.IOException {
    cat.debug("ProtocolEndpoint::shutdown()");

    if ( protocol_association != null ) {
      running = false;
      protocol_association.close();
      protocol_association = null;
      assoc_status = ASSOC_STATUS_IDLE;
    }
  }

  public void run() {
    cat.debug("Bringing assoc up........Active Thread counter = "+(++active_thread_counter));
    cat.debug("My thread priority : "+this.getPriority());
    cat.debug("My isDaemon: "+this.isDaemon());

    try {
      assoc_status = ASSOC_STATUS_CONNECTING;
      connect();
      assoc_status = ASSOC_STATUS_CONNECTED;
      cat.debug("Connect completed OK, Listening for incoming PDUs");
    }
    catch ( java.net.ConnectException ce ) {
      cat.info(ce.toString());
      assoc_status = ASSOC_STATUS_PERM_FAILURE;
      running = false;
    }
    catch ( java.io.IOException ioe ) {
      cat.info(ioe.toString());
      assoc_status = ASSOC_STATUS_PERM_FAILURE;
      running = false;
    }

    while(running) {
      BERInputStream bds = new BERInputStream(incoming_data, charset_encoding,DEFAULT_BUFF_SIZE);
      try {
        cat.debug("Waiting for data on input stream.....");
        RootTypeClass pdu = null;
        pdu = (RootTypeClass)codec.serialize(bds, pdu, false, "PDU");
        cat.debug("Notifiy observers");

        decOpCount();

        notifyAPDUEvent(pdu);

        // If the target does not support concurrent operations then it's possible that
        // outbound APDU's have stacked up whilst we wait for the response handled here.
        // Therefore, here we  check the outgoing apdu queue and send any messages that
        // have been queued
        if ( !close_notified )
          sendPending();
      }
      catch ( java.io.InterruptedIOException iioe ) {
        cat.debug("Processing java.io.InterruptedIOException, shut down association"+" - hostname="+target_hostname);
        cat.info(iioe.toString(),iioe);
        running=false;
      }
      catch ( java.net.SocketException se ) {
        // Most likely socket closed.
        // if ( cat.isDebugEnabled() )
        //   se.printStackTrace();
        cat.info(se.toString()+" - hostname="+target_hostname+" hashcode of endpoint:"+this.hashCode());
        System.err.println(se.toString()+" - hostname="+target_hostname+" hashcode of endpoint:"+this.hashCode());
        running=false;
      }
      catch ( java.io.IOException ioe ) {
        cat.debug("Processing java.io.IOException, shut down association"+" - hostname="+target_hostname);
        System.err.println("Processing java.io.IOException, shut down association"+" - hostname="+target_hostname+" hashcode of endpoint:"+this.hashCode());
        cat.info(ioe.toString(),ioe);
        running=false;
      }
      catch ( Exception e ) {
        if ( cat.isDebugEnabled() )
          e.printStackTrace();

        cat.info(e.toString()+" - hostname="+target_hostname,e);

        running=false;
      }
      finally
      {
      }
    }

    synchronized( op_counter_lock ) {
      op_counter_lock.notifyAll();
    }

    try {
      incoming_data=null;
      outgoing_data=null;
      if ( protocol_association != null )
        protocol_association.close();
    }
    catch( Exception e ) {
      // catches the socket close execption...
    }

    assoc_status = ASSOC_STATUS_IDLE;
    protocol_association=null;

  }

  protected void notifyAPDUEvent(RootTypeClass pdu) {
    cat.debug("notifyAPDUEvent");
    // Create the new event and add the PDU that was sent
    APDUEvent e = new APDUEvent(this, pdu);

    pdu_announcer.setChanged();
    pdu_announcer.notifyObservers(e);

  }


  
}

