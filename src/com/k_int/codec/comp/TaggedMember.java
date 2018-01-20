package com.k_int.codec.comp;

public class TaggedMember
{
  private String element_name;
  private String type_reference;
  private int tag_class;
  private int tag_number;
  private boolean is_implicit;
  private boolean is_optional;

  public TaggedMember(String element_name,
                      String type_reference,
                      int tag_class,
                      int tag_number,
                      boolean is_implicit,
                      boolean is_optional)
  {
    this.element_name = element_name;
    this.type_reference = type_reference;
    this.tag_class=tag_class;
    this.tag_number=tag_number;
    this.is_implicit=is_implicit;
    this.is_optional=is_optional;
  }

  public String getTypeReference()
  {
    return type_reference;
  }

  public String getModuleReference()
  {
    return null;
  }

  public boolean isImplicit()
  {
    return is_implicit;
  }

  public boolean isOptional()
  {
    return is_optional;
  }

  public int getTagClass()
  {
    return tag_class;
  }

  public int getTagNumber()
  {
    return tag_number;
  }

  public String getMemberName()
  {
    return element_name;
  }
}

