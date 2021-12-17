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
package vizard.glyph;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

/**
 * This class tries to pack the glyphs in vertical order either descending or ascending
 * based on a certain user defined property such as the bit score.
 */
import vizard.Glyph;
import vizard.ParentGlyph;


public class PropertySortedPacker extends Packer{


  private String currentPropValue;



  public void packChild(Glyph child){

    TranslationGlyph.Concrete t = new TranslationGlyph.Concrete(0,0);
    this.addChild(t);
    t.addChild(child);

        t.setTranslation(0,10* this.childCount()+50);
        t.boundsChanged();

  }

   /** Packed child count equals the
    *  number of Translation Glyphs as
    *  every Genomic glyph is singly parented
    *  by one Translation Glyph
    */

  public int packedChildCount(){return this.childCount();}

  /**
   * This method first removes the glyph
   * from its immediate parent and then
   * removes the parent(translationglyph)
   *  from its parent(packer)
   */
  public  void unpackChild(Glyph child){
    ParentGlyph parent =child.parent();
    parent.removeChild(child);
    if(parent instanceof TranslationGlyph){
      this.removeChild(parent);
    }
  }

  public  Glyph packedChildAt(int n){return null;}


    public void collapse() {
	for(int i = 0; i < childCount(); ++i) {

	     translation(i).setTranslation(0, 0);

	}
    }

    public void expand(){
      for(int i = 0; i < childCount(); ++i) {

	     translation(i).setTranslation(0, 10*i);

	}
    }

 private TranslationGlyph.Concrete translation(int i) {
        return (TranslationGlyph.Concrete)child(i);
    }

/*
 public void setCurrentPropValue(String newValue){
    if(!currentPropValue.equals(newValue)){
        currentPropValue = newValue;
        incrementTranslation = true;
    }else incrementTranslation = false;
 }
*/


}