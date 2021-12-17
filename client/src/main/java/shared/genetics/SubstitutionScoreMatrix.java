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
package shared.genetics;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import java.util.Enumeration;
import java.util.Hashtable;

public class SubstitutionScoreMatrix {

  Hashtable outer = new Hashtable();

  /**
   * establishes a score for the substition of amino acid a with amino acid b.
   */
  public void put(char a, char b, double score) {

    Character obja = new Character(a);
    Character objb = new Character(b);
    Hashtable inner = (Hashtable) outer.get(obja);

    if (null == inner) {
      inner = new Hashtable();
      outer.put(obja, inner);
    }
    Double objscore = new Double(score);
    inner.put(objb, objscore);
  }

  /**
   * retreives the score for a substituted by b.
   */
  public double get(char a, char b) {
    double score = 0;
    Character obja = new Character(a);
    Hashtable inner = (Hashtable) outer.get(obja);
    if (null != inner) {
      Double objscore = (Double) (inner.get(new Character(b)));
      if (null != objscore) {
	score = objscore.doubleValue();
      }
    }
    return score;
  }

  /**
   * Output the matrix stored
   */
  public String toString() {
    StringBuffer s = new StringBuffer();
    s.append("Substitution Matrix\n");
    Enumeration eo = outer.keys();
    while (eo.hasMoreElements()) {
      Character obja = (Character) eo.nextElement();
      Hashtable inner = (Hashtable) outer.get(obja);
      Enumeration ei = inner.keys();
      s.append("" + obja + "\t");
      while (ei.hasMoreElements()) {
	Character objb = (Character) ei.nextElement();
	double f = get(obja.charValue(), objb.charValue());
	s.append(" " + f);
      }
      s.append("\n");
    }
    return new String(s);
  }
}
