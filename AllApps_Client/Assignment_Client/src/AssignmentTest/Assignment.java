package AssignmentTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;


import com.cloud.cloudTest.Cloud;


/**
* AssignmentTest
*
* Perform the assignment benchmark for the jBYTEmark
* benchmark. The algorithm presented here was adapted
* from the step-by-step guide found in "Quantitative
* Decision Making for Business" (Gordon, Pressman, and
* Cohn; Prentice-Hall)
*
* As in the original benchmark, this test preserves the
* "squareness" requirements of the arrays.
*
* Test result is in iterations per second.
*/

public class Assignment
 extends CloudRemotable {

// The array of arrays.

private int [][][] aarray;

// Instance variables common to all tests follow


/**
* constructor
*
*/

public Assignment()
{
    adjust_flag = BMglobals.assignadjust; // Has test adjusted to clock?
                                          // Default is false at first,
                                          // so adjust.

    testname = "CPU: Assignment";          // Set test name.

    // Set instance variables to "starting" values.

    array_rows = BMglobals.assignrows;  // Number of row (default 51).

    array_cols = BMglobals.assigncols;  // Number of cols (default 51).

    base_iters_per_sec = BMglobals.asgntestbase; // Score indexing value.

    num_arrays = BMglobals.assignarrays;    // Number of arrays.
                                            // Default 1 at start.
}

public static void main(String[] args) {

    System.out.println("\nAverage Assignment Time: " + new Assignment().dotest());
}

/*
* initialize
*
*/

void initialize()
{
    // We'll need new random numbers.

    RandNum rndnum = new RandNum();

    // Allocate the array or arrays.

    aarray = new int [num_arrays][array_rows][array_cols];

    // Garbage collect.

    System.gc();

    // Set up the first array. Then, if there are more arrays,
    // just copy the first to all the rest.

    for (int i = 0; i < array_rows; i++)
        for (int j = 0; j < array_cols; j++)
            aarray[0][i][j] = rndnum.abs_nextwc(50000);

    if (num_arrays > 1)
        for (int i = 1; i < num_arrays; i++)
            for(int j = 0; j < array_rows; j++)
                System.arraycopy(aarray[0][j],0,aarray[i][j],0,array_cols);
}

/*
* DoIteration
*
*/

private long DoIteration()
{
long testTime;      // Duration of the test (milliseconds).

// Start the stopwatch.

testTime = System.currentTimeMillis();

// Step through each of the assignment arrays. Do an assignment
// operation on each.

for (int i = 0; i < num_arrays; i++)
    assignment(i);

testTime = System.currentTimeMillis() - testTime;

return (testTime);

}

/**
* dotest
*
* Perform the actual assignment benchmark test.
* The steps involved are:
*  1. See if the test has already been "adjusted".
*     If it hasn't do an adjustment phase.
*  2. If the test has been adjusted, go ahead and
*     run it.
*/

@Cloud
public long localdotest()
{

long duration;      // Time in milliseconds for doing test.

// Has it been adjusted yet?

if (adjust_flag == false)
{                                   // Do adjustment phase.
    while (true)
    {
        initialize();

        // See if the current setting meets requirements.

        duration = DoIteration();
        if (duration > BMglobals.minTicks / 10) // minTicks = 100 ticks.
            break;                              // We'll take just 10.

        // Current setting does not meet requirements.
        // Increase # of arrays and try again.

        num_arrays += 1;
    }

    // Scale up to whatever it takes to do 100 clock ticks.

    int factor = (int) (BMglobals.minTicks / duration);
    num_arrays += (factor * num_arrays);

    adjust_flag = true;                // Don't adjust next time.

}   // End adjust section.

// If we fall through preceding IF statement, adjustment
// not necessary -- simply initialize.

initialize();

// Do the test.

duration = DoIteration();

// Calculate the iterations per second. Note that we
// assume here that duration is in milliseconds.

iters_per_sec = (double)num_arrays / ((double)duration
                / (double)1000);

// Debug code.

if (debug_on == true)
{
    System.out.println("--- Assignment test debug data ---");
    System.out.println("Number of arrays: " + num_arrays);
    System.out.println("Elapsed time: " + duration);
    System.out.println("Iterations per sec: " + iters_per_sec);

} //End debug code.

// Clean up and exit.

cleanup();

return duration;

}

/*
* cleanup
*
*/

void cleanup()
{
    aarray = new int [0][0][0];   // Empty the array.

    System.gc();                  // Garbage collect it.
}

/*
* assignment
*
*/

private void assignment(int anum)
{
short [][] assignedtableau;

// Set up assignedtableau.

assignedtableau = new short [array_rows][array_cols];

// Calculate minimum costs.

calc_minimum_costs(anum);


// Repeat the following until the number of rows selected
// equals the number of rows in the tableau.

while (first_assignments(anum,assignedtableau) != array_rows)
{
    second_assignments(anum,assignedtableau);
}

}

/*
* calc_minimum_costs
*
* Revise the tableau by calculating the minimum costs on a
* row and column bases. These minima are subtracted from
* their rows and columns, creating a new tableau.
*/

private void calc_minimum_costs(int anum)
{

int currentmin; // Current minimum.

// Determine minimum costs on row basis. This is done by
// subtracting -- on a row-per-row basis, the minimum
// value of that row.

for (int i = 0; i < array_rows; i++)
{
    currentmin = 5000001; // Initialize minimum.
    for (int j = 0; j < array_cols; j++)
        if (aarray[anum][i][j] < currentmin)
            currentmin = aarray[anum][i][j];

    for(int j = 0; j < array_cols; j++)
        aarray[anum][i][j] -= currentmin;
}

// Determine the minimum cost on a column basis. This works just
// as above, only now we step through the array column-wise.

for (int j = 0; j < array_cols; j++)
{
    currentmin = 5000001;       // Initialize minimum.
    for (int i = 0; i < array_rows; i++)
        if (aarray[anum][i][j] < currentmin)
            currentmin = aarray[anum][i][j];

    // Here we'll take the trouble to see if the current
    // minimum is zero. This is likely worth it, since the
    // preceding loop will have created at least one zero in
    // each row. We can save ourselves a few iterations.

    if (currentmin != 0)
        for (int i = 0; i < array_rows; i++)
            aarray[anum][i][j] -= currentmin;
}

}

/*
* first_assignments
*
* Do first assignments.
* The assignedtableau[] array holds a set of values that indicate
* the assignment of a value, or its elimination.
* The values are:
*  0 = Item is neither assigned nor eliminated
*  1 = Item is assigned
*  2 = Item is eliminated
* Retuns the number of selections made. If this equals
* the number of rows, then an optimum has been determined.
*/

private int first_assignments(int anum,short [][] assignedtableau)
{
int i,j,k;                  // Index variables.
int numassigns;             // # of assignments.
int totnumassigns;          // Total # of assignments.
int numzeros;               // # of zeros in row.
int selected;               // Flag used to indicate selection.

// Clear the assignedtableau, setting all members to show
// that no one is yet assigned, eliminated, or anything.

for (i = 0; i < array_rows; i++)
    for (j = 0; j < array_rows; j++)
        assignedtableau[i][j] = 0;
totnumassigns = 0;

selected = 0;     // Make java happy.

do {
    numassigns = 0;

    // Step through rows. For each one that is not currently
    // assigned, see if the row has only one zero in it. If so,
    // mark that as an assigned row/col. Eliminate other zeros
    // in the same column.

    for (i = 0; i < array_rows; i++)
    {   numzeros = 0;
        for (j = 0; j < array_cols; j++)
            if (aarray[anum][i][j] == 0L)
                if (assignedtableau[i][j] == 0)
                {
                    numzeros++;
                    selected = j;
                }

        if (numzeros == 1)
        {
            numassigns++;
            totnumassigns++;
            assignedtableau[i][selected] = 1;
            for (k = 0; k < array_rows; k++)
                if ((k != i) && (aarray[anum][k][selected] == 0))
                    assignedtableau[k][selected] = 2;
        }
    }

    // Step through columns, doing same as above. Now, be careful
    // of items in the other rows of a selected column.

    for (j = 0; j < array_cols; j++)
    {   numzeros = 0;
        for (i = 0; i < array_rows; i++)
            if (aarray[anum][i][j] == 0)
                if (assignedtableau[i][j] == 0)
                {
                    numzeros++;
                    selected = i;
                }

        if (numzeros == 1)
        {   numassigns++;
            totnumassigns++;
            assignedtableau[selected][j] = 1;
            for (k = 0; k < array_cols; k++)
                if((k != j) && (aarray[anum][selected][k] == 0))
                    assignedtableau[selected][k] = 2;
        }
    }

    // Repeat until no more assignments to be made.

} while (numassigns != 0);

// See if we can leave at this point.

if (totnumassigns == array_rows) return (totnumassigns);

// Now step through the array by row. If you find any
// unassigned zeros, pick the first in the row. Eliminate
// all zeros from that same row and column. This occurs if
// there are multiple optima...possibly.

for (i = 0; i < array_rows; i++)
{   selected = -1;
    for (j = 0; j < array_cols; j++)
        if ((aarray[anum][i][j] == 0) && (assignedtableau[i][j] == 0))
        {
            selected = j;
            break;
        }

    if (selected != -1)
    {
        assignedtableau[i][selected] = 1;
        totnumassigns++;
        for (k = 0; k < array_cols; k++)
            if ((k != selected) && (aarray[anum][i][k] == 0))
                assignedtableau[i][k] = 2;
        for (k = 0; k < array_rows; k++)
            if ((k != i) && (aarray[anum][k][selected] == 0))
                assignedtableau[k][selected] = 2;
    }
}
return (totnumassigns);
}

/*
* second_assignments
*
* This section of the algorithm is difficult to explain.
* It creates the revised tableau. I suggest you refer to
* the algorithm's source, mentioned in comments at the
* beginning of this file.
*/

private void second_assignments(int anum, short [][] assignedtableau)
{
int i,j;                    // Indexes.
short [] linesrow = new short[array_rows];
short [] linescol = new short[array_cols];

int smallest;               // Holds smallest value.
int numassigns;             // Number of assignments.
int newrows;                // New rows to be considered.

// Scan rows, flag each row that has no assignment in it.

for (i = 0; i < array_rows; i++)
{   numassigns = 0;
    for (j = 0; j < array_cols; j++)
        if (assignedtableau[i][j] == 1)
        {
            numassigns++;
            break;
        }
    if (numassigns == 0) linesrow[i] = 1;
}

do {
    newrows = 0;

    // For each row checked above, scan for any zeros. If found,
    // check the associated column.

    for (i = 0; i < array_rows; i++)
    {
        if (linesrow[i] == 1)
            for (j = 0; j < array_cols; j++)
                if (aarray[anum][i][j] == 0)
                    linescol[j] = 1;
    }

    // Now scan checked columns. If any contain assigned zeros, check
    // the associated row.

    for (j = 0; j < array_cols; j++)
        if (linescol[j] == 1)
            for (i = 0; i < array_cols; i++)
                if ((assignedtableau[i][j] == 1) && (linesrow[i] != 1))
                {
                    linesrow[i] = 1;
                    newrows++;
                }

} while (newrows != 0);

// linesrow[n] == 0 indicate rows covered by imaginary line.
// linescol[n] == 1 indicate cols covered by imaginary line.
// For all cells not covered by imaginary lines, determine smallest
// value.

smallest = 5000001;
for (i = 0; i < array_rows; i++)
    if (linesrow[i] != 0)
        for (j = 0; j < array_cols; j++)
            if (linescol[j] != 1)
                if (aarray[anum][i][j] < smallest)
                    smallest = aarray[anum][i][j];

// Subtract the smallest from all cells in the above set.

for (i = 0; i < array_rows; i++)
    if (linesrow[i] != 0)
        for (j = 0; j < array_cols; j++)
            if (linescol[j] != 1)
                aarray[anum][i][j] -= smallest;

// Add smallest to all cells covered by two lines.

for (i = 0; i < array_rows; i++)
    if (linesrow[i] == 0)
        for (j = 0; j < array_cols; j++)
            if (linescol[j] == 1)
                aarray[anum][i][j] += smallest;
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
	Assignment
 localstate = (Assignment
) state;
	this.aarray = localstate.aarray;
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
