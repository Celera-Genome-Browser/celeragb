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
package api.entity_model.access.observer;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 *
 * This is an empty place-holder interface, similar to jaav.io.Serializable.
 *
 * The intent of this interface is to allow model components to register as
 * model observers.  By implenmenting this interface, these model components are
 * are stating that they need to be synchronized with model changes BEFORE any
 * other notifications are sent out.  Further, all implementors of this interface
 * will be notified on whatever thread is available, and NOT necessarily the AWT Event
 * Queue thread.  Therefore, a view should NEVER implement this interface!!
 */

public interface ModelSynchronizationObserver  {


}