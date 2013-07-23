package com.cloud.cloudTest;


import java.util.Vector;

import com.cloud.cloudTest.MainActivity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class CloudService extends Service {
  
	private static Vector<Double> execTimes= new Vector<Double>();
	
	Thread Clientthread = null;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // Retrieve the shared preferences
//    Context context = getApplicationContext();
//    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      
	  Clientthread = new Thread(new ClientThread());
	  Clientthread.start();
      return Service.START_STICKY;
  };

  private class ClientThread implements Runnable {
	
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

      double avgMergeSortTime = 0;
      for (int i = 0; i < 4; i++){
          long internalstTime = System.nanoTime();

          MergeSort pf = new MergeSort();
          pf.RunMergeSort();

          execTimes.add((System.nanoTime() - internalstTime)*1.0e-6);
      }
      avgMergeSortTime = avg(execTimes);


      notifytochangelable("Average MergeSort Time: " + avgMergeSortTime);
  }
    
  static double avg(Vector<Double> nums){
      double avg = 0;
      for(int i = 0; i < nums.size(); i++){
          avg += (double)nums.get(i);
      }

      avg = avg / nums.size();
      return avg;
  }

  public void notifytochangelable(String MergeSortTime){
		Intent intent = new Intent(MainActivity.changelabel);
	    intent.putExtra("iscloud", true);
	    intent.putExtra("MergeSortintent", MergeSortTime);
	    
	    sendBroadcast(intent);
	  }

}
