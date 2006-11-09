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

import java.util.*;

/**
 * ObserverList is used to implement the "observable" part of
 * the observer pattern.
 *
 * INTRODUCTION
 * An observable must store its list of observers and provide the functionality
 * of adding an observer, removing an observer, and notifying its observers.
 * Our intent is to provide a reusable object to help the implementation
 * of concrete observables.
 *
 * CONSTRAINT
 * The idea of providing an Observable superclass does not work
 * in this case, because if a thing is observable it is because this thing
 * has a data structure and a behavior (and java forbids multiple inheritance).
 *
 * SOLUTION
 * Make a thing observable simply by adding a public ObserverList data member.
 * The ObserverList provides methods to add or remove observers.
 * the ObserverList also provides a generic way of notifying observers
 * with the "Caller" interface.
 *
 * EXAMPLE
 * Implementation of an observable:
 *
 * public class Axis extends WhateverRequired
 * {
 *     public ObserverList observers;
 *
 *     public static interface Observer
 *     {
 *         void baseChanged(Axis axis, int atIndex);
 *     }
 *
 *     public void setBase(int index, char base) {
 *         ...changes the base at the given index...
 *
 *         observers.notify(new Caller() {
 *             public void call(Object observer) {
 *                 ((Observer)observer).baseChanged(this, index);
 *             }
 *     }
 * }
 *
 * Implementation of an observer:
 *
 * public class AxisGlyph extends WhateverRequired
 *     implements Axis.Observer
 * {
 *     public AxisGlyph(Axis axis) {
 *         axis.observers.add(this);
 *     }
 *
 *     public void delete() {
 *         axis.observers.remove(this);
 *     }
 *
 *     public void baseChanged(Axis axis, int atIndex) {
 *         <... reacts to the notification ...>
 *     }
 * }
 *
 * PROS
 * - Does not enforce a superclass on observables.
 * - Does not enforce a superclass on observers.
 * - Allows reuse of the functionality that is common to all observables.
 * - Easy to refactor a pre-existing thing into an observable.
 * - The ability to notify observers is public.
 *
 * CONS
 * - The addition of an observer of the wrong kind is not detected at
 *   compile time (it will throw a failed assertion at run time).
 * - The ability to notify observers is public.
 */
public class ObserverList {
   /**
    * The list of observers.
    */
   protected ArrayList observers = new ArrayList();
   protected Class observerClass;

   /**
    * Instanciate a new observer list for the given kind of observers.
    */
   public ObserverList(Class observerClass) {
      this.observerClass = observerClass;
   }

   /**
    * Return true if there is at least one observer.
    */
   public boolean hasObservers() {
      return !observers.isEmpty();
   }

   /**
    * Add an observer to the list of observers.
    *
    * A failed assertion is thrown if the given observer instance already
    * belongs to the list,
    */
   public void addObserver(Object o) {
      if (Assert.debug) {
         Assert.vAssert(!observers.contains(o));
         Assert.vAssert(observerClass.isInstance(o));
      }

      observers.add(o);
   }

   /**
    * Add a high priority observer to the list of observers.
    *
    * A "high priority" observer will be notified before ordinary observers.
    * The order of notifications between two high-priority observers
    * is undefined.
    *
    * If the given observer instance already belongs to the list,
    * it is moved at the beginning of the list of observers.
    */
   public void addHighPriorityObserver(Object o) {
      observers.remove(o);
      observers.add(0, o);
   }

   /**
    * Remove an observer from the list of observers.
    *
    * A failed assertion is thrown if the given observer does not belong
    * to the list.
    */
   public void removeObserver(Object o) {
      observers.remove(o);
   }

   /**
    * Notifies all the observers.
    *
    * The "Caller.call(observer)" method is called once per observer.
    */
   public void notify(Caller caller) {
      ArrayList copyInCaseOriginalChanges = new ArrayList(observers);
      int count = copyInCaseOriginalChanges.size();
      for (int i = 0; i < count; ++i) {
         caller.call(copyInCaseOriginalChanges.get(i));
      }
   }

   /**
    * This interface is used by the ObserverList to delegate the
    * notification to the concrete observable
    * (see a usage example above).
    */
   public static interface Caller {
      void call(Object observer);
   }
}
