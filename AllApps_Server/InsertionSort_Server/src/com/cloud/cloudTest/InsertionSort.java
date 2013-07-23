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

/**
*
* @author Barca
*/
public class InsertionSort extends CloudRemotable  {

   public static final long IM = 139968;
   public static final long IA =   3877;
   public static final long IC =  29573;

   @Cloud
   public void localRunInsertionSort() {

	int N = Integer.parseInt("100000");
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(10);
	nf.setMinimumFractionDigits(10);
	nf.setGroupingUsed(false);
	double []ary = (double[])Array.newInstance(double.class, N+1);
	for (int i=1; i<=N; i++) {
	    ary[i] = gen_random(1);
	}

	InsertionSort(ary);
//	System.out.print(nf.format(ary[N]) + "\n");

   }

   public static long last = 42;
   public static double gen_random(double max) {
	return( max * (last = (last * IA + IC) % IM) / IM );
   }

   public static double[] InsertionSort(double[] data){
     int len = data.length;
     double key = 0;
     int i = 0;
     for(int j = 1;j<len;j++){
       key = data[j];
       i = j-1;
       while(i>=0 && data[i]>key){
         data[i+1] = data[i];
         i = i-1;
         data[i+1]=key;
       }
     }
     return data;
   }

   public void RunInsertionSort() {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localRunInsertionSort", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localRunInsertionSort();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	InsertionSort localstate = (InsertionSort) state;
	this.last = localstate.last;
}
}

