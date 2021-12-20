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
package api.stub.data;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class FlaggedGenomicProperty extends GenomicProperty {

  private transient boolean hasBeenModified = false;
  private transient String watchedValue = "";

  /**
   * If anyone can think of a better name, please do.  This is a GenomicProperty
   * whose boolean flag alerts the user to the fact that it has been changed from
   * the initial value.
   */
    public FlaggedGenomicProperty() {}

    public FlaggedGenomicProperty
        (String name,
        String editingClass,
        String initialValue,
        boolean editable,
        String vocabIndex,
        boolean isComputed) {
     super (name, editingClass, initialValue, editable, vocabIndex, isComputed);
     watchedValue = initialValue;
    }


    public FlaggedGenomicProperty
        (String name,
        String editingClass,
        String initialValue,
        boolean editable,
        String vocabIndex) {
      super( name, editingClass, initialValue, editable, vocabIndex);
      watchedValue = initialValue;
    }

    public FlaggedGenomicProperty
        (String name,
        String editingClass,
        String initialValue,
        boolean editable,
        String vocabIndex,
        GenomicProperty[] subProps,
        boolean isComputed) {
      super (name, editingClass, initialValue, editable, vocabIndex, subProps, isComputed);
      watchedValue = initialValue;
    }


    public FlaggedGenomicProperty
        (String name,
        String editingClass,
        String initialValue,
        boolean editable,
        String vocabIndex,
        GenomicProperty[] subProps)
    {
      super(name, editingClass, initialValue, editable, vocabIndex, subProps);
      watchedValue = initialValue;
    }

    public void setInitialValue(String value) {
      if (value.equals(watchedValue)) {
        hasBeenModified = false;
      }
      else hasBeenModified = true;
      super.setInitialValue(value);
    }

    public boolean hasBeenModified() { return hasBeenModified; }
    public void setModifiedFlag(boolean newValue)  { hasBeenModified = newValue; }

    public Object clone() {
      FlaggedGenomicProperty retVal =
        new FlaggedGenomicProperty(this.getName(),
                            this.getEditingClass(),
                            this.getInitialValue(),
                            this.getEditable(),
                            this.getVocabIndex(),
                            this.isComputed());
      GenomicProperty[] subProps = this.getSubProperties();
      if (subProps != null) {
        FlaggedGenomicProperty[] newSubProps = new FlaggedGenomicProperty[subProps.length];
        for (int i=0; i<subProps.length; i++) {
          newSubProps[i] = (FlaggedGenomicProperty)subProps[i].clone();
        }
        retVal.setSubProperties(newSubProps);
      }
      retVal.setModifiedFlag(this.hasBeenModified);
      return retVal;
    }

}