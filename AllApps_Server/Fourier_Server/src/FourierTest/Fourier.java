/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FourierTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;


import com.cloud.cloudTest.Cloud;

/**
* FourierTest
*
* Performs the transcendental/trigonometric portion of the
* benchmark. This test calculates the first n fourier
* coefficients of the function (x+1)^x defined on the interval
* 0,2 (where n is an arbitrary number that is set to make the
* test last long enough to be accurately measured by the system
* clock). Results are reported in number of coefficients calculated
* per sec.
*
* The first four pairs of coefficients calculated shoud be:
* (2.83777, 0), (1.04578, -1.8791), (0.2741, -1.15884), and
* (0.0824148, -0.805759).
*/

public class Fourier
 extends CloudRemotable {

// Declare class data.

private double [] [] TestArray;  // Array of arrays.

// The array holds the A and B coefficients from the fourier
// calculations. It's a double array that will be instantiated
// to a length specified by the variable array_rows, which starts
// at a length of 100 and it incremented by 50 until the test
// lasts long enough for accurate measurement.

/**
* constructor
* This routine sets the adjustment flag to false (indicating
* that the initialize method hasn't yet adjusted the number of
* arrays to the clock accuracy, and initializes
* the test name (for error context reasons).
* It also loads up important instance variables with their
* "starting" values. Variables in this method are all instance
* variables.
*/

public Fourier()
{
    testname = "FPU: Fourier";  // Set test name for error context.

    array_rows = BMglobals.fourarraysize;   // Default is 100.

    base_iters_per_sec = BMglobals.fourtestbase; // Score indexing value.

    adjust_flag = BMglobals.fouradjust; // Has test adjusted to clock?
                                        // Default is false at first,
                                        // so adjust.
}


//public static void main(String[] args) {
//
//    System.out.println("\nAverage Fourier Time: " + new Fourier().dotest());
//}

/*
* initialize
*
* Sets the size (array_rows) of the double array in preparation
* for the test. Repeatedly runs a single iteration of the test
* with with a progressively larger array (by 50 elements) until
* the elapsed time falls within the accuracy of the system clock.
* It's two-step process: Increment the data size linearly until
* elapsed time is ten times the duration of a measurable clock
* tick, and then calculate the data size to get 100 times the
* minimum clock accuracy. Once complete, the test array is ready
* to go for the actual test.
*/

void initialize()
{

    long duration;  // Elapsed time (ms) to do doing test iteration.

    while (true)
    {
        // Increment data size until test runs for ten clock ticks
        // Clock tick is minimum measurable interval in milliseconds
        // of the system clock. Global variable minTicks is 100
        // times accuracy of system clock in milliseconds.

        duration = DoIteration();
        if (duration > BMglobals.minTicks/10)   // minTicks is 100 ticks.
            break;                              // We want just 10.

        // Current setting does not meet clock requrements.
        // Increase length of arrays and try again.

        array_rows += 50;

        // Clean up memory used by array.

        freeTestData(); // Nulls array and forces garbage collection.

        // Build new arrays (with one more this time).

        buildTestData();
    }

    // Scale up to whatever it takes to do 100 clock ticks.

    int factor = (int) (BMglobals.minTicks / duration);
    array_rows += (factor * array_rows);

    // Clean up memory used by arrays.

    freeTestData();

    // Build new arrays using final adjusted size.

    buildTestData();

    // Flag so that other iterations of test don't have to repeat this.

    adjust_flag = true;
}

/*
* buildTestData
*
*/

// Instantiate array(s) to hold fourier coefficients.

private void buildTestData()
{
    // Allocate appropriate length for the double array of doubles.

    TestArray = new double [2][array_rows];
}

/**
* dotest
*
* Perform the actual benchmark test.
* The steps involved are:
* 1. See if the test has already been "adjusted".
*    If it hasn't do an adjustment phase (initialize()).
* 2. If the test has been adjusted, go ahead and
*    run it.
*/

@Cloud
public long localdotest()
{
    long duration;          // Time in milliseconds for doing test.
    int iterations;         // Number of coefficents calculated.

    // Create array(s).

    buildTestData();

    if(adjust_flag == false)    // Has it been initialized yet?
        initialize();           // Adjust number of arrays to clock.

    duration=DoIteration();

    // Calculate the coefficients calculated per second (the instance
    // variable iters_per_sec). Note that we assume here that duration
    // is in milliseconds.

    iterations = (array_rows * 2) - 1;      // Number of coefficients.
    iters_per_sec=(double)iterations / ((double)duration /
        (double)1000);

    // Debug code: Prints out first 4 pairs of calculated coefficients.

    if (debug_on == true)
    {
        System.out.println("--- Fourier Test debug data ---");
        System.out.println("Number of coefficients calculated: "
                            + iterations);
        System.out.println("Elapsed time (ms): " + duration);
        System.out.println("Coefficients per sec: " + iters_per_sec);

        // Print out pairs of calculated coefficients.

        System.out.println("First four pairs of coefficients:");
        for (int i = 0; i < 4; i++)
        {
            System.out.print("(" + TestArray[0][i] + ", ");
            System.out.println(TestArray[1][i] + ")");
        }
        System.out.print("\n");

    } //End debug code.

    // Clean up and exit.

    cleanup();

    return duration;
}

/*
* DoIteration
*
* Perform an iteration of the FPU Transcendental/trigonometric
* benchmark. Here, an iteration consists of calculating the
* first n pairs of fourier coefficients of the function (x+1)^x on
* the interval 0,2. n is given by array_rows, the array size.
* NOTE: The # of integration steps is fixed at 200. Returns the
* elapsed time in milliseconds.
*/

private long DoIteration()
{
    long testTime;      // Duration of the test (milliseconds).
    double omega;       // Fundamental frequency.

    // Start the stopwatch.

    testTime=System.currentTimeMillis();

    // Calculate the fourier series. Begin by calculating A[0].

    TestArray[0][0]=TrapezoidIntegrate((double)0.0, // Lower bound.
                            (double)2.0,            // Upper bound.
                            200,                    // # of steps.
                            (double)0.0,            // No omega*n needed.
                            0) / (double)2.0;       // 0 = term A[0].

    // Calculate the fundamental frequency.
    // ( 2 * pi ) / period...and since the period
    // is 2, omega is simply pi.

    omega = (double) 3.1415926535897932;

    for (int i = 1; i < array_rows; i++)
    {
        // Calculate A[i] terms. Note, once again, that we
        // can ignore the 2/period term outside the integral
        // since the period is 2 and the term cancels itself
        // out.

        TestArray[0][i] = TrapezoidIntegrate((double)0.0,
                          (double)2.0,
                          200,
                          omega * (double)i,
                          1);                       // 1 = cosine term.

        // Calculate the B[i] terms.

        TestArray[1][i] = TrapezoidIntegrate((double)0.0,
                          (double)2.0,
                          200,
                          omega * (double)i,
                          2);                       // 2 = sine term.
    }


    // Stop the stopwatch.

    testTime=System.currentTimeMillis() - testTime;

    return(testTime);   // Returns elapsed time for test iteration.
}

/*
* TrapezoidIntegrate
*
* Perform a simple trapezoid integration on the function (x+1)**x.
* x0,x1 set the lower and upper bounds of the integration.
* nsteps indicates # of trapezoidal sections.
* omegan is the fundamental frequency times the series member #.
* select = 0 for the A[0] term, 1 for cosine terms, and 2 for
* sine terms. Returns the value.
*/

private double TrapezoidIntegrate (double x0,     // Lower bound.
                        double x1,                // Upper bound.
                        int nsteps,               // # of steps.
                        double omegan,            // omega * n.
                        int select)               // Term type.
{
    double x;               // Independent variable.
    double dx;              // Step size.
    double rvalue;          // Return value.

    // Initialize independent variable.

    x = x0;

    // Calculate stepsize.

    dx = (x1 - x0) / (double)nsteps;

    // Initialize the return value.

    rvalue = thefunction(x0, omegan, select) / (double)2.0;

    // Compute the other terms of the integral.

    if (nsteps != 1)
    {
            --nsteps;               // Already done 1 step.
            while (--nsteps > 0)
            {
                    x += dx;
                    rvalue += thefunction(x, omegan, select);
            }
    }

    // Finish computation.

    rvalue=(rvalue + thefunction(x1,omegan,select) / (double)2.0) * dx;
    return(rvalue);
}

/*
* thefunction
*
* This routine selects the function to be used in the Trapezoid
* integration. x is the independent variable, omegan is omega * n,
* and select chooses which of the sine/cosine functions
* are used. Note the special case for select=0.
*/

private double thefunction(double x,      // Independent variable.
                double omegan,              // Omega * term.
                int select)                 // Choose type.
{

    // Use select to pick which function we call.

    switch(select)
    {
        case 0: return(Math.pow(x+(double)1.0,x));

        case 1: return(Math.pow(x+(double)1.0,x) * Math.cos(omegan*x));

        case 2: return(Math.pow(x+(double)1.0,x) * Math.sin(omegan*x));
    }

    // We should never reach this point, but the following
    // keeps compilers from issuing a warning message.

    return (0.0);
}

/*
* freeTestData
*
* Nulls array that is created with every run and forces garbage
* collection to free up memory.
*/

private void freeTestData()
{
    TestArray = null;    // Destroy the array.
    System.gc();         // Force garbage collection.
}


/*
* cleanup
*
* Clean up after the benchmark. This simply calls freeTestData,
* which nulls the test arrays and forces system garbage collection.
* This is a required BmarkTest class method (abstract in super).
*/

void cleanup()
{
    freeTestData();
}


boolean adjust_flag; // Initially set false. Set to true
                                  // if the adjustment phase has
                                  // has already occurred on this
                                  // object.
double iters_per_sec = 0;   // Iterations per second. This is
                        // the calculated result of the
                        // current test.

double base_iters_per_sec = 0;  // Baseline iterations per sec. Used
                            // to calculate the index and based
                            // on some standard system.

int numtries = 0;           // # of tries attempted.

double scores[] =       // Array of five scores for confidence
   {0,0,0,0,0};         //  interval testing.

double mean = 0;            // Mean of the scores.

double stdev = 0;           // Standard deviation of scores.

int num_arrays = 0;         // # of arrays used in a test.

int array_rows = 0;         // Size of array in rows. This is also
                        // the variable used to indicate array
                        // size for 1D array tests.

int array_cols = 0;         // Size of array in columns.

int loops_per_iter = 0;         // Some tests increase workload by
                        // adding loops. This holds the # of
                        // loops used in the test.

String testname = "";        // Name of current test.

// Flag to run test in debug mode

boolean debug_on = false;

// Flag to indicate successful test.

boolean testConfidence = true;

/**
* dotest (debug version)
*
* Do the test with debug error checking. This is an overload of the
* dotest() method and is usually the same code from test to test so
* abstract isn't appropriate.
*/

void dotest (boolean debug_on)
{
    this.debug_on = debug_on;
    dotest();
    this.debug_on = false;
}
/**
* benchWithConfidence
*
* Given a reference to a BmarkTest object, this routine
* repeatedly executes its associated benchmark, seeking
* to collect enough scores to get 5 that meet the
* confidence criteria. Return true if OK, else return false.
*/

void benchWithConfidence()
{

double halfinterval;    // Confidence half interval.

// Get first 5 scores. Then begin confidence testing.

for (int i = 0; i < 5; i++)
{
    try
    {
        dotest();        // Run once.
    }
    catch (OutOfMemoryError e)  // Handle test errors/exceptions.
    {
        testConfidence = false; // Flag for bad test.
        throw new OutOfMemoryError(testname + ". Error allocating"
            + " memory for test data.");
    }
}

numtries = 5;                       // Show 5 tries already.

// The system allows a maximum of 10 tries before it
// gives up. Since we've done 5 already, we'll allow
// 5 more.

// Enter loop to test for confidence criteria

while(true)
{
    // Calculate confidence

    halfinterval = calcConfidence();

    // Is half interval 5% or less of mean?
    // If so, we can go home. Otherwise, we
    // have to continue.

    if(halfinterval / mean <= (double).05)
        break;

    // Go get a new score and see if it improves
    // the existing scores.

    do
    {
        if (numtries == 10)
        {
            testConfidence = false;
            return;
        }

        // Run the test

        try
        {
            dotest();               // Run test.
        }
        catch (OutOfMemoryError e)  // Handle test errors/exceptions.
        {
            testConfidence = false; // Flag for bad test.
            throw new OutOfMemoryError(testname + ": Error allocating"
                + " memory for test data.");
        }

        numtries++;

    } while (!seekConfidence());
}

testConfidence = true;
}


/**
* seekConfidence
*
* Given an array of 5 scores PLUS a new score, this routine tries
* the new score in place of each of the other 5 to determine if
* the new score, when replacing one of the others, improves the
* confidence half-interval.
* Return FALSE if failure...original 5 scores unchanged.
* Also calculates new half-interval, mean, and std. deviation.
*/

private boolean seekConfidence()
{

double halfinterval;    // Confidence halfinterval
double stdev_to_beat;   // Standard deviation to be improved
double temp;            // Holding place for score being swapped out
int isbeaten;           // Flag indicating progress; also used to
                        //  hold index of swapped score.

// Calculate original standard deviation.

halfinterval = calcConfidence();
stdev_to_beat = stdev;
isbeaten = -1;

// Try to beat original score by exchanging new score
// with each of the originals one at a time.

for (int i = 0; i < 5; i++)
{
    temp=scores[i];
    scores[i] = iters_per_sec;
    calcConfidence();
    if(stdev_to_beat > stdev)
    {   isbeaten = i;
        stdev_to_beat = stdev;
    }
}

if (isbeaten != -1)
{
    scores[isbeaten] = iters_per_sec;
    return(false);
}

return(true);

}

/**
* calcConfidence
*
* Given a set of 5 scores, calculate the confidence half-interval.
* We'll also calculate the sample mean and sample standard deviation.
* Returns the half-interval. NOTE: This routine presumes a confidence
* of 95% and a confidence coefficient of .95.
*/

private double calcConfidence()
{
double halfinterval;    // Returned half interval value.

// Calculate mean

mean = (scores[0] + scores[1] + scores[2] + scores[3] + scores[4])
       / (double)5;

// Calculate standard deviation -- first get variance.

stdev = (double)0;
for (int i = 0; i < 5; i++)
    stdev += (scores[i] - mean) * (scores[i] - mean);
stdev /= (double) 4;
stdev = Math.sqrt(stdev / (double) 5);

// Now calculate the confidence half-interval. For a confidence
// level of 95% our confidence coefficient gives us a mulitplying
// factor of 2.776. (The upper .025 quartile of a t distribution
// with 4 degrees of freedom.)

halfinterval = stdev * (double) 2.776;
return (halfinterval);
}


public long dotest()
{
	Method toExecute; 
	Class<?>[] paramTypes = null; 
	Object[] paramValues = null; 
	Long result = null;
	try{
		toExecute = this.getClass().getDeclaredMethod("localdotest", paramTypes);
		Vector results = getCloudController().execute(toExecute,paramValues,this,this.getClass());
		if(results != null){
			result = (Long)results.get(0);
			copyState(results.get(1));
		}else{
			result = localdotest();
		}
	}  catch (SecurityException se){
	} catch (NoSuchMethodException ns){
	}catch (Throwable th){
	}
	
	return result;
	}

void copyState(Object state){
	Fourier
 localstate = (Fourier
) state;
	this.TestArray = localstate.TestArray;
	this.adjust_flag = localstate.adjust_flag;
	this.iters_per_sec = localstate.iters_per_sec;
	this.base_iters_per_sec = localstate.base_iters_per_sec;
	this.numtries = localstate.numtries;
	this.scores = localstate.scores;
	this.mean = localstate.mean;
	this.stdev = localstate.stdev;
	this.num_arrays = localstate.num_arrays;
	this.array_rows = localstate.array_rows;
	this.array_cols = localstate.array_cols;
	this.loops_per_iter = localstate.loops_per_iter;
	this.testname = localstate.testname;
	this.debug_on = localstate.debug_on;
	this.testConfidence = localstate.testConfidence;
}
}
