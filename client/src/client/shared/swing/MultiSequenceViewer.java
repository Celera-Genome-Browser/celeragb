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
package client.shared.swing;

import api.stub.sequence.Sequence;
import client.shared.swing.genomic.Adornment;
import client.shared.swing.genomic.DefaultLocationRenderer;
import client.shared.swing.genomic.DefaultSeqTableModel;
import client.shared.swing.genomic.SeqTableModel;
import client.shared.swing.genomic.SequenceAdjustmentEvent;
import client.shared.swing.genomic.SequenceAdjustmentListener;
import client.shared.swing.genomic.SequenceKeyEvent;
import client.shared.swing.genomic.SequenceKeyListener;
import client.shared.swing.genomic.SequenceMouseEvent;
import client.shared.swing.genomic.SequenceMouseListener;
import client.shared.swing.genomic.SequenceSelectionEvent;
import client.shared.swing.genomic.SequenceSelectionListener;
import client.shared.swing.genomic.SwingRange;
import client.shared.swing.genomic.ViewerSequence;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * <code>MultiSequenceVeiwer</code> is a user-interface component that presents
 * genomic sequence(s) in a two-dimensional display.
 *
 */
public class MultiSequenceViewer extends JComponent {
    protected SeqTable seqTable = new SeqTable();
    protected JScrollPane scrollPane = new JScrollPane(seqTable);
    protected SeqTableModel seqModel;
    protected DefaultLocationRenderer locationRenderer = new DefaultLocationRenderer();
    protected int selectedRow = -1;
    protected int selectedColumn = -1;
    private transient Vector sequenceMouseListeners;
    private transient Vector sequenceKeyListeners;
    private transient Vector sequenceAdjustmentListeners;
    private transient Vector sequenceSelectionListeners;
    private boolean editable = true;
    private boolean isSelectable = true;

    /**
     * Constructs a <code>MultiSequenceViewer</code> that is initialized with
     * <code>DefaultSeqTableModel</code> as the data model
     */
    public MultiSequenceViewer() {
        this(new DefaultSeqTableModel());
    }

