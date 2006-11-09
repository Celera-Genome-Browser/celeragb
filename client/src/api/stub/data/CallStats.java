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
package api.stub.data;

public class CallStats implements java.io.Serializable
{
    public String classname;

    public String methodname;

    public int totalCalls;

    public long totalTimeMS;

    public long averageTimeMS;

    public long smallestTimeMS;

    public long largestTimeMS;

    private CallStats()
    {
    }

    public CallStats
        (String classname,
        String methodname,
        int totalCalls,
        long totalTimeMS,
        long averageTimeMS,
        long smallestTimeMS,
        long largestTimeMS)
    {
        this.classname = classname;
        this.methodname = methodname;
        this.totalCalls = totalCalls;
        this.totalTimeMS = totalTimeMS;
        this.averageTimeMS = averageTimeMS;
        this.smallestTimeMS = smallestTimeMS;
        this.largestTimeMS = largestTimeMS;
    }

    public String getClassName()      { return classname; }
    public String getMethodName()     { return methodname; }
    public int    getTotalCalls()     { return totalCalls; }
    public long   getTotalTimeMS()    { return totalTimeMS; }
    public long   getAverageTimeMS()  { return averageTimeMS; }
    public long   getLargestTimeMS()  { return largestTimeMS; }
}
