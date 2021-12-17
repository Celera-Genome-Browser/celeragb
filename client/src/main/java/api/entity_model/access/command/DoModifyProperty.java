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
package api.entity_model.access.command;
/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Deepali Bhandari
 * @version $Id$
 */

import api.entity_model.management.Command;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.stub.data.GenomicProperty;
import api.stub.data.Util;

import java.util.Date;

public class DoModifyProperty extends Command {
    private GenomicEntity entity;
    private String propertyName;
    private String initialValue;
    private String newValue;
    private static String CMDNAME="Modify Property";
    private String undoToScratchState = null;
    private String parentPropertyName;
    private GenomicEntity.GenomicEntityMutator entityMutator=null;

    /**
     * Constuctor used when acting as the original command, and NOT as an Undo...
     */
    public DoModifyProperty(GenomicEntity entity,String propertyName, String initialValue, String newValue) {
        this.entity=entity;
        this.propertyName=propertyName;
        this.initialValue=initialValue;
        this.newValue=newValue;
        // this.setIsActingAsUndoAndUndoName(false, null);
    }

    public DoModifyProperty(GenomicEntity entity,String propertyName, String initialValue, String newValue,
                            String parentProperty) {
        this(entity,propertyName,initialValue,newValue);
        this.parentPropertyName=parentProperty;
    }



    /**
     * Execute this command.
     */
    public Command execute() throws Exception{

        // Make the change...
        GenomicProperty parentProperty=entity.getParentProperty(propertyName);
        entity.getMutator(this, "acceptEntityMutator");
        if (parentPropertyName==null) entityMutator.setProperty(propertyName,newValue);

        if(propertyName.equals(FeatureFacade.REVIEWED_BY_PROP) && (newValue!=null && ! newValue.equals(" ")&&!newValue.equals(""))){
            entityMutator.setProperty(FeatureFacade.DATE_REVIEWED_PROP,Util.getDateTimeStringNow());
        }
        else if(propertyName.equals(FeatureFacade.REVIEWED_BY_PROP) && (newValue==null ||newValue.equals(" ")|| newValue.equals(""))){
            entityMutator.setProperty(FeatureFacade.DATE_REVIEWED_PROP," ");
        }

        //  Only want to register the true values.
        if(parentProperty!=null && parentProperty.getName().equals(TranscriptFacade.CURATION_FLAGS_PROP)){
          /**
           * Even though we already have the parent property WE MUST ASK AGAIN.  Somewhere,
           * someone is not passing the original so setting the child property is not
           * reflected in the parent we have.  Asking for the parent again gives us the one that the
           * entity has now; which in turn has the correct child with the new value.
           */
          parentProperty=entity.getParentProperty(propertyName);
          int trueValue = 0;
          GenomicProperty[] tmpFlags = parentProperty.getSubProperties();
          for (int x = 0; x < tmpFlags.length; x++) {
            String initialValue = ((GenomicProperty)tmpFlags[x]).getInitialValue();
            if (initialValue.equals("1") || initialValue.equals("0"))
              trueValue++;
          }
          entityMutator.setProperty(TranscriptFacade.CURATION_FLAGS_PROP,String.valueOf(trueValue));
        }
        setFocusEntity(entity);
        this.timeofCommandExecution=new Date().toString();
        return null;
    }


    /**
     * getMutator() call back for feature.....
     */

    public void acceptEntityMutator(GenomicEntity.GenomicEntityMutator emutator){

        this.entityMutator=(GenomicEntity.GenomicEntityMutator)emutator;

    }



    public String toString() {
        return CMDNAME;
    }


    public String getCommandLogMessage(){

       String entityOid=entity.getOid().toString();
       String entityType=entity.getEntityType().toString();
       this.actionStr="Modified Property "+propertyName+" with newValue="+newValue+" on Entity "+entityType+ ", id="+entityOid;
       return(super.getCommandLogMessage());
    }

}