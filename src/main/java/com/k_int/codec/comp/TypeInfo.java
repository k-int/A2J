/**
 *
 * TypeInfo
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: TypeInfo.java,v 1.1.1.1 2003/07/17 17:21:49 ianibbo Exp $
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
 */

/*
 * $Log: TypeInfo.java,v $
 * Revision 1.1.1.1  2003/07/17 17:21:49  ianibbo
 * Initial import
 *
 * Revision 1.8  2002/07/18 12:18:00  ianibbo
 * Fixed a bug with nested choice elements
 *
 */

package com.k_int.codec.comp;

import java.io.StringWriter;
import java.io.FileWriter;
import java.io.File;              
import java.util.Vector;
import java.util.Enumeration;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;            

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class TypeInfo
{
  protected String type_reference;
  protected int tag_class;
  protected int tag_number;
  protected boolean is_implicit;
  protected String basetype;
  protected boolean has_tagging = false;
  protected String internal_type;
  protected boolean extends_builtin_type;
  protected ModuleInfo parent = null;

  protected Vector cons_members = new Vector();

  protected String codec_class_name = null;
  protected String type_class_name = null;

  protected Set codecs_used_by_this_codec = Collections.synchronizedSet(new HashSet());        

  private Log log = LogFactory.getLog(this.getClass());

  public TypeInfo(String type_reference,
                  boolean builtin_type,
                  int tag_class,
                  int tag_number,
                  boolean is_implicit,
                  String basetype,
                  String internal_type,
                  ModuleInfo parent)
  {
    this.type_reference = type_reference;
    this.extends_builtin_type = builtin_type;
    this.has_tagging=true;
    this.tag_class=tag_class;
    this.tag_number=tag_number;
    this.is_implicit=is_implicit;
    this.basetype=basetype;
    this.internal_type=internal_type;
    this.parent = parent;

    codec_class_name = type_reference.replace('-','_')+"_codec";
    type_class_name = type_reference.replace('-','_')+"_type";

    log.debug("TypeInfo::TypeInfo("+type_reference+","+builtin_type+","+tag_class+","+tag_number+","+is_implicit+","+basetype+","+internal_type+")");
  }

  public TypeInfo(String type_reference,
                  boolean builtin_type,
                  String basetype,
                  String internal_type,
                  ModuleInfo parent)
  {
    this.type_reference = type_reference;
    this.extends_builtin_type = builtin_type;
    this.basetype=basetype;
    this.internal_type=internal_type;
    this.parent = parent;

    codec_class_name = type_reference.replace('-','_');

    log.debug("TypeInfo::TypeInfo("+type_reference+","+builtin_type+","+basetype+","+internal_type+")");
  }

  public void registerTaggedMember(String element_name,
                                   int tag_class,
                                   int tag_number,
                                   boolean is_implicit,
                                   String type_reference,
                                   boolean is_optional)
  {
    cons_members.add(new TaggedMember(element_name, type_reference, tag_class, tag_number, is_implicit, is_optional));
  }

  public void createCode()
  {
    // Figure out which codecs are used by this codec so we can create members for each
    // and set up that member in the static initialisation function.
    for ( Enumeration e = cons_members.elements(); e.hasMoreElements(); )
    {
       TaggedMember t = (TaggedMember)e.nextElement();
       log.debug("Processing Element name: "+t.getMemberName()+" "+t.getTypeReference());
 
       TypeInfo ti = parent.lookup(t.getModuleReference(),t.getTypeReference(),true);
 
       if ( null != ti )
       {
         registerUsedCodec(ti);
         // codecs_used_by_this_codec.add(ti.getCodecClassName());
       }
       else
       {
         // This means that one of the members was of a type we aren't able to look up...
         log.warn("Unable to lookup type info for "+t.getTypeReference()+" element name is "+t.getMemberName());
       }                                   
    }

    log.debug("Create codec file");
    createCodecClassFile();

    log.debug("Create type file");
    createTypeClassFile();
  }

  public void createCodecClassFile()
  {
    String codec_file_name = parent.getModulePackageDir()+"/"+type_reference.replace('-','_')+"_codec.java";

    File codec_source_file = new File(codec_file_name);
    FileWriter codec_writer = null;

    try
    {
        if ( codec_source_file.exists() )
        {
            log.debug(codec_file_name+" codec already exists");
        }
        else
        {
            log.debug("            create codec source file : "+codec_file_name);
            codec_source_file.createNewFile();
        }

        codec_writer = new FileWriter(codec_source_file);

        log.debug("Package is "+parent.getModulePackageName()+"."+type_reference.replace('-','_'));

        StringWriter imports_stream = new StringWriter();
        StringWriter declarations_stream = new StringWriter();
        StringWriter static_get_stream = new StringWriter();
        StringWriter serialize_method_stream = new StringWriter();


        // codec_writer.write("package "+parent.getModulePackageName()+"."+type_reference.replace('-','_')+";\n\n");
        codec_writer.write("package "+parent.getModulePackageName()+";\n\n");

        // Run through imported modules / types

        codec_writer.write("// For logging\nimport org.apache.commons.logging.Log;\n");
        codec_writer.write("import org.apache.commons.logging.LogFactory;\n");

        codec_writer.write("import java.math.BigInteger;\n");
        parent.addImportStatementsToClass(imports_stream, true, true);
        codec_writer.write("import com.k_int.codec.runtime.*;\n");
        codec_writer.write("import com.k_int.gen.AsnUseful.*;\n\n");

        codec_writer.write(imports_stream.toString());

        codec_writer.write("public class "+this.codec_class_name+" extends base_codec\n{\n");

        codec_writer.write("  private static Log log = LogFactory.getLog("+this.codec_class_name+".class);\n");

        createStaticGetMethod(static_get_stream, declarations_stream);
        writeSerializeMethod(serialize_method_stream, declarations_stream);

        codec_writer.write(declarations_stream.toString());
        codec_writer.write(static_get_stream.toString());
        codec_writer.write(serialize_method_stream.toString());

        codec_writer.write("\n}\n");

        codec_writer.flush();
        codec_writer.close();
    }
    catch ( java.io.IOException ioe )
    {
        ioe.printStackTrace();
        System.exit(0);
    }
  }

  public abstract void createTypeClassFile();

  public void createStaticGetMethod(StringWriter func, StringWriter declarations)
  {
    // Output a static member for this codec
    declarations.write("  public static "+codec_class_name+" me = null;\n\n");
    // Might need to call a function to add member codecs in here

    func.write("  public synchronized static "+codec_class_name+" getCodec()\n  {\n");
    func.write("    if ( me == null )\n    {\n      me = new "+codec_class_name+"();\n");

    writeTypeSpecificStaticInitialisationCode(func, declarations);

    func.write("    }\n");
    func.write("    return me;\n");
    func.write("  }\n\n");
  }

  public abstract void writeTypeSpecificStaticInitialisationCode(StringWriter func, StringWriter declarations);

  // The default writeSerializeMethod function

  public void writeSerializeMethod(StringWriter func, StringWriter declarations)
  {
    TypeInfo derived_from = parent.lookup(null, basetype, true);

    if ( null != derived_from )
    {
      declarations.write("  private "+derived_from.getCodecClassName()+" i_"+derived_from.getCodecClassName().toLowerCase()+" = "+derived_from.getCodecClassName()+".getCodec();\n\n");

      func.write("  public Object serialize(SerializationManager sm,\n");
      func.write("                          Object type_instance,\n");
      func.write("                          boolean is_optional,\n");
      func.write("                          String type_name) throws java.io.IOException\n");
      func.write("  {\n"); 

      // We need to figure out what the ultimate base type is for this type

      if ( tag_class == -1 )
      {
        func.write("    return i_"+derived_from.getCodecClassName().toLowerCase()+".serialize(sm, type_instance, is_optional, type_name);\n");
      }
      else
      {
        if ( is_implicit )
        {
          func.write("    return sm.implicit_tag(i_"+derived_from.getCodecClassName().toLowerCase()+", type_instance, "+tag_class+", "+tag_number+", is_optional, \""+type_reference+"\");\n");
        }
        else
        {
          func.write("    return sm.explicit_tag(i_"+derived_from.getCodecClassName().toLowerCase()+", type_instance, "+tag_class+", "+tag_number+", is_optional, \""+type_reference+"\");\n");
        }
      }
      func.write("  }\n"); 
    }
    else
    {
      log.warn("WARNING: Unable to locate type we are derived from : "+basetype);
    }
  }

  public String getCodecClassName()
  {
    return codec_class_name;
  }

  public String getTypeClassName()
  {
    return type_class_name;
  }

  public void registerUsedCodec(TypeInfo ti)
  {
    codecs_used_by_this_codec.add(ti);
  }

  public String getInternalType()
  {
    log.debug("TypeInfo::getInternalType()");

    // if internal_type is null, lookup the basetype and use the internal type from there
    if ( internal_type == null )
    {
      log.debug("Looking up internal type based on basetype: "+basetype);
      // Odds are we are just a defined type that changes the tagging around some
      // internal class. Therefore, just try and copy the internal type from the
      // base type.
      TypeInfo derived_from = parent.lookup(null, basetype, true);
 
      if ( null != derived_from )
      {
        log.debug("OK.. Located that internal type");
        this.internal_type = derived_from.getInternalType();
      }
      else
        log.warn("Unable to locate base type");
    }                        

    log.debug("getInternalType returns "+internal_type);

    return internal_type;
  }

  public ModuleInfo getParent()
  {
    return parent;
  }
}

