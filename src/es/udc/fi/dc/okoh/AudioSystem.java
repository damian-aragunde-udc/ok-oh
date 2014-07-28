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

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;


public class AudioSystem extends Service {
	private static int modoReproduccion= AudioTrack.MODE_STREAM;
	private static int[] muestreos = new int[] {8000, 11025, 16000, 22050, 44100};
	private static int origenAudio= AudioSource.VOICE_RECOGNITION;
	private static int tipoStream= AudioManager.STREAM_MUSIC;
	
	private byte[] bufferGrabacion;
	private byte[] bufferReproduccion;
	private Filtro filtro;
	private AudioRecord grabador;
	private AsyncTask<AudioSystem, Void, Void> hilo;
	private AudioTrack reproductor;
	private int tamanoBuffer;
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		if (!inicializarDispositivos()) {
			Log.e("OK-OH!.AudioSystem", "Imposible encontrar una configuracion de grabacion/reproduccion valida para el dispositivo");
			stopSelf();
		}
		this.bufferGrabacion= new byte[tamanoBuffer];
		this.bufferReproduccion= new byte[tamanoBuffer];
		this.filtro= new Filtro(this);
		this.hilo= new AudioThread();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		hilo.cancel(true);
		reproductor.stop();
		grabador.stop();
		reproductor.release(); // Suprimir esto para que el Huawei falle y detectar problemas al cerrarse inesperadamente el servicio
		grabador.release(); // Suprimir esto para que el Huawei falle y detectar problemas al cerrarse inesperadamente el servicio
		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		grabador.startRecording();
		reproductor.play();
		hilo.execute(this);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* Busca la configuracion ideal para el grabador y reproductor
	 * La configuracion de ambos debe coincidir
	 * Es dependiente del modelo del telefono
	 * Inicialmente busca de los valores mas simples (peor calidad pero menos latencia) a los optimos
	 */
	private boolean inicializarDispositivos() { // Tenemos un claro problema por aqui, que al voltear se reinicializa todo
	    for (int muestreo: muestreos) { // 8000, 11025, 16000, 22050 o 44100
            for (int canal: new int[] {AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
    	        for (int formato: new int[] {AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                    int tamano= AudioRecord.getMinBufferSize(muestreo, canal, formato);
                    if (tamano != AudioRecord.ERROR_BAD_VALUE) {
                        AudioRecord grabador = new AudioRecord(origenAudio, muestreo, canal, formato, tamano);
                        if (grabador.getState() == AudioRecord.STATE_INITIALIZED) {
                        	int canalReproduccion;
                        	if (canal == AudioFormat.CHANNEL_IN_MONO) {
                            	canalReproduccion= AudioFormat.CHANNEL_OUT_MONO;
                        	}
                        	else {
                            	canalReproduccion= AudioFormat.CHANNEL_OUT_STEREO;
                        	}
                        	
                        	AudioTrack reproductor= new AudioTrack(tipoStream, muestreo, canalReproduccion, formato, tamano, modoReproduccion);
                            if (reproductor.getState() == AudioTrack.STATE_INITIALIZED) {
                            	// Momento en el que damos con la configuracion adecuada
                            	this.tamanoBuffer= tamano;
                            	this.grabador= grabador;
                            	this.reproductor= reproductor;
                                return true;
                            }
                            else {
                            	grabador.release();
                            }
                        }
                    }
	            }
	        }
	    }
	    // Si acabo la busqueda y no obtuvimos una configuracion exitosa
	    return false;
	}
	
	/**
	 * @return the bufferGrabacion
	 */
	public byte[] getBufferGrabacion() {
		return bufferGrabacion;
	}

	/**
	 * @return the filtro
	 */
	public Filtro getFiltro() {
		return filtro;
	}

	/**
	 * @return the tamanoBuffer
	 */
	public int getTamanoBuffer() {
		return tamanoBuffer;
	}

	/**
	 * @param bufferReproduccion the bufferReproduccion to set
	 */
	public void setBufferReproduccion(byte[] bufferReproduccion) {
		this.bufferReproduccion = bufferReproduccion;
	}

	public void grabar() {
		grabador.read(bufferGrabacion, 0, bufferGrabacion.length);
	}

	public void reproducir() {
		reproductor.write(bufferReproduccion, 0, bufferGrabacion.length);
	}
}
