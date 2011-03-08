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
package client.gui.components.annotation.axis_annotation;

import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.HSPFeature;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.OID;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import client.gui.framework.session_mgr.SessionMgr;
import vizard.Glyph;
import vizard.genomics.model.FeatureAdapter;

import java.awt.*;


public class HSPGlyph extends FeatureGlyph {
   private MutableRange highlightedRange;
   private boolean isPrevAdj = false;
   private boolean isPrevNonAdj = false;
   
   public HSPGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment) {
      super(glyphFactory, alignment);
      loadProperties();
   }

   protected void loadProperties() {
      super.loadProperties();
   }

   protected void checkReverseComplementAndSetRange(Range range) {
	  super.checkReverseComplementAndSetRange(range);
	  getHSPIntronGlyphIfNecessary();	  
   }

   public Glyph getHSPIntronGlyphIfNecessary() {
      boolean intronStateSucceeded = computeHighlightedRange();
      if ( highlightedRange == null ) {
         return (null);
      }
      FeatureAdapter highlightedRangeAdapter = new FeatureAdapter() {
         public Color color() { return (HSPGlyph.this.color());}
         public int start() {
            int tmpStart = 0;
            if (null!=highlightedRange) {
            	tmpStart = highlightedRange.getMinimum();
            }
            return tmpStart;
         }
         public int end() {
            int tmpEnd = 0;
            if (null!=highlightedRange) {
            	tmpEnd = highlightedRange.getMaximum();
            }
            return tmpEnd;
         }
         public double height() { return (HSPGlyph.this.height());}
      };
      HspGapPainter gapPainter = new HspGapPainter(highlightedRangeAdapter);
      if (!intronStateSucceeded) {
      	gapPainter.setIntronHeight(HspGapPainter.DEFAULT_INTRON_WIDTH);
      }
      gapPainter.setPrevAdjHsp(isPrevAdj);
      gapPainter.setPrevNonAdjHsp(isPrevNonAdj);
	  return (gapPainter);
   }

   /**
    * This method tries to determine the intron range and thickness properties
    * @return boolean as to whether the intron state check was successful.  This is used to determine the
    * intron thickness.
    */
   protected boolean computeHighlightedRange() {
      highlightedRange = null;
	  boolean previousHSPMatchedCriteria = false;
	  
      HSPFeature hsp = (HSPFeature)alignment.getEntity();
      if ( hsp == null )
         return false;
      OID prevOid;
      // Check the HSP Intron Display State: Adjacent, Non-Adjacent, or Off.
      String intronState = (String) SessionMgr.getSessionMgr().getModelProperty(HspGapPainter.HSP_INTRON_DISPLAY_STATE);
      if (null==intronState) {
		intronState = HspGapPainter.HSP_INTRON_STATE_OFF;
      }
      
      // See if the intron state produces a winning HSP
      if (HspGapPainter.HSP_INTRON_STATE_ADJACENT.equalsIgnoreCase(intronState)) { 
      	prevOid = hsp.getPreviousAdjacentHSP();
		if (prevOid!=null) {
			previousHSPMatchedCriteria=true;
			isPrevAdj = true;
		}
		//System.out.println("Returning the prev adj hsp of "+prevOid);
      }
      else if (HspGapPainter.HSP_INTRON_STATE_NON_ADJACENT.equalsIgnoreCase(intronState)) {
      	prevOid = hsp.getPreviousNonAdjacentHSP();
		if (prevOid!=null) {
			previousHSPMatchedCriteria=true;
			isPrevNonAdj = true;
		}
        //System.out.println("Returning the prev non-adj hsp of "+prevOid);
      }
      // For the off state, the intron width is already set so just get the nearest HSP adj or not.
      else if (HspGapPainter.HSP_INTRON_STATE_OFF.equalsIgnoreCase(intronState)) {
      	prevOid = hsp.getPreviousAdjacentHSP();
      	// If not prev adj hsp then MUST be non-adj, if hsp exists at all
      	if (null==prevOid) {
      		prevOid = hsp.getPreviousNonAdjacentHSP();
 			if (prevOid!=null) {
 				isPrevNonAdj = true;     	
 			}
      	}
      	else {
      		isPrevAdj = true;
      	}
		if (prevOid!=null) {
			previousHSPMatchedCriteria=true;
		}
      }
      else {
      	prevOid = null;
      }
      
      // If the previous OID is null, the adjacent or non-adjacent conditions failed.
      // Check for any case HSP and set the intron width to nominal if found.
      if ( prevOid == null ) { 
		prevOid = hsp.getPreviousAdjacentHSP();
		// If not prev adj hsp then MUST be non-adj, if hsp exists at all
		if (null==prevOid) {
			prevOid = hsp.getPreviousNonAdjacentHSP();
			if (prevOid!=null) {
				isPrevNonAdj = true;     	
			}
		}
		else {
			isPrevAdj = true;
		}
      } 

	  // This should only happen when there is no HSP before the one being checked.
	  // Should be null for the leading edge HSP.
	  if (prevOid==null) {
		return false;	  	
	  }

      GenomeVersion genome = hsp.getGenomeVersion();
      HSPFeature prevHsp = (HSPFeature)genome.getLoadedGenomicEntityForOid(prevOid);
      if ( prevHsp == null ) {
      	System.out.println("The prevHsp is not loaded into the model.");
      	return false;
      } 

      GeometricAlignment prevAlignment = (GeometricAlignment)prevHsp.getOnlyAlignmentToOnlyAxis();

      // If the first HSP arrives before the adjacent one, the alignment will be null.
      // Eventually, the adjacent one will arrive and draw the thick line.
      // Otherwise, no thick line for you!
      if ( prevAlignment == null ) return false;
      Range prevRange = prevAlignment.getRangeOnAxis();
      Range nextRange = alignment.getRangeOnAxis();
      if ( nextRange.getMinimum() < prevRange.getMinimum() ) {
         Range tmp = prevRange; prevRange = nextRange; nextRange = tmp;
      }
	  highlightedRange = new MutableRange(prevRange.getMaximum(), nextRange.getMinimum());
      if ( glyphFactory.isReverseComplement() ) {
    	 highlightedRange.mirror(glyphFactory.axis().getMagnitude());
      }
      return previousHSPMatchedCriteria;
	  //System.out.println("The highlighted range is "+highlightedRange);
   }
}
