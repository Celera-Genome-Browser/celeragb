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
package shared.util;

/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.SwingUtilities;

/**
* This class was created to have similar behavior to the java.util.Observable
* class.  There are key differences. This class is
* designed to notify on the System Event Queue thread.  This is import (and
* necessary) when using Swing components.  The java.util.Observable class will
* always notify on the thread that calls notifyObservers.  Further, it will not
* allow a call to notifyObservers on a thread that is different than the one that
* caused the change.  This will cause serious problems in any application where
* a model entity is being modified in a multi-threaded manor, but the View enities
* must be modified on the System Event Queue, such as with Swing and a multi-threaded
* model.
*
* @author P. Davies
*
*/
public class MTObservable extends Observable {

  private Vector systemEventThreadObservers;
  private Vector anyThreadObservers;

  public MTObservable() { }

  /**
  * Add observer to the list that wil be notified on the SystemEventThread.
  *  No synchronization necessary as underlying Vector is sychronized
  */
  public void addObserver(Observer observer){
     if (systemEventThreadObservers==null) systemEventThreadObservers=new Vector();
     if (systemEventThreadObservers.contains(observer)) return;
     systemEventThreadObservers.addElement(observer);
  }

  /**
  * Add observer to the list that wil be notified on the SystemEventThread
  */
  public void addSystemEventThreadObserver(Observer observer){
     addObserver(observer);
  }

  /**
  * Add observer to the list that will be notified on the observables thread
  * No synchronization necessary as underlying Vector is sychronized
  */
  public void addAnyThreadObserver(Observer observer){
     if (anyThreadObservers==null) anyThreadObservers=new Vector();
     if (anyThreadObservers.contains(observer)) return;
     anyThreadObservers.addElement(observer);
  }

  /**
  * Remove observer from the list.  No synchronization necessary as underlying Vector is sychronized
  */
  public void deleteObserver(Observer observer) {
     if (systemEventThreadObservers==null &&  anyThreadObservers==null) return;
     if ((systemEventThreadObservers!=null) && systemEventThreadObservers.contains(observer)) {
         systemEventThreadObservers.removeElement(observer);
         if (systemEventThreadObservers.size()==0) systemEventThreadObservers=null;
     }
     if ((anyThreadObservers!=null) && anyThreadObservers.contains(observer)) {
         anyThreadObservers.removeElement(observer);
         if (anyThreadObservers.size()==0) anyThreadObservers=null;
     }
  }

  /**
  * Remove all observer from the list.  No synchronization necessary as underlying Vector is sychronized
  */
  public void deleteObservers() {
     systemEventThreadObservers=null;
     anyThreadObservers=null;
  }

  public void notifyObservers() {
    notifyObservers(null);
  }

  public void notifyObservers(Object arg) {
     if (!hasChanged()) return;
     //take snapshot of observer array
     if (systemEventThreadObservers!=null) {
        Object[] obsArray = systemEventThreadObservers.toArray();
        SwingUtilities.invokeLater(new Notifier(this,obsArray,arg));
     }
     if (anyThreadObservers!=null) {
       Object[] observers=anyThreadObservers.toArray();
       for (int i=0;i<observers.length;i++) {
          ((Observer)observers[i]).update(this, arg);
       }
     }
  }

  private class Notifier implements Runnable {
     Object[] observers;
     Object arg;
     Observable observable;
     public Notifier (Observable observable,Object[] observers, Object arg) {
       this.observers=observers;
       this.arg=arg;
       this.observable=observable;
     }

     public void run() {
       for (int i=0;i<observers.length;i++) {
          ((Observer)observers[i]).update(observable, arg);
       }
     }

  }


}

/*
$Log$
Revision 1.6  2002/11/07 18:38:36  lblick
Removed obsolete imports and unused local variables.

Revision 1.5  2001/02/09 02:16:39  pdavies
Now have the Progress meter working.

Revision 1.4  2000/02/10 21:56:33  pdavies
Mods for new packaging

Revision 1.3  1999/11/12 20:22:33  DaviesPE
Now can do notification via origination thread or SystemEventQueue thread (defaults to System Event Queue thread)

Revision 1.2  1999/08/27 16:01:33  DaviesPE
Removed null pointer bug

Revision 1.1  1999/08/27 15:04:07  DaviesPE
Replacement class of java.util.Observable.  Provides update notifications on the System Event Queue, instead of the thread that the modification occured on as Observable does.

*/