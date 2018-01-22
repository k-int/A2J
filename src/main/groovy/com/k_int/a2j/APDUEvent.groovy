package com.k_int.a2j;

import java.util.EventObject;

public class APDUEvent<RootAPDUType> extends EventObject {

  private RootAPDUType pdu;
  private String reference;

  public APDUEvent(Object source) {
    super(source);
  }

  public APDUEvent(Object source,
                   RootAPDUType thepdu) {
    this(source, thepdu, null);
  }

  public APDUEvent(Object source,
                   RootAPDUType thepdu,
                   String reference) {
    super(source);
    this.pdu=thepdu;
    this.reference=reference;
  }

  public RootAPDUType getPDU() {
    return pdu;
  }

  public String getReference() {
    return reference;
  }
}

