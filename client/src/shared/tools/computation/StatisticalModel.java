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
 *                  Confidential -- Do Not Distribute                  *
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
package shared.tools.computation;

import api.stub.sequence.DNA;
import api.stub.sequence.Sequence;

import java.io.*;
import java.util.StringTokenizer;


/**
 * A class for computing statistics about a sequence based on a profile.
 */
public class StatisticalModel {
    public static final Type DONOR = new Type();
    public static final Type ACCEPTOR = new Type();
    public static final Type NEITHER = new Type();
    private final static int A_INDEX = 0;
    private final static int C_INDEX = 1;
    private final static int T_INDEX = 2;
    private final static int G_INDEX = 3;
    private final static int MAX_ROW_INDEX = 3;
    private int windowSize = -1;
    private int exonOffset = -1;
    private float[][] profile;
    private float modelPriorProb = -1.0f;
    private boolean profileRead = false;
    private boolean windowSizeSet = false;
    private boolean exonOffsetSet = false;
    private boolean modelPriorProbSet = false;
    private boolean aProfileSet = false;
    private boolean cProfileSet = false;
    private boolean gProfileSet = false;
    private boolean tProfileSet = false;

    //public StatisticalModel(String inputFileName, int windowSize) {
    //     profile = new float[MAX_ROW_INDEX + 1][];
    //     this.windowSize = windowSize;
    //
    //     readProfile(inputFileName);
    //}
    public StatisticalModel(String resourceFileName) {
        profile = new float[MAX_ROW_INDEX + 1][];
        readProfile(resourceFileName, true);
    }

    public StatisticalModel(File inputFile) {
        profile = new float[MAX_ROW_INDEX + 1][];
        readProfile(inputFile.getAbsolutePath(), false);
    }

    /**
     * Get a profile
     */
    private boolean getProfile(StringTokenizer st, int index) {
        try {
            if ((index < 0) || (index > MAX_ROW_INDEX)) {
                return false;
            }

            if (st == null) {
                return false;
            }

            if (windowSizeSet) {
                profile[index] = new float[windowSize];

                for (int i = 0; i < windowSize; i++) {
                    if (st.hasMoreTokens()) {
                        profile[index][i] = Float.parseFloat(
                                                    st.nextToken().trim());
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * read in a statistical profile
     * @param profileIn the stream from which to read the profile.
     */
    public void readProfile(String inputFileName, boolean isFromJarResourceFile) {
        int lineCount = 0;
        BufferedReader profileIn;
        String line;
        String token;
        StringTokenizer t;

        if (inputFileName == null) {
            return;
        }

        //System.out.println("Reading Splice Profile: " + inputFileName);
        try {
            if (isFromJarResourceFile) {
                profileIn = new BufferedReader(
                                    new InputStreamReader(
                                            this.getClass()
                                                .getResourceAsStream(inputFileName)));
            } else {
                profileIn = new BufferedReader(
                                    new InputStreamReader(
                                            new FileInputStream(inputFileName)));
            }

            while ((line = profileIn.readLine()) != null) {
                lineCount++;
                t = new StringTokenizer(line.trim().toUpperCase(), ",");

                if (t.hasMoreTokens()) {
                    token = t.nextToken().trim();

                    if (token.equals("WINDOW_SIZE")) {
                        //get the window size
                        if (t.hasMoreTokens()) {
                            windowSize = Integer.parseInt(t.nextToken().trim());
                            windowSizeSet = true;
                        }
                    } else if (token.equals("EXON_OFFSET")) {
                        //get the exon offset
                        if (t.hasMoreTokens()) {
                            exonOffset = Integer.parseInt(t.nextToken().trim());
                            exonOffsetSet = true;
                        }
                    } else if (token.equals("MODEL_PRIOR_PROB")) {
                        //get the prior probabliliy for this model
                        if (t.hasMoreTokens()) {
                            modelPriorProb = Float.parseFloat(
                                                     t.nextToken().trim());
                            modelPriorProbSet = true;
                        }
                    } else if (token.equals("A_PROFILE")) {
                        //get the profile for A
                        aProfileSet = getProfile(t, A_INDEX);
                    } else if (token.equals("C_PROFILE")) {
                        //get the profile for C
                        cProfileSet = getProfile(t, C_INDEX);
                    } else if (token.equals("G_PROFILE")) {
                        //get the profile for G
                        gProfileSet = getProfile(t, G_INDEX);
                    } else if (token.equals("T_PROFILE")) {
                        //get the profile for T
                        tProfileSet = getProfile(t, T_INDEX);
                    } else {
                        //ignore the line
                    }
                }
            } //end while

            profileIn.close();

            profileRead = windowSizeSet && exonOffsetSet && aProfileSet && 
                          cProfileSet && gProfileSet && tProfileSet && 
                          modelPriorProbSet;

            if (!profileRead) {
                throw new Exception("Error reading splice profile: " + 
                                    inputFileName);
            }
        } catch (Exception ex) {
            System.err.println("Error (StatisticalModel.readProfile) file=" + 
                               inputFileName + " line=" + lineCount);

            try {
                api.entity_model.management.ModelMgr.getModelMgr()
                                               .handleException(ex);
            } catch (Exception ex1) {
                ex.printStackTrace();
            }
        }
    }

    /*
     * dump a statistical profile
     * @param out output is sent to this stream.
     * @param banner a header line place at the top of the output.
     */
    public void dumpProfile(PrintStream out, String banner) {
        if (!profileRead) {
            return;
        }

        if (banner != null) {
            out.println("==== " + banner + " ====");
        }

        out.println("WINDOW_SIZE=" + getWindowSize());
        out.println("EXON_OFFSET=" + getExonOffset());

        for (int i = 0; i <= MAX_ROW_INDEX; i++) {
            for (int j = 0; j < profile[i].length; j++) {
                out.print(profile[i][j] + "  ");
            }

            out.println("");
        }

        out.println("");
    }

    /**
     * get the size of the sequence window used for statistical computations.
     */
    public int getWindowSize() {
        return windowSize;
    }

    /**
     * get the offset of the 1st exon base wrt the profile array.
     */
    public int getExonOffset() {
        return exonOffset;
    }

    /**
     * get the prior probability of the model.
     */
    public float getModelPriorProb() {
        return modelPriorProb;
    }

    /**
     * compute the probability of a window based on a profile.
     * @param window the input sequence window.
     */
    public float computeProbability(Sequence window) {
        float priorProb = 1.0f;

        if (!profileRead) {
            return -1.0f;
        }

        if (window.length() != windowSize) {
            System.out.println(
                    "Error (StatisticalModel.computeProbability) window length bad.");
            System.exit(1);
        }

        int win_index;

        for (int i = 0; i < window.length(); i++) {
            win_index = getIndex(window.get(i));

            if (win_index >= 0) {
                priorProb *= profile[win_index][i];
            }
        }

        return priorProb;
    }

    private int getIndex(int base) {
        switch (base) {
        case DNA.A:
            return A_INDEX;

        case DNA.C:
            return C_INDEX;

        case DNA.T:
            return T_INDEX;

        case DNA.G:
            return G_INDEX;

        default:
            return -1;
        }
    }

    public static class Type {
    }
}