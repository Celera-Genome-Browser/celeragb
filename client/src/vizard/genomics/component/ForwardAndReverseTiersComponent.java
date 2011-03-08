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
package vizard.genomics.component;

import vizard.GraphicContext;
import vizard.component.GScrollBar;
import vizard.component.GSlider;
import vizard.genomics.glyph.*;
import vizard.genomics.interactor.RulerController;
import vizard.genomics.interactor.SliderController;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.model.WorldViewModel;
import vizard.util.Assert;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;


public class ForwardAndReverseTiersComponent extends JPanel
{
    public static final String AXIS_TIER_NAME = "Axis";

    //@todo preferences
    public static int SEPARATOR_SIZE = 3;

    private GenomicAxisViewModel axisModel;
    private TierTableComponent forward;
    private TierTableComponent axis;
    private TierTableComponent reverse;
    private AxisRulerGlyph ruler;
    private AxisRulerSliderGlyph slider;
    private JComponent upSeparator, downSeparator;
    private JComponent forwardPanel, reversePanel, axisPanel;
    private GScrollBar forwardScrollBar, reverseScrollBar;
    private VerticalZoomBarGlyph zoomBar;

    private JPanel zoomPanel;
    private JRadioButton xZoomerButton;
    private JRadioButton yZoomerButton;
    private JSlider horizontalZoomSlider;
    private JSlider verticalZoomSlider;

    public ForwardAndReverseTiersComponent(GenomicAxisViewModel axisModel) {
	super(new BorderLayout());
	this.axisModel = axisModel;

	createComponents();
	createAxisTier();
	setupInteractors();

	forward.tiersComponent().verticalModel().mode = WorldViewModel.GLUE_END;
    }

    public void reset() {
	forward.reset();
	reverse.reset();

	//from the axisColumn, we keep the axis-tier and delete the others:

	ArrayList forDeletion = new ArrayList();
	TiersColumnGlyph axisColumn = axisColumn();
	int numTiers = axisColumn.tierCount();
	for(int i = 0; i < numTiers; ++i) {
	    if (!axisColumn.tier(i).name().equals(AXIS_TIER_NAME))
		forDeletion.add(axisColumn.tier(i));
	}

	Iterator i = forDeletion.iterator();
	while(i.hasNext()) {
	    TierGlyph tier = (TierGlyph)i.next();
	    tier.delete();
	}
    }

    public void delete() {
	reset();

	forward.delete();
	reverse.delete();
	axis.delete();
    }

    public GenomicAxisViewModel axisModel() {
	return axisModel;
    }

    public TierTableComponent forwardTable() {
	return forward;
    }

    public TierTableComponent reverseTable() {
	return reverse;
    }

    public TierTableComponent axisTable() {
	return axis;
    }

    public AxisRulerGlyph axisRulerGlyph() {
	return ruler;
    }

    public TiersColumnGlyph forwardColumn() {
	return forward.tiersComponent().tiersColumn();
    }

    public TiersColumnGlyph reverseColumn() {
	return reverse.tiersComponent().tiersColumn();
    }

    public TiersColumnGlyph axisColumn() {
	return axis.tiersComponent().tiersColumn();
    }

    public GScrollBar forwardScrollBar() {
        return forwardScrollBar;
    }

    public GScrollBar reverseScrollBar() {
        return reverseScrollBar;
    }

    protected void setAxisRange(double begin, double magnitude) {
      axisModel.setWorld(begin, magnitude);
    }

    private int axisHeight() {
        WorldViewModel verticalAxisModel = axis.tiersComponent().verticalModel();
        int axisHeight = (int)verticalAxisModel.sizeToPixels(verticalAxisModel.worldSize());
        return axisHeight;
    }

    private void createComponents() {
	upSeparator = new JPanel();
	upSeparator.setBorder(new BevelBorder(BevelBorder.RAISED));
	upSeparator.setPreferredSize(new Dimension(1, SEPARATOR_SIZE));
        upSeparator.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        upSeparator.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    setAxisY(e.getComponent().getY() + e.getY() + SEPARATOR_SIZE);
                }});
        upSeparator.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    setAxisY(-1000);
                }});

	downSeparator = new JPanel();
	downSeparator.setBorder(new BevelBorder(BevelBorder.RAISED));
	downSeparator.setPreferredSize(new Dimension(1, SEPARATOR_SIZE));
        downSeparator.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        downSeparator.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    setAxisY(e.getComponent().getY() + e.getY() - axisHeight());
                }});
        downSeparator.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    setAxisY(10000);
                }});

        forwardPanel = new JPanel(new BorderLayout());
	forward = new TierTableComponent(axisModel) {
		public void repaint(long t, int x, int y, int w, int h) {
                    Component p = getParent();
                    if (p != null && p.getParent() != null)
                        p.getParent().repaint(t, x + getX() + p.getX(),
                                              y + getY() + p.getY(), w, h);
		}};
        forwardPanel.add("Center", forward);
        forwardScrollBar = new GScrollBar(forward.tiersComponent().verticalModel());
        forwardPanel.add("East", forwardScrollBar);

        reversePanel = new JPanel(new BorderLayout());
	reverse = new TierTableComponent(axisModel) {
		public void repaint(long t, int x, int y, int w, int h) {
                    Component p = getParent();
                    if (p != null && p.getParent() != null)
                        p.getParent().repaint(t, x + getX() + p.getX(),
                                              y + getY() + p.getY(), w, h);
		}};
        reversePanel.add("Center", reverse);
        reverseScrollBar = new GScrollBar(reverse.tiersComponent().verticalModel());
        reversePanel.add("East", reverseScrollBar);

        axisPanel = new JPanel(new BorderLayout());
	axis = new TierTableComponent(axisModel) {
		public void repaint(long t, int x, int y, int w, int h) {
                    Component p = getParent();
                    if (p != null && p.getParent() != null)
                        p.getParent().repaint(t, x + getX() + p.getX(),
                                              y + getY() + p.getY(), w, h);
		}};
        axis.tiersComponent().verticalModel().observers.addObserver
            (new WorldViewModel.Observer() {
                public void zoomCenterChanged(WorldViewModel model) {}
                public void modelChanged(WorldViewModel model) {
                    int pixels = (int)model.sizeToPixels(model.worldSize());
                    if (pixels != axisPanel.getHeight()) {
                        setAxisY(axisPanel.getY());
                    }
                }});
        axisPanel.add("Center", axis);

        JComponent mainPanel = new JPanel((LayoutManager)null);
        mainPanel.add(forwardPanel);
        mainPanel.add(upSeparator);
        mainPanel.add(axisPanel);
        mainPanel.add(downSeparator);
        mainPanel.add(reversePanel);

        mainPanel.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    setAxisY(axisPanel.getY());
                    setTierNamesWidth(forward.tierNamesComponent().getPreferredSize().width);
                }});

	add("Center", mainPanel);
	add("South", new AxisScrollBar(axisModel, AxisScrollBar.HORIZONTAL));

        createZoomPanel();
	add(zoomPanel, BorderLayout.WEST);
    }

    private void createZoomPanel() {
        xZoomerButton = new JRadioButton("X");
        xZoomerButton.setSelected(true);
        xZoomerButton.setHorizontalTextPosition(xZoomerButton.CENTER);
        xZoomerButton.setVerticalTextPosition(xZoomerButton.TOP);
        yZoomerButton = new JRadioButton("Y");
        yZoomerButton.setHorizontalTextPosition(yZoomerButton.CENTER);
        yZoomerButton.setVerticalTextPosition(yZoomerButton.BOTTOM);

        ButtonGroup xyGroup = new ButtonGroup();
        xyGroup.add(xZoomerButton);
        xyGroup.add(yZoomerButton);

        xZoomerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setupZoomAlongGenomicAxis(true);
                }});
        yZoomerButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setupZoomAlongGenomicAxis(false);
                }});

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(0, 1));
        radioPanel.add(xZoomerButton);
        radioPanel.add(yZoomerButton);

        zoomPanel = new JPanel(new BorderLayout());
        horizontalZoomSlider =new GSlider(axisModel);
        zoomPanel.add(horizontalZoomSlider, BorderLayout.CENTER);
        horizontalZoomSlider.setVisible(true);
        zoomPanel.add(radioPanel, BorderLayout.SOUTH);
        zoomPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));


        verticalZoomSlider = new JSlider(JSlider.VERTICAL, 0, 10, 4);
        verticalZoomSlider.setValue(10);
        verticalZoomSlider.setSnapToTicks(true);
        verticalZoomSlider.setPaintTicks(true);
        verticalZoomSlider.setMinorTickSpacing(1);
        verticalZoomSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    double scale;
                    switch(verticalZoomSlider.getValue()) {
                        case 10: scale = 1; break;
                        case 9: scale = 1.1; break;
                        case 8: scale = 1.3; break;
                        case 7: scale = 1.5; break;
                        case 6: scale = 1.8; break;
                        case 5: scale = 2.1; break;
                        case 4: scale = 2.5; break;
                        case 3: scale = 3; break;
                        case 2: scale = 3.6; break;
                        case 1: scale = 4.3; break;
                        default: scale = 5; break;
                    }
                    forward.tiersComponent().verticalModel().setScale(scale);
                    reverse.tiersComponent().verticalModel().setScale(scale);

                }});

              // zoomPanel.getParent().validate();
    }

    private void setupZoomAlongGenomicAxis(boolean b) {
        if (b) {
            if (horizontalZoomSlider.getParent() == null) {
                zoomPanel.remove(verticalZoomSlider);
                zoomPanel.add(horizontalZoomSlider, BorderLayout.CENTER);
                zoomPanel.getParent().validate();
            }
        }
        else {
            if (verticalZoomSlider.getParent() == null) {
                zoomPanel.remove(horizontalZoomSlider);
                zoomPanel.add(verticalZoomSlider, BorderLayout.CENTER);
                zoomPanel.getParent().validate();
            }
        }
    }

    private boolean firstTime = true;

    protected void setZoomLocation(double location) {
      slider.setLocation((int)location);
      axisModel().setZoomCenter(location);
    }

    protected void ensureComponentIsVisible(TiersComponent component) {
        final int MIN_COMPONENT_VISIBLE_SIZE = 16;
        final int RESTORE_VISIBILITY_SIZE = 64;

        Component container = axisPanel.getParent();

        if (component.getParent() == axis) {
            if (axisPanel.getY() < 0)
                setAxisY(0);
            else if (axisPanel.getY() + axisPanel.getHeight() > container.getHeight())
                setAxisY(container.getHeight() - axisPanel.getHeight());
        }
        else if (component.getParent() == forward) {
            if (axisPanel.getY() < MIN_COMPONENT_VISIBLE_SIZE)
                setAxisY(RESTORE_VISIBILITY_SIZE);
        }
        else {
            if (Assert.debug) Assert.vAssert(component.getParent() == reverse);
            if (container.getHeight() - axisPanel.getY() - axisPanel.getHeight() < MIN_COMPONENT_VISIBLE_SIZE)
                setAxisY(container.getHeight() - RESTORE_VISIBILITY_SIZE - axisPanel.getHeight());
        }
    }

    private void setAxisY(int y) {
        Component parent = axisPanel.getParent();
        if (firstTime) {
            if (parent.getHeight() < 10)
                return;
            firstTime = false;
            y = parent.getHeight() * 2 / 3;

            setTierNamesWidth(TierNamesComponent.WIDTH);
        }

        TierGlyph tier = ruler.tierAncestor();
        if (tier == null) //can happen when the component is being deleted
            return;
        TiersColumnGlyph column = tier.tierColumn();
        if (column == null) //can happen when the component is being deleted
            return;

        int width = parent.getWidth();

        y = Math.max(y, (int)-column.yForTier(tier));
        y = Math.min(y, (int)(parent.getHeight() - column.yForTier(tier) - tier.height()));

        int currentY = Math.min(0, y - SEPARATOR_SIZE);
        int currentHeight = Math.max(0, y - SEPARATOR_SIZE);
        boolean forwardHeightChanged = currentHeight != forwardPanel.getHeight();
        forwardPanel.setBounds(0, currentY, width, currentHeight);

        currentY += currentHeight;
        currentHeight = SEPARATOR_SIZE;
        upSeparator.setBounds(0, currentY, width, currentHeight);

        currentY += currentHeight;
        currentHeight = axisHeight();
        boolean axisHeightChanged = currentHeight != axisPanel.getHeight();
        axisPanel.setBounds(0, currentY, width, currentHeight);

        currentY += currentHeight;
        currentHeight = SEPARATOR_SIZE;
        downSeparator.setBounds(0, currentY, width, currentHeight);

        currentY += currentHeight;
        currentHeight = Math.max(0, parent.getHeight() - currentY);
        boolean reverseHeightChanged = currentHeight != reversePanel.getHeight();
        reversePanel.setBounds(0, currentY, width, currentHeight);

        if (forwardHeightChanged) {
            forward.tiersComponent().verticalModel().checkAndNotify();
        }
        if (axisHeightChanged)
            axis.tiersComponent().verticalModel().checkAndNotify();
        if (reverseHeightChanged)
            reverse.tiersComponent().verticalModel().checkAndNotify();

        parent.validate();
    }

    public void setTierNamesWidth(int width) {
        if (width < 20)
            width = 20;
        forward.setTierNamesWidth(width);
        reverse.setTierNamesWidth(width);

        int extra = forwardScrollBar.getWidth();
        axis.setTierNamesWidth(width + extra);
    }

    private void createAxisTier() {
	TierGlyph axisTier = new TierGlyph(AXIS_TIER_NAME, axisModel, TierGlyph.FIXED) {
                public void paint(GraphicContext gc) {
                    super.paint(gc); //for debug
                }};
	ruler = new AxisRulerGlyph(axisModel);
	axisTier.addGenomicChild(ruler);
	slider = new AxisRulerSliderGlyph(ruler);
	axisTier.addChild(slider);
	axis.tiersComponent().tiersColumn().addTier(axisTier, 0);

	zoomBar = new VerticalZoomBarGlyph(axisModel);
	zoomBar.showUnder(forward.tiersComponent().viewTransform());
	zoomBar.showUnder(reverse.tiersComponent().viewTransform());
	zoomBar.showUnder(axis.tiersComponent().viewTransform());
    }

    private void setupInteractors() {
	new RulerController(ruler, axisModel);
	new SliderController(slider, axisModel, zoomBar);

	/*
	ParentGlyph rulerMultiplexer = axis.tiersComponent().multiplexer();
	forward.tiersComponent().rootGlyph().setController
	    (new MagnifyingGlassController(rulerMultiplexer));
	reverse.tiersComponent().rootGlyph().setController
	    (new MagnifyingGlassController(rulerMultiplexer));
	*/
    }
}

