// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package shared.util;

import api.entity_model.access.observer.LoadRequestStatusObserverAdapter;
import api.entity_model.model.fundtype.LoadRequestState;
import api.entity_model.model.fundtype.LoadRequestStatus;

public class FreeMemoryWatcher extends MTObservable {
  static private FreeMemoryWatcher freeMemoryWatcher;
  private int numSecondsToUpdate;
  private Thread updater;
  private Runtime runtime=Runtime.getRuntime();
  private long totalMemory,reservedMemory;
  private static long DEFAULT_RESERVED_MEMORY=5*1024*1000;  //5MB
  private static long DEFAULT_TOTAL_MEMORY=64*1024*1000; //64MB
  private  LoadRequestStatusObserverAdapter statusObserver =
     new MyLoadRequestStatusObserver();


  private FreeMemoryWatcher() {
    this(5,true);
  }

  private FreeMemoryWatcher(int numSecondsToUpdate, boolean startUpdate) {
       this.numSecondsToUpdate=numSecondsToUpdate;
       if (startUpdate) startUpdate();
  }

  static public FreeMemoryWatcher getFreeMemoryWatcher() {
     if (freeMemoryWatcher==null) freeMemoryWatcher=new FreeMemoryWatcher();
     return freeMemoryWatcher;
  }

  public void startUpdate() {
     updater=new Thread(new Updater(numSecondsToUpdate), "Free Memory Watcher");
     updater.setDaemon(true);
     updater.setPriority(Thread.MIN_PRIORITY);
     updater.start();
  }

  public void stopUpdate() {
     updater.interrupt();
     updater=null;
  }

  private void update() {
      setChanged();
      notifyObservers(new Integer( (int) ( ((double)getFreeMemory()/(double)getTotalMemory()) *100)) );
      clearChanged();
//      System.out.println("Free Memory Percent: "+(int)((double)runtime.freeMemory()/(double)runtime.totalMemory()*100));
  }

  public long getTotalMemory() {
    if (totalMemory>0) return totalMemory;
    String memoryStr=System.getProperty("x.genomebrowser.TotalMemory");
    try {
       if (memoryStr.endsWith("m") || memoryStr.endsWith("M")) {
          totalMemory=1024*1000*Long.parseLong(memoryStr.substring(0,memoryStr.length()-1));
          return totalMemory;
       }
       if (memoryStr.endsWith("k") || memoryStr.endsWith("K")) {
          totalMemory=1024*Long.parseLong(memoryStr.substring(0,memoryStr.length()-1));
          return totalMemory;
       }
       totalMemory=Long.parseLong(memoryStr);
    }
    catch (Exception ex) {
       totalMemory=DEFAULT_TOTAL_MEMORY;
    }
    return totalMemory;
  }


  public long getUsedMemory() {
    return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
  }

  public long getFreeMemory() {
    return getTotalMemory()-getUsedMemory();
  }


  public LoadRequestStatusObserverAdapter getLoadStatusObserver(){
    return statusObserver;
  }


   private class MyLoadRequestStatusObserver extends LoadRequestStatusObserverAdapter {
    public void stateChanged(LoadRequestStatus loadRequestStatus, LoadRequestState newState){

      if (newState==LoadRequestStatus.COMPLETE ) {
           loadRequestStatus.removeLoadRequestStatusObserver(this);
           System.gc();
      }
    }
}


  class Updater implements Runnable {
     private int numSecondsToUpdate;

     Updater (int numSecondsToUpdate) {
       this.numSecondsToUpdate=numSecondsToUpdate;
     }

     public void run () {
         while (true) {
           update();
           try {
             Thread.sleep(numSecondsToUpdate*1000);
           }
           catch (InterruptedException ie) {
             System.err.println("Memory Watcher Threads Sleep was interrupted");
           }
         }
     }
  }


}
