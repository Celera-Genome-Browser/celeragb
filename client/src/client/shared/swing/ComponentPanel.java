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

import client.shared.swing.border.HandleBorder;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class ComponentPanel extends JPanel {
    private HashMap componentMap;
    private HashMap containerMap;
    private HashMap boundsMap;
    private JPopupMenu popupMenu;
    private CompPanelMouseListener compPanelMouseListener;
    private int currX = 0;
    private int currY = 0;
    private int maxHeight = 0;
    private SnapGridLayout layout;
    private Rectangle dragRect = null;
    private Color wrapperBorderColor = Color.lightGray;
    private Color wrapperHandleColor = Color.lightGray;
    private Color wrapperHighlightColor = Color.black;

    /**
     * Creates a Component Panel with default colors
     */
    public ComponentPanel() {
        init();
    }

    /**
     * Returns the border color for the Components that wrap around Components added
     * to this ComponentPanel.
     */
    public Color getWrapperBorderColor() {
        return wrapperBorderColor;
    }

    /**
     * Returns the handle color for the Components that wrap around Components added
     * to this ComponentPanel.
     */
    public Color getWrapperHandleColor() {
        return wrapperHandleColor;
    }

    /**
     * Sets the border color for the Components that wrap around Components added
     * to this ComponentPanel.
     */
    public void setWrapperBorderColor(Color c) {
        wrapperBorderColor = c;
    }

    /**
     * Sets the handle color for the Components that wrap around Components added
     * to this ComponentPanel.
     */
    public void setWrapperHandleColor(Color c) {
        wrapperHandleColor = c;
    }

    /**
     * Returns the highlight color for the Components that wrap around Components added
     * to this ComponentPanel.
     */
    public Color getWrapperHighlightColor() {
        return wrapperHighlightColor;
    }

    /**
     * Sets the highlight color for the Components that wrap around Components added
     * to this ComponentPanel.
     */
    public void setWrapperHighlightColor(Color c) {
        wrapperHighlightColor = c;
    }

    /**
     * Adds the specified component to the <code>ComponentPanel</code>
     * @param component the component to be added
     * @param name the name for the component, this name will be used in the
     *        popup menu used for component visibility selection.
     */
    public Component add(Component component, String name) {
        ComponentWrapper wrap = new ComponentWrapper(component, 
                                                     wrapperBorderColor, 
                                                     wrapperHandleColor, 
                                                     wrapperHighlightColor);
        wrap.addMouseListener(compPanelMouseListener);
        wrap.addMouseMotionListener(compPanelMouseListener);
        wrap.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                showIfPopupTrigger(e);
            }

            public void mouseReleased(MouseEvent e) {
                showIfPopupTrigger(e);
            }
        });
        componentMap.put(name, component);
        containerMap.put(component, wrap);
        boundsMap.put(component, component); //@todo do something w/ bounds

        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.setSelected(component.isVisible());
        item.addActionListener(new PopupActionListener());
        popupMenu.add(item);

        //return super.add(component);
        return super.add(wrap);
    }

    /**
     * Adds the component to this <code>ComponentPanel</code>, the
     * label for the popup menu will be set to the string returned
     * by <code>component.getName()</code>
     */
    public Component add(Component component) {
        return add(component, component.getName());
    }

    /**
     * Removes a component from this <code>ComponentPanel</code>, also removes
     * it from this component panel's popup menu.
     */
    public void remove(Component component) {
        Set keySet = componentMap.keySet();
        Iterator itr = keySet.iterator();
        String key = null;
        String walker;

        while (itr.hasNext() && (key == null)) {
            walker = (String) itr.next();

            if (component.equals(componentMap.get(walker))) {
                key = walker;
                itr.remove();
            }
        }

        boundsMap.remove(component);

        MenuElement[] menuElements = popupMenu.getSubElements();

        for (int i = 0; i < menuElements.length; i++) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) menuElements[i];

            if (item.getText().equals(key)) {
                popupMenu.remove(item);
            }
        }

        Component wrap = (Component) containerMap.remove(component);
        super.remove(wrap);
        revalidate();
        repaint();
    }

    protected void init() {
        componentMap = new HashMap();
        boundsMap = new HashMap();
        containerMap = new HashMap();
        compPanelMouseListener = new CompPanelMouseListener();
        setOpaque(true);
        layout = new SnapGridLayout(this);
        setLayout(layout);
        popupMenu = new JPopupMenu();
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                showIfPopupTrigger(e);
            }

            public void mouseReleased(MouseEvent e) {
                showIfPopupTrigger(e);
            }
        });
    }

    /**
     * Shows the popup menu if the mousevent is a popup trigger
     */
    protected void showIfPopupTrigger(MouseEvent e) {
        if (popupMenu.isPopupTrigger(e)) {
            popupMenu.pack();
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (dragRect != null) {
            g.setColor(wrapperHighlightColor);
            g.drawRect(dragRect.x, dragRect.y, dragRect.width, dragRect.height);
        }
    }

    /**
     * Used for testing only
     */
    public static void main(String[] args) {
        final ComponentPanel cp = new ComponentPanel();
        JFrame app = new JFrame();
        Container mainP = app.getContentPane();
        mainP.setBackground(Color.white);

        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        mainP.setLayout(gb);
        mainP.add(cp, c);
        c.gridy = 1;

        JPanel p2 = new JPanel();
        p2.setBackground(Color.blue);
        p2.setOpaque(true);
        mainP.add(p2, c);

        final JButton clearB = new JButton("Remove ME");
        clearB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cp.remove(clearB);
            }
        });
        cp.add(new JButton("Button 1"), "button1");
        cp.add(new JButton("Button 2"), "button2");
        cp.add(clearB, "Clear Button");
        cp.add(new JLabel("hello world"), "Label");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        app.setSize(400, 100);
        app.setVisible(true);
    }

    /**
     * action listener used to set components visibility based on the selected
     * state of the popup menu's checkbox
     */
    class PopupActionListener implements ActionListener {
        public void actionPerformed(ActionEvent actionevent) {
            AbstractButton aButton = (AbstractButton) actionevent.getSource();
            Component c = (Component) componentMap.get(
                                  actionevent.getActionCommand());
            Component wrap = (Component) containerMap.get(c);
            wrap.setVisible(aButton.getModel().isSelected());
        }
    }

    /**
     * Used to handle mouse events in the panel
     */
    class CompPanelMouseListener extends MouseInputAdapter {
        int xOffset = 0;
        int yOffset = 0;
        boolean isDragging = false;

        public void mousePressed(MouseEvent e) {
            xOffset = e.getX();
            yOffset = e.getY();
        }

        public void mouseDragged(MouseEvent e) {
            layout.setGrowingToFit(false);
            isDragging = true;

            Component c = e.getComponent();
            int horOffset = c.getX();
            int verOffset = c.getY();
            e.translatePoint(horOffset, verOffset);

            Point translatedP = e.getPoint();
            Dimension compD = c.getSize();
            dragRect = new Rectangle(translatedP, compD);
            dragRect.x -= xOffset;
            dragRect.y -= yOffset;

            Rectangle dirtyRect = new Rectangle(dragRect);
            dirtyRect.grow(60, 60);
            repaint(dirtyRect);

            if (translatedP.y > getSize().height) {
                layout.setGrowingToFit(true);
                invalidate();
                getParent().doLayout();
            }

            //layout.moveComponent(c,translatedP);
        }

        public void mouseReleased(MouseEvent e) {
            if (((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) && 
                    isDragging) {
                isDragging = false;

                Component c = e.getComponent();
                int horOffset = c.getX();
                int verOffset = c.getY();
                e.translatePoint(horOffset, verOffset);

                Point translatedP = e.getPoint();
                Rectangle dirtyRect = new Rectangle(dragRect);
                dirtyRect.grow(60, 60);
                dragRect = null;
                repaint(dirtyRect);
                layout.moveComponent(c, translatedP);
                layout.setGrowingToFit(false);
            }
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
            //System.out.println(e.getSource().getClass());
        }
    }

    /**
     * Class to create the handled JPanel's that wrap the components to be added
     * to the <code>ComponentPanel</code>.
     */
    class ComponentWrapper extends JPanel {
        private Component component;
        private HandleBorder border;
        private Color borderColor;
        private Color handleColor;
        private Color highlightColor;

        public ComponentWrapper(Component c, Color borderColor, 
                                Color handleColor, Color highlightColor) {
            component = c;
            this.handleColor = handleColor;
            this.borderColor = borderColor;
            this.highlightColor = highlightColor;
            init();
            this.add(component);
        }

        protected void init() {
            border = new HandleBorder(borderColor, handleColor);
            setBorder(border);

            WrapperMouseListener myMouseListener = new WrapperMouseListener();
            this.addMouseListener(myMouseListener);
            this.addMouseMotionListener(myMouseListener);
            this.setLayout(new GridLayout(1, 1));
        }

        class WrapperMouseListener extends MouseInputAdapter {
            public void mousePressed(MouseEvent e) {
                repaint();
            }

            public void mouseDragged(MouseEvent e) {
                border.setBackground(highlightColor);
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                border.setHandleColor(handleColor);
                border.setBackground(borderColor);
                repaint();
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }
        }
    }
}