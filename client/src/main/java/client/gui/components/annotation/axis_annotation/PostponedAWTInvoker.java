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
package client.gui.components.annotation.axis_annotation;

import java.awt.*;


//view.delete should ask PostponedAWTInvoker.timeToQuit = true

public class PostponedAWTInvoker
{
    private boolean moreWorkAdded;
    private boolean timeToQuit;
    private Runnable runnable;
    private int waitMillisec;
    private int maxWaitMillisec;
    Object o;

    private Thread myThread = new Thread() {
            public void run() {
                while(!timeToQuit) {
                    gotoSleep();
                    if (timeToQuit)
                        break;

                    int totalWait = 0;
                    do {
                        moreWorkAdded = false;
                        try { Thread.sleep(waitMillisec); }
                        catch(InterruptedException ex) {}
                        totalWait += waitMillisec;
                    }
                    while(!timeToQuit && moreWorkAdded && totalWait < maxWaitMillisec);

                    if (!timeToQuit) {
                        try { EventQueue.invokeAndWait(runnable); }
                        catch(Exception ex) {}
                    }
                }
            }};

    public PostponedAWTInvoker(Runnable runnable) {
        this(runnable, 200, 2000);
    }

    public PostponedAWTInvoker(Runnable runnable,
                               int waitMillisec,
			       int maxWaitMillisec)
    {
	this.runnable = runnable;
	this.waitMillisec = waitMillisec;
	this.maxWaitMillisec = maxWaitMillisec;

        myThread.start();
    }

/*    public boolean isRunning() {
        return isRunning;
    }*/

    public void executeLater() {
        wakeUp();
    }

    public void executeNow() {
        runnable.run();
    }

//    public void reset() {}

    public void delete() {
        timeToQuit = true;
        wakeUp();
    }

    private void gotoSleep() {
        synchronized(myThread) {
            if (moreWorkAdded)
                return;
            try { myThread.wait(); }
            catch(InterruptedException ex) {}
        }
    }

    private void wakeUp() {
        synchronized(myThread) {
            moreWorkAdded = true;
            myThread.notify();
        }
    }

    /*
    // The purpose of the processPostponedActions() method is to minimize
    // the time spent repainting an AWT component when a bunch of entities are
    // aligned/unaligned.
    private void processPostponedActions() {
        if (postponedThread != null)
            return;

        postponedThread = new Thread() {
            public void run() {
                final long UNIT_WAIT = 200; //millisec
                final long MAX_WAIT = 2000; //millisec

                //Sleep until either:
                //  - the maximum wait time has been reached, or
                //  - nothing new has been postponed
                //    (implying that, eg, the loading process is over, and so
                //     we can process the postponed things now).
                int totalWait = 0;
                for(;;) {
                    int previousSize = postponedActionList.size();
                    totalWait += UNIT_WAIT;
                    try { Thread.sleep(UNIT_WAIT); }
                    catch(InterruptedException ex) {}
                    boolean noNewRequest = postponedActionList.size() == previousSize;
                    if (noNewRequest || totalWait >= MAX_WAIT)
                        break;
                }

                EventQueue.invokeLater(new Runnable() {
                    //The following runs in the AWT thread
                    public void run() {
                        // Execute postponed glyph creations and deletions
                        AlignableGenomicEntity selection = (AlignableGenomicEntity)browserModel.getCurrentSelection();
                        for(int i = 0; i < postponedActionList.size(); ++i) {
                            Object action = postponedActionList.get(i++);
                            if (action == POSTPONED_CREATION) {
                                Feature rootFeature = (Feature)postponedActionList.get(i);
                                TierGlyph tier = getTier(rootFeature);
                                createGlyphsAndAddToView(tier,
                                                         rootFeature,
                                                         rootFeature == selection);
                            }
                            else if (action == POSTPONED_DELETION) {
                                GeometricAlignment alignment = (GeometricAlignment)postponedActionList.get(i);
                                deleteGlyphsFor(alignment);
                            }
                            else {
                                AlignableGenomicEntity entity = (AlignableGenomicEntity)postponedActionList.get(i);
                                boolean zoomIn = action == POSTPONED_SELECTION_AND_ZOOM;
                                selectionChanged(entity, zoomIn);
                            }
                        }
                        postponedActionList.clear();
                        postponedThread = null;
                    }});
            }};
        postponedThread.start();
    }
*/

}
