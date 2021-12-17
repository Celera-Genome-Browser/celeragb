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

/*
 * Most of the code is cutted and pasted from Java2D demo
 */
package client.gui.other.accession_numbers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Vector;


public class UserGuide extends JPanel implements Runnable {
    static Color black = new Color(20, 20, 20);
    static Color white = new Color(240, 240, 255);
    static Color red = new Color(149, 43, 42);
    static Color blue = new Color(94, 105, 176);
    static Color yellow = new Color(255, 255, 140);
    static JPanel surf;
    static Image cupanim;
    static Image java_logo;
    static Image snpQuery;
    static Image snpView;
    static Image snpTrace;
    static BufferedImage bimg;
    static Image[] images = new Image[5];
    public Director director;
    public int index;
    public long sleepAmt = 30;
    private Thread thread;

    /*
    public static void main(String argv[]) {
         final SNPIntro intro = new SNPIntro();
         WindowListener l = new WindowAdapter() {
              public void windowClosing(WindowEvent e) {System.exit(0);}
              public void windowDeiconified(WindowEvent e) { intro.start(); }
              public void windowIconified(WindowEvent e) { intro.stop(); }
         };
         JFrame f = new JFrame("SNP - Intro");
         f.addWindowListener(l);
         f.getContentPane().add("Center", intro);
         f.pack();
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         int w = 700;
         int h = 500;
         f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
         f.setSize(w, h);
         f.setVisible(true);
         //intro.start();
    }
    */

