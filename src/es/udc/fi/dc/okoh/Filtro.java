//    This file is part of "OK-OH!".
//
//	  Copyright 2014 Damián Aragunde Pérez <damian.aragunde@udc.es>
//	  Copyright 2014 Javier Parapar López  <javierparapar@udc.es>
//
//    "OK-OH!" is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    "OK-OH!" is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with "OK-OH!".  If not, see <http://www.gnu.org/licenses/>.

package es.udc.fi.dc.okoh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Filtro {
	private int bits;
	private int tamanoBuffer;
	
	public Filtro(int tamanoBuffer, int bits) {
		this.tamanoBuffer= tamanoBuffer;
		this.bits= bits;
	}
	
	public byte[] filtrar(byte[] bufferIn) {
		byte[] bufferOut= new byte[tamanoBuffer];
		
		for (int i= 0; i < bufferIn.length; i++) {
			if (bits == 16) {
				// Conversion a short
				ByteBuffer bufferAux= ByteBuffer.allocate(2);
				bufferAux.order(ByteOrder.LITTLE_ENDIAN);
				bufferAux.put(bufferIn[i]);
				bufferAux.put(bufferIn[i+1]);
				short pulso= bufferAux.getShort(0);
				
				// Aumento de volumen
				if (pulso < -4 && pulso > 4 && pulso > -8192 && pulso < 8192) {
					pulso *= 4;
				}
				
				// Conversion a byte
				bufferOut[i]= (byte) (pulso & 0xff);
				bufferOut[i+1]= (byte) ((pulso >> 8) & 0xff);
				
				i++;
			}
			else {
				byte pulso= bufferIn[i];
				
				// Aumento de volumen
				if (pulso < -4 && pulso > 4 && pulso > -32 && pulso < 32) {
					bufferOut[i]= (byte) (pulso*4);
				}
			}
		}
		
		return bufferOut;
	}
}