    /**
     * Constructs a <code>MultiSequenceViewer</code> that is initialized with
     * <code>SeqTableModel</code> as the data model
     */
    public MultiSequenceViewer(SeqTableModel model) {
        try {
            seqModel = model;
            initTable();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     * Sets the specified boolean to indicate whether or not this
     * Component should be editable.
     */
    public void setEditable (boolean editable) {
        this.editable = editable;
    }

    /**
     * Returns the boolean indicating whether this Component is
     * editable or not.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Adds an <code>Adornment</code> to this viewer
     */
    public void addAdornment(Adornment adornment) {
        locationRenderer.addAdornment(adornment);

        seqTable.repaint();
    }

    /**
     * Override requestFocus() in order to continue editing upon show/display.
     */
    public void requestFocus() {
      seqTable.requestFocus();
    }

    /**
     * Removes an <code>Adornment</code> from this viewer
     */
    public void removeAdornment(Adornment adornment) {
        locationRenderer.removeAdornment(adornment);

        seqTable.repaint();
    }

    /**
     * Removes all <code>Adornment</code> from this viewer
     */
    public void clearAllAdornments() {
        locationRenderer.clearAll();
        seqTable.repaint();
    }

    /**
     * Scrolls the view to the specified location.
     *
     * @param location the axis location to scroll to (set visible)
     */
    public void scrollToLocation(long location) {
        seqTable.centerVertically((int)seqModel.locationToRow(location), false);
    }

    /**
     * Returns the scrollbar associated with the pane
     */
     public JScrollPane getScrollPane() {
        return scrollPane;
     }

     /**
     * Returns the foreground color for selected sequence.
     *
     * @return the <code>Color</code> object for the foreground property
     * @see #setSelectionForeground
     * @see #setSelectionBackground
     */
    public Color getSelectionForeground() {
        return seqTable.getSelectionForeground();
    }

    /**
     * Sets the foreground color for selected intervals.
     *
     * @param selectionForeground  the <code>Color</code> to use in the foreground
     *                             for selected intervals
     */
    public void setSelectionForeground(Color selectionForeground) {
        seqTable.setSelectionForeground(selectionForeground);
    }

    /**
     * Returns the background color for selected interval
     *
     * @return the <code>Color</code> used for the background of selected interval
     */
    public Color getSelectionBackground() {
        return seqTable.getSelectionBackground();
    }

    /**
     * Sets the background color for selected interval.
     *
     * @param selectionBackground  the <code>Color</code> to use for the background
     *                             of selected interval
     */
    public void setSelectionBackground(Color selectionBackground) {
        seqTable.setSelectionBackground(selectionBackground);
    }

    /**
     * Change the selection to be between beginLocation and endLocation inclusive.
     * Note that beginLocation doesn't have to be less than or equal to endLocation.
     * The viewer will scroll to the location
     *
     * @param beginLocation one end of the interval.
     * @param endLocation other end of the interval
     */
    public void setSelectionInterval(long beginLocation, long endLocation) {
      setSelectionInterval(beginLocation,endLocation,true);
    }

    protected void setSelectionInterval(long beginLocation, long endLocation, boolean isSelected){
        seqTable.setSelectedRange(new SwingRange(beginLocation, endLocation));
        long row1=seqModel.locationToRow(beginLocation);
        long col1=seqModel.locationToColumn(beginLocation);
        long row2=seqModel.locationToRow(endLocation);
        long col2=seqModel.locationToColumn(endLocation);
        if(isSelected){
          seqTable.getColumnModel().getSelectionModel().setSelectionInterval((int)col1, (int)col2);
          seqTable.getSelectionModel().setSelectionInterval((int)row1, (int)row2);
        }
        else{
          seqTable.getColumnModel().getSelectionModel().clearSelection();
          seqTable.getSelectionModel().clearSelection();
        }
        SequenceSelectionEvent selEvent =
                    new SequenceSelectionEvent(this,
                          seqTable.selectedRange.getStartRange(),
                          seqTable.selectedRange.getEndRange(),isSelected);
        fireSequenceSelection(selEvent);
    }

    /**
     * Returns the start location for a current selection.
     *
     * Note that the lead and anchors of selection constitute where the user
     * actually ends a drag and starts a drag, respectively.  Comparisons
     * need be made in order to avoid a wrong assumption that the min col
     * is for the start and a max col is for the end, since users may start
     * a drag further to the right than they end it, if they cross lines.
     *
     * @returns long the location of the begin/start axis location of the current
     * selection.  Returns Long.MIN_VALUE if no selection
     * @see Long
     */
    public long getSelectionBegin() {
        long returnVal = Math.min(getSelectionLead(), getSelectionAnchor());
        if (returnVal < 0)
            return Long.MAX_VALUE;
        else
            return returnVal;
    }

    /**
     * Returns the location of the cursor
     */
    public long getCursorLocation(){
      return seqTable.getSelectedRange().getEndRange();
    }

    /**
     * Returns the end location for a current selection.
     *
     * Note that the lead and anchors of selection constitute where the user
     * actually ends a drag and starts a drag, respectively.  Comparisons
     * need be made in order to avoid a wrong assumption that the min col
     * is for the start and a max col is for the end, since users may start
     * a drag further to the right than they end it, if they cross lines.
     *
     * @returns long the location of the end axis location of the current
     * selection.  Returns Long.MAX_VALUE if no selection
     * @see Long
     */
    public long getSelectionEnd() {
        long returnVal = Math.max(getSelectionAnchor(), getSelectionLead());
        if (returnVal < 0)
            return Long.MAX_VALUE;
        else
            return returnVal;
    }

    private long getSelectionLead() {
        int leadRow = seqTable.getSelectionModel().getLeadSelectionIndex();
        int leadColumn = seqTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        if(leadRow < 0 || leadColumn < 0)
          return Long.MAX_VALUE;
        else
          return seqModel.cellToLocation(leadRow, leadColumn);
    }

    private long getSelectionAnchor() {
        int anchorRow = seqTable.getSelectionModel().getAnchorSelectionIndex();
        int anchorColumn = seqTable.getColumnModel().getSelectionModel().getAnchorSelectionIndex();
        if(anchorRow < 0 || anchorColumn < 0)
          return Long.MIN_VALUE;
        else
          return seqModel.cellToLocation(anchorRow, anchorColumn);
    }

    /**
     * Clears the current selection.
     */
    public void clearSelection() {
        seqTable.getSelectionModel().clearSelection();
    }

    /**
     * Sets the specified boolean to determine whether this
     * <code>MultiSequenceViewer</code> is selectable.
     * @see #isSelectable
     */
    public void setSelectable(boolean value) {
        this.isSelectable = value;
        seqTable.setCellSelectionEnabled(value);
    }

    /**
     * Returns whether or not this <code>MultiSequenceViewer</code>
     * is selectable
     * @see #setSelectable
     */
    public boolean isSelectable() {
        return this.isSelectable;
    }


    /**
     * Adds a <code>Sequence</code> to this viewer at the specified axis
     * start location. The sequence label defaults to the range count.
     */
    public void addSequence(Sequence sequence, long startLocation) {
        addSequence(null, sequence, startLocation);
    }

    /**
     * Adds a labled <code>Sequence</code> to this viewer at the specified axis
     * start location.
     */
    public void addSequence(String label, Sequence sequence, long startLocation) {
        SwingRange range = new SwingRange(startLocation, (startLocation + sequence.length() - 1));

        addSequence(seqModel.getSequenceCount(), label, sequence, range);
    }

    /**
     * Adds a sequence to this <code>MultiSequenceViewer</code>.
     * @param index The index, in the list, at which the sequence is to be inserted.
     * @param label The label for the sequence
     * @param sequence The <code>Sequence</code> to be inserted
     * @param range The range over which the sequence is applicable.
     * @throw java.lang.IndexOutOfBoundsException If the <code>index</code> is
     *        < 0, or > current number of sequences.
     */
    protected void addSequence(int index, String label, Sequence sequence, SwingRange range) {
        long previousLocation = seqModel.cellToLocation(getTopVisibleRow(),0);
        seqModel.addSequence(index, label, new ViewerSequence(sequence), range);
        fireSequenceViewerChanged(previousLocation);
    }

    /**
     * Removes a sequence from this <code>MultiSequenceViewer</code>.
     * @param index the index of the sequence to be removed
     * @throws java.lang.IndexOutOfBoundsException If the <code>index</code> is <
     *          0, or >= current number of sequences.
     * @see #addSequence
     */
    public void removeSequenceAt(int index) {
        long previousLocation = seqModel.cellToLocation(getTopVisibleRow(),0);
        seqModel.removeSequence(index);
        fireSequenceViewerChanged(previousLocation);
    }

    /**
     * Clears all Sequences, Adornments, and selections from this
     * <code>MultiSeqenceViewer</code>
     */
    public void clearAll() {
        seqModel.removeAll();
        clearSelection();
        clearAllAdornments();
    }

    /**
     * Returns a specified sequence from this <code>MultiSequenceViewer</code>.
     * @param index the index of the sequence to be returned
     * @throws java.lang.IndexOutOfBoundsException If the <code>index</code> is <
     *          0, or >= current number of sequences.
     */
    public Sequence getSequenceAt(int index) {
        return seqModel.getSequenceAt(index);
    }

    /**
     * Sets the font used by this <code>MultiSequenceViewer</code>
     * @see #getFont
     */
    public void setFont(Font font) {
        locationRenderer.setRendererFont(font);

        this.fireSequenceViewerChanged();
    }

    /**
     * Gets the font used by this <code>MultiSequenceViewer</code>
     * @see #setFont
     */
    public Font getFont() {
        return locationRenderer.getRendererFont();
    }

    /**
     * Initializes this <code>MultiSequenceViewer</code>.
     */
    private void initTable() {
        setLayout(new BorderLayout());
        seqTable.setModel(seqModel);

        // remove the grid
        seqTable.setShowGrid(false);
        seqTable.getColumnModel().setColumnMargin(0);
        seqTable.setRowMargin(0);
        seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        seqTable.getTableHeader().setResizingAllowed(false);
        seqTable.getTableHeader().setReorderingAllowed(false);
        seqTable.sizeColumnsToFit(1);
        seqTable.setAutoCreateColumnsFromModel(true);
        seqTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        seqTable.setRowSelectionAllowed(false);
        seqTable.setColumnSelectionAllowed(false);
        seqTable.setCellSelectionEnabled(false);
        seqTable.setIntercellSpacing(new Dimension(0, 0));

        // Initialize the default colors
        seqTable.setBackground(super.getBackground());
        seqTable.setForeground(super.getForeground());
        setBackground(Color.black);
        setForeground(Color.yellow);
        seqTable.setSelectionBackground(Color.red);
        seqTable.setSelectionForeground(Color.yellow);

        UIManager.put("Table.focusCellHighlightBorder", "none");

        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                fireSequenceViewerChanged();
            }
        });

