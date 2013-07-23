package com.cloud.cloudTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;



import java.lang.reflect.Array;
import java.text.NumberFormat;

/**
*
* @author Barca
*/

//SelectionSort with 100000 random numbers

public class SelectionSort extends CloudRemotable  {
   
   public static final long IM = 139968;
   public static final long IA =   3877;
   public static final long IC =  29573;

   @Cloud
   public void localRunSelectionSort() {

	int N = Integer.parseInt("100000");
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(10);
	nf.setMinimumFractionDigits(10);
	nf.setGroupingUsed(false);
	double []ary = (double[])Array.newInstance(double.class, N+1);
	for (int i=1; i<=N; i++) {
	    ary[i] = gen_random(1);
	}

	selectionSort(ary);
//	System.out.print(nf.format(ary[N]) + "\n");

   }

   public static long last = 42;
   public static double gen_random(double max) {
	return( max * (last = (last * IA + IC) % IM) / IM );
   }
   
   public static double[] selectionSort(double[] data){
     int lenD = data.length;
     int j = 0;
     double tmp = 0;
     for(int i=0;i<lenD;i++){
       j = i;
       for(int k = i;k<lenD;k++){
         if(data[j]>data[k]){
           j = k;
         }
       }
       tmp = data[i];
       data[i] = data[j];
       data[j] = tmp;
     }
     return data;
   }

   public void RunSelectionSort() {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localRunSelectionSort", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localRunSelectionSort();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	SelectionSort localstate = (SelectionSort) state;
	this.last = localstate.last;
}
}
