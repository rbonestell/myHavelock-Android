package com.rbonestell.myhavelock;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigActivity extends Activity
{
	
	private EditText txtAPIKey;
	private Button btnSave;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		setupActionBar();
		
		btnSave = (Button)findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ConfigActivity.this).edit();
			    editor.putString("apikey", txtAPIKey.getText().toString());
			    editor.commit();
			    Toast.makeText(ConfigActivity.this, "API Key Saved", Toast.LENGTH_LONG).show();
			    ConfigActivity.this.finish();
			}
		});
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String savedAPIKey = settings.getString("apikey", "");
		txtAPIKey = (EditText)findViewById(R.id.txtAPIKey);
		txtAPIKey.setText(savedAPIKey);
		
	}
	
	@Override
	public void finish()
	{
		super.finish();
		overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right );
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar()
	{
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
}
