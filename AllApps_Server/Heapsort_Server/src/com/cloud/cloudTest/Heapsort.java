package com.cloud.cloudTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;


/**
*
* @author Barca
*/
import java.text.*;
import java.lang.reflect.Array;

//heapsort with 10000000 random numbers

public class Heapsort extends CloudRemotable  {

   public static final long IM = 139968;
   public static final long IA =   3877;
   public static final long IC =  29573;

   @Cloud
   public void localRunHeapsort() {
       
	int N = Integer.parseInt("10000000");
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(10);
	nf.setMinimumFractionDigits(10);
	nf.setGroupingUsed(false);
	double []ary = (double[])Array.newInstance(double.class, N+1);
	for (int i=1; i<=N; i++) {
	    ary[i] = gen_random(1);
	}

	heapsort(N, ary);
       
   }

   public static long last = 42;
   public static double gen_random(double max) {
	return( max * (last = (last * IA + IC) % IM) / IM );
   }

   public static void heapsort(int n, double ra[]) {
	int l, j, ir, i;
	double rra;

	l = (n >> 1) + 1;
	ir = n;
	for (;;) {
	    if (l > 1) {
		rra = ra[--l];
	    } else {
		rra = ra[ir];
		ra[ir] = ra[1];
		if (--ir == 1) {
		    ra[1] = rra;
		    return;
		}
	    }
	    i = l;
	    j = l << 1;
	    while (j <= ir) {
		if (j < ir && ra[j] < ra[j+1]) { ++j; }
		if (rra < ra[j]) {
		    ra[i] = ra[j];
		    j += (i = j);
		} else {
		    j = ir + 1;
		}
	    }
	    ra[i] = rra;
	}
   }

   public void RunHeapsort() {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localRunHeapsort", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localRunHeapsort();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	Heapsort localstate = (Heapsort) state;
	this.last = localstate.last;
}
}
