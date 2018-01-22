/**
 *
 * SequenceTypeInfo
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: SequenceTypeInfo.java,v 1.1.1.1 2003/07/17 17:21:48 ianibbo Exp $
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

import java.io.StringWriter;
import java.io.FileWriter;
import java.io.File;              

import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;      
import java.util.Enumeration;      

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SequenceTypeInfo extends TypeInfo
{

  private Log log = LogFactory.getLog(this.getClass());

  public SequenceTypeInfo(String type_reference,
                          boolean builtin_type,
                          int tag_class,
                          int tag_number,
                          boolean is_implicit,
                          String basetype,
                          String internal_type,
                          ModuleInfo mi)
  {
    super(type_reference,builtin_type, tag_class, tag_number,is_implicit,basetype,internal_type,mi);
    this.internal_type=this.type_class_name; 
  }

  public SequenceTypeInfo(String type_reference,
                  boolean builtin_type,
                  String basetype,
                  String internal_type,
                  ModuleInfo mi)
  {
    super(type_reference, builtin_type, basetype, internal_type, mi);
    this.internal_type=this.type_class_name; 
  }

  public void writeTypeSpecificStaticInitialisationCode(StringWriter func, StringWriter declarations)
  {
    // Just to be on the safe side... I don't know why but under some conditions
    // I'm getting 2 declarations for a type
    Vector added_so_far = new Vector();

    for(Iterator i=codecs_used_by_this_codec.iterator(); i.hasNext(); )
    {
      TypeInfo codec_to_add = (TypeInfo)i.next();
      String cname = codec_to_add.getCodecClassName();

      if (  added_so_far.contains(cname) )
      {
        // Don't add it twice
      }
      else
      {
        added_so_far.add(cname);
        declarations.write("  private "+codec_to_add.getCodecClassName()+" i_"+codec_to_add.getCodecClassName().toLowerCase()+" = "+codec_to_add.getCodecClassName()+".getCodec();\n");
      }
    }
    declarations.write("\n");        
  }       

  public void createTypeClassFile()
  {
    log.debug("SequenceTypeInfo::createTypeClassFile()");


    String type_file_name = parent.getModulePackageDir()+"/"+type_reference.replace('-','_')+"_type.java";

    File type_source_file = new File(type_file_name);
    FileWriter type_writer = null;

    try
    {
        if ( type_source_file.exists() )
        {
            log.debug(type_file_name+" type already exists");
        }
        else
        {
            log.debug("            create type source file : "+type_file_name);
            type_source_file.createNewFile();
        }

        type_writer = new FileWriter(type_source_file);

        log.debug("Package is "+parent.getModulePackageName()+"."+type_reference.replace('-','_'));

        StringWriter declarations_stream = new StringWriter();
        // StringWriter static_get_stream = new StringWriter();
        // StringWriter serialize_method_stream = new StringWriter();
        StringWriter imports_stream = new StringWriter();
        StringWriter constructor_sig_stream = new StringWriter();
        StringWriter constructor_body_stream = new StringWriter();
        StringWriter to_string_stream = new StringWriter();

        parent.addImportStatementsToClass(imports_stream, false, true);

        // type_writer.write("package "+parent.getModulePackageName()+"."+type_reference.replace('-','_')+";\n\n");
        type_writer.write("package "+parent.getModulePackageName()+";\n\n");

        type_writer.write("import java.math.BigInteger;\n"); 
        type_writer.write("import java.util.Vector;\n"); 
        type_writer.write("import java.io.Serializable;\n");
        type_writer.write("import com.k_int.codec.runtime.*;\n");
        type_writer.write("import com.k_int.gen.AsnUseful.*;\n\n");
        type_writer.write(imports_stream.toString());

        type_writer.write("/** A Java holder for the ASN type : "+this.type_class_name+" \n");
        type_writer.write(" *  @author Auto generated bu A2J\n");
        type_writer.write(" */ \n");
        type_writer.write("public class "+this.type_class_name+" implements Serializable \n{\n");

        to_string_stream.write("\n\n    public String toString()\n    {\n        java.io.StringWriter sw = new java.io.StringWriter();\n");
        to_string_stream.write("        sw.write(\"\\n{\\n\");\n");
        constructor_sig_stream.write("\n\n    public "+this.type_class_name+"(");
        constructor_body_stream.write("    {");


        boolean first=true;

        for ( Enumeration e = cons_members.elements(); e.hasMoreElements(); )
        {
           TaggedMember t = (TaggedMember)e.nextElement();
           log.debug("Looking up type information for "+t.getTypeReference());

           TypeInfo ti = parent.lookup(null,t.getTypeReference(),true);
           if ( null != ti )
           {
             log.debug("Got type reference for "+t.getMemberName());
             type_writer.write("    /** ");
             if ( t.isOptional() )
               type_writer.write("Optional member ");
             else
               type_writer.write("Mandatory member ");

             type_writer.write("*/\n");
             type_writer.write("    public "+ti.getInternalType()+" "+t.getMemberName()+" = null;\n");

             // Writes a line in the constructor signature
             if ( first )
               first=false;
             else
               constructor_sig_stream.write(",\n      ");
             constructor_sig_stream.write(ti.getInternalType()+" "+t.getMemberName());

             constructor_body_stream.write("\n        this."+t.getMemberName()+"="+t.getMemberName()+";");
             to_string_stream.write("\n        sw.write(\""+t.getMemberName()+"=\""+t.getMemberName()+");");
           }
           else
           {
             log.warn("Unable to locate type information for "+t.getTypeReference());
           }
        }

        constructor_sig_stream.write(")\n");
        constructor_body_stream.write("\n    }");
        to_string_stream.write("\n        sw.write(\"\\n}\\n\");");
        to_string_stream.write("\n        return sw.toString();\n    }");

        type_writer.write(declarations_stream.toString());
        type_writer.write(constructor_sig_stream.toString());
        type_writer.write(constructor_body_stream.toString());
        type_writer.write(to_string_stream.toString());
        type_writer.write("\n\n    public "+this.type_class_name+"() {}\n\n");
        // type_writer.write(static_get_stream.toString());
        // type_writer.write(serialize_method_stream.toString());

        type_writer.write("\n}\n");

        type_writer.flush();
        type_writer.close();
    }
    catch ( java.io.IOException ioe )
    {
        ioe.printStackTrace();
        System.exit(0);
    }
  }  

  public void writeSerializeMethod(StringWriter func, StringWriter declarations)
  {
    func.write("/** Convert this java type to or from a string of octets using the supplied \n");
    func.write(" *  serialisation manager\n");
    func.write(" *  @author A2J Auto Generated Java Class\n");
    func.write(" *  @param sm The Serialisation Manager \n");
    func.write(" *  @param type_instance If we are encoding, the type to encode.\n");
    func.write(" *  @param is_optional Flag indicating if this type is optional\n");
    func.write(" *  @param type_name The type name for diagnostic information\n");
    func.write(" *  @return A decoded java type or the type that was passed in for encoding\n");
    func.write(" */  \n");
    func.write("  public Object serialize(SerializationManager sm,\n");
    func.write("                          Object type_instance,\n");
    func.write("                          boolean is_optional,\n");
    func.write("                          String type_name) throws java.io.IOException\n");
    func.write("  {\n");

    func.write("    "+this.type_class_name+" retval = ("+this.type_class_name+")type_instance;\n\n");

    if ( tag_class == -1 )
    {
      func.write("    if ( sm.sequenceBegin() )\n    {\n");
    }
    else
    {
      if ( is_implicit )
      {
        func.write("    sm.implicit_settag("+tag_class+", "+tag_number+");\n");
        func.write("    if ( sm.sequenceBegin() )\n    {\n");
      }
      else
      {
          func.write("    if ( sm.constructedBegin("+tag_class+", "+tag_number+") )\n    {\n");
          func.write("      sm.sequenceBegin();\n");
      }
    } 

    func.write("\n      if ( sm.getDirection() == SerializationManager.DIRECTION_DECODE )\n      {\n");
    func.write("          retval = new "+this.type_class_name+"();\n");
    func.write("      }\n\n");


    // Skip through members, calling appropriate m's
    for ( Enumeration e = cons_members.elements(); e.hasMoreElements(); )
    {
       TaggedMember t = (TaggedMember)e.nextElement();

       TypeInfo ti = parent.lookup(null,t.getTypeReference(),true);

       if ( null != ti )
       {
         String codec_to_use = "i_"+ti.getCodecClassName().toLowerCase();

         if ( t.getTagClass() == -1 )
         {
             func.write("      retval."+t.getMemberName()+" = ("+ti.getInternalType()+")"+codec_to_use+".serialize(sm, retval."+t.getMemberName()+","+t.isOptional()+", \""+t.getMemberName()+"\");\n");
         }
         else
         {
           if ( t.isImplicit() )
           {
             func.write("      retval."+t.getMemberName()+" = ("+ti.getInternalType()+")sm.implicit_tag("+codec_to_use+", retval."+t.getMemberName()+", "+ t.getTagClass() +", "+t.getTagNumber()+", "+t.isOptional()+", \""+t.getMemberName()+"\");\n");
           }
           else
           {
             func.write("      retval."+t.getMemberName()+" = ("+ti.getInternalType()+")sm.explicit_tag("+codec_to_use+", retval."+t.getMemberName()+", "+ t.getTagClass() +", "+t.getTagNumber()+", "+t.isOptional()+", \""+t.getMemberName()+"\");\n");
           }
         }
      }
    }

    if ( tag_class == -1 )
    {
      func.write("      sm.sequenceEnd();\n    }\n\n");
    }
    else
    {
      if ( is_implicit )
      {
        func.write("      sm.sequenceEnd();\n    }\n\n");
      }
      else
      {
          func.write("      sm.constructedEnd();\n");
          func.write("      sm.sequenceEnd();\n    }\n\n");
      }
    }   


    func.write("    return retval;\n");
    func.write("  }\n");                 
  }
}

