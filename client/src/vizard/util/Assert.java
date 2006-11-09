/*
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 1999 - 2006 Applera Corporation.
 301 Merritt 7 
 P.O. Box 5435 
 Norwalk, CT 06856-5435 USA

 This is free software; you can redistribute it and/or modify it under the 
 terms of the GNU Lesser General Public License as published by the 
 Free Software Foundation; version 2.1 of the License.

 This software is distributed in the hope that it will be useful, but 
 WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE. 
 See the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License 
 along with this software; if not, write to the Free Software Foundation, Inc.
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
*/
package vizard.util;


/**
 * Assert is used to add assertions to the code.
 *
 * INTRODUCTION
 * Assertions are perhaps the most useful method of finding bugs.
 * Assertions are not exceptions. An exception is thrown when something
 * unusual happens. A typical example is an IO error.
 * An assertion is something that can never happen, unless the programmer
 * has a bug in its program.
 *
 * CONSTRAINT
 * The performance of an application is affected by the addition of
 * assertions. This is particularly aggravating once the application has
 * become stable, and the assertions are true most -if not all- the time.
 * Still, leaving the assertions in the code is very helpful for future
 * refactoring and maintenance. Also, assertions are in-code documentation
 * of the intent of the original programmer.
 * Languages like C or C++ solve the performance issue with a preprocessor
 * pass that removes assertions from the production version of the application.
 *
 * SOLUTION
 * Check the value of a static final boolean (eg, "debug") before calling
 * the assertion. If the boolean is set to false, the java compiler is smart
 * enough, not only not to call the assertion, but also to skip testing
 * the value of the boolean (because it has been declared as "final").
 *
 * Actually, some assertions can be so slow to execute (eg, checking the
 * validity of a complex structure) that it makes the process of debugging
 * an application very painful. These assertions require a separate flag
 * (eg, "slowDebug") and will be activated only once in a while, or to help
 * catching a bug that is particularly hard to find.
 *
 * EXAMPLE
 * class Set
 * {
 *     List list; //the set stores its elements in a list.
 *
 *     void remove(Object element) {
 *         ...removes the element from the list...
 *         if (Assert.debug) Assert.vAssert(!list.contains(element));
 *     }
 *
 *     void merge(Set anotherSet) {
 *         ...do the merge...
 *         if (Assert.slowDebug) Assert.vAssert(checkNoDuplicates());
 *     }
 * }
 *
 * PROS
 * - Assertions can be kept in the code without adding any performance
 *   penalty once the application is ready for production.
 * - Takes into account the desire for a programmer to have a reasonably
 *   fast application during the debugging phase by providing a separate
 *   boolean for slow assertions.
 *
 * CONS
 * - Switching between debug-mode and production-mode requires a recompile
 *   of the whole application.
 */
public abstract class Assert
{
    /**
     * The boolean "debug" must be checked prior to the execution of
     * an assertion.
     *
     * Once the application is ready for production, debug should
     * be initialized to false, and the whole application recompiled.
     * This will ensure that the application will have no performance
     * penalty due to assertions.
     */
    public final static boolean debug = false;

    /**
     * The boolean "slowDebug" must be checked prior to the execution
     * of a slow assertion.
     *
     * The normal value for this boolean is "false". From time to time, or
     * in order to help catching a difficult bug, the boolean can be
     * set to true and the whole application must be recompiled.
     */
    public final static boolean slowDebug = false;

    /**
     * If the given "assertion" boolean is false, the method
     * prints the stack trace and throws an error.
     */
    public static void vAssert(boolean assertion) {
	vAssert(assertion, null);
    }

    /**
     * If the given "assertion" boolean is false, the method
     * prints the given error message, the stack trace, and throws an error.
     */
    public static void vAssert(boolean assertion, String errorMessage) {
	if (!assertion) {
	    String s = "ASSERTION FAILED" +
		((errorMessage == null) ? "" : (": " + errorMessage));
	    pl(s);
	    Error error = new Error(s);
	    error.printStackTrace();
	    throw error;
	}
    }

    /**
     * Use Assert.p instead of System.out.print for debugging messages.
     *
     * Some applications do not have an available standard output.
     * Redefining this method to write in a file instead of
     * on the standard output will restore all the debugging messages.
     */
    public static void p(String s) {
	System.out.print(s);
    }

    /**
     * Use Assert.pl instead of System.out.println for debugging message.
     *
     * Some applications do not have an available standard output.
     * Redefining this method to write in a file instead of
     * on the standard output will restore all the debugging messages.
     */
    public static void pl(String s) {
	System.out.println(s);
    }

    //@todo doc
    public static void pl(Object o, String s) {
	pl(o.getClass().getName() + ": " + s);
    }

    public static int debugCounter;
    public static void incrDebugCounter(Object obj) {
        if (++debugCounter % 1000 == 0)
            pl(obj, "COUNTER reached " + debugCounter);
    }

    static Object[] infos = new Object[5];
    static int[] ids = new int[5];
    public static int getGenomeVersionIdPerformanceTest(api.stub.data.GenomeVersionInfo info) {
        for(int i = 0; i < 5; ++i) {
            if (info == infos[i])
                return ids[i];
            if (infos[i] == null) {
                infos[i] = info;
                ids[i] = (info.getSpeciesName() + info.getDataSource() + info.getAssemblyVersion()).hashCode();
                return ids[i];
            }
        }
        Assert.vAssert(false);
        return 0;
    }

    public static void printCurrentTime(String msg) {
        long currentTime = System.currentTimeMillis();
        System.out.println("TIME AT " + msg + ' ' + currentTime);
    }
}

