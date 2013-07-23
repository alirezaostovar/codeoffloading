/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package HuffmanTest;

import java.lang.reflect.Method; 
import MC.NetClasses.CloudRemotable;
import java.util.Vector;


import java.io.Serializable;
import com.cloud.cloudTest.Cloud;



/**
* HuffmanTest
*
* Perform the Huffman compression benchmark for the jBYTEmark
* benchmark.
*/

public class Huffman
 extends CloudRemotable {

// Plaintext, compression, and decompression array.

byte [] plaintext;          // Plain text.
byte [] comparray;          // Compression.
byte [] decomparray;        // Decompression.
huff_node [] hufftree;      // Huffman tree.
byte [] bitstring;          // Array that holds constructed bitstring
                            // ...used during compression/decompression.

/**
* constructor
*/

//public static void main(String[] args) {
//
//    new Huffman().dotest();
//}
public Huffman()
{
    // Set the initial # of loops per iteration. Default is 2.

    loops_per_iter = BMglobals.huffloops;

    // Set size of array. Default is 5000.

    array_rows = BMglobals.huffarraysize;

    // Set indexing base.

    base_iters_per_sec = BMglobals.hufftestbase;

    testname = "CPU: Huffman Compression";  // Set test name.

    adjust_flag = BMglobals.huffadjust; // Has test adjusted to clock?
                                        // Default is false at first,
                                        // so adjust.
}

/*
* initialize
*
* Allocate memory for the plaintext and compressed text. We'll be
* really pessimistic here, and allocate equal amounts for both
* (though we know...well, we PRESUME) the compressed stuff will
* take less than the plain stuff. Also note that we'll build a 3rd
* buffer to decompress into, and we preallocate space for the
* Huffman tree. (We presume that the Huffman tree will grow no
* larger than 512 bytes. This is actually a super-conservative
* estimate...but, who cares?)
*/

void initialize()
{

plaintext = new byte [array_rows];
comparray = new byte [array_rows];
decomparray = new byte [array_rows];
hufftree = new huff_node[512];
bitstring = new byte[30];

// Gotta instantiate the huff_node members.

for (int i = 0; i < 512; i++)
    hufftree[i] = new huff_node();

// Build the plaintext buffer.

create_text_block(array_rows - 1, 500);
plaintext[array_rows - 1] = (byte) 0;    // Put null at end.

// Garbage collect.

System.gc();

}

/*
* DoIteration
*/

private long DoIteration()
{
int i;              // for index
long testTime;      // Duration of test (milliseconds).
int loops;          // Local for # of loops.
int root;           // Root node offset.
float lowfreq1;     // Low frequency counters.
float lowfreq2;
int lowidx1;        // Indexes of low freq. elements.
int lowidx2;
int bitoffset;      // Offset of bit into compressed array.
int maxbitoffset;   // Maximum bit offset.
int textoffset;     // Offset into text array.
int c;              // "Character" for compression/decompression.
int bitstringlen;   // Length of bitstring.

// Set local loops.

loops = loops_per_iter;

// Start the stopwatch.

testTime = System.currentTimeMillis();

// Do the test loops times.

while ((loops--) != 0)
{

    // Calculate the frequency of each byte value. Store the
    // results in what will become the "leaves" of the
    // Huffman tree. Interior nodes will be built in those
    // nodes greater than node #255.

    for (i = 0; i < 256; i++)
    {
    hufftree[i].freq = (float)0.0;
       hufftree[i].c = (byte) i;
    }

    for (i = 0; i < array_rows; i++)
        hufftree[plaintext[i]].freq += (float) 1.0;

    for (i = 0; i < 256; i++)
        if (hufftree[i].freq != (float) 0.0)
            hufftree[i].freq /= (float) array_rows;

    // Build the Huffman tree. First clear all the parent
    // pointers and left/right pointers. Also, discard all
    // nodes that have a frequency of true 0.

    for (i = 0; i < 256; i++)
    {
        if (hufftree[i].freq == (float) 0.0)
        hufftree[i].parent = 15000;       // Exclude this node.
        else
        {
        hufftree[i].parent = -1;
        hufftree[i].left = -1;
        hufftree[i].right = -1;
        }
    }

    // Go through the tree. Locate nodes of really low frequency.

    root = 255;               // Starting root node - 1.

    while (true)
    {
        lowfreq1 = (float) 2.0;
        lowfreq2 = (float) 2.0;
        lowidx1 = -1;
        lowidx2 = -1;

        // Find first-lowest frequency.

        for (i = 0; i <= root; i++)
            if (hufftree[i].parent < 0)
                if (hufftree[i].freq <= lowfreq1)
                {
                    lowfreq1 = hufftree[i].freq;
                    lowidx1 = i;
                }

        // Did we find a lowest value? If not, the tree is done.

        if (lowidx1 == -1) break;

        // Find next lowest frequency.

        for (i = 0; i <= root; i++)
            if ((hufftree[i].parent < 0) && (i != lowidx1))
                if (hufftree[i].freq <= lowfreq2)
                {
                    lowfreq2 = hufftree[i].freq;
                    lowidx2 = i;
                }

        // If we could only find one "lowest" frequency, then
        // that item is surely the root, and (as above) the
        // tree is done.

        if (lowidx2 == -1) break;

        // Make a new root, and attach the two selected nodes to it.

        root++;                         // New root.
        hufftree[lowidx1].parent = root;
        hufftree[lowidx2].parent = root;
        hufftree[root].freq = lowfreq1 + lowfreq2;
        hufftree[root].left = lowidx1;
        hufftree[root].right = lowidx2;
        hufftree[root].parent = -2;     // Show this as new root.
    }

    // Huffman tree is built. Compress the plaintext.

    bitoffset = 0;                   // Initialize the bit offset.
    for (i = 0; i < array_rows; i++)
    {
        c = plaintext[i];             // Fetch a "character."
        c = c & 255;

        // Build a bit string for byte c.

        bitstringlen = 0;

        while (hufftree[c].parent != -2)   // ...until you hit the root.
        {
            if(hufftree[hufftree[c].parent].left == c)
                bitstring[bitstringlen] = 0;
            else
                bitstring[bitstringlen] = 1;
            c = hufftree[c].parent;
            bitstringlen++;
        }

        // Step backward through the bit string, setting
        // bits in the compressed array as you go.

        while (0 != bitstringlen--)
        {
            SetCompBit(bitoffset,bitstring[bitstringlen]);
            bitoffset++;
        }
    }

    // Compression done. Perform de-compression.

    maxbitoffset = bitoffset;
    bitoffset = 0;
    textoffset = 0;

    do {
        i = root;
        while (hufftree[i].left != -1)
        {
            if (GetCompBit(bitoffset) == 0)
                i = hufftree[i].left;
            else
                i = hufftree[i].right;
            bitoffset++;
        }

        decomparray[textoffset] = hufftree[i].c;
        textoffset++;

    } while (bitoffset < maxbitoffset);

} // End while(loops--).

testTime = System.currentTimeMillis() - testTime;

return(testTime);     // All done.

}

/**
* dotest
*
* Perform the actual Huffman benchmark test.
* The steps involved are:
* 1. See if the test has already been "adjusted".
*    If it hasn't do an adjustment phase.
* 2. If the test has been adjusted, go ahead and
*    run it.
*/

@Cloud
public long localdotest()
{

long duration;      // Time in milliseconds for doing test.

// Has it been adjusted yet?

if (adjust_flag == false)    // Do adjustment phase.
{
    while (true)
    {
        initialize();

        // See if the current setting meets requirements.

        duration = DoIteration();
        if (duration > BMglobals.minTicks / 10) // minTicks = 100 ticks.
            break;                              // We'll take just 10.

        // Current setting does not meet requirements.
        // Increase the # of loops and try again.

        loops_per_iter += 1;
    }

    // Scale up to whatever it takes to do 100 clock ticks.

    int factor = (int) (BMglobals.minTicks / duration);
    loops_per_iter += (factor * loops_per_iter);

    adjust_flag = true; // No adjustment next time through.

}

    // If we fall through preceding IF statement, adjustment
    // not necessary -- simply initialize.

    initialize();

    // All's well if we get here. Perform the test.

    duration = DoIteration();


    

    // Calculate the iterations per second. Note that we
    //  assume here that the duration is in milliseconds.

    iters_per_sec = (double)loops_per_iter / ((double)duration
                    / (double)1000);

    // Debug code.

    if (debug_on == true)
    {
        System.out.println("--- Huffman Test debug data ---");
        System.out.println("Number of loops performed: "
                            + loops_per_iter);
        System.out.println("Elapsed time (ms): " + duration);
        System.out.println("Iterations per sec: " + iters_per_sec);

        // Compare original plaintext with decompressed output.

        System.out.println("Testing that decompressed output " +
                            "matches original plaintext...");

        for (int i = 0; i < array_rows; i++)
        {
            if (decomparray[i] != plaintext[i])
            {
                System.out.println("Huffman Error: Decompressed text "
                            + "doesn't agree with original plaintext.");
                break;
            }
        }

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

// Point the arrays at empty items, then garbage collect.

plaintext = null;
comparray = null;
decomparray = null;
hufftree = null;
bitstring = null;

// Garbage collect.

System.gc();

}

/*
* create_text_line
*
* Create a random line of text, stored at offset within the
* plaintext array. This line of text may be no more than
* nchars long. (NOTE: In the Java version, the array of text
* is actually stored as a series of bytes.)
* Returns the new offset
*/

private int create_text_line(int offset, int nchars)
{

int charssofar;         // # of characters so far.
int tomove;             // # of characters to move.
char [] myword;         // Local buffer for words.
String mystring;
int wordoff;            // Word offset.
wordlist mywordlist;    // Local wordlist object.
RandNum rndnum;         // Random number.

charssofar = 0;
mywordlist = new wordlist();  // Create new wordlist object.
rndnum = new RandNum();       // Create to generate random numbers.

do {
    // Grab a random word from the word catalog.

    wordoff = rndnum.abs_nextwc(wordlist.LISTLENGTH);
    tomove = mywordlist.words[wordoff].length();
    mystring = new String(mywordlist.words[wordoff] + " ");

    // Attach a blank.

    tomove++;
    myword = mystring.toCharArray();

    // See how long the word is. If its length + charssofar > nchars,
    // we have to trim it.

    if ((tomove + charssofar) > nchars)
        tomove = nchars - charssofar;

    // Attach the word to the current line. Increment counter.

    for (int i = 0; i < tomove; i++)
    {
        plaintext[offset] = (byte) myword[i];
        offset++;
    }

    charssofar += tomove;     // Show how many chars we did.

// If we're done, bail out. Otherwise, go get another word.

} while (charssofar < nchars);

return(offset);             // Return new offset.

}

/*
* create_text_block
*
* Build a block of text randomly loaded with words. The words
* come from the wordcatalog (which must be loaded before you
* call this).
* tblen is the # of bytes to be put into the block.
* maxlinlen is the maximum length of anyline (line end indicated
* by a carriage return).
*/

private void create_text_block(int tblen,
        int maxlinlen)
{
int bytessofar;     // # of bytes so far.
int linelen;        // Line length.
int offset;         // Offset into table.
RandNum rndnum;     // Random number.

offset = 0;
bytessofar = 0;
rndnum = new RandNum();
do {
    // Pick a random length for a line and fill the line.
    // Make sure the line can fit (haven't exceeded tablen) and also
    // make sure you leave room to append a carriage return.

    linelen = rndnum.abs_nextwc(maxlinlen - 6) + 6;
    if ((linelen + bytessofar) > tblen)
        linelen = tblen - bytessofar;
    if (linelen > 1)
        offset = create_text_line(offset,linelen);

    // Add the carriage return.

    plaintext[offset - 1] = (byte) '\n';
    bytessofar += linelen;

} while (bytessofar < tblen);

}

/*
* SetCompBit
*
* Set a bit in the compression array. The value of the bit
* is set according to bitval (1 or 0).
*/

private void SetCompBit(int bitoffset, byte bitval)
{
int byteoffset;     // Offset in the array.
int bitnumb;        // Bit number.
byte mybyte;

// First calculate which element in the comparray to
// alter. Then calculate bitnumber.

byteoffset = bitoffset >> 3;
bitnumb = bitoffset % 8;

mybyte = comparray[byteoffset];

// Set or clear.

if (bitval == 1)
    mybyte |= (byte) (1 << bitnumb);
else
    mybyte &= (byte) (~(1 << bitnumb));

comparray[byteoffset] = mybyte;

}

/*
* GetCompBit
*
* Return the bit value of a bit in the compression array.
* Returns 0 if the bit is clear, nonzero otherwise.
*/

private int GetCompBit(int bitoffset)
{

int byteoffset;     // Offset of byte into array.
int bitnumb;        // Bit number in byte.

// Calculate byte offset and bit number.

byteoffset = bitoffset >> 3;
bitnumb = bitoffset % 8;

// Fetch.

return((int) ((1 << bitnumb) & comparray[byteoffset]));
}


/**
* wordlist
*
* This is simply a list of English words that the
* benchmarks draw from.
*/
class wordlist implements Serializable
{
    private static final long serialVersionUID = 11;
    String [] words = { "Hello",
	"He",
	"Him",
	"the",
	"this",
	"that",
	"though",
	"rough",
	"cough",
	"obviously",
	"But",
	"but",
	"bye",
	"begin",
	"beginning",
	"beginnings",
	"of",
	"our",
	"ourselves",
	"yourselves",
	"to",
	"together",
	"togetherness",
	"from",
	"either",
	"I",
	"A",
	"return",
	"However",
	"that",
	"example",
	"yet",
	"quickly",
	"all",
	"if",
	"were",
	"includes",
	"always",
	"never",
	"not",
	"small",
	"returns",
	"set",
	"basic",
	"Entered",
	"with",
	"used",
	"shown",
	"you",
	"know" };

    static final int LISTLENGTH = 50;
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
	Huffman
 localstate = (Huffman
) state;
	this.plaintext = localstate.plaintext;
	this.comparray = localstate.comparray;
	this.decomparray = localstate.decomparray;
	this.hufftree = localstate.hufftree;
	this.bitstring = localstate.bitstring;
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
