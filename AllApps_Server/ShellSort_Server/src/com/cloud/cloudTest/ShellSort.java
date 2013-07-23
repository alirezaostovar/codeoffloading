package com.cloud.cloudTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;



/**
 *
 * @author Barca
 */
import java.lang.reflect.Array;
import java.text.NumberFormat;


// ShellSort with 10000000 random numbers

public class ShellSort extends CloudRemotable  {

    public static final long IM = 139968;
    public static final long IA =   3877;
    public static final long IC =  29573;

    @Cloud
    public void localRunShellSort() {

	int N = Integer.parseInt("10000000");
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(10);
	nf.setMinimumFractionDigits(10);
	nf.setGroupingUsed(false);
	double []ary = (double[])Array.newInstance(double.class, N+1);
	for (int i=1; i<=N; i++) {
	    ary[i] = gen_random(1);
	}

        shellSort(ary);

    }

    public static long last = 42;
    public static double gen_random(double max) {
	return( max * (last = (last * IA + IC) % IM) / IM );
    }

   public static double[] shellSort(double[] data){
      int lenD = data.length;
      int inc = lenD/2;
      while(inc>0){
        for(int i=inc;i<lenD;i++){
          double tmp = data[i];
          int j = i;
          while(j>=inc && data[j-inc]>tmp){
            data[j] = data[j-inc];
            j = j-inc;
          }
          data[j] = tmp;
        }
        inc = (inc /2);
      }
      return data;
    }

    public void RunShellSort() {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localRunShellSort", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localRunShellSort();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	ShellSort localstate = (ShellSort) state;
	this.last = localstate.last;
}
}
