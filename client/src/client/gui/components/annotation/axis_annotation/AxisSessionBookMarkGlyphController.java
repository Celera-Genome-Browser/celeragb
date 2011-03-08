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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.data.OID;
import api.stub.geometry.Range;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModelListener;
import vizard.genomics.glyph.AxisRulerGlyph;
import vizard.glyph.FastRectGlyph;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Iterator;

public class AxisSessionBookMarkGlyphController extends Controller implements BrowserModelListener{

   private  AxisRulerGlyph axisRulerGlyph;
   private Browser browser;
   private HashMap axesBookmarksHash=new HashMap();
   private GenomicAxisAnnotationView view;
   private OID axisOid=new OID();

   public AxisSessionBookMarkGlyphController(GenomicAxisAnnotationView view,AxisRulerGlyph axisRulerGlyph,
			  Browser browser  )
    {
       super(view);
       this.view=view;
       this.browser = browser;
	   browser.getBrowserModel().addBrowserModelListener(this);
       this.axisRulerGlyph=axisRulerGlyph;
	   this.axisOid = browser.getBrowserModel().getMasterEditorEntity().getOid();
	   
	   // Set the waypoint setters and getters
	   for (int x=0; x<10; x++) {
		   int keyBase = 48+x;
		   view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyBase, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, true), "setWaypoint"+x);
		   view.getActionMap().put("setWaypoint"+x, new SetWaypointAction(x));
		   
		   view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyBase, InputEvent.CTRL_MASK, true), "getWaypoint"+x);
  		   view.getActionMap().put("getWaypoint"+x, new GetWaypointAction(x));
	   }
    }


	private class SetWaypointAction extends AbstractAction {
		private int slot = 0;
		
		public SetWaypointAction(int slot) {
			this.slot = slot;
		}
		
		public void actionPerformed(ActionEvent e) {
		 int end=(int)view.axisModel().origin()+(int)view.axisModel().viewSize();
		 Range visibleRange=new Range((int)view.axisModel().origin(),end);
		 AxisSessionBookmarkGlyph bookMarkGlyph = new AxisSessionBookmarkGlyph(axisRulerGlyph,
		 	browser.getBrowserModel().getMasterEditorEntity().getOid(), slot,  visibleRange);
		 
		 //check if this book mark already exists if so remove it.
		 if(axesBookmarksHash.get(axisOid)!=null){
			 HashMap h=(HashMap)axesBookmarksHash.get(axisOid);
			 AxisSessionBookmarkGlyph g=(AxisSessionBookmarkGlyph)h.get(new Integer(slot));
			 if(g!=null){
			   if(g.equals(bookMarkGlyph)){
				 h.remove(g);
				 axisRulerGlyph.ruler().parent().removeChild(g);
			   }
			 }
		 }
		 bookMarkGlyph.setLocation((int)view.axisModel().origin());
		 axisRulerGlyph.ruler().parent().addChild(bookMarkGlyph);

		 if(axesBookmarksHash.get(browser.getBrowserModel().getMasterEditorEntity().getOid())==null){
			 HashMap h=new HashMap();
			 h.put(new Integer(slot),bookMarkGlyph);
			 axesBookmarksHash.put(browser.getBrowserModel().getMasterEditorEntity().getOid(),h);
		  }else{
			 HashMap h=(HashMap)axesBookmarksHash.get(browser.getBrowserModel().getMasterEditorEntity().getOid());
			 h.put(new Integer(slot),bookMarkGlyph);
		 }

	  	axisOid=browser.getBrowserModel().getMasterEditorEntity().getOid();
	  }
	}	


	private class GetWaypointAction extends AbstractAction {
		private int slot = 0;
		
		public GetWaypointAction(int slot) {
			this.slot = slot;
		}
		
		public void actionPerformed(ActionEvent e) {
		 HashMap h=(HashMap)axesBookmarksHash.get(browser.getBrowserModel().getMasterEditorEntity().getOid());
		 if(h!=null){
			AxisSessionBookmarkGlyph a=(AxisSessionBookmarkGlyph)h.get(new Integer(slot));
			if(a !=null){
			 view.axisModel().setViewMinMax(a.getVisibleRange().getMinimum(), a.getVisibleRange().getMaximum());
			}
		 }
	  }
	}


    public void browserSubViewFixedRangeChanged(Range subViewFixedRange){}
    public void browserSubViewVisibleRangeChanged(Range subViewVisibleRange){}

    public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity){
    // first of all remove the bookmarks from the previous axis selection
       HashMap h1=(HashMap)axesBookmarksHash.get(axisOid);
	   if(h1!=null){
		 for(Iterator iter=h1.values().iterator();iter.hasNext();){
		  FastRectGlyph g= (FastRectGlyph)iter.next();
		  axisRulerGlyph.ruler().parent().removeChild(g);
      	 }
       }
     //second add the bookmarks for this axis if they exist in the hashmap.
      HashMap h=(HashMap)axesBookmarksHash.get(masterEditorEntity.getOid());
      if(h!=null){
        for(Iterator iter=h.values().iterator();iter.hasNext();){
           FastRectGlyph g= (FastRectGlyph)iter.next();
           axisRulerGlyph.ruler().parent().addChild(g);
        }
      }

       axisOid=masterEditorEntity.getOid();
    }
    public void browserCurrentSelectionChanged(GenomicEntity newSelection){}
    public void browserMasterEditorSelectedRangeChanged(Range masterEditorSelectedRange){}
    public void browserClosing(){}
    public void modelPropertyChanged(Object key, Object oldValue, Object newValue){}
}