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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

package shared.util;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.SwingUtilities;


/**
* This class acts as a generic queue mechanism for passing execution between threads.
* This class creates a queue that accepts Runnables.  It then executes the Runnables
* in one of several threads from a thread group that it manages.  The number of
* threads executing can be set by the client, as well as the priorities of the
* threads. This class speeds execution in a multi-threaded app as it eliminates
* the longer overhead associated with creating and killing threads many times.  It
* also eliminates the potencial for many, many threads to be created without that intent.
*
* This class also will run runnables directly if numThreads is 0
*
* Defaults: Priority=normal
*           Execution on ThreadQueue threads (not System Event Queue)
*/
public class ThreadQueue {
  LinkedList queue=new LinkedList();
  int queueDepthMonitorCounter;
  Vector threads=new Vector();
  Vector runners=new Vector();
  boolean monitor;
  boolean threadQueueNotification=true;
  ThreadGroup threadGroup;
  boolean runDirectly;
  int totalThreads;
  
  public ThreadQueue(int numThreads, String groupName) {
    this(numThreads,groupName,Thread.NORM_PRIORITY,true);
  }

  public ThreadQueue(int numThreads, String groupName, int priority) {
    this(numThreads,groupName,priority,true);
  }

  public ThreadQueue(int numThreads, String groupName, int priority, boolean threadQueueNotification) {
    this.threadQueueNotification=threadQueueNotification;
    if (numThreads==0) runDirectly=true;
    else initThreads(numThreads,groupName,priority);
    totalThreads = numThreads;
  }

  public int getTotalThreads() {
    return totalThreads;
  }
  
  public void setPriority(int priority) {
    for (Enumeration e=threads.elements();e.hasMoreElements();) {
       ((Thread)e.nextElement()).setPriority(priority);
    }
  }

  public void monitor(boolean monitor) {
     this.monitor=monitor;
  }

  synchronized public void addQueue(Runnable runnable) {
     if (!runDirectly) {
       queue.add(runnable);
       Object obj;
       for (int i=0;i<runners.size();i++) {
         obj=runners.elementAt(i);
         synchronized (obj) {
           obj.notify();
         }
       }
     }
     else runnable.run();
  }

  synchronized private Runnable deQueue() {
     if (queue.size()==0) return null;
     if (monitor) {
       queueDepthMonitorCounter++;
       if (queueDepthMonitorCounter==10) {
         queueDepthMonitorCounter=0;
         System.out.println("ThreadGroup: "+threadGroup.getName()+" Current Queue Depth: "+queue.size());
       }
     }
     return (Runnable) queue.removeFirst();
  }

  private void initThreads(int numberThreads,String groupName,int priority) {
    Thread thread;
    threadGroup=new ThreadGroup(groupName);
    threadGroup.setDaemon(true);
    Runner runner;
    for (int i=1;i<=numberThreads;i++) {
        runner=new Runner();
        thread=new Thread(threadGroup,runner,"Runner Thread "+i);
        threads.add(thread);
        runners.add(runner);
        thread.setPriority(priority);
        thread.start();
    }
  }


  class Runner implements Runnable {
      Runnable runnable;

      public void run () {
        while (true) {
          runnable=deQueue();
          if (runnable==null) {
            synchronized(this) {
              try {
                wait();
              }
              catch (Exception ie) {}
            }
          }
          if (runnable==null) runnable=deQueue();
          try {
             if (runnable!=null)
               if (threadQueueNotification) runnable.run();
               else SwingUtilities.invokeAndWait(runnable);
          }
          catch (Exception ite) {
             ite.printStackTrace();
          }
        }
     }
  }


}

/*
$Log$
Revision 1.11  2002/11/07 18:38:37  lblick
Removed obsolete imports and unused local variables.

Revision 1.10  2000/05/09 21:50:20  simpsomd
Merges from BRANCH_UPDATE_SCHEMA_3_0

Revision 1.9.4.1  2000/04/28 18:35:45  cedahlke
Added getTotalThreads methods

Revision 1.9  2000/04/03 21:09:08  tsaf
Dead code removal

Revision 1.8  2000/02/10 21:56:34  pdavies
Mods for new packaging

Revision 1.7  1999/10/07 12:31:49  DaviesPE
Removed debug

Revision 1.6  1999/10/07 12:30:02  DaviesPE
After testing, went back to invoke and wait as the application becomes unusable under invokeLater with lots of data.

Revision 1.5  1999/08/27 15:03:00  DaviesPE
Now using invokeLater

Revision 1.4  1999/08/25 17:41:53  DaviesPE
Removed the additional thread that was being created by mistake

Revision 1.3  1999/08/23 20:35:15  DaviesPE
Will act properly if numThreads constructor arguement is 0, by running the runnable instead of queuing it.

Revision 1.2  1999/08/20 16:06:09  DaviesPE
Uses multiple run objects to avoid synchronization issues

Revision 1.1  1999/08/20 14:24:46  DaviesPE
Initial add - moved MultiHash from access.util

*/
