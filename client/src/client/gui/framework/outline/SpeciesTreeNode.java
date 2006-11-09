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
/**
 ********************************************************************
 * CVS_ID:  $Id$
 */

package client.gui.framework.outline;

import api.entity_model.access.observer.AxisObserverAdapter;
import api.entity_model.access.observer.LoadRequestStatusObserverAdapter;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.LoadRequest;
import api.entity_model.model.fundtype.LoadRequestState;
import api.entity_model.model.fundtype.LoadRequestStatus;
import api.entity_model.model.genetics.Chromosome;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class SpeciesTreeNode extends GenomicEntityTreeNode {
    // JCVI LLF: 10/20/2006
//	 RT 10/27/2006
    private static final ImageIcon targetIcon = new ImageIcon(Renderer.class.getResource("/resource/client/images/genome.gif"));
    List children;
    LoadRequestStatus status;
    SpeciesObserver speciesObserver=new SpeciesObserver();
    GenomeVersion genomeVersion;


    public SpeciesTreeNode(Species species) {
        super(species);
        genomeVersion=species.getGenomeVersion();
        species.addAxisObserver(speciesObserver,true);
    }

    public Color getNonSelectedRenderTextColor() {
      if (genomeVersion.hasWorkspace()) return Color.red;
      return super.getNonSelectedRenderTextColor();
    }

    public Icon getNodeIcon() {
      return targetIcon;
    }

    public void loadChildren() {
       if (!((Species)userObject).getChromosomeLoadFilter().getLoadFilterStatus().isCompletelyLoaded()) {
           LoadRequest chromosomeLoadRequest=((Species)userObject).
             getChromosomeLoadRequest();
           status=((Axis)userObject).loadAlignmentsToEntitiesBackground(chromosomeLoadRequest);
           status.addLoadRequestStatusObserver(new MyLoadRequestStatusObserver(),true);
       } else{
          childrenLoaded=true;
       }
    }

    public String toString() {
       return getUserObject().toString()+":"+((Species)getUserObject()).getGenomeVersion().getDescription();
       //super.toString()+" : "+((Species)getUserObject()).getGenomeVersion().getAssemblyVersion();
    }

    void aboutToBeRemoved(){
       ((GenomicEntity)getUserObject()).removeGenomicEntityObserver(speciesObserver);
    }

    private void displayChildren() {
       if (children!=null) {
           removeAllChildren();
           Collections.sort(children,new Sorter());
           int[] changedIndicies=new int[children.size()];
           for (int i=0;i<children.size();i++) {
              add(new ChromosomeTreeNode((Chromosome)children.get(i)));
              changedIndicies[i]=i;
           }
           postChildrenAdded(SpeciesTreeNode.this, changedIndicies);
       }
    }

    class SpeciesObserver extends AxisObserverAdapter {
        private SpeciesObserver() { }

        public void noteAlignmentOfEntity(Alignment alignment) {
            GenomicEntity ge=alignment.getEntity();
            if (ge instanceof Chromosome) {
                if (children==null) children=new ArrayList();
                children.add(ge);
                if (status==null) {
                  displayChildren();
                }
            }
        }
    }

    class MyLoadRequestStatusObserver extends LoadRequestStatusObserverAdapter {
       public void stateChanged(LoadRequestStatus loadRequestStatus, LoadRequestState newState){
           if (newState==LoadRequestStatus.COMPLETE) {
               childrenLoaded=true;
               loadRequestStatus.removeLoadRequestStatusObserver(this);
               status=null;
               displayChildren();
           }
       }

    }

    class Sorter implements Comparator {
       public int compare(Object o1, Object o2) {
         String str1=o1.toString();
         String str2=o2.toString();

         if (str1.equals(str2)) return 0;
         if (str1.charAt(3)=='u' || str1.charAt(3)=='U' &&
          !(str2.charAt(3)=='u' || str2.charAt(3)=='U'))
          return 1;
         if (str2.charAt(3)=='u' || str2.charAt(3)=='U' &&
          !(str1.charAt(3)=='u' || str1.charAt(3)=='U'))
          return -1;
         if (str1.endsWith("U") || str1.endsWith("u")) return 1;
         if (str2.endsWith("U") || str2.endsWith("u")) return -1;
         int lastChar1=str1.length();
         int lastChar2=str2.length();
         for (int i=0;i<str1.length();i++) {
           if (Character.isDigit(str1.charAt(i))) {
             lastChar1=i-1;
             break;
           }
         }
         for (int i=0;i<str2.length();i++) {
           if (Character.isDigit(str2.charAt(i))) {
             lastChar2=i-1;
             break;
           }
         }
         int rtn=str1.substring(0,lastChar1).compareTo(str2.substring(0,lastChar2));
         if (rtn!=0) return rtn;

         String num1=str1.substring(lastChar1+1,str1.length());
         String num2=str2.substring(lastChar2+1,str2.length());

         long number1=0;
         long number2=0;

         try {
           number2=Long.parseLong(num2);
           number1=Long.parseLong(num1);
         }
         catch (Exception ex) {
           System.out.println("Exception in StringNumberComparator");
           return 0;
         }

         if (number1>number2) return 1;
         if (number2>number1) return -1;
         return 0;
       }

   }

}

