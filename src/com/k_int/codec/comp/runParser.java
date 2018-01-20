/**
 *
 * runParser
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: runParser.java,v 1.1.1.1 2003/07/17 17:21:49 ianibbo Exp $
 *
 * Copyright:   Copyright (C) 2000, Knowledge Integration Ltd.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the license, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite
 * 330, Boston, MA  02111-1307, USA.
 *   
 *
 */ 

package com.k_int.codec.comp;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;

public class runParser
{
  public static void main(String args[]) throws ParseException 
  {
    System.err.println("Args :"+args);

    for ( int i=0; i<args.length; i++ )
    {
      System.err.println("Processing asn source file : "+args[i]);

      File next_asn_file = new File(args[i]);

      if ( next_asn_file.exists() )
      {
        // parser = new AsnParser(System.in);
        try
        {
          AsnParser parser = new AsnParser(new FileInputStream(next_asn_file));
          parser.Input();
          System.out.println("ASN.1 file "+next_asn_file+" parsed successfully... Calling pass1");
          parser.jjtree.rootNode().pass1();
        }
        catch(ParseException e)
        {
          System.out.println(e.toString());
          e.printStackTrace();
        }
        catch(java.io.FileNotFoundException fnfe)
        {
          System.out.println(fnfe.toString());
          fnfe.printStackTrace();
        }
      }
    }

    // Generate codecs for all processed types
    CodecBuilderInfo.getInfo().create();
  }
}
