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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		setIcon(R.drawable.ic_launcher);
		setCopyright(R.raw.copyright, R.style.TextAppearance_AppCompat_Base_Widget_PopupMenu_Large);
		setCopyright(R.raw.license, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setIcon(int resource) {
		LinearLayout layout= (LinearLayout) findViewById(R.id.aboutLayout);
		
		ImageView icon= new ImageView(this);
		icon.setImageResource(resource);
		
		layout.addView(icon);
	}
	
	private void setCopyright(int resource, int style) {
		LinearLayout layout= (LinearLayout) findViewById(R.id.aboutLayout);
		String line= "";
		
		InputStream iStream= getResources().openRawResource(resource);
		InputStreamReader iStreamR= new InputStreamReader(iStream);
		BufferedReader bReader= new BufferedReader(iStreamR);
		
		try {
			while (line != null) {
				TextView text= new TextView(this);
				text.setTextAppearance(this, style);
				
				text.setText(line);
				layout.addView(text);
				
				line= bReader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
