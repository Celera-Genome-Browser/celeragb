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
 * CVS_ID:  $Id$
 */

package api.entity_model.model.fundtype;

import shared.util.MTObservable;

import java.util.*;

/** This is a model of the threads that are actively loading in the system */
public class ActiveThreadModel extends MTObservable {
    static private ActiveThreadModel activeThreadModel;

    private Map statusObjects = Collections.synchronizedMap(new HashMap());

    private ActiveThreadModel() { }

    public static ActiveThreadModel getActiveThreadModel() {
        if (activeThreadModel == null) activeThreadModel = new ActiveThreadModel();
        return activeThreadModel;
    }

    public LoadRequestStatus[] getActiveLoadRequestStatusObjects() {
      Set activeEntries=null;
      synchronized (statusObjects) {
        activeEntries=statusObjects.entrySet();
      }
      List statusObjects=new ArrayList();
      for (Iterator it=activeEntries.iterator();it.hasNext();) {
        statusObjects.add( ((Map.Entry)it.next()).getValue());
      }
      return (LoadRequestStatus[])statusObjects.toArray(new LoadRequestStatus[statusObjects.size()]);
    }

    public int getActiveThreadCount() {
        return statusObjects.size();
    }

    public void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    public void addObserver(Observer observer, boolean bringUpToDate) {
        addObserver(observer);
        for (Iterator it = statusObjects.keySet().iterator(); it.hasNext(); ) {
            observer.update(this, statusObjects.get(it.next()));
        }
    }

    void addActiveLoadRequestStatus(LoadRequestStatus loadRequestStatus) {
        statusObjects.put(loadRequestStatus, loadRequestStatus);
        setChanged();
        notifyObservers(loadRequestStatus);
        clearChanged();
    }

    void removeActiveLoadRequestStatus(LoadRequestStatus loadRequestStatus) {
        statusObjects.remove(loadRequestStatus);
        setChanged();
        notifyObservers(loadRequestStatus);
        clearChanged();
    }


}


