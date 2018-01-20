/**
 *
 * AsnBitString : A utility class representing an ASN bit string
 *
 * @author Ian Ibbotson ( ibbo@k-int.com )
 * @version $Id: AsnBitString.java,v 1.1.1.1 2003/07/17 17:21:50 ianibbo Exp $
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

import java.io.Serializable;

public class AsnBitString implements Serializable
{
    public int numbytes=0;
    public int unused_bits = 0;
    public byte[] value = null;

    public AsnBitString()
    {
      // Default to 8 bits of data, we can always grow later...
      numbytes=1;
      value = new byte[1];
    }

    public AsnBitString(byte[] bits, int unused)
    {
      value = bits;
      unused_bits = unused;
      numbytes = bits.length;
    }

    public AsnBitString(int top_bit_pos)
    {
        // Allocate the value buffer based on the top bit position...
        numbytes = 1 + ( top_bit_pos / 8 ) ;
        // numbytes += ( top_bit_pos % 8 > 0 ? 1 : 0 );
        value = new byte[numbytes];
    }

    byte[] getValue()
    {
        return value;
    }

    private void ensureSize(int bitpos)
    {
        int num_bytes_needed = 1 + ( (bitpos) / 8 );
        // num_bytes_needed += ( (bitpos+1) % 8 > 0 ? 1 : 0 );
        if ( num_bytes_needed > numbytes )
        {
            // System.err.println("re-allocating bitstring");

            // We need to re-allocate the buffer
            byte [] newvalue = new byte[num_bytes_needed];
            if ( null != value )
                System.arraycopy(value, 0, newvalue, 0, numbytes);

            numbytes = num_bytes_needed;
            value = newvalue;
        }
    }

    // Set a specific bit position
    public void setBit(int bitpos)
    {
        setBit(bitpos, true);
    }

    public void clearBit(int bitpos)
    {
        setBit(bitpos, false);
    }

    public void setBit(int bitpos, boolean set)
    {
        ensureSize(bitpos);
        int octet_to_set = bitpos / 8;
        int bit_to_set = 7 - ( bitpos % 8 );
        if ( set )
          value[octet_to_set] |= ( 1 <<  bit_to_set );
        else
          value[octet_to_set] &= ( ~ ( 1 <<  bit_to_set ) );
    }

    // Check a specific bit position
    public boolean isSet(int bitpos)
    {
      int which_octet = bitpos / 8;

      if ( which_octet < value.length )
      {
        int bit_to_check = 7 - ( bitpos % 8 );
        byte mask = (byte)(1 << bit_to_check);

        // Yep, we have access to that bitpos
        if ( ( value[which_octet] & mask ) == mask )
          return true;
      }

      return false;
    }
}
