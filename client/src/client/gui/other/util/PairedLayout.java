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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
package client.gui.other.util;

import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;

public class PairedLayout implements LayoutManager {

    private Vector labels = new Vector();
    private Vector fields = new Vector();
    private boolean rightFill = true;
    private boolean useMaxHeight = true;

    private int yGap = 3;
    private int xGap = 8;
	//minimum value the label area should be
	private int minLabelWidth = 0;

    public static final String LABEL = "label";
    public static final String FIELD = "field";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";

    public PairedLayout()
    {
        super();
    }

    public PairedLayout( boolean useMax )
    {
        this();
        setUseMaxHeight( useMax );
    }

    public PairedLayout( String region )
    {
        this( 8, 3, region );
    }

    public PairedLayout( int xgap, int ygap )
    {
        this( xgap, ygap, RIGHT );
    }

    public PairedLayout( int xgap, int ygap, String region )
    {
        this();
        setXGap( xgap );
        setYGap( ygap );
        setFillRegion( region );
    }

    public void addLayoutComponent(String s, Component c)
    {
	    if (s.equals(LABEL))
	    {
	        labels.addElement(c);
	    }
	    else
	    {
	        fields.addElement(c);
	    }
    }

    public int getYGap()
    {
        return yGap;
    }

    public void setYGap( int yGap )
    {
        this.yGap = yGap;
    }

    public int getXGap()
    {
        return xGap;
    }

    public void setXGap( int xGap )
    {
        this.xGap = xGap;
    }

    public void setFillRegion( String region )
    {
        if( region.equals( LEFT ) ) rightFill = false;
        else rightFill = true;
    }

    public String getFillRegion()
    {
        if( rightFill ) return RIGHT;
        return LEFT;
    }

    public void setUseMaxHeight( boolean max )
    {
        useMaxHeight = max;
    }

    public boolean isUseMaxHeight()
    {
        return useMaxHeight;
    }

    public int getLabelWidth()
    {
        return Math.max(getComponentMaxWidth( labels.elements()), minLabelWidth);
    }

	/**
	 * Set the minimum width of the labels.  The area may be larger if
	 * a component label's preferred width is larger.
	 */
	public void setMinLabelWidth(int min)
	{
		minLabelWidth = min;
	}

    private boolean isComponentIncluded( Container c, Component com )
    {
        Component[] comArr = c.getComponents();
        for( int i = 0; i < comArr.length; i++ )
        {
            if( comArr[i] == com ) return true;
        }
        return false;
    }

    private int getComponentMaxWidth( Enumeration iter )
    {
        int width = 0;

        while(iter.hasMoreElements())
	    {
	        Component comp = (Component)iter.nextElement();
	        width = Math.max( width, comp.getPreferredSize().width );
	    }

	    return width;
	}

	private int getComponentMaxHeight( Enumeration iter )
    {
        int height = 0;

        while(iter.hasMoreElements())
	    {
	        Component comp = (Component)iter.nextElement();
	        height = Math.max( height, comp.getPreferredSize().height );
	    }

	    return height;
	}

    public void layoutContainer(Container c)
    {
	    Insets insets = c.getInsets();

	    int labelWidth = 0;
	    int fieldWidth = 0;
	    int height = 0;

	    if( useMaxHeight )
	    {
	        height = Math.max( getComponentMaxHeight( labels.elements() ),
	                           getComponentMaxHeight( fields.elements() ) );
	    }

	    if( rightFill )
	    {
			labelWidth = getLabelWidth();
	        fieldWidth = c.getSize().width - (labelWidth + xGap + insets.left + insets.right);
	    }
	    else
	    {
	        fieldWidth = getComponentMaxWidth( fields.elements() );
			labelWidth = c.getSize().width - (fieldWidth + xGap + insets.left + insets.right);
			labelWidth = Math.max(labelWidth, minLabelWidth);
	    }

	    int yPos = insets.top;

    	Enumeration fieldIter = fields.elements();
    	Enumeration labelIter = labels.elements();
    	while(labelIter.hasMoreElements() && fieldIter.hasMoreElements())
    	{
	        Component label = (Component)labelIter.nextElement();
	        Component field = (Component)fieldIter.nextElement();
	        if( isComponentIncluded( c, label ) )
	        {
	            if( !useMaxHeight )
	            {
	                height = Math.max( label.getPreferredSize().height,
	                                   field.getPreferredSize().height );
	            }
	            label.setBounds( insets.left, yPos, labelWidth, height );
	            field.setBounds( insets.left + labelWidth + xGap, yPos, fieldWidth, height );

	            yPos += (height + yGap);
	        }
	    }
    }

    public Dimension minimumLayoutSize(Container c)
    {
	    Insets insets = c.getInsets();

	    int labelWidth = 0;
	    int height = 0;

	    if( useMaxHeight )
	    {
	        height = Math.max( getComponentMaxHeight( labels.elements() ),
	                           getComponentMaxHeight( fields.elements() ) );
	    }

		labelWidth = getLabelWidth();

	    int yPos = insets.top + insets.bottom;

    	Enumeration labelIter = labels.elements();
    	Enumeration fieldIter = fields.elements();
    	while(labelIter.hasMoreElements() && fieldIter.hasMoreElements())
    	{
	        Component label = (Component)labelIter.nextElement();
	        Component field = (Component)fieldIter.nextElement();
	        if( isComponentIncluded( c, label ) )
	        {
	            if( !useMaxHeight )
	            {
	                height = Math.max( label.getPreferredSize().height,
	                                   field.getPreferredSize().height );
	            }
	            yPos += (height + yGap);
	        }
	    }
	    yPos -= yGap;

	    return new Dimension( labelWidth * 3 , yPos );
    }

    public Dimension preferredLayoutSize(Container c)
    {
	    Dimension d = minimumLayoutSize(c);
	    d.width *= 2;

        return d;
    }

    public void removeLayoutComponent(Component c) {}

}
