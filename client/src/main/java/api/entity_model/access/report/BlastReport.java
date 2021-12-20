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
package api.entity_model.access.report;

import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.HSPFeature;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.HSPFacade;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reports the location of Blast hits that are aligned
 * to a particular Genomic Axis as a result of blasting a query
 * Sequence against the axis sequence in a given range.
 *
 * @author James Baxendale <jbaxenda>
 * @version 1.0
 */
public class BlastReport extends AbstractReport
{
  private static final String NO_ALIGNMENT_TO_AXIS_MSG
    = "HSP had no alignment to axis!";


  static public class BlastLineItem implements LineItem
  {
    private OID axisOID;
    private String axisName;
    /**
     * Each line item records a reference to the blast that it is for and
     * which child hsp the line item represents
     */
    private GeometricAlignment parentBlastAlignment;
    private GeometricAlignment hspAlignment;

    private static List fields=new ArrayList();

    static {
       fields.add(FeatureFacade.GENOMIC_AXIS_ID_PROP);
       fields.add(FeatureFacade.GENOMIC_AXIS_NAME_PROP);
       fields.add(FeatureFacade.AXIS_BEGIN_PROP);
       fields.add(FeatureFacade.AXIS_END_PROP);
       fields.add(HSPFacade.SUM_E_VAL_PROP);
       fields.add(HSPFacade.BIT_SCORE_PROP);
       fields.add(HSPFacade.INDIVIDUAL_E_VAL_PROP);
       fields.add(HSPFacade.PERCENT_IDENTITY_PROP);
    }

    public BlastLineItem
      (
        OID axisOID,
        String axisName,
        GeometricAlignment parentBlastAlignment,
        GeometricAlignment hspAlignment
      )
    {
      this.axisOID = axisOID;
      this.axisName = axisName;
      this.parentBlastAlignment = parentBlastAlignment;
      this.hspAlignment = hspAlignment;
    }

    public String getAxisOID()
    {
      return axisOID.toString();
    }

    public HSPFeature getHsp()
    {
      if (hspAlignment == null) {
        return null;
      }
      else {

        return (HSPFeature)hspAlignment.getEntity();
      }
    }

    public String getAxisName()
    {
      return axisName;
    }

    public int getEntityBeginOnAxis()
    {
      if (hspAlignment == null) {
        return 0;
      }
      else {
        return hspAlignment.getRangeOnAxis().getStart();
      }
    }

    public int getEntityEndOnAxis()
    {
      if (hspAlignment == null) {
        return 0;
      }
      else {
        return hspAlignment.getRangeOnAxis().getEnd();
      }
    }

    public String getSummaryEVal()
    {
      if (hspAlignment == null) {
        return NO_ALIGNMENT_TO_AXIS_MSG;
      }
      else {

        GenomicProperty prop
          = ((HSPFeature)hspAlignment.getEntity()).getProperty(HSPFacade.SUM_E_VAL_PROP);
        if (prop != null) {
          return prop.getInitialValue();
        }
        else {
          return "";
        }
      }
    }

    public String getBitScore()
    {
      if (hspAlignment == null) {
        return NO_ALIGNMENT_TO_AXIS_MSG;
      }
      else {
        GenomicProperty prop
          = ((HSPFeature)hspAlignment.getEntity()).getProperty(HSPFacade.BIT_SCORE_PROP);
        if (prop != null) {
          return prop.getInitialValue();
        }
        else {
          return "";
        }
      }
    }

    public String getIndividualEVal()
    {
      if (hspAlignment == null) {
        return NO_ALIGNMENT_TO_AXIS_MSG;
      }
      else {
        GenomicProperty prop
          = ((HSPFeature)hspAlignment.getEntity()).getProperty(HSPFacade.INDIVIDUAL_E_VAL_PROP);
        if (prop != null) {
          return prop.getInitialValue();
        }
        else {
          return "";
        }
      }
    }

    public String getPercentIdentity()
    {
      if (hspAlignment == null) {
        return NO_ALIGNMENT_TO_AXIS_MSG;
      }
      else {
        GenomicProperty prop
          = ((HSPFeature)hspAlignment.getEntity()).getProperty(HSPFacade.PERCENT_IDENTITY_PROP);
        if (prop != null) {
          return prop.getInitialValue();
        }
        else {
          return "";
        }
      }
    }

   /**
    * Returns the value for a particular field
    */
    public Object getValue(Object field){
       int index=fields.indexOf(field);
       switch (index) {
         case 0:
           return getAxisOID();
         case 1:
           return getAxisName();
         case 2:
           return Integer.toString(getEntityBeginOnAxis());
         case 3:
           return Integer.toString(getEntityEndOnAxis());
         case 4:
           return getSummaryEVal();
         case 5:
           return getBitScore();
         case 6:
           return getIndividualEVal();
         case 7:
           return getPercentIdentity();
         default:
           return null;
       }

    }

    public List getFields(){
       return Collections.unmodifiableList(fields);
    }
  }
}

