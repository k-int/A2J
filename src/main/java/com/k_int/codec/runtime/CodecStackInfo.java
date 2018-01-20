/**
 *
 * CodecStackInfo: Used in encoding/decoding to track constructed memebers
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: CodecStackInfo.java,v 1.1.1.1 2003/07/17 17:21:51 ianibbo Exp $
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

package com.k_int.codec.runtime;

public class CodecStackInfo
{
  public int len_offset;
  public int contents_offset;
  public int lenlen;

  // Used in decoding
  public int content_length;
  public int bytes_processed;
  public boolean is_constructed;
  public boolean is_indefinite_length;

  public CodecStackInfo()
  {
  }
}
