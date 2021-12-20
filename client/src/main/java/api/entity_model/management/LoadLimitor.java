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

/**
 * This class can effectively limit the load to the available memory based on the load type
 */
package api.entity_model.management;

import api.stub.geometry.Range;
import shared.util.FreeMemoryWatcher;

public class LoadLimitor {

  static private final LoadLimitor loadLimitor=new LoadLimitor();
  private long maxRequestSize;
  private long hiPriRequiredMemory;
  private long loPriRequiredMemory;
  private long humanRequiredMemory;
  private long spliceRequiredMemory;
  private long codonRequiredMemory;
  private long contigRequiredMemory;

  private LoadLimitor() {
     maxRequestSize=convertStringToLong(System.getProperty("x.genomebrowser.SingleRequestLoadingLimit"));
     hiPriRequiredMemory=convertStringToLong(System.getProperty("x.genomebrowser.HiPriReqMemoryForLoadingLimit"));
     loPriRequiredMemory=convertStringToLong(System.getProperty("x.genomebrowser.LoPriReqMemoryForLoadingLimit"));
     spliceRequiredMemory=convertStringToLong(System.getProperty("x.genomebrowser.SpliceReqMemoryForLoadingLimit"));
     humanRequiredMemory=convertStringToLong(System.getProperty("x.genomebrowser.HumanReqMemoryForLoadingLimit"));
     codonRequiredMemory=convertStringToLong(System.getProperty("x.genomebrowser.CodonReqMemoryForLoadingLimit"));
     contigRequiredMemory=convertStringToLong(System.getProperty("x.genomebrowser.CotigReqMemoryForLoadingLimit"));
  }

  private long convertStringToLong(String memoryStr) {
    try {
       if (memoryStr.endsWith("m") || memoryStr.endsWith("M")) {
           return 1024*1000*Long.parseLong(memoryStr.substring(0,memoryStr.length()-1));
       }
       if (memoryStr.endsWith("k") || memoryStr.endsWith("K")) {
          return 1024*Long.parseLong(memoryStr.substring(0,memoryStr.length()-1));
       }
       return Long.parseLong(memoryStr);
    }
    catch (Exception ex) {
       return 0;
    }

  }

  public static LoadLimitor getLoadLimitor() {
     return loadLimitor;
  }

  public boolean canLoadHiPriAndHumanOverRange(Range range) {
    return canLoad(range,hiPriRequiredMemory+humanRequiredMemory);
  }

  public boolean canLoadHiPriorityOverRange(Range range) {
     return canLoad(range,hiPriRequiredMemory);
  }

  public boolean canLoadLoPriorityOverRange(Range range) {
     return canLoad(range,loPriRequiredMemory);
  }

  public boolean canLoadHumanOverRange(Range range) {
     return canLoad(range,humanRequiredMemory);
  }

  public boolean canLoadSpliceSitesOverRange(Range range) {
     return canLoad(range,spliceRequiredMemory);
  }

  public boolean canLoadCodonsOverRange(Range range) {
     return canLoad(range,codonRequiredMemory);
  }

  public boolean canLoadContigsOverRange(Range range) {
     return canLoad(range,contigRequiredMemory);
  }

  private boolean canLoad(Range range, long requiredMemoryForMaxRequest) {
     long freeMemory=FreeMemoryWatcher.getFreeMemoryWatcher().getFreeMemory();
     if (freeMemory>requiredMemoryForMaxRequest) return true;
     int rangeMag=range.getMagnitude();
     long memoryRequiredForRequest=(long)(((double)rangeMag/(double)maxRequestSize)*requiredMemoryForMaxRequest);
     if (freeMemory>memoryRequiredForRequest) return true;
     System.gc();
     freeMemory=FreeMemoryWatcher.getFreeMemoryWatcher().getFreeMemory();
     if (freeMemory>memoryRequiredForRequest) return true;
     return false;
  }

}