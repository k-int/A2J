/* Generated By:JJTree: Do not edit this line. ASTElementType.java */

package com.k_int.codec.comp;

public class ASTElementType extends SimpleNode {

  // 1=named type, 2=components
  public int which = 0;
  public boolean optional = false;
  public boolean has_default = false;

  public ASTElementType(int id) {
    super(id);
  }

  public ASTElementType(AsnParser p, int id) {
    super(p, id);
  }

}