        // Get rid of all the built in keybindings.
        seqTable.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,null);
        seqTable.setInputMap(JComponent.WHEN_FOCUSED,null);
        seqTable.setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW,null);

        seqTable.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if( e.getModifiers() == InputEvent.BUTTON1_MASK )//handle select + redraw
                {/*left button*/
                    Point point = e.getPoint();
                    int curRow = seqTable.rowAtPoint(point);
                    int curCol = seqTable.columnAtPoint(point);
                    long location = seqModel.cellToLocation(curRow, curCol);
                    if(location == Long.MIN_VALUE)//dragged out of bounds
                      location = seqModel.getMaxAxisLocation();
                    if (location > 0 && location <= seqModel.getMaxAxisLocation() ) {
                        setSelectionInterval(seqTable.selectedRange.getStartRange(),location,true);
                    }

                    // Repaint on the "dirty" mouse-d region
                    Rectangle dirtyRegion = seqTable.getCellRect(curRow, curCol, true);
                    dirtyRegion.grow(60, 60);
                    seqTable.repaint(dirtyRegion);
                }
                if (isSelectable) {
                     SequenceMouseEvent event = new SequenceMouseEvent((Component)e.getSource(), e.getID(),
                                                      e.getWhen(), e.getModifiers(), e.getX(), e.getY(),
                                                      e.getClickCount(), e.isPopupTrigger(), getSelectionAnchor(),
                                                      getSelectionLead(), seqModel.getMinAxisLocation());

                     MultiSequenceViewer.this.fireMouseSelectedRange(event);
                }
            }

            public void mouseMoved(MouseEvent e) {
                long location = seqModel.cellToLocation(seqTable.rowAtPoint(e.getPoint()), seqTable.columnAtPoint(e.getPoint()));
                SequenceMouseEvent event = new SequenceMouseEvent((Component)e.getSource(), e.getID(),
                                                  e.getWhen(), e.getModifiers(), e.getX(), e.getY(),
                                                  e.getClickCount(), e.isPopupTrigger(), location,
                                                  location, seqModel.getMinAxisLocation());
                MultiSequenceViewer.this.fireMouseMoved(event);
            }
        });

        seqTable.addMouseListener(new MouseAdapter() {
            public void mouseReleased (MouseEvent e) {
               SequenceMouseEvent event = new SequenceMouseEvent((Component)e.getSource(), e.getID(),
                                                  e.getWhen(), e.getModifiers(), e.getX(), e.getY(),
                                                  e.getClickCount(), e.isPopupTrigger(), getSelectionAnchor(),
                                                  getSelectionLead(), seqModel.getMinAxisLocation());
               MultiSequenceViewer.this.fireMouseReleased(event);
            }

            public void mousePressed (MouseEvent e) {
               if (e.getModifiers() == e.BUTTON1_MASK) {//handle selection + redrawing
                    Point point = e.getPoint();
                    int curRow = seqTable.rowAtPoint(point);
                    int curCol = seqTable.columnAtPoint(point);

                    long location = seqModel.cellToLocation(curRow, curCol);
                    if(location == Long.MIN_VALUE)//dragged out of bounds
                      location = seqModel.getMaxAxisLocation();
                    setSelectionInterval(location,location,false);
                    // Repaint on the "dirty" mouse-d region
                    Rectangle dirtyRegion = seqTable.getCellRect(curRow, curCol, true);
                    dirtyRegion.grow(60, 60);
                    seqTable.repaint(dirtyRegion);
               }

               long location = seqModel.cellToLocation(seqTable.rowAtPoint(e.getPoint()), seqTable.columnAtPoint(e.getPoint()));
               SequenceMouseEvent event = new SequenceMouseEvent((Component)e.getSource(), e.getID(),
                                                  e.getWhen(), e.getModifiers(), e.getX(), e.getY(),
                                                  e.getClickCount(), e.isPopupTrigger(), location,
                                                  location, seqModel.getMinAxisLocation());
               MultiSequenceViewer.this.fireMousePressed(event);
            }

            /**
             * Adding this in as a feed through to fix a problem.  Can change if someone needs
             * specific granularity.
             */
            public void mouseClicked (MouseEvent e) {
                this.mousePressed(e);
            }
        });

        seqTable.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e){
                boolean isPaged = false;
                long col = seqModel.locationToColumn(seqTable.selectedRange.getEndRange());
                long row = seqModel.locationToRow(seqTable.selectedRange.getEndRange());
                int scrollDirection = 0;
                switch( e.getKeyCode() ) {
                    case KeyEvent.VK_UP:
                        if (row > 0) {
                            row--;
                        }
                        scrollDirection = -1;
                        break;
                    case KeyEvent.VK_DOWN:
                        if (row < seqModel.getRowCount() - 1* seqModel.getSequenceCount()) {
                            row = row + seqModel.getSequenceCount();
                        }
                        scrollDirection = 1;
                        break;
                    case KeyEvent.VK_LEFT:
                        if (col > 1) {
                            col--;
                        } else {
                            row--; col = seqModel.getColumnCount() - 1;
                            if(row < 0){
                              row = 0;
                              col = 1;
                            }
                        }
                        scrollDirection = -1;
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (col < seqModel.getColumnCount()) {
                            col++;
                        } else {
                            row++; col = 1;
                        }
                        scrollDirection = 1;
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        isPaged = true;
                        row -= seqTable.getScrollableBlockIncrement(
                                seqTable.getVisibleRect(),
                                SwingConstants.VERTICAL,1) / seqTable.getRowHeight() - 1;
                        row = row < 0 ? 0 : row;
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        isPaged = true;
                        row += seqTable.getScrollableBlockIncrement(
                                seqTable.getVisibleRect(),
                                SwingConstants.VERTICAL,1) / seqTable.getRowHeight() + 1;
                        row = row >= seqModel.getRowCount() ? seqModel.getRowCount()-1 : row;
                        break;
                    default:
		    /*
		     long location = getCursorLocation();
                    if (editable && location != Long.MAX_VALUE) {
                    char character = e.getKeyChar();
                    SequenceKeyEvent event = new SequenceKeyEvent((Component)e.getSource(), e.getID(),
                                                  e.getWhen(), e.getModifiers(), e.getKeyCode(), character,
                                                  location,
                                                  seqModel.getMinAxisLocation());
                    MultiSequenceViewer.this.fireKeyPressed(event);

            }
	    */
                        return;
                }


                e.consume();
                long location = seqModel.cellToLocation(row, col);
                if(location == Long.MIN_VALUE){
                  location = seqModel.getMaxAxisLocation();
                }
                else if(location == Long.MAX_VALUE)
                  location = seqModel.getMinAxisLocation();
                if(e.isShiftDown()){
                    // Repaint on the "dirty" key-d region
                  Rectangle dirtyRegion = seqTable.getCellRect((int)row, (int)col, true);
                  dirtyRegion.grow(60, 60);
                  seqTable.repaint(dirtyRegion);
                  setSelectionInterval(seqTable.selectedRange.getStartRange(),location,true);
                }else{
                  setSelectionInterval(location,location,false);
                }
                if(isPaged)
                  scrollToLocation(location);
                else{
                  row = seqModel.locationToRow(location);
                  col = seqModel.locationToColumn(location);
                  row = (scrollDirection > 0) ? row + seqModel.getSequenceCount() - 1 : row;
                  if( ! seqTable.isCellVisible((int)row,(int)col)){
                    scrollOneUnit(scrollDirection);
                  }
                }
            }

            public void keyTyped(KeyEvent e) {
                // fire events only if the viewer is editable
                long location = getCursorLocation();
                //long location = getSelectionEnd();
                if (editable && location != Long.MAX_VALUE) {
                    char character = e.getKeyChar();
                    SequenceKeyEvent event = new SequenceKeyEvent((Component)e.getSource(), e.getID(),
                                                  e.getWhen(), e.getModifiers(), e.getKeyCode(), character,
                                                  location,
                                                  seqModel.getMinAxisLocation());
                    MultiSequenceViewer.this.fireKeyTyped(event);
                }
            }

