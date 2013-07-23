package com.cloud.cloudTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;


import java.util.ArrayList;
import java.util.List;

public class PrimeFactors extends CloudRemotable  {


  @Cloud
  public void localfindPrimeFactors() {
        int[] numbers = { 453463, 47895, 2345, 3873, 3452, 234508, 1231551, 12453151, 1253562621, 12451511, 1241512 };
        List<Integer> factors = new ArrayList<Integer>();

        for (Integer num : numbers) {
                for (int i = 2; i <= num; i++) {
                        while (num % i == 0) {
                                factors.add(i);
                                num /= i;
                        }
                }
        }

//        for (Integer integer : factors) {
//                System.out.println(integer);
//        }
}

  public void findPrimeFactors() {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localfindPrimeFactors", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localfindPrimeFactors();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	PrimeFactors localstate = (PrimeFactors) state;
}
}