    /**
     * Surface is the stage where the Director plays its scenes.
     */
    public UserGuide() {
        surf = this;
        setBackground(black);
        setLayout(new BorderLayout());
        setToolTipText("click to skip intro");
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (thread == null) {
                    start();
                } else {
                    stop();
                }
            }
        });

        for (int i = 0; i < images.length; i++) {
            images[i] = createImage("Instruct" + (i + 1) + ".jpg");
        }

        director = new Director();
        start();
    }

    static FontMetrics getMetrics(Font font) {
        return surf.getFontMetrics(font);
    }

    private Image createImage(String fileName) {
        URL url = UserGuide.class.getResource(
                          "/resource/client/images/AccessionClient/" + 
                          fileName);
        Image img = getToolkit().createImage(url);
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(img, 0);

        try {
            tracker.waitForID(0);

            if (tracker.isErrorAny()) {
                System.out.println("Error loading image " + fileName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return img;
    }

    public void paint(Graphics g) {
        Dimension d = getSize();

        if ((bimg == null) || (bimg.getWidth() != d.width) || 
                (bimg.getHeight() != d.height)) {
            bimg = (BufferedImage) createImage(d.width, d.height);

            // reset future scenes
            for (int i = index + 1; i < director.size(); i++) {
                ((Scene) director.get(i)).reset(d.width, d.height);
            }
        }

        Scene scene = (Scene) director.get(index);

        if (thread != null) {
            scene.step(d.width, d.height);
        }

        Graphics2D g2 = bimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setBackground(getBackground());
        g2.clearRect(0, 0, d.width, d.height);

        scene.render(d.width, d.height, g2);

        if (thread != null) {
            // increment scene.index after scene.render
            scene.index++;
        }

        g.drawImage(bimg, 0, 0, this);
        g2.dispose();
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setName("Intro");
            thread.start();
        }
    }

    public synchronized void stop() {
        if (thread != null) {
            thread.interrupt();
        }

        thread = null;
        notifyAll();
    }

    public void reset() {
        index = 0;

        Dimension d = getSize();

        for (int i = 0; i < director.size(); i++) {
            ((Scene) director.get(i)).reset(d.width, d.height);
        }
    }

    public void run() {
        if (index == 0) {
            reset();
        }

        System.gc();

        Thread me = Thread.currentThread();

        while (thread == me) {
            Scene scene = (Scene) director.get(index);

            if (((Boolean) scene.participate).booleanValue()) {
                repaint();

                try {
                    thread.sleep(sleepAmt);
                } catch (InterruptedException e) {
                    break;
                }

                if (scene.index >= (scene.length + 1)) {
                    --scene.index; // some other repaint on the queue?
                    scene.pause(thread);

                    if ((++index) >= director.size()) {
                        reset();
                    }
                }
            } else {
                if ((++index) >= director.size()) {
                    reset();
                }
            }
        }

        thread = null;
    }

    /**
    * Part is a piece of the scene.  Classes must implement Part
    * inorder to participate in a scene.
    */
    interface Part {
        public void reset(int newwidth, int newheight);

        public void step(int w, int h);

        public void render(int w, int h, Graphics2D g2);

        public int getBegin();

        public int getEnd();
    }

    /**
    * Director is the holder of the scenes, their names & pause amounts
    * between scenes.
    */
    static class Director extends Vector {
        GradientPaint gp = new GradientPaint(0, 40, blue, 38, 2, black);
        Font f1 = new Font("serif", Font.PLAIN, 200);
        Font f2 = new Font("serif", Font.PLAIN, 120);
        Font f3 = new Font("serif", Font.PLAIN, 72);
        Font f4 = new Font("serif", Font.PLAIN, 50);
        Object[][][] parts = 
        {

            {
                { "User Instructions", "0" }, 
                { new Instructions(10, 200) }
            }, 

            {
                { "Images", "0" }, 
                {
                    new Temp(Temp.IMG, images[0], 0, 30), //begins and ends timing
                    new Temp(Temp.IMG, images[1], 30, 60), //begins and ends timing
                    new Temp(Temp.IMG, images[2], 60, 90), //begins and ends timing
                    new Temp(Temp.IMG, images[3], 90, 120), //begins and ends timing
                    new Temp(Temp.IMG, images[4], 120, 150), //begins and ends timing

                }
            }, 

            {
                { "Contributors", "0" }, 
                {  //new Temp(Temp.RECT, null, 0, 30),

                    new Contributors(34, 200)}
            }
        };

        public Director() {
            for (int i = 0; i < parts.length; i++) {
                Vector v = new Vector();

                for (int j = 0; j < parts[i][1].length; j++) {
                    v.addElement(parts[i][1][j]);
                }

                addElement(new Scene(v, parts[i][0][0], parts[i][0][1]));
            }
        }
    } // end of director class

    /**
     * Scene is the manager of the parts.
     */
    static class Scene extends Object {
        public Object name;
        public Object participate = new Boolean(true);
        public Object pauseAmt;
        public Vector parts;
        public int index;
        public int length;

        public Scene(Vector parts, Object name, Object pauseAmt) {
            this.name = name;
            this.parts = parts;
            this.pauseAmt = pauseAmt;

            for (int i = 0; i < parts.size(); i++) {
                if (((Part) parts.get(i)).getEnd() > length) {
                    length = ((Part) parts.get(i)).getEnd();
                }
            }
        }

        public void reset(int w, int h) {
            index = 0;

            for (int i = 0; i < parts.size(); i++) {
                ((Part) parts.get(i)).reset(w, h);
            }
        }

        public void step(int w, int h) {
            for (int i = 0; i < parts.size(); i++) {
                Part part = (Part) parts.get(i);

                if ((index >= part.getBegin()) && (index <= part.getEnd())) {
                    part.step(w, h);
                }
            }
        }

        public void render(int w, int h, Graphics2D g2) {
            for (int i = 0; i < parts.size(); i++) {
                Part part = (Part) parts.get(i);

                if ((index >= part.getBegin()) && (index <= part.getEnd())) {
                    part.render(w, h, g2);
                }
            }
        }

        public void pause(Thread thread) {
            try {
                thread.sleep(Long.parseLong((String) pauseAmt));
            } catch (Exception e) {
            }

            System.gc();
        }
    } // End Scene class

    /**
     * Template for Instructions & Contributors consisting of translating
     * blue and red rectangles and an image going from transparent to
     * opaque.
     */
    static class Temp implements Part {
        static final int NOANIM = 1;
        static final int RECT = 2;
        static final int RNA = RECT | NOANIM;
        static final int IMG = 4;
        static final int INA = IMG | NOANIM;
        private int beginning;
        private int ending;
        private float alpha;
        private float aIncr;
        private int type;
        private Rectangle rect1;
        private Rectangle rect2;
        private int x;
        private int y;
        private int xIncr;
        private int yIncr;
        private Image img;
        private int incPos = 10;

        public Temp(int type, Image img, int beg, int end) {
            this.type = type;
            this.img = img;
            this.beginning = beg;
            this.ending = end;


            //aIncr = 0.9f / (ending - beginning);
            aIncr = ending - beginning;

            if ((type & NOANIM) != 0) {
                alpha = 1.0f;
            }
        }

        public void reset(int w, int h) {
            rect1 = new Rectangle(8, 20, w - 20, 30);
            rect2 = new Rectangle(20, 8, 30, h - 20);

            if ((type & NOANIM) == 0) {
                alpha = 0.0f;
                xIncr = w / (ending - beginning);
                yIncr = h / (ending - beginning);
                x = w + (int) (xIncr * 1.4);
                y = h + (int) (yIncr * 1.4);
            }
        }

        public void step(int w, int h) {
            if ((type & NOANIM) != 0) {
                return;
            }

            if ((type & RECT) != 0) {
                rect1.setLocation(x -= xIncr, 20);
                rect2.setLocation(20, y -= yIncr);
            }

            if ((type & IMG) != 0) {
                alpha += aIncr;
            }
        }

        public void render(int w, int h, Graphics2D g2) {
            //incPos += 5;
            incPos = 5;

            if ((type & RECT) != 0) {
                g2.setColor(blue);
                g2.fill(rect1);
                g2.setColor(red);
                g2.fill(rect2);
            }

            if ((type & IMG) != 0) {
                Composite saveAC = g2.getComposite();

                if ((alpha >= 0) && (alpha <= 1)) {
                    g2.setComposite(AlphaComposite.getInstance(
                                            AlphaComposite.SRC_OVER, alpha));
                }


                //g2.drawImage(img, 50, 5, 300, 300, null);
                g2.drawImage(img, 50, 5, null);
                g2.setComposite(saveAC);
            }
        }

        public int getBegin() {
            return beginning;
        }

        public int getEnd() {
            return ending;
        }
    } // End Temp class

    /**
     * Features of Java2D.  Single character advancement effect.
     */
    static class Instructions implements Part {
        static Font font1 = new Font("serif", Font.BOLD, 24);
        static Font font2 = new Font("serif", Font.PLAIN, 20);
        static FontMetrics fm1 = getMetrics(font1);
        static FontMetrics fm2 = getMetrics(font2);
        private String[] list = {
            "Instructions:", "1. Read selections info.", "2. Select a host", 
            "3. Select a port", "4. Click the get button", "5. Observe output.", 
            "6. CG or CT"
        };
        private int beginning;
        private int ending;
        private int strH;
        private int endIndex;
        private int listIndex;
        private Vector v = new Vector();

        /**
         * Constructor takes begins and ends times
         */
        public Instructions(int beg, int end) {
            this.beginning = beg;
            this.ending = end;
        }

        public void reset(int w, int h) {
            strH = (int) (fm2.getAscent() + fm2.getDescent());
            endIndex = 1;
            listIndex = 0;
            v.clear();
            v.addElement(list[listIndex].substring(0, endIndex));
        }

        public void step(int w, int h) {
            if (listIndex < list.length) {
                if ((++endIndex) > list[listIndex].length()) {
                    if ((++listIndex) < list.length) {
                        endIndex = 1;
                        v.addElement(list[listIndex].substring(0, endIndex));
                    }
                } else {
                    v.set(listIndex, list[listIndex].substring(0, endIndex));
                }
            }
        }

        public void render(int w, int h, Graphics2D g2) {
            g2.setColor(white);
            g2.setFont(font1);
            g2.drawString((String) v.get(0), 10, 25);
            g2.setFont(font2);

            for (int i = 1, y = 30; i < v.size(); i++) {
                g2.drawString((String) v.get(i), 20, y += strH);
            }
        }

        public int getBegin() {
            return beginning;
        }

        public int getEnd() {
            return ending;
        }
    } // End Features class

    /**
     * Scrolling text of Java2D contributors.
     */
    static class Contributors implements Part {
        static String[] members = {
            "James Baxandale", "Deepali Bhandari", "Peter Davies", 
            "Les L. Foster", "Scott Henderson", "Mike Harris", "Gragg Helt", 
            "Joe M. Morris", "Mike Simpson", "Eric Sun", "Russell Turner", 
            "David Wu"
        };
        static Font font = new Font("serif", Font.PLAIN, 26);
        static FontMetrics fm = getMetrics(font);
        private int beginning;
        private int ending;
        private int nStrs;
        private int strH;
        private int index;
        private int yh;
        private int height;
        private Vector v = new Vector();
        private Vector cast = new Vector(members.length + 3);
        private int counter;
        private int cntMod;
        private GradientPaint gp;

        public Contributors(int beg, int end) {
            this.beginning = beg;
            this.ending = end;
            java.util.Arrays.sort(members);
            cast.addElement("CONTRIBUTORS");
            cast.addElement(" ");

            for (int i = 0; i < members.length; i++) {
                cast.addElement(members[i]);
            }

            cast.addElement(" ");
            cast.addElement(" ");
            cntMod = ((ending - beginning) / cast.size()) - 1;
        }

        public void reset(int w, int h) {
            v.clear();
            strH = (int) (fm.getAscent() + fm.getDescent());
            nStrs = ((h - 40) / strH) + 1;
            height = (strH * (nStrs - 1)) + 48;
            index = 0;
            gp = new GradientPaint(0, h / 2, Color.white, 0, h + 20, 
                                   Color.black);
            counter = 0;
        }

        public void step(int w, int h) {
            if ((counter++ % cntMod) == 0) {
                if (index < cast.size()) {
                    v.addElement(cast.get(index));
                }

                if ((v.size() == nStrs || index >= cast.size()) && 
                        (v.size() != 0)) {
                    v.removeElementAt(0);
                }

                ++index;
            }
        }

        public void render(int w, int h, Graphics2D g2) {
            g2.setPaint(gp);
            g2.setFont(font);

            double remainder = counter % cntMod;
            double incr = 1.0 - (remainder / cntMod);
            incr = (incr == 1.0)        ? 0 : incr;

            int y = (int) (incr * strH);

            if (index >= cast.size()) {
                y = yh + y;
            } else {
                y = yh = height - (v.size() * strH) + y;
            }

            for (int i = 0; i < v.size(); i++) {
                String s = (String) v.get(i);
                g2.drawString(s, (w / 2) - (fm.stringWidth(s) / 2), y += strH);
            }
        }

        public int getBegin() {
            return beginning;
        }

        public int getEnd() {
            return ending;
        }
    } // End Contributors class
} // End Intro class
/*
$Log$
Revision 1.1  2006/11/09 21:36:06  rjturner
Initial upload of source

Revision 1.4  2003/03/05 19:29:36  grahamkj
No changes for now just a test GB-123456789

Revision 1.3  2002/11/07 16:10:32  lblick
Removed obsolete imports and unused local variables.

Revision 1.2  2000/03/31 16:05:24  dwu
Accession Server GUI Client is completed with instructions panel.

Revision 1.1  2000/03/29 21:03:45  dwu
New GUI interface for easy testing of Accession number servers.
*/
