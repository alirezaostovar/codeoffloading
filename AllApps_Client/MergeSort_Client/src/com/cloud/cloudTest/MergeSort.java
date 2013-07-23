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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
*
* @author Barca
*/

//MergeSort with 1000000 random numbers

public class MergeSort extends CloudRemotable  {

   public static final long IM = 139968;
   public static final long IA =   3877;
   public static final long IC =  29573;

   @Cloud
   public void localRunMergeSort() {

	int N = Integer.parseInt("1000000");
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(10);
	nf.setMinimumFractionDigits(10);
	nf.setGroupingUsed(false);
	double []ary = (double[])Array.newInstance(double.class, N+1);
	for (int i=1; i<=N; i++) {
	    ary[i] = gen_random(1);
	}

	mergeSort(ary);
      
   }

   public static long last = 42;
   public static double gen_random(double max) {
	return( max * (last = (last * IA + IC) % IM) / IM );
   }

  public static double[] mergeSort(double[] data){
     int lenD = data.length;
     if(lenD<=1){
       return data;
     }
     else{
       double[] sorted = new double[lenD];
       int middle = lenD/2;
       int rem = lenD-middle;
       double[] L = new double[middle];
       double[] R = new double[rem];
       System.arraycopy(data, 0, L, 0, middle);
       System.arraycopy(data, middle, R, 0, rem);
       L = mergeSort(L);
       R = mergeSort(R);
       sorted = merge(L, R);
       return sorted;
     }
   }

   public static double[] merge(double[] L, double[] R){
     int lenL = L.length;
     int lenR = R.length;
     double[] merged = new double[lenL+lenR];
     int i = 0;
     int j = 0;
     while(i<lenL||j<lenR){
       if(i<lenL & j<lenR){
         if(L[i]<=R[j]){
           merged[i+j] = L[i];
           i++;
         }
         else{
           merged[i+j] = R[j];
           j++;
         }
       }
       else if(i<lenL){
         merged[i+j] = L[i];
         i++;
       }
       else if(j<lenR){
         merged[i+j] = R[j];
         j++;
        }
      }
      return merged;
   }

   public void RunMergeSort() {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localRunMergeSort", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localRunMergeSort();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	MergeSort localstate = (MergeSort) state;
	this.last = localstate.last;
}
}