/*
	     public void keyReleased(KeyEvent e) {
                // fire events only if the viewer is editable
                long location = getCursorLocation();
                if (editable && location != Long.MAX_VALUE) {
                    char character = e.getKeyChar();
                    SequenceKeyEvent event = new SequenceKeyEvent((Component)e.getSource(), e.getID(),
                                                  e.getWhen(), e.getModifiers(), e.getKeyCode(), character,
                                                  location,
                                                  seqModel.getMinAxisLocation());
                    MultiSequenceViewer.this.fireKeyReleased(event);
                }
            }

	    */
        });

        this.scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
            public void adjustmentValueChanged(AdjustmentEvent e) {
                SequenceAdjustmentEvent event = new SequenceAdjustmentEvent((Adjustable)e.getSource(),
                e.getID(), e.getAdjustmentType(), e.getValue(),
                getVisibleBeginLocation(), getVisibleEndLocation());

                MultiSequenceViewer.this.fireAdjustment(event);
                repaint();
            }
        });

        // want the first column to have the same appearance as your column
        // headers, than just set that column's renderer to a header renderer.

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);

        seqTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
        seqTable.setTableHeader(null);

        setBandingInterval(10);
        calcColumnWidths();
        ToolTipManager.sharedInstance().unregisterComponent(seqTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    protected void scrollOneUnit(int direction){
      JViewport vp = scrollPane.getViewport();
      Point pt = vp.getViewPosition();
      int incr = seqTable.getRowHeight() * direction;
      incr *= seqModel.getSequenceCount();
      pt.y = pt.y + incr;
      pt.y = Math.max(0,pt.y);
      pt.y = Math.min(vp.getView().getHeight() - vp.getHeight(),pt.y);
      vp.setViewPosition(pt);
    }

    /**
     * Sets the background color of this <code>MultiSequenceViewer</code>.
     * @see #getBackground
     */
    public void setBackground(Color color) {
        locationRenderer.setBackgroundColor(color);
    }

    /**
     * Gets the background color of this <code>MultiSequenceViewer</code>.
     * @see #setBackground
     */
    public Color getBackground() {
        return locationRenderer.getBackgroundColor();
    }

    /**
     * Sets the foreground color of this <code>MultiSequenceViewer</code>.
     * @see #getForeground
     */
    public void setForeground(Color color) {
        locationRenderer.setForegroundColor(color);
    }

    /**
     * Gets the foreground color of this <code>MultiSequenceViewer</code>.
     * @see #setForeground
     */
    public Color getForeground() {
        return locationRenderer.getForegroundColor();
    }

    /**
     * Sets the Band background color of this <code>MultiSequenceViewer</code>.
     * @see #getBandBackground
     */
    public void setBandBackground(Color color) {
        locationRenderer.setBandBackground(color);
    }

    /**
     * Gets the Band background color of this <code>MultiSequenceViewer</code>.
     * @see #setBandBackground
     */
    public Color getBandBackground() {
        return locationRenderer.getBandBackground();
    }

    /**
     * Sets the interval over which the bands are applied.
     * @param interval The number of columns the band spans
     */
    public void setBandingInterval(int interval) {
        locationRenderer.setBandInterval(interval);

        for (int i = 1; i < seqTable.getColumnCount(); i++) {
            TableColumn tm = seqTable.getColumnModel().getColumn(i);
            tm.setCellRenderer(locationRenderer);
        }
        seqModel.addTableModelListener(locationRenderer);
    }

    /**
     * Returns the a <code>long</code> representing the first visible location
     */
    public long getVisibleBeginLocation() {
        return seqModel.cellToLocation(getTopVisibleRow(), 1);
    }

    /**
     * Returns the a <code>long</code> representing the last visible location
     */
    public long getVisibleEndLocation() {
        long location = seqModel.cellToLocation(getBottomVisibleRow(), getLastVisibleColumn());
        return location == Long.MIN_VALUE ? getWorldEndLocation() : location;
    }

    /**
     * Returns the a <code>long</code> representing the first location on the axis
     * displayed by this <code>MultiSequenceViewer</code>
     */
    public long getWorldBeginLocation() {
        return seqModel.getMinAxisLocation();
    }

    /**
     * Returns the a <code>long</code> representing the last location on the axis
     * displayed by this <code>MultiSequenceViewer</code>
     */
    public long getWorldEndLocation() {
        return seqModel.getMaxAxisLocation();
    }

   protected void sequencePaint(Graphics g) {
   }

   /**
    * Returns the first visible row
    */
   protected int getTopVisibleRow() {
      return seqTable.rowAtPoint(scrollPane.getViewport().getViewPosition());
   }

   /**
    * Returns the last visible row
    */
   protected int getBottomVisibleRow() {
      int y  = scrollPane.getViewport().getViewPosition().y;
      y += scrollPane.getViewport().getExtentSize().getHeight();
      int row = seqTable.rowAtPoint(new Point(0, y));

      return (row > -1) ? row : (seqTable.getRowCount() - 1);
   }

   /**
    * Returns the last visible column.  This is only needed if the horizontal
    * scrollbar is enabled; otherwise this
    */
   protected int getLastVisibleColumn() {
        // getting the table width minus the cell insets
        Point point = new Point(seqTable.getWidth() - 5, 0);
        return seqTable.columnAtPoint(point);
   }

   /**
    * Returns the number of columns which can be displayed in the viewport
    */
   protected int calcNumColumns () {
        try {
            int width = (int)scrollPane.getViewport().getExtentSize().getWidth();
            int columnWidth = calcDataColumnWidth();
            int labelWidth = calcLabelColumnWidth();
            int numColumns = (width - labelWidth)/columnWidth + 1;
            return numColumns > 1 ? numColumns : 2;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
   }

   /**
    * Returns the width in pixels of a label column
    */
   private int calcLabelColumnWidth(){
    int width = 0;
    int margin = seqTable.getColumnModel().getColumnMargin();
    TableColumn column = seqTable.getColumnModel().getColumn(0);
    TableModel dataModel = seqTable.getModel();
    TableCellRenderer r = column.getCellRenderer();
    if(r == null)
      r = seqTable.getDefaultRenderer(seqTable.getColumnClass(0));
    try{
      String maxString = ((SeqTableModel)dataModel).getMaxLabel();
      Component c = r.getTableCellRendererComponent(seqTable,
                                             dataModel.getValueAt(0,0),
                                             false, false, 0, 0);
      Font f = c.getFont();
      FontMetrics fm = c.getFontMetrics(f);
      width = fm.stringWidth(maxString) + fm.getMaxAdvance() + margin;
    }
    catch(Exception e){
      width = column.getWidth();
      e.printStackTrace();
    }
    return width;

   }

   /**
    * Returns the width in pixels of a data column
    */
   private int calcDataColumnWidth(){
    int width = 0;
    int margin = seqTable.getColumnModel().getColumnMargin();
    TableColumn column = seqTable.getColumnModel().getColumn(1);
    TableModel dataModel = seqTable.getModel();
    TableCellRenderer r = column.getCellRenderer();
    if(r == null)
      r = seqTable.getDefaultRenderer(seqTable.getColumnClass(1));
    try{
      Component c = r.getTableCellRendererComponent(seqTable,
                                             dataModel.getValueAt(0, 0),
                                             false, false, 0, 0);
      Font font = c.getFont();
      FontMetrics fm = c.getFontMetrics(font);
      /**
       * I'm looping through all of the characters that
       * we currently display because Fontmetrics.getMaxAdvance
       * usually creates too much spacing between the characters.
       * The widest character is probably something we never display.
       */
      char biggestChar = 'A';
      for(char upC='A'; upC <= 'Z'; upC++){//look at uppercase chars
        if(fm.charWidth(upC) > width){
          biggestChar = upC;
          width = fm.charWidth(upC);
        }
      }
      for(char loC='a'; loC <= 'z'; loC++){//look at lowercase chars
        if(fm.charWidth(loC) > width){
          biggestChar = loC;
          width = fm.charWidth(loC);
        }
      }
      char[] others = new char[] { '-', '*' }; //other possible chars
      for(int i=0; i < others.length; i ++){
        if(fm.charWidth(others[i]) > width){
          biggestChar = others[i];
          width = fm.charWidth(others[i]);
        }
      }
      width = width + margin;
      c = r.getTableCellRendererComponent(seqTable,String.valueOf(biggestChar),false,false,0,0);
      width = c.getPreferredSize().width + margin;
      //width = fm.getMaxAdvance() + margin;
    }
    catch(Exception e){
      width = column.getWidth();
      e.printStackTrace();
    }
    return width;
   }


   /**
    * Returns the number of rows.  If the number of rows do not fix within the viewport,
    * the actual number of rows are return; otherwise a "padded" number is return to
    * fill the exact size of the viewport.
    */
   protected int calcNumRows () {
        try {
            // the sequence might not occupy the entire table.  But we want the
            // entire visible ranged cell filled with the band
            int height = (int)scrollPane.getViewport().getSize().getHeight();
            int visibleRows = (int)Math.ceil(height/seqTable.getRowHeight());
            int numRows = (int)Math.ceil(seqModel.getSequenceSize()/
                                    (seqModel.getColumnCount()-1.0))*seqModel.getSequenceCount() + 1;

            return (int)Math.max(visibleRows, numRows);
        } catch (Exception ex) {
            return 0;
        }
   }

   /**
    * Calulates and set's the row heights for the <code>SequenceTable</code>
    */
   private void calcRowHeights() {
    int rowHeight = 0;
    TableModel data = seqTable.getModel();

    for(int i=0; i < 2; i++){
      TableCellRenderer r = seqTable.getCellRenderer(0,i);
      Component c = r.getTableCellRendererComponent(seqTable, data.getValueAt(0, i),
                                                    false, false, 0, i);
      //fm.getMaxAscent()
      rowHeight = Math.max(rowHeight,c.getPreferredSize().height );
    }
    seqTable.setRowHeight(rowHeight);
   }

   /**
    * Calculates and sets the widths for the columns in a
    * <code>SequenceTable</code>
    */
   private void calcColumnWidths(){
    int labelWidth = calcLabelColumnWidth();
    int dataWidth = calcDataColumnWidth();
    TableColumnModel columnModel = seqTable.getColumnModel();
    TableColumn column = columnModel.getColumn(0);
    column.setMinWidth(labelWidth);
    column.setPreferredWidth(labelWidth);
    for(int i = 1; i < columnModel.getColumnCount(); i++){
      column = columnModel.getColumn(i);
      column.setMinWidth(dataWidth);
      column.setPreferredWidth(dataWidth);
    }
    seqTable.sizeColumnsToFit(-1);
   }

    /**
     * Handles the necessary updates to this <code>MultiSequenceViewer</code>, when
     * the the sequences have changed.
     */
    protected void fireSequenceViewerChanged() {
      fireSequenceViewerChanged(seqModel.cellToLocation(getTopVisibleRow(),0));
    }

    /**
     * Handles the necessary updates to this <code>MultiSequenceViewer</code>, when
     * the the sequences have changed.
     * @param previousLocation The top visible location before the change.
     */
    protected void fireSequenceViewerChanged(long previousLocation) {
        calcRowHeights();
        seqModel.setColumnCount(calcNumColumns());
        seqModel.setRowCount(calcNumRows());
        setBandingInterval(locationRenderer.getBandInterval());
        calcColumnWidths();
        scrollToLocation(previousLocation);
    }

    /**
     * Removes a <code>SequenceMouseListener</code> from this <code>MultiSequenceViewer</code>.
     * @param l the listener to be removed
     * @see #addSequenceMouseListener
     */
    public synchronized void removeSequenceMouseListener(SequenceMouseListener l) {
        if (sequenceMouseListeners != null && sequenceMouseListeners.contains(l)) {
            Vector v = (Vector) sequenceMouseListeners.clone();
            v.removeElement(l);
            sequenceMouseListeners = v;
        }
    }

    /**
     * Adds a <code>SequenceMouseListener</code> that will be notified of
     *  sequence mouse events.
     * @param l the listener to be notified
     * @see #removeSequenceMouseListener
     */
    public synchronized void addSequenceMouseListener(SequenceMouseListener l) {
        Vector v = sequenceMouseListeners == null ? new Vector(2) : (Vector) sequenceMouseListeners.clone();
        if (!v.contains(l)) {
            v.addElement(l);
            sequenceMouseListeners = v;
        }
    }

    /**
     * Removes a <code>SequenceKeyListener</code> from this <code>MultiSequenceViewer</code>.
     * @param l the listener to be removed
     * @see #addSequenceKeyListener
     */
    public synchronized void removeSequenceKeyListener(SequenceKeyListener l) {
        if (sequenceKeyListeners!= null && sequenceKeyListeners.contains(l)) {
            Vector v = (Vector) sequenceKeyListeners.clone();
            v.removeElement(l);
            sequenceKeyListeners = v;
        }
    }

    /**
     * Adds a <code>SequenceKeyListener</code> that will be notified of sequence key
     * events.
     * @param l the listener to be notified
     * @see #removeSequenceKeyListener
     */
    public synchronized void addSequenceKeyListener(SequenceKeyListener l) {
        Vector v = sequenceKeyListeners == null ? new Vector(2) : (Vector) sequenceKeyListeners.clone();
        if (!v.contains(l)) {
            v.addElement(l);
            sequenceKeyListeners = v;
        }
    }



     public void addKeyListener(KeyListener l) {
        super.addKeyListener(l);
	seqTable.addKeyListener(l);
    }

    public void removeKeyListener(KeyListener l) {
        super.removeKeyListener(l);
	seqTable.removeKeyListener(l);
    }




    /**
     * Adds a <code>SequenceSelectionListener</code> that will be notified of
     * sequence selection events.
     * @param l the listener to be notified
     * @see #removeSequenceSelectionListener
     */
    public synchronized void addSequenceSelectionListener(SequenceSelectionListener l) {
        Vector v = sequenceSelectionListeners == null ? new Vector(2) : (Vector) sequenceSelectionListeners.clone();
        if (!v.contains(l)) {
            v.addElement(l);
            sequenceSelectionListeners = v;
        }
    }

    /**
     * Removes a <code>SequenceAdjustmentListener</code> from this <code>MultiSequenceViewer</code>.
     * @param l the listener to be removed
     * @see #addSequenceSelectionListener
     */
    public synchronized void removeSequenceSelectionListener(SequenceSelectionListener l) {
        if (sequenceSelectionListeners != null && sequenceSelectionListeners.contains(l)) {
            Vector v = (Vector) sequenceSelectionListeners.clone();
            v.removeElement(l);
            sequenceSelectionListeners = v;
        }
    }
    /**
     * Removes a <code>SequenceAdjustmentListener</code> from this <code>MultiSequenceViewer</code>.
     * @param l the listener to be removed
     * @see #addSequenceAdjustmentListener
     */
    public synchronized void removeSequenceAdjustmentListener(SequenceAdjustmentListener l) {
        if (sequenceAdjustmentListeners != null && sequenceAdjustmentListeners.contains(l)) {
            Vector v = (Vector) sequenceAdjustmentListeners.clone();
            v.removeElement(l);
            sequenceAdjustmentListeners = v;
        }
    }

    /**
     * Adds a <code>SequenceAdjustmentListener</code> to be notified of changes in
     * the sequences contained by this <code>MultiSequenceViewer</code>.
     * @param l the listener to be notified of changes
     * @see #removeSequenceAdjustmentListener
     */
    public synchronized void addSequenceAdjustmentListener(SequenceAdjustmentListener l) {
        Vector v = sequenceAdjustmentListeners == null ? new Vector(2) : (Vector) sequenceAdjustmentListeners.clone();
        if (!v.contains(l)) {
            v.addElement(l);
            sequenceAdjustmentListeners = v;
        }
    }

    protected class SeqTable extends JTable {
        private static final int EMPTY_SELECTION_INDICATOR = 0;
        private SwingRange selectedRange = new SwingRange(0,0);

        // Seed selection start/end with detectable dummy values.
        private int wrappableSelectionColumnStart = EMPTY_SELECTION_INDICATOR;
        private int wrappableSelectionColumnEnd = EMPTY_SELECTION_INDICATOR;
        private int wrappableSelectionRowStart = EMPTY_SELECTION_INDICATOR;
        private int wrappableSelectionRowEnd = EMPTY_SELECTION_INDICATOR;

        public SeqTable() {
        }

        /*
         * A series of methods design to set/get "wrappable" values for selection.
         * These values may be entered as starting in the high value, and ending
         * in the low value, which is necessary with the way the table selection
         * is used in this application.
         */
        public void setWrappableSelectionInterval(int startingCol, int endingCol, int startingRow, int endingRow, boolean isForwardInterval) {
            // Adjust for forward versus reverse drag gestures.
            if (isForwardInterval) {
                wrappableSelectionColumnStart = startingCol;
                wrappableSelectionColumnEnd = endingCol;
                wrappableSelectionRowStart = startingRow;
                wrappableSelectionRowEnd = endingRow;
            }
            else {
                wrappableSelectionColumnStart = endingCol;
                wrappableSelectionColumnEnd = startingCol;
                wrappableSelectionRowStart = endingRow;
                wrappableSelectionRowEnd = startingRow;
            }
        }

        public void clearWrappableSelectionInterval() {
            wrappableSelectionColumnStart = EMPTY_SELECTION_INDICATOR;
            wrappableSelectionColumnEnd = EMPTY_SELECTION_INDICATOR;
            wrappableSelectionRowStart = EMPTY_SELECTION_INDICATOR;
            wrappableSelectionRowEnd = EMPTY_SELECTION_INDICATOR;
        }

        public int getWrappableSelectionColumnStart() {
            return wrappableSelectionColumnStart;
        }

        public int getWrappableSelectionColumnEnd() {
            return wrappableSelectionColumnEnd;
        }

        public int getWrappableSelectionRowStart() {
            return wrappableSelectionRowStart;
        }

        public int getWrappableSelectionRowEnd() {
            return wrappableSelectionRowEnd;
        }


        private SwingRange getSelectedRange() {
            return selectedRange;
        }

        public void setSelectedRange(SwingRange range) {
            selectedRange = range;
            repaint();
        }



      public void paintComponent(Graphics g) {
           super.paintComponent(g);

            if (selectedRange.getEndRange() != Long.MAX_VALUE && (selectedRange.getEndRange()!=0 && selectedRange.getStartRange()!=0 )) {
                g.setColor(Color.yellow);
                Rectangle rec = calcSeqRectangle(selectedRange.getEndRange(), 1);
                g.drawRect((int)rec.getX(), (int)rec.getY(), (int)rec.getWidth(), (int)rec.getHeight());
            }

            // allow subclasses to paint on the table
            sequencePaint(g);
      }

      public int getScrollableUnitIncrement(Rectangle visibleRect,
                                            int orientation,
                                            int direction){
        int unit = super.getScrollableUnitIncrement(visibleRect,orientation,direction);
        if( orientation == SwingConstants.VERTICAL)
          unit = unit * seqModel.getSequenceCount();
        return unit;
      }

      protected Rectangle calcSeqRectangle(long location, int width) {
            int firstRow =  (int)seqModel.locationToRow(location);

            Rectangle rect = this.getCellRect(firstRow, (int)seqModel.locationToColumn(location), false);
            rect.width = rect.width * width;
            rect.height = rect.height * seqModel.getSequenceCount();

            return rect;
      }

        public boolean isCellSelected(int row, int column) {
            // Column 0 should never be selected.  This is a label
            if (column == 0) {
                return false;
            }

            long location = seqModel.cellToLocation(row, column);
            if (location >= 0 && selectedRange.containsLocation(location) &&
                isSelectable && !getSelectionModel().isSelectionEmpty()) {
                return true;
            }
            else {
                return false;
            }
         }



    // Assumes table is contained in a JScrollPane.
    // Returns true iff the cell (rowIndex, vColIndex) is
    // completely visible within the viewport.
    public boolean isCellVisible(int rowIndex, int vColIndex) {
        if (!(getParent() instanceof JViewport)) {
            return false;
        }
        JViewport viewport = (JViewport)getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0)
        Rectangle rect = getCellRect(rowIndex, vColIndex, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x-pt.x, rect.y-pt.y);

        // Check if view completely contains cell
        return new Rectangle(viewport.getExtentSize()).contains(rect);
    }


        /**
         * Scrolls the given row to the top of the viewport
         * @param row the row to scroll to the top
         * @param withInsets determines whether to use insets, currently
         *                   not implementented.
         */
        public void centerVertically(int row, boolean withInsets)
        {
            Rectangle rect = getCellRect(row,0,true);
            Point p = new Point( rect.x, rect.y );
            // check to make sure we're not passing in a bad viewport position
            // not necessary because of simplescroll mode.
           // int lowestViewPosition = vport.getViewSize().height - vport.getExtentSize().height;
           // if(p.y < lowestViewPosition || currP.y < lowestViewPosition)
           scrollPane.getViewport().setViewPosition(p);
        }

        /**
         * Returns the a Rectangle representing the bounds of a given row
         */
        public Rectangle getRowBounds(int row)
        {
            Rectangle result = this.getCellRect(row, -1, true);
            Insets i = this.getInsets();

            result.x = i.left;
            result.width = this.getWidth() - i.left - i.right;

            return result;
        }
    }

    /**
     * Notifies all <code>SequenceMouseListener</code>'s of a sequence mouse
     * moved event.
     */
    protected void fireMouseMoved(SequenceMouseEvent e) {
        if (sequenceMouseListeners != null) {
            Vector listeners = sequenceMouseListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceMouseListener) listeners.elementAt(i)).mouseMoved(e);
            }
        }
    }

    /**
     * Notifies all <code>SequenceSelectionListener</code>'s that a sequence
     * selection event has occurred.
     */
    protected void fireSequenceSelection(SequenceSelectionEvent e){
        if (sequenceSelectionListeners != null) {
            Vector listeners = sequenceSelectionListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceSelectionListener) listeners.elementAt(i)).selectionChanged(e);
            }
        }
    }

    /**
     * Notifies all <code>SequenceMouseListener</code>'s of a sequence mouse
     * selected range event.
     */
    protected void fireMouseSelectedRange(SequenceMouseEvent e) {
        if (sequenceMouseListeners != null) {
            Vector listeners = sequenceMouseListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceMouseListener) listeners.elementAt(i)).mouseSelectedRange(e);
            }
        }
    }

    /**
     * Notifies all <code>SequenceMouseListener</code>'s of a sequence mouse
     * pressed event.
     */
    protected void fireMousePressed(SequenceMouseEvent e) {
        if (sequenceMouseListeners != null) {
            Vector listeners = sequenceMouseListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceMouseListener) listeners.elementAt(i)).mousePressed(e);
            }
        }
    }

    /**
     * Notifies all <code>SequenceMouseListener</code>'s of a sequence mouse
     * released event.
     */
    protected void fireMouseReleased(SequenceMouseEvent e) {
        if (sequenceMouseListeners != null) {
            Vector listeners = sequenceMouseListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceMouseListener) listeners.elementAt(i)).mouseReleased(e);
            }
        }
    }

    /**
     * Notifies all <code>SequenceKeyListener</code>'s of a sequence key
     * event.
     */
    protected void fireKeyTyped(SequenceKeyEvent e) {
        if (sequenceKeyListeners != null) {
            Vector listeners = sequenceKeyListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceKeyListener) listeners.elementAt(i)).keyTyped(e);
            }
        }
    }
