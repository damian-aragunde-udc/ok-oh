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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static int duracionFocus= AudioManager.AUDIOFOCUS_GAIN;
	private static int tipoStream= AudioManager.STREAM_MUSIC;
	
	private BluetoothAdapter bluetoothAdapter;
	private boolean botonActivo;
	private Context contexto;
	private AudioListener listener;
	private AudioManager manager;
	private boolean headset;
	private IntentReceiver receiver;
	private Intent servicio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
		this.botonActivo= false;
		this.contexto=  getApplicationContext();
		this.listener= new AudioListener();
		this.manager= (AudioManager) contexto.getSystemService(AUDIO_SERVICE);
		this.headset= false;
		this.receiver= new IntentReceiver();
		contexto.registerReceiver(receiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		contexto.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		this.servicio= new Intent(this, AudioSystem.class);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		// Apaño para verificar que el servicio está activo al inicio
		if (stopService(servicio)) {
			activarBoton();
			startService(servicio);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.action_about) {
			Intent aboutActivity= new Intent(this, AboutActivity.class);
			startActivity(aboutActivity);
		}
		
		return super.onOptionsItemSelected(item);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		
		freeFocus();
	}

	// Problema: basta con que el bluetooth este encendido y exista un dispositivo de ese tipo memorizado para iniciarse
	private boolean existBluetoothHeadset() {
		for (BluetoothDevice dispositivo: bluetoothAdapter.getBondedDevices()) {
			BluetoothClass clase= dispositivo.getBluetoothClass();
			switch (clase.getDeviceClass()) {
			case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
				return true;
			case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
				return true;
			case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
				return true;
			}
		}
		return false;
	}

	/*
	 * Intenta obtener el foco de sonido
	 */
	private boolean grantFocus() {
		int tengoFoco= (manager.requestAudioFocus(listener, tipoStream, duracionFocus));
		if (tengoFoco == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			return true;
		}
		return false;
	}

	/*
	 * Libera el foco de sonido. Evita errores en algunos dispositivos
	 */
	private void freeFocus() {
		// Antes de liberarse del foco, desactiva Bluetooth SCO
		if (manager.isBluetoothScoAvailableOffCall()) {
			manager.setBluetoothScoOn(false);
			manager.stopBluetoothSco();
		}
		
		manager.abandonAudioFocus(listener);
	}

	private void pararServicio() {
		stopService(servicio);
		freeFocus();
	}

	/*
	 * Simplemente refresca la imagen y cambia el estado del boolean
	 */
	private void activarBoton() {
		Drawable imagen= contexto.getResources().getDrawable(R.drawable.on);
		((ImageButton) findViewById(R.id.start_button)).setImageDrawable(imagen);
		botonActivo= true;
	}

	/*
	 * Simplemente refresca la imagen y cambia el estado del boolean
	 */
	private void desactivarBoton() {
		Drawable imagen= contexto.getResources().getDrawable(R.drawable.off);
		((ImageButton) findViewById(R.id.start_button)).setImageDrawable(imagen);
		botonActivo= false;
	}
	
	public void botonPulsado(View view) {
		pararServicio(); // Evita posibles errores extraños, por eso esta al principio
		if (!botonActivo) {
			if (headset || existBluetoothHeadset()) {
				if (grantFocus()) {
					if (manager.isBluetoothScoAvailableOffCall()) {
						manager.startBluetoothSco();
						manager.setBluetoothScoOn(true);
					}
					activarBoton();
					startService(servicio);
				}
				/*
				 * Si no se obtiene el foco, no ocurre nada de nada al pulsar.
				 * El usuario presupondra que no pulso y le dara de nuevo.
				 * Se supone que si no se obtiene el foco es por algo puntual, a la siguiente vez que se pulse ya lo obtendra
				 */
			}
			else {
				// Mensaje de error: Faltan cascos
				Toast avisoError= Toast.makeText(contexto, R.string.error_headset_not_found, Toast.LENGTH_SHORT);
				avisoError.show();
				
				desactivarBoton();
			}
		}
		else {
			// Ya para el servicio al inicio, no hace falta ponerlo dentro
			
			desactivarBoton();
		}
	}
	
	/*
	 * Clase anidada obligatoria para el manejo del foco de sonido
	 * Vigila los cambios de estado del foco
	 */
	private class AudioListener implements OnAudioFocusChangeListener {
		@Override
		public void onAudioFocusChange(int focusChange) {
			if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				// Do nothing
			}
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				pararServicio();
				desactivarBoton();
			}
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				pararServicio();
				desactivarBoton();
			}
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
				// Me he fijado que baja el volumen automaticamente
			}
		}
	}
	
	/*
	 * Clase anidada que gestiona los intens recividos
	 * Actualmente vigila dos intents:
	 * - Que se desconecte o no el jack de audio
	 * - Que el bluetooth este activo (para verificar posteriormente que dispositivos hay conectados)
	 */
	private class IntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Si se conecta unos cascos/manos libres por cable
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int estado= intent.getIntExtra("state", -1);
				if (estado == 1) {
					headset= true;
					/*
					 * El texto comentado a continuacion verifica si lo que se ha conectado posee microfono o no
					 * Sirve para comprobar si el modelo de telefono en cuestion hace distincion entre conectar unos cascos o unos manos libres
					 * Ha servico para verificar que el error del Huawei Ascend Y200 ha sido por ello 
					 */
//					String texto_fuera1= "Headset conectado!";
//					Toast tostada_fuera1= Toast.makeText(context, texto_fuera1, Toast.LENGTH_SHORT);
//					tostada_fuera1.show();
//					
//					int microfono= intent.getIntExtra("microphone", -1);
//					if (microfono == 1) {
//						String texto_fuera2= "Tiene microfono!";
//						Toast tostada_fuera2= Toast.makeText(context, texto_fuera2, Toast.LENGTH_SHORT);
//						tostada_fuera2.show();
//					}
//					else {
//						String texto_fuera3= "No tiene microfono :(";
//						Toast tostada_fuera3= Toast.makeText(context, texto_fuera3, Toast.LENGTH_SHORT);
//						tostada_fuera3.show();
//					}
				}
				else { // Eliminar esto para comprobarlo sin cascos
					headset= false;
					if (!existBluetoothHeadset()) {
						pararServicio();
						desactivarBoton();
					}
				}
			}
			
			// Si cambia el estado del Bluetooth
			if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
				case BluetoothAdapter.STATE_OFF:
					if (!headset) {
						pararServicio();
						desactivarBoton();
					}
					break;
				}
			}
		}
	}
}
