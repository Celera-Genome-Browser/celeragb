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
package client.gui.framework.inspector;

import api.stub.data.*;
import java.util.*;
import api.stub.data.ControlledVocabulary;

public class SelectableProperty {


  private GenomicProperty genomicProperty;
  private ControlledVocabulary controlledVocabulary;

  public SelectableProperty(GenomicProperty gp,ControlledVocabulary cv) {
    this.controlledVocabulary=cv;
    this.genomicProperty=gp;
  }

  public boolean isEditable() {
     return genomicProperty.getEditable();
  }

  public String getSelectedElement() {
     return controlledVocabulary.lookup(genomicProperty.getInitialValue());
  }

  public Collection getSelectables() {
     return controlledVocabulary.getNames();
  }

  public GenomicProperty getGenomicProperty() { return genomicProperty; }

}