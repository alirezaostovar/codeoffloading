package com.cloud.cloudTest.Nbody;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;



/**
 *
 * @author Barca
 */

// nbody with n = 5685547

public final class Nbody extends CloudRemotable  {

    @Cloud
    public void localRunNbody() {

        int n = Integer.parseInt("5685547");

        NBodySystem bodies = new NBodySystem();
        bodies.energy();
//        System.out.printf("%.9f\n", bodies.energy());
        for (int i=0; i<n; ++i)
           bodies.advance(0.01);
        
        bodies.energy();
//        System.out.printf("%.9f\n", bodies.energy());

    }

    public void RunNbody() {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localRunNbody", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localRunNbody();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	Nbody localstate = (Nbody) state;
}
}