/*
     protected void fireKeyPressed(SequenceKeyEvent e) {
        if (sequenceKeyListeners != null) {
            Vector listeners = sequenceKeyListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceKeyListener) listeners.elementAt(i)).keyPressed(e);
            }
        }
    }


     protected void fireKeyReleased(SequenceKeyEvent e) {
        if (sequenceKeyListeners != null) {
            Vector listeners = sequenceKeyListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceKeyListener) listeners.elementAt(i)).keyReleased(e);
            }
        }
    }

*/
    /**
     * Notifies all <code>SequenceAdjustmentListener</code>'s of a Sequence Adjustment
     * event.
     */
    protected void fireAdjustment(SequenceAdjustmentEvent e) {
        if (sequenceAdjustmentListeners != null) {
            Vector listeners = this.sequenceAdjustmentListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                ((SequenceAdjustmentListener) listeners.elementAt(i)).adjustmentValueChange(e);
            }
        }
    }

//    class RowLabelRenderer extends DefaultTableCellRenderer {
//            /**
//     *
//     * Returns the default table cell renderer.
//     *
//     * @param table  the <code>JTable</code>
//     * @param value  the value to assign to the cell at
//     *			<code>[row, column]</code>
//     * @param isSelected true if cell is selected
//     * @param isFocus true if cell has focus
//     * @param row  the row of the cell to render
//     * @param column the column of the cell to render
//     * @return the default table cell renderer
//     */
//    public Component getTableCellRendererComponent(JTable table, Object value,
//                          boolean isSelected, boolean hasFocus, int row, int column) {
//
//	if (isSelected) {
//	   super.setForeground(Color.red);
//	   super.setBackground(Color.red);
//	}
//	else {
//	    super.setForeground(Color.red);
//	    super.setBackground(Color.red);
//	}
//
//	setFont(table.getFont());
//
//	if (hasFocus) {
//	    setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
//	    if (table.isCellEditable(row, column)) {
//	        super.setForeground( UIManager.getColor("Table.focusCellForeground") );
//	        super.setBackground( UIManager.getColor("Table.focusCellBackground") );
//	    }
//	} else {
//	    setBorder(noFocusBorder);
//	}
//
//        setValue(value);
//
//	// ---- begin optimization to avoid painting background ----
//	Color back = getBackground();
//	boolean colorMatch = (back != null) && ( back.equals(table.getBackground()) ) && table.isOpaque();
//        setOpaque(!colorMatch);
//	// ---- end optimization to aviod painting background ----
//
//	return this;
//    }
//    }
}