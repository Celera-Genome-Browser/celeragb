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

import api.entity_model.model.fundtype.GenomicEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 *
 * This class adds a ability to get the current list of observables.  The list is maintained
 * for the user, and is available for deregistration.
 */


public abstract class GenomicEntityObserverAdapter
       implements GenomicEntityObserver {

    private List observables=new ArrayList();

    protected GenomicEntityObserverAdapter (){}  //Constructor Protected to force subclassing

    /**
     * @see GenomicEntityObserver.noteEntityDetailsChanged
     */
    public void noteEntityDetailsChanged(GenomicEntity entity, boolean initialLoad){}

    /**
     * @see GenomicEntityObserver.notePropertiesChanged
     */
    public void notePropertiesChanged(GenomicEntity entity, boolean initialLoad){}

    /**
     * @see GenomicEntityObserver.noteAliasesChanged
     */
    public void noteAliasesChanged(GenomicEntity entity, boolean initialLoad){}

    /**
     * @see GenomicEntityObserver.noteCommentsChanged
     */
    public void noteCommentsChanged(GenomicEntity entity, boolean initialLoad){}

    public Collection getCurrentObservables() {
       return observables;
    }

    /**
     * This method should only be called by the entity classes, not the view classes
     * --PED 2/27/01
     */
    public void addObservable(GenomicEntity entity) {
       if (!observables.contains(entity)) observables.add(entity);
    }
}
