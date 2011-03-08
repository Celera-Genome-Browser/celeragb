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
package client.gui.other.widget;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Deepali Bhandari
 * @version $Id$
 */

import api.entity_model.model.annotation.PolyMorphism;

import java.awt.*;

/**
 * Class external to the peferences mechanism. This class retrieves Colors
 * based on the FunctionalDomain and Validation Status attributes of the Snp
 * Feature. The colors are returned based on the Snp Story board. Ultimately this
 * class should be consolidated with the preferences mechanism.
 */

public class SnpColors {

  private static SnpColors sc = new SnpColors();

  private SnpColors() {}// Singleton enforcement.

  public static SnpColors getSnpColors() { return sc; }

  public static Color getSnpStemColor(String validationStatus){
    Color retColor=null;
    if(validationStatus.equals(PolyMorphism.ValidationStatus.COMPUTATIONAL)){
      retColor=Color.blue;
    }else if(validationStatus.equals(PolyMorphism.ValidationStatus.VALIDATED)){
      retColor=Color.yellow;
    }else if(validationStatus.equals(PolyMorphism.ValidationStatus.UNKNOWN)){
       retColor=Color.gray;
    }
    if(retColor==null)retColor=Color.gray; // making the default color
    return retColor;
  }


  public static Color getSnpHeadColor(String functionalDomain){
    Color retColor=null;

    if(functionalDomain.equals(PolyMorphism.FunctionalDomain.INTERGENIC)){
      retColor=Color.blue;
    }else if(functionalDomain.equals(PolyMorphism.FunctionalDomain.INTRON)){
      retColor=Color.magenta;
    }else if(functionalDomain.equals(PolyMorphism.FunctionalDomain.MISSENSE)){
       retColor=Color.yellow;
    }
    else if(functionalDomain.equals(PolyMorphism.FunctionalDomain.NONSENSE)){
       retColor=Color.red;
    }
    else if(functionalDomain.equals(PolyMorphism.FunctionalDomain.SILENT)){
       retColor=Color.green;
    }
    else if(functionalDomain.equals(PolyMorphism.FunctionalDomain.UTR_3)){
       //light green color
       retColor=new Color(100, 255, 100);
    }
    else if(functionalDomain.equals(PolyMorphism.FunctionalDomain.UTR_5)){
       //light red Color
       retColor=new Color(255,100,100);
    }
    if(retColor==null) retColor=Color.gray;
    return retColor;
  }

}