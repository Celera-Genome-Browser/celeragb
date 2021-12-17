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
package api.stub.data;

public class GenomicProperty implements java.io.Serializable
{
    private String name;
    private String editingClass;
    private String initialValue;
    private boolean editable;
    private String vocabIndex;
    private GenomicProperty[] subProps = null;
    private boolean isComputed;

    public GenomicProperty() {}

    public GenomicProperty
        (String name,
        String editingClass,
        String initialValue,
        boolean editable,
        String vocabIndex,
        boolean isComputed) {
     this (name,editingClass,initialValue,editable,vocabIndex);
     this.isComputed=isComputed;
    }


    public GenomicProperty
        (String name,
        String editingClass,
        String initialValue,
        boolean editable,
        String vocabIndex)
    {
        // Do not permit a null name because it is used in hashCode
        this.name = (name != null) ? name : "";
        this.editingClass = editingClass;
        // Do not permit a null editing class!!!!
        if (this.editingClass==null) this.editingClass="";
        this.initialValue = initialValue;
        this.editable = editable;
        this.vocabIndex = vocabIndex;
    }

    public GenomicProperty
        (String name,
        String editingClass,
        String initialValue,
        boolean editable,
        String vocabIndex,
        GenomicProperty[] subProps,
        boolean isComputed) {
     this (name,editingClass,initialValue,editable,vocabIndex,subProps);
     this.isComputed=isComputed;
    }


    public GenomicProperty
        (String name,
        String editingClass,
        String initialValue,
        boolean editable,
        String vocabIndex,
        GenomicProperty[] subProps)
    {
      this(name,editingClass,initialValue,editable,vocabIndex);
      this.setSubProperties(subProps);
    }

    public String getName() { return name; }
    public String getEditingClass() { return editingClass; }
    public String getInitialValue() { return initialValue; }

    public boolean getExpandable() {
      if (getSubProperties()==null && getEditingClass()==null) return false;
      if (getSubProperties()!=null && getSubProperties().length>=0) return true;
      if (getEditingClass()!=null && !getEditingClass().equals("")) return true;
      return false;
    }

    public boolean getEditable() { return editable; }
    public String getVocabIndex() { return vocabIndex; }
    public boolean isComputed() { return isComputed; }

    public GenomicProperty[] getSubProperties() { return subProps; }
    public boolean hasSubProperty() { return subProps != null; }

    public void setInitialValue(String value) {
      this.initialValue = value;
    }
    public void setSubProperties(GenomicProperty[] subProps) { this.subProps = subProps; }
    public void setIsComputed(boolean isComputed) { this.isComputed = isComputed; }

    public int hashCode() {
       return name.hashCode();
    }

    public String debugString()
    {
      String str =
        "name " + name + ", " +
        "initial val " + initialValue + ", " +
        "editingClass " + editingClass + ", " +
        "expandable " + getExpandable() + ", " +
        "editable " + editable + ", " +
        "vocab idx " + vocabIndex;
      return str;
    }

    /**
     * Override equals so that dedups and other comparisons may be made within
     * collections.
     */
    public boolean equals(Object otherObject) {

      if (! (otherObject instanceof GenomicProperty))
          return false;

      GenomicProperty otherProperty = (GenomicProperty)otherObject;

      // Strategy: build one single string out of all the string values in
      // each object.  Concatenate it with 'rare' separators to avoid overlaps
      // of values that "look" the same by coincidence.
      StringBuffer testValues = new StringBuffer(100);
      testValues.append(name);
      testValues.append(editingClass);
      testValues.append(initialValue);
      testValues.append(vocabIndex);
      testValues.append(isComputed);
      testValues.append(getExpandable());

      StringBuffer otherTestValues = new StringBuffer(100);
      otherTestValues.append(otherProperty.getName());
      otherTestValues.append(otherProperty.getEditingClass());
      otherTestValues.append(otherProperty.getInitialValue());
      otherTestValues.append(otherProperty.getVocabIndex());
      otherTestValues.append(otherProperty.isComputed());
      otherTestValues.append(otherProperty.getExpandable());

      if (testValues.toString().equals(otherTestValues.toString()) &&
          (editable == otherProperty.getEditable()) &&
          (getExpandable() == otherProperty.getExpandable()))
        return true;
      else
        return false;

    } // End method: equals


    public Object clone() {
      GenomicProperty retVal =
        new GenomicProperty(this.getName(),
                            this.getEditingClass(),
                            this.getInitialValue(),
                            this.getEditable(),
                            this.getVocabIndex(),
                            this.isComputed);
      GenomicProperty[] subProps = this.getSubProperties();
      if (subProps != null) {
        GenomicProperty[] newSubProps = new GenomicProperty[subProps.length];
        for (int i=0; i<subProps.length; i++) {
          newSubProps[i] = (GenomicProperty)subProps[i].clone();
        }
        retVal.setSubProperties(newSubProps);
      }
      return retVal;
    }

}
