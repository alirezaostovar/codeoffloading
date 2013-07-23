package com.cloud.cloudTest;



import com.cloud.cloudTest.MainActivity;
import com.cloud.cloudTest.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

	public static String changelabel = "CHANGE_LABEL"; 
	MessageReceiver receiver;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button runcloud = (Button)this.findViewById(R.id.runcloud);
        runcloud.setOnClickListener(new Button.OnClickListener() {
        	@Override
			public void onClick(View arg0) {
        		RunCloud();
			}
        	});
        
        Button runlocal = (Button)this.findViewById(R.id.runlocal);
        runlocal.setOnClickListener(new Button.OnClickListener() {
        	@Override
			public void onClick(View arg0) {
        		RunLocal();
			}
        	});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    private void RunCloud() {
        startService(new Intent(this, CloudService.class));
      }
    
    private void changelabelcloud(String Spectralnormintent){
    	TextView tv1 = (TextView)this.findViewById(R.id.label);
    	tv1.setText(Spectralnormintent);
    }
    
    private void RunLocal() {
        startService(new Intent(this, LocalService.class));
      }
    
    private void changelabellocal(String Spectralnormintent){
    	TextView tv1 = (TextView)this.findViewById(R.id.labelLocal);
    	tv1.setText(Spectralnormintent);
    }
    
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	
            boolean iscloud = intent.getBooleanExtra("iscloud", true);
        	if(iscloud){
        		String Spectralnormintent = intent.getStringExtra("Spectralnormintent");
                
                changelabelcloud(Spectralnormintent);
        	}else{
        		String SpectralnormLocalintent = intent.getStringExtra("Spectralnormintent");
                
                changelabellocal(SpectralnormLocalintent);
        	}
        	try {
                Uri notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
                android.media.Ringtone r = android.media.RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {}
        }
     	}

      @Override 
      public void onResume() {
        IntentFilter filter;
        filter = new IntentFilter(MainActivity.changelabel);
        receiver = new MessageReceiver();
        registerReceiver(receiver, filter);

        super.onResume();
      }

      @Override
      public void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
      }
}
