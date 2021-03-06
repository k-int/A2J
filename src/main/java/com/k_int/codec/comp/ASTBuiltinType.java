/* Generated By:JJTree: Do not edit this line. ASTBuiltinType.java */

package com.k_int.codec.comp;

import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ASTBuiltinType extends SimpleNode {

  public int which = 0;

  public ASTBuiltinType(int id) {
    super(id);
  }

  public ASTBuiltinType(AsnParser p, int id) {
    super(p, id);
  }

  public String getTypeName()
  {
    return AsnParser.builtinTypes[which];
  }

  // public String getBaseClassName(String element_name)
  // {
  //     switch ( which )
  //     {
  //         case 0:
  //             return "com.k_int.codec.asn.AsnInteger";
  //         case 1:
  //             return "com.k_int.codec.asn.AsnBitString";
  //         case 2:
  //             return ((ASTSetOrSequenceType)jjtGetChild(0)).getBaseClassName(element_name);
  //         case 3:
  //             return ((ASTSetOrSequenceOfType)jjtGetChild(0)).getBaseClassName(element_name);
  //         case 4:
  //             return ((ASTChoiceType)jjtGetChild(0)).getBaseClassName(element_name);
  //         case 5: // Selection ????
  //             return "Unknown";
  //         case 6: // Tagged Type... Figure out what's inside the tagged type
  //             return ((ASTTaggedType)jjtGetChild(0)).getBaseClassName(element_name);
  //         case 7: // ANY
  //             return "com.k_int.codec.asn.AsnAny";
  //         case 8: // Enumeration
  //             return ((ASTEnumeratedType)jjtGetChild(0)).getBaseClassName(element_name);
  //         case 9: // OctetString
  //             return "com.k_int.codec.asn.AsnOctetString";
  //         case 10: // OID
  //             return "com.k_int.codec.asn.AsnOID";
  //         case 11: // REAL
  //             return "com.k_int.codec.asn.AsnReal";
  //         case 12: // BOOL
  //             return "com.k_int.codec.asn.AsnBoolean";
  //         case 13: // NULL
  //             return "com.k_int.codec.asn.AsnNull";
  //         case 14: // ASNExtermal
  //             return "com.k_int.codec.AsnUseful.EXTERNAL";
  //         default:
  //             return "Unknown";
  //     }
  // }
}
