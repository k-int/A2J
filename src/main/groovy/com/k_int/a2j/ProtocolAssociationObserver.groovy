package com.k_int.a2j;

public interface ProtocolAssociationObserver<RootTypeClass> {
  public notify(ProtocolAssociation source, RootTypeClass apdu);
}
