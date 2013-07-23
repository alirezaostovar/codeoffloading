package com.cloud.cloudTest;


import java.util.Vector;

import com.cloud.cloudTest.MainActivity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class LocalService extends Service {
  
	private static Vector<Double> execTimes= new Vector<Double>();
	
	Thread Runningthread = null;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // Retrieve the shared preferences
//    Context context = getApplicationContext();
//    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      
	  Runningthread = new Thread(new RunningThread());
	  Runningthread.start();
      return Service.START_STICKY;
  };

  private class RunningThread implements Runnable {
	
	@Override
	public void run() {
		RunFuncs();
	}
  };

  @Override
  public void onCreate() {
	  
  }
	
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  
  private void RunFuncs() {
	  	  
	  execTimes.removeAllElements();

      double avgAckermannTime = 0;
      for (int i = 0; i < 4; i++){
    	  long internalstTime = System.nanoTime();

    	  Ackermann pf = new Ackermann();
          pf.localRunAckermann();

          execTimes.add((System.nanoTime() - internalstTime)*1.0e-6);
      }
      avgAckermannTime = avg(execTimes);



      notifytochangelable("Average Ackermann Time: " + avgAckermannTime);
  }
    
  static double avg(Vector<Double> nums){
      double avg = 0;
      for(int i = 0; i < nums.size(); i++){
          avg += (double)nums.get(i);
      }

      avg = avg / nums.size();
      return avg;
  }

  public void notifytochangelable(String AckermannTime){
		Intent intent = new Intent(MainActivity.changelabel);
	    intent.putExtra("iscloud", false);
	    intent.putExtra("Ackermannintent", AckermannTime);
	    
	    sendBroadcast(intent);
	  }

}
