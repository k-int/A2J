/**
 *
 * ChoiceTypeInfo
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: ChoiceTypeInfo.java,v 1.1.1.1 2003/07/17 17:21:45 ianibbo Exp $
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
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ChoiceTypeInfo extends TypeInfo
{
  private Log log = LogFactory.getLog(this.getClass());

  public ChoiceTypeInfo(String type_reference,
                        boolean builtin_type,
                        int tag_class,
                        int tag_number,
                        boolean is_implicit,
                        String basetype,
                        String internal_type,
                        ModuleInfo mi)
  {
    super(type_reference,builtin_type, tag_class, tag_number,is_implicit,basetype,internal_type, mi);
    this.internal_type=this.type_class_name;
  }

  public ChoiceTypeInfo(String type_reference,
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
    int cid_counter = 0;
    log.debug("writeTypeSpecificStaticInitialisationCode ref="+type_reference);
    // All choices will be mapped onto Object, user will have to cast or we will provide a util func.
    declarations.write("\n");
    declarations.write("  private static Object[][] choice_info = { \n");

    // Figure out which codecs are used by this codec so we can create members for each
    // and set up that member in the static initialisation function.
    for ( Enumeration e = cons_members.elements(); e.hasMoreElements(); )
    {
       TaggedMember t = (TaggedMember)e.nextElement();
 
       TypeInfo ti = parent.lookup(t.getModuleReference(),t.getTypeReference(),true);

       log.debug("Processing choice tagged member "+t.getTypeReference());
 
       if ( null != ti )
       {
         declarations.write("    { "+
                            ( ( t.getTagClass() == -1 ) ? "SerializationManager.TAGMODE_NONE, " : ( t.isImplicit() ? "SerializationManager.IMPLICIT, " : "SerializationManager.EXPLICIT," ) ) +
                            "new Integer("+t.getTagClass()+"), new Integer("+t.getTagNumber()+"), "+
                            ti.getCodecClassName()+".getCodec() , \""+t.getMemberName()+"\", new Integer("+(cid_counter++)+") }"+
                            ( e.hasMoreElements() ? "," : "" ) +
                            "\n" );
       }
       else
       {
         log.warn("Unable to lookup type info for Choice Member "+t.getTypeReference());
         System.err.println("Unable to lookup type info for Choice Member "+t.getTypeReference()+
			        " module is "+t.getModuleReference());
       }
    }                        

    declarations.write("  };\n\n");

    func.write("\n");
  }       

  public void createTypeClassFile()
  {
    int cid_counter = 0;
    String type_file_name = parent.getModulePackageDir()+"/"+type_reference.replace('-','_')+"_type.java";

    File type_source_file = new File(type_file_name);
    FileWriter type_writer = null;

    try
    {
        if ( type_source_file.exists() )
        {
            System.err.println(type_file_name+" type already exists");
        }
        else
        {
            System.err.println("            create type source file : "+type_file_name);
            type_source_file.createNewFile();
        }

        type_writer = new FileWriter(type_source_file);

        System.err.println("Package is "+parent.getModulePackageName()+"."+type_reference.replace('-','_'));

        StringWriter declarations_stream = new StringWriter();
        // StringWriter static_get_stream = new StringWriter();
        // StringWriter serialize_method_stream = new StringWriter();
        // StringWriter helper_stream = new StringWriter();

        // type_writer.write("package "+parent.getModulePackageName()+"."+type_reference.replace('-','_')+";\n\n");
        type_writer.write("package "+parent.getModulePackageName()+";\n\n");

        type_writer.write("import org.apache.commons.logging.Log;\n");
        type_writer.write("import org.apache.commons.logging.LogFactory;\n");
        type_writer.write("import java.io.Serializable;\n");
        type_writer.write("import com.k_int.codec.runtime.*;\n\n");

        type_writer.write("public class "+this.type_class_name+" extends ChoiceType implements Serializable \n{\n");

        type_writer.write("  private transient static LoggingContext log = LogFactory.getLog("+this.type_class_name+".class);\n");

        // declarations_stream.write("  public Object o = null;\n");
        // declarations_stream.write("  public int which = -1;\n");

        // Add static int CID's
        for ( Enumeration e = cons_members.elements(); e.hasMoreElements(); )
        {
           TaggedMember t = (TaggedMember)e.nextElement();
           String var_name =  t.getMemberName().replace('-','_');
           String id_var_name = var_name.toLowerCase();
           id_var_name = id_var_name.replace('-','_')+"_CID";

           declarations_stream.write("  public transient static final int "+id_var_name+" = "+(cid_counter)+";\n");

           // Write appropriate get and set helpers
           // TypeInfo ti = parent.lookup(null,t.getTypeReference(),true);
           // if ( null != ti )
           // {
           //   helper_stream.write("  public void set"+var_name+"("+ti.getInternalType()+" o)\n  {\n");
           //   helper_stream.write("    this.which = "+id_var_name+";\n    this.o = o;\n  }\n\n");

           //   helper_stream.write("  public "+ti.getInternalType()+" get"+var_name+"()\n  {\n");
           //   helper_stream.write("    return ("+ti.getInternalType()+")o;\n  }\n\n");
           // } 

           cid_counter++;
        }
        declarations_stream.write("\n");
 

        // Write out CID's for each member of choice and maybe helper functions

        type_writer.write(declarations_stream.toString());

        type_writer.write("\n\n    public "+this.type_class_name+"(int which, Object o)\n");
        type_writer.write("    {\n");
        type_writer.write("      this.which = which;\n");
        type_writer.write("      this.o = o;\n");
        type_writer.write("    }\n\n");
        type_writer.write("    public "+this.type_class_name+"() {}\n\n");

        // Write a to_String method
        type_writer.write("\n\n    public String toString()\n");
        type_writer.write("    {\n");
        type_writer.write("      return this.o.toString();\n");
        type_writer.write("    }\n\n");


        // type_writer.write(static_get_stream.toString());
        // type_writer.write(serialize_method_stream.toString());
        type_writer.write("\n");
        // type_writer.write(helper_stream.toString());

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
    func.write("  public Object serialize(SerializationManager sm,\n");
    func.write("                          Object type_instance,\n");
    func.write("                          boolean is_optional,\n");
    func.write("                          String type_name) throws java.io.IOException\n");
    func.write("  {\n");
    func.write("    "+this.type_class_name+" retval = ("+this.type_class_name+")type_instance;\n\n");

    if ( tag_class != -1 )
    {
      func.write("    if ( sm.constructedBegin("+tag_class+", "+tag_number+") )\n    {\n");
    }


    // I don't think we need this (In fact, I think this is just plain wrong) Since some
    // choice types are mandatory (For example Records in Z39.50:SearchResponse) and we don't want
    // to Return a choice that has no value ( which set to -1 ) do we.... Better to just return null?

    func.write("      if ( sm.getDirection() == SerializationManager.DIRECTION_DECODE )\n      {\n");
    func.write("        retval = new "+this.type_class_name+"();\n");
    func.write("        retval = ("+this.type_class_name+")sm.choice(retval, choice_info, type_name);\n");
    func.write("      }\n      else\n      {\n");
    func.write("        if ( retval != null )\n");
    func.write("          sm.choice(retval, choice_info, type_name);\n");
    func.write("      }\n\n");

    func.write("      if ( ( retval==null ) && ( !is_optional ) )\n      {\n");
    // func.write("        throw new java.io.IOException(\"Missing mandatory choice for \"+type_name);\n      }\n\n");
    func.write("        log.info(\"Missing mandatory choice for \"+type_name);\n      }\n\n");

    if ( tag_class != -1 )
    {
      func.write("    }\n    sm.constructedEnd();\n");
    }

    func.write("    return retval;\n");
    func.write("  }\n");
  }
}

