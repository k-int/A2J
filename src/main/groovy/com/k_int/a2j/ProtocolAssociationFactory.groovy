package com.k_int.a2j;

import java.net.Socket;


/**
 * Interface that allows for the creation of new protocol associations based on a socket
 */
public interface ProtocolAssociationFactory<RootCodecClass, RootTypeClass> {

  public ProtocolAssociation create(Socket socket,
                                    RootCodecClass root_codec,
                                    String association_name);
}

