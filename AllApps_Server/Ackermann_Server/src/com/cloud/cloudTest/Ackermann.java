package com.cloud.cloudTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;


/**
*
* @author Barca
*/

//Ackermann function for non-negative integers with m = 3 and n = 10

public class Ackermann extends CloudRemotable  {

   @Cloud
   public void localRunAckermann() {
	int num = Integer.parseInt("10");
	Ack(3, num);

   }
   public static int Ack(int m, int n) {
	return (m == 0) ? (n + 1) : ((n == 0) ? Ack(m-1, 1) :
				     Ack(m-1, Ack(m, n - 1)));
   }

   public void RunAckermann() {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localRunAckermann", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localRunAckermann();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	Ackermann localstate = (Ackermann) state;
}
}
