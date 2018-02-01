package com.k_int.a2j;

import java.net.Socket;


/**
 * Interface that allows for the creation of new protocol associations based on a socket
 */
public class SimpleProtocolAssociationFactory<RootCodecClass, RootTypeClass> 
            implements ProtocolAssociationFactory {

  public ProtocolAssociation create(Socket socket,
                                    RootCodecClassroot_codec,
                                    String association_name) {
    return new ProtocolAssociation<RootCodecClass,RootTypeClass>(socket,root_codec,'ServerAssociation')
  }
}

