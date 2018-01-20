/**
 *
 * OIDRegister
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: OIDRegister.java,v 1.1.1.1 2003/07/17 17:21:49 ianibbo Exp $
 *
 * Copyright:   Copyright (C) 2000, Knowledge Integration Ltd (See the file LICENSE for details.)
 *
 */

package com.k_int.codec.util;

import java.util.Hashtable;
import java.io.StringWriter;
import com.k_int.codec.runtime.base_codec;


// The idea of this class is to map an OID onto a class that is capable of
// turning a Stream into some kind of structured object

public class OIDRegister
{
  private static OIDRegister me = null;
  private Hashtable register_by_name = new Hashtable();
  private Hashtable register_by_value = new Hashtable();

  private OIDRegister()
  {
  }

  public static synchronized OIDRegister getRegister()
  {
    if ( me == null )
    {
      me = new OIDRegister();
      OIDRegConfigurator.load(me,"/a2j.properties");
    }

    return me;
  }


  public void register_oid(OIDRegisterEntry entry)
  {
    // System.err.println("Registering entry "+entry.getName()+" with oid "+entry.getStringValue());
    register_by_name.put(entry.getName(), entry);
    register_by_value.put(entry.getStringValue(), entry);
  }

  public OIDRegisterEntry lookupByOID(String oid_as_string)
  {
    OIDRegisterEntry e = (OIDRegisterEntry)register_by_value.get(oid_as_string);
    return e;
  }

  public OIDRegisterEntry lookupByOID(int[] oid)
  {
    // We hope we won't be creating oid strings much bigger than 32!
    StringWriter sw = new StringWriter(32);  

    sw.write("{");

    for ( int i = 0; i<oid.length; i++)
    {
      if ( i > 0 )
        sw.write(",");

      sw.write(""+oid[i]);
    }

    sw.write("}");

    // System.err.println("OID:"+sw.toString());

    return lookupByOID(sw.toString());
  }

  public int[] oidByName(String name)
  {
    OIDRegisterEntry o = lookupByName(name);

    if ( null != o )
    {
      return o.getValue();
    }
    
    return null;
  }

  public OIDRegisterEntry lookupByName(String name)
  {
    return (OIDRegisterEntry) register_by_name.get(name);
  }

}
