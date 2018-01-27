package com.k_int.a2j;

public interface APDUNotificationTarget<RootTypeClass> {

  public void notify(RootTypeClass apdu);

}

