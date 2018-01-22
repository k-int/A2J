package com.k_int.a2j;

import java.util.Observable;


/**
  APDUObserver : Allow observers to monitor a protocol stack.
  * @author Ian Ibbotson
  * @version $Id: APDUObservable.java,v 1.2 2002/11/29 13:24:01 ianibbo Exp $
  */


public class APDUObservable extends Observable {

  public void setChanged() {
    super.setChanged();
  }

}

