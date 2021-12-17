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

public class ControlledVocabElement implements java.io.Serializable
{
    public String value;

    public String name;

    public ControlledVocabElement()
    {
    }

    public ControlledVocabElement
        (String value,
        String name)
    {
        this.value = value;
        this.name = name;
    }

    public String getValue() { return value; }
    public String getName() { return name; }

    public int hashCode() {
      return name.hashCode();
    }

    public boolean equals(Object otherObject) {
      if (!(otherObject instanceof ControlledVocabElement)) return false;
      ControlledVocabElement other=(ControlledVocabElement) otherObject;
      //Only test the key here for equality.
      if (other.name.equals(name)) return true;
      else return false;
    }
}
