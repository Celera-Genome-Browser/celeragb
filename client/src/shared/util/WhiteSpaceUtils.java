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
/**
 * Title:        White Space Utility Methods<p>
 * Description:  Bag-o-functions for white space handling<p>
 * Company:      []<p>
 * @author Unknown (Probably Gregg Helt); refactored to this file by
 *    Les Foster, 6/22/2000
 * @version 1.0
 */
package shared.util;

/**
 * Simply a bag of functions to support diverse client-side classes.  These
 * all deal with white space removal.
 */
public class WhiteSpaceUtils {
    private WhiteSpaceUtils() {
        // This will probably never be attempted, but if it were, even from
        // within the class, this exception forces it to fail.
        throw new IllegalArgumentException("This class may not be instantiated");
    } // No construction allowed!

    /**
     *  boolean keep_real_spaces specifies whether "real spaces" (' ') should be removed or kept
     *  if true then removes tabs, returns, etc. but leaves in ' ' characters
     */
    public static String removeWhiteSpace(String str, boolean keep_real_spaces) {
        StringBuffer buf = new StringBuffer();
        char ch;

        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);

            if (!Character.isWhitespace(ch)) {
                buf.append(ch);
            } else if ((ch == ' ') && keep_real_spaces) {
                buf.append(ch);
            }
        }

        return buf.toString();
    } // End method: removeWhiteSpace

    /**
     * Formatting routine used for prep of residues strings by eliminating
     * start or end characters of space or hyphen.
     */
    public static String stripNonResidueStartEndChar(String str) {
        String result = str;

        if (result.length() <= 0) {
            return result;
        }

        char ch;
        ch = result.charAt(0);

        if ((!Character.isLetter(ch)) && (ch != ' ') && (ch != '-')) {
            //      System.out.println("stripping out weird start character");
            if (result.length() == 1) {
                result = "";
            } else {
                result = result.substring(1);
            }
        }

        if (result.length() > 0) {
            ch = result.charAt(result.length() - 1);

            if ((!Character.isLetter(ch)) && (ch != ' ') && (ch != '-')) {
                //	System.out.println("stripping out weird end character");
                if (result.length() == 1) {
                    result = "";
                } else {
                    result = result.substring(0, result.length() - 1);
                }
            }
        }

        return result;
    } // End method: stripNonResidueStartEndChar

    /**
     *  Just a test method for looking at spaces
     */
    public static String convertWhiteSpace(String str) {
        StringBuffer buf = new StringBuffer();
        char ch;

        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);

            if (!Character.isWhitespace(ch)) {
                buf.append(ch);
            } else {
                if (ch == '\t') {
                    buf.append('#');
                } else if (ch == ' ') {
                    buf.append('^');
                }
            }
        }

        return buf.toString();
    } // End method: convertWhiteSpace
} // End class: WhiteSpaceUtils
