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
package api.facade.concrete_facade.xml.model;

/**
 * Title:        NonHierarchicalFeatureModel
 * Description:  Compound Feature Model Implementation--with no child models.
 * @author Les Foster
 * @version $Id$
 */

import api.entity_model.model.annotation.PolyMorphism;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.concrete_facade.xml.XmlFacadeManager;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.facade.concrete_facade.xml.sax_support.PropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A 'feature model' or entity-building object, that creates 'compound-level'
 * objects.  In other words, an entity-builder that does not create entities
 * having child entities.  Rather the entity created would be more of a
 * single-span-is-always-all.
 */
public class NonHierarchicalFeatureModel extends CompoundFeatureModel {

  //-----------------------------------MEMBER VARIABLES
  private String mSelectedPolyType = null;
  private List mAlleleVariants = null;
  private int mSubjectStart;
  private int mSubjectEnd;
  private int mStart;
  private int mEnd;

  //-----------------------------------CONSTRUCTORS
  /**
   * Constructor which takes the oid of this model as well as the OID agains
   * which it will be aligned.
   */
  public NonHierarchicalFeatureModel(OID lCompoundFeatureOID, OID lOidOfAlignment,
    XmlFacadeManager lReadFacadeManager) {

    super(lCompoundFeatureOID, lOidOfAlignment, lReadFacadeManager);

  } // End constructor

  //-----------------------------------PUBLIC INTERFACE
  /**
   * Dummied up to protect against the eventuality of a decision to use
   * human curated features in this 'model' type.
   */
  public void addEvidenceList(List lEvidenceOIDList) {
    if ((lEvidenceOIDList != null) && (lEvidenceOIDList.size() > 0))
      System.out.println("Attention, developer: "+
      getClass().getName()+
      " Not yet usable for human curated features.  Consider extending!");
  } // End method

  /**
   * Allele strings, of nucleotides, are stored as <alignment tag data.
   * They tell a "sort of" alignment to the axis indicated.
   *
   * Force try/catch to allow abandon of feature creation. Stop checking stuff
   * if error found.
   */
  public void addPolymorphismAlleleText(String lPolyType, String lText) throws Exception {
    if (mSelectedPolyType == null) {
      mSelectedPolyType = lPolyType;
      mAlleleVariants = new ArrayList();
    } // Not done yet.
    else {

      if (! mSelectedPolyType.equals(lPolyType)) {
        throw new IllegalArgumentException("ERROR: poly type "+lPolyType+
          " must match first given poly type of "+mSelectedPolyType+
          " for polymorphism feature "+getOID());
      } // Invalid poly type.

    } // Done.  Must test.

    // Add this latest allele text to the list.
    mAlleleVariants.add(lText.trim());

  } // End method

  /**
   * Returns the range covered by this compound feature.  Because this particular
   * type of feature MAY have start equal to end, we will set orientation forward
   * if that is the case.
   */
  public Range calculateFeatureRange() {
    Range.Orientation lOrientation = (getStart() <= getEnd()) ? Range.FORWARD_ORIENTATION : Range.REVERSE_ORIENTATION;
    int lMagnitude = Math.abs(getEnd() - getStart());
    return new Range(getStart(), lMagnitude, lOrientation);
  } // End method

  /**
   *  Create a Feature from this feature model.
   */
  public GenomicEntity createFeatureEntity() {

    GenomicEntity lFeatureEntity = super.createFeatureEntity();
    if (lFeatureEntity instanceof PolyMorphism) {
      PolyMorphism lPolyEntity = (PolyMorphism)lFeatureEntity;
      for (Iterator it = getPolymorphismAlleleTexts(); it.hasNext(); )
        lPolyEntity.addAllele((String)it.next());
      lPolyEntity.setFunctionalDomains(
        new short[] { PolyMorphism.FunctionalDomain.convertFunctionalDomain(
          getFunctionalDomainFromProperties()
        ) }
      );
      lPolyEntity.setPolyMutationType( mSelectedPolyType );

      // NOT SETTING validation status at this time...
    } // Got a polymorph!

    return lFeatureEntity;

  } // End method

  /**
   * Series of setters to populate this "model".  NOTE: these are borrowed
   * from the "grandparent" class.  Probably should have a common base class
   * for Compound Feature Model, and inherit from it HERE, and in the Real
   * CompoundFeatureModel class.
   */
  public void setSubjectStart(int val) { mSubjectStart = val; }
  public void setSubjectEnd(int val) { mSubjectEnd = val; }

  public void setStart(String startVal) {
    try {
      mStart = Integer.parseInt(startVal);
    } catch (NumberFormatException nfe) {
      FacadeManager.handleException(new Exception("Bad numeric start value for span: "+startVal));
    } // End catch block for conversion
  } // End method: setStart

  public void setEnd(String endVal) {
    try {
      mEnd = Integer.parseInt(endVal);
    } catch (NumberFormatException nfe) {
      FacadeManager.handleException(new Exception("Bad numeric end value for span: "+endVal));
    } // End catch block for conversion
  } // End method: getEnd

  public void setStart(int startInt) {
    mStart = startInt;
  } // End method: setStart

  public void setEnd(int endInt) {
    mEnd = endInt;
  } // End method: setEnd

  public int getStart() { return mStart; }
  public int getEnd() { return mEnd; }
  public int getSubjectStart() { return mSubjectStart; }
  public int getSubjectEnd() { return mSubjectEnd; }

  //-----------------------------------HELPER METHODS
  /**
   * Returns all variations on the insertion/deletion theme.
   */
  private Iterator getPolymorphismAlleleTexts() {
    if (mAlleleVariants == null)
      return Collections.EMPTY_LIST.iterator();
    else
      return mAlleleVariants.iterator();
  } // End method

  /** Returns the functional domain setting for polymorphism. */
  private String getFunctionalDomainFromProperties() {
    for (Iterator it = getPropertySources().iterator(); it.hasNext(); ) {
      PropertySource nextSource = (PropertySource)it.next();
      if (nextSource.getName().equals("functional_domain")) {
        if (nextSource.getValue().equals(PolyMorphism.FunctionalDomain.UTR_5))
          return PolyMorphism.FunctionalDomain.UTR_5;
        if (nextSource.getValue().equals(PolyMorphism.FunctionalDomain.UTR_3))
          return PolyMorphism.FunctionalDomain.UTR_3;
        if (nextSource.getValue().equals(PolyMorphism.FunctionalDomain.STOP_CODON))
          return PolyMorphism.FunctionalDomain.STOP_CODON;
        if (nextSource.getValue().equals(PolyMorphism.FunctionalDomain.SILENT))
          return PolyMorphism.FunctionalDomain.SILENT;
        if (nextSource.getValue().equals(PolyMorphism.FunctionalDomain.MISSENSE))
          return PolyMorphism.FunctionalDomain.MISSENSE;
        if (nextSource.getValue().equals(PolyMorphism.FunctionalDomain.NONSENSE))
          return PolyMorphism.FunctionalDomain.NONSENSE;
        if (nextSource.getValue().equals(PolyMorphism.FunctionalDomain.INTRON))
          return PolyMorphism.FunctionalDomain.INTRON;
        if (nextSource.getValue().equals(PolyMorphism.FunctionalDomain.INTERGENIC))
          return PolyMorphism.FunctionalDomain.INTERGENIC;

        return nextSource.getValue();
      } // Got our domain.
    } // For all property sources.
    return PolyMorphism.FunctionalDomain.UNKNOWN;
  } // End method

} // End class
