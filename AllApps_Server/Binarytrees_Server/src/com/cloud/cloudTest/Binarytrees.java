package com.cloud.cloudTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;


/**
*
* @author Barca
*/

//Two-thread binarytrees with maxdepth = 18

public class Binarytrees extends CloudRemotable  {

  private final static int minDepth = 4;
  private final static int threadCount = Runtime.getRuntime().availableProcessors() > 1 ? 2 : 1;
  private final static TreeGenerator[] threads = new TreeGenerator[threadCount + 1];


  @Cloud
  public void localRunbinarytrees()
  {
     int n = 18;
     int maxDepth = (minDepth + 2 > n) ? minDepth + 2 : n;

     for (int i = 0; i < threadCount + 1; i++) {
        threads[i] = new TreeGenerator();
        threads[i].start();
     }

     TreeGenerator lastThread = threads[threadCount];
     lastThread.depth = maxDepth + 1;
     lastThread.run = true;
     try {
        synchronized(lastThread) {
           lastThread.notify();
           lastThread.wait();
        }
     } catch (InterruptedException e) {}

//     System.out.println("stretch tree of depth " + lastThread.depth + "\t check: " + lastThread.result);

     lastThread.depth = maxDepth;
     lastThread.run = true;
     try {
        synchronized(lastThread) {
           lastThread.notify();
           lastThread.wait();
        }
     } catch (InterruptedException e) {}

     for (int depth = minDepth; depth <= maxDepth; depth+=2 ) {

        int check = 0;
        int iterations = 1 << (maxDepth - depth + minDepth);
        int length = iterations / threadCount;

        for (int i = 0; i < threadCount; i++) synchronized(threads[i]) {
           threads[i].depth = depth;
           threads[i].start = i * length;
           threads[i].end = (i + 1) * length;
           threads[i].run = true;
           threads[i].notify();
        }
        for (int i = 0; i < threadCount; i++) try {
           synchronized(threads[i]) {
              if (threads[i].run) threads[i].wait();
           }
           check += threads[i].result;
        } catch (InterruptedException e) {}

//        System.out.println((iterations * 2) + "\t trees of depth " + depth + "\t check: " + check);
     }

//     System.out.println("long lived tree of depth " + maxDepth + "\t check: "+ lastThread.result);

     for (int i = 0; i < threadCount + 1; i++) {
        threads[i].terminate = true;
        synchronized(threads[i]) {
           threads[i].notify();
        }
     }
  }

  private static class TreeGenerator extends Thread
  {
     private boolean run = false;
     private boolean terminate = false;

     private int start = 0;
     private int end = 0;
     private int result = 0;
     private int depth;

     private static TreeNode bottomUpTree(int item, int depth)
     {
        TreeNode node = new TreeNode();
        node.item = item;
        if (depth > 0) {
           node.left = bottomUpTree(2 * item - 1, depth - 1);
           node.right = bottomUpTree(2 * item, depth - 1);
        } else {
           node.left = null;
        }
        return node;
     }

     private static int checkItems(TreeNode node)
     {
        if (node.left == null) {
           return node.item;
        } else {
           return node.item + checkItems(node.left) - checkItems(node.right);
        }
     }


     public synchronized void run()
     {
        while (!terminate) {
           if (run) {
              result = 0;
              if (start == end) {
                 result += checkItems(bottomUpTree(start, depth));
              } else for (int i = start; i < end; i++) {
                 result += checkItems(bottomUpTree(i, depth)) + checkItems(bottomUpTree(-i, depth));
              }
              run = false;
              notify();
           }
           try {
              wait();
           } catch (InterruptedException e) {}
        }
     }
  }

  private static class TreeNode
  {
     private int item;
     private TreeNode left, right;
  }

  public void Runbinarytrees()
  {
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	
	try{
		toExecute = this.getClass().getDeclaredMethod("localRunbinarytrees", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			copyState(results.get(1));
		}else{
			localRunbinarytrees();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	}

void copyState(Object state){
	Binarytrees localstate = (Binarytrees) state;
}
}
