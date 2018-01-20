// Title:       OIDRegConfigurator
// @version:    $Id: OIDRegConfigurator.java,v 1.1.1.1 2003/07/17 17:21:49 ianibbo Exp $
// Copyright:   Copyright (C) 2001 Knowledge Integration Ltd.
// @author:     Ian Ibbotson (ibbo@k-int.com)
// Company:     KI
// Description: 


//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the license, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite
// 330, Boston, MA  02111-1307, USA.
// 

package com.k_int.codec.util;

import java.util.Properties;
import java.util.Enumeration;
import java.io.StringWriter;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * Load the contents of a file into the OID Register
 *
 * @author Ian Ibbotson ( ian.ibbotson@k-int.com )
 * @version $Id: OIDRegConfigurator.java,v 1.1.1.1 2003/07/17 17:21:49 ianibbo Exp $
 * Copyright:   Copyright (C) 2001, Knowledge Integration Ltd (See the file LICENSE for details.)
 *
 */
public class OIDRegConfigurator
{
  private static Log log = LogFactory.getLog(OIDRegConfigurator.class);

  public static void load(OIDRegister reg, String config_file_name)
  {
    InputStream is = null;
    InputStream defaults_is = null;
    Properties p = null;

    // OIDRegister reg = OIDRegister.getRegister();
 
    try 
    {
      // read from top of any classpath entry
      is = reg.getClass().getResourceAsStream(config_file_name);
      defaults_is = reg.getClass().getResourceAsStream("/oidreg.default");

      if ( defaults_is != null )
      {
        Properties default_props = new Properties();
        default_props.load(defaults_is);
        p = new Properties(default_props);
      }
      else
      {
        p = new Properties();
      }

      if ( is != null )
      {
        p.load(is);
      }

      for (Enumeration enum=p.keys(); enum.hasMoreElements(); ) 
      {
        String key = (String) enum.nextElement();
 
        try
        {
          String oid_name = key.substring(key.indexOf('.'), key.length());

          // Every entry in the file must have an OID
          if (key.startsWith("oid")) 
          {
             String oid_string = p.getProperty("oid"+oid_name);
             String entry_name = p.getProperty("name"+oid_name);
             String codec_name = p.getProperty("codec"+oid_name);
             Object the_codec = null;

             try
             {
               if ( codec_name != null )
               {
                 Class codec_class = Class.forName(codec_name);
                 Method get_codec_method = codec_class.getMethod("getCodec",null);
                 the_codec = get_codec_method.invoke(null,null);
               }
             }
             catch ( java.lang.ClassNotFoundException cnfe )
             {
               log.info("Unable to find codec class : "+codec_name);
             }

             reg.register_oid( new OIDRegisterEntry(oid_name.substring(1,oid_name.length()), oid_string, entry_name, the_codec ));
          }
        }
        catch ( StringIndexOutOfBoundsException sbe) 
        {
          log.warn("Problem loading register",sbe);
        }  
        catch ( Exception e )
        {
          log.warn("Problem loading register",e);
        }
      }
    } 
    catch (Exception e) 
    {
      log.warn("Problem loading register",e);
    }
  }
}
