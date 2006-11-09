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
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import vizard.genomics.glyph.TranscriptPainter;
import vizard.genomics.model.FeatureAdapter;

public class HspGapPainter extends TranscriptPainter {

	protected static final String HSP_INTRON_DISPLAY_STATE 			= "HSPIntronDisplayState";
	protected static final String HSP_INTRON_STATE_ADJACENT 		= "Adjacent";
	protected static final String HSP_INTRON_STATE_NON_ADJACENT 	= "NonAdjacent";
	protected static final String HSP_INTRON_STATE_OFF 				= "Off";
	protected static final String HSP_INTRON_THICKNESS_PERCENTAGE	= "PercentIntronThickness";
	protected static final double DEFAULT_INTRON_WIDTH				= 0.10;
	
	protected String intronState = HSP_INTRON_STATE_OFF;
	protected double intronHeight = DEFAULT_INTRON_WIDTH;
	protected SessionModelListener mySessionModelListener;
    private boolean isPrevHSPAdjHSP = false;
    private boolean isPrevHspNonAdj = false;
    
    public void paint(vizard.GraphicContext gc) {
        super.paint(gc);
    }

    public HspGapPainter(FeatureAdapter transcript) {
		super(transcript);
		String tmpHspIntronState = (String)SessionMgr.getSessionMgr().getModelProperty(HSP_INTRON_DISPLAY_STATE);
		if (tmpHspIntronState==null || tmpHspIntronState.equals("")) {
		  tmpHspIntronState=HSP_INTRON_STATE_OFF;
		  SessionMgr.getSessionMgr().setModelProperty(HSP_INTRON_DISPLAY_STATE, HSP_INTRON_STATE_OFF);
		}
		  
		String tmpIntronWidth = (String)SessionMgr.getSessionMgr().getModelProperty(HSP_INTRON_THICKNESS_PERCENTAGE);
		if (tmpIntronWidth==null || tmpIntronWidth.equals("")) {
			  tmpIntronWidth = "30";
			  SessionMgr.getSessionMgr().setModelProperty(HSP_INTRON_THICKNESS_PERCENTAGE, tmpIntronWidth);
		} 
		  
		setIntronHeight(Double.parseDouble(tmpIntronWidth)/100.0);
		setIntronState(tmpHspIntronState);
		
		if (mySessionModelListener == null) {
		  mySessionModelListener = new MySessionModelListener();
		  //System.out.println("Adding session model listener");
		  SessionMgr.getSessionMgr().addSessionModelListener(mySessionModelListener);
		}
    }

	protected class MySessionModelListener implements SessionModelListener {
	  public void browserAdded(BrowserModel browserModel){}
	  public void browserRemoved(BrowserModel browserModel){}
	  public void sessionWillExit(){}
	  public void modelPropertyChanged(Object key, Object oldValue, Object newValue){
		if (key.equals(HSP_INTRON_THICKNESS_PERCENTAGE)) {
		  	setIntronHeight(Double.parseDouble((String)newValue)/100.0);
			if ((HSP_INTRON_STATE_ADJACENT.equalsIgnoreCase(intronState) && isPrevHSPAdjHSP) ||
					 (HSP_INTRON_STATE_NON_ADJACENT.equalsIgnoreCase(intronState) && isPrevHspNonAdj)) {
				String tmpIntronWidth = (String)SessionMgr.getSessionMgr().getModelProperty(HSP_INTRON_THICKNESS_PERCENTAGE);
				if (tmpIntronWidth==null || tmpIntronWidth.equals("")) {
					  tmpIntronWidth = "30";
					  SessionMgr.getSessionMgr().setModelProperty(HSP_INTRON_THICKNESS_PERCENTAGE, tmpIntronWidth);
				} 
		
				setIntronHeight(Double.parseDouble(tmpIntronWidth)/100.0);
			}
			else {
				setIntronHeight(DEFAULT_INTRON_WIDTH);
			} 
		}
		else if (key.equals(HSP_INTRON_DISPLAY_STATE)) {
			setIntronState((String)newValue);
			if ((HSP_INTRON_STATE_ADJACENT.equalsIgnoreCase(intronState) && isPrevHSPAdjHSP) ||
					 (HSP_INTRON_STATE_NON_ADJACENT.equalsIgnoreCase(intronState) && isPrevHspNonAdj)) {
				String tmpIntronWidth = (String)SessionMgr.getSessionMgr().getModelProperty(HSP_INTRON_THICKNESS_PERCENTAGE);
				if (tmpIntronWidth==null || tmpIntronWidth.equals("")) {
					  tmpIntronWidth = "30";
					  SessionMgr.getSessionMgr().setModelProperty(HSP_INTRON_THICKNESS_PERCENTAGE, tmpIntronWidth);
				} 
		
				setIntronHeight(Double.parseDouble(tmpIntronWidth)/100.0);
			}
			else {
				setIntronHeight(DEFAULT_INTRON_WIDTH);
			} 
		}
	  }
	}

	/**
	 * Glyph specialization.
	 */
	public double y() {
	  return transcript.height() * (1 - heightCoeff()) / 2;
	}

	/**
	 * Glyph specialization.
	 */
	public double height() {
	  return transcript.height() * heightCoeff();
	}

	protected double heightCoeff() {
		return getIntronHeight();
	}

	protected double getIntronHeight() { return intronHeight; }
	protected void setIntronHeight(double height) { 
		this.intronHeight = height; 
		//System.out.println("Setting the height to "+height);
	}

	protected String getIntronState() { return intronState; }
	protected void setIntronState(String state) { 
		intronState = state; 
		//System.out.println("Setting the state to "+state);
		if (HSP_INTRON_STATE_OFF.equalsIgnoreCase(state)) {
			setIntronHeight(DEFAULT_INTRON_WIDTH);			
		}
	}

	protected void setPrevAdjHsp(boolean isPrevAdjHsp) {
		this.isPrevHSPAdjHSP = isPrevAdjHsp;
	}

	protected void setPrevNonAdjHsp(boolean isPrevNonAdjHsp) {
		this.isPrevHspNonAdj = isPrevNonAdjHsp;
	}

}