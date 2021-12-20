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




public class GenomicAxisInfo implements java.io.Serializable
{
  private OID axisOID;
  private String axisName;
  private long magnitude;
  private int axisTypeSid;
  private int unit;
  private float conversionFactor;

/**
 * @level developer
 */
  public GenomicAxisInfo( OID oid, String axisName, long magnitude, int axisTypeSID, int unit, float conversionFactor)
  {
    this.axisOID = oid;
    this.axisName = axisName;
    this.magnitude = magnitude;
    this.axisTypeSid = axisTypeSID;
    this.unit = unit;
    this.conversionFactor = conversionFactor;
  }

  public OID getGenomicAxisID()
  {
    return axisOID;
  }

  public String getGenomicAxisName()
  {
    return axisName;
  }

  public long getMagnitude()
  {
    return magnitude;
  }

  public long getUnit()
  {
    return unit;
  }

  public float getConversionFactor()
  {
    return conversionFactor;
  }

  public void print()
  {
    System.out.println("OID: " + axisOID + ", name " + axisName + ", magnitude " +
      magnitude + ", unit " + unit + ", conversion factor " + conversionFactor);
  }

  public boolean equals(Object anotherObject)
  {
    GenomicAxisInfo anotherAxis = (GenomicAxisInfo)anotherObject;
    return (this.axisOID.equals(anotherAxis.axisOID));
  }
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("OID: ");
    buf.append(axisOID);
    buf.append(", axisName ");
    buf.append(axisName);
    buf.append(", magnitude ");
    buf.append(magnitude);
    buf.append(", unit ");
    buf.append(unit);
    buf.append(", conversion factor ");
    buf.append(conversionFactor);
    return buf.toString();
  }
}
