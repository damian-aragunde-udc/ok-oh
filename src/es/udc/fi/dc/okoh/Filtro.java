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


public class Filtro {
	private AudioSystem audio;
	private byte[] bufferIn;
	private byte[] bufferOut;
	private int tamanoBuffer;
	
	public Filtro(AudioSystem audio) {
		this.audio= audio;
		this.tamanoBuffer= audio.getTamanoBuffer();
		this.bufferIn= new byte[tamanoBuffer];
		this.bufferOut= new byte[tamanoBuffer];
	}
	
	public void filtrar() {
		leer();
		procesar();
		escribir();
	}
	
	private void leer() {
		bufferIn= audio.getBufferGrabacion();
	}
	
	private void escribir() {
		audio.setBufferReproduccion(bufferOut);
	}
	
	private void procesar() {
//		bufferOut= bufferIn;
		
		int i= 0;
		for (byte pulso: bufferIn) {
			// Aumento de volumen
			if (pulso < -4 && pulso > 4 && pulso > -32 && pulso < 32)
				pulso *= 4;
			
			bufferOut[i]= pulso;
			i++;
		}
	}
}
