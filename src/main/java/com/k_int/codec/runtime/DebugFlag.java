/**
 *
 * DebugFlag : Simple static class to hold a final boolean.
 *
 * Apparently, the comiler will realise that any code based on
 * a conditional which is final should be compiled out if it will
 * never be executed. Therefor, this class holds the final boolean
 * that can be used to utterly remove logging from a2jruntime.
 *
 * @author Ian Ibbotson ( ian.ibbotson@k-int.com )
 * @version $Id: DebugFlag.java,v 1.1.1.1 2003/07/17 17:21:51 ianibbo Exp $
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
 */

package com.k_int.codec.runtime;

public interface DebugFlag
{
  public static final boolean debug_enabled = false;
}
