/* Generated By:JJTree: Do not edit this line. ASTAssignmentList.java */

package com.k_int.codec.comp;

public class ASTAssignmentList extends SimpleNode {
  public ASTAssignmentList(int id) {
    super(id);
  }

  public ASTAssignmentList(AsnParser p, int id) {
    super(p, id);
  }

  public void pass1()
  {
    int i, k = jjtGetNumChildren();
 
    for (i = 0; i < k; i++)
    {
      jjtGetChild(i).pass1();
    }
 
  }                                    
}
