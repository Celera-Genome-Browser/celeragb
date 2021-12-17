/**
 *   Copyright Leslie L. Foster, 2011.
 *
 * This is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; version 2.1 of the License.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this software; if not, write to the Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */
package oss.model.builder.gff3;

import java.util.Map;

/**
 * Bag-o-data, interpreted per http://www.eu-sol.net/science/bioinformatics/standards-documents/gff3-format-description
 * @author Leslie L Foster
 */
public class Gff3GenericModel {
	public enum Strand { positive, negative, none, unknown, misSpecified }
	
	private String landmarkId;
	private String type;
	private String source;
	private Double score;
	private Strand strand;

	private Integer start;
	private Integer end;
	
	private Integer phase; // This can be null, for "." on input.
	private Map<String,String[]> attributes;
	
	private String id;
	private String name;
	private String nonUniqueAlias;
	private String[] parents;
	private String targetOfAlignment;
	private String gap; // todo more work on this.
	private String derivesFrom;
	private String note;
	private String[] dbxref;
	private String ontologyTerm;

	/**
	 * @param landmarkId the landmarkId to set
	 */
	public void setLandmarkId(String landmarkId) {
		this.landmarkId = landmarkId;
	}
	/**
	 * @return the landmarkId
	 */
	public String getLandmarkId() {
		return landmarkId;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(Double score) {
		this.score = score;
	}
	/**
	 * @return the score
	 */
	public Double getScore() {
		return score;
	}
	/**
	 * @param strand the strand to set
	 */
	public void setStrand(Strand strand) {
		this.strand = strand;
	}
	/**
	 * @return the strand
	 */
	public Strand getStrand() {
		return strand;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(Integer start) {
		this.start = start;
	}
	/**
	 * @return the start
	 */
	public Integer getStart() {
		return start;
	}
	/**
	 * @param end the end to set
	 */
	public void setEnd(Integer end) {
		this.end = end;
	}
	/**
	 * @return the end
	 */
	public Integer getEnd() {
		return end;
	}
	/**
	 * @param phase the phase to set
	 */
	public void setPhase(Integer phase) {
		this.phase = phase;
	}
	/**
	 * @return the phase
	 */
	public Integer getPhase() {
		return phase;
	}
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Map<String,String[]> attributes) {
		this.attributes = attributes;
	}
	/**
	 * @return the attributes
	 */
	public Map<String,String[]> getAttributes() {
		return attributes;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param nonUniqueAlias the nonUniqueAlias to set
	 */
	public void setNonUniqueAlias(String nonUniqueAlias) {
		this.nonUniqueAlias = nonUniqueAlias;
	}
	/**
	 * @return the nonUniqueAlias
	 */
	public String getNonUniqueAlias() {
		return nonUniqueAlias;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(String[] parents) {
		this.parents = parents;
	}
	/**
	 * @return the parent
	 */
	public String[] getParent() {
		return parents;
	}
	/**
	 * @param targetOfAlignment the targetOfAlignment to set
	 */
	public void setTargetOfAlignment(String targetOfAlignment) {
		this.targetOfAlignment = targetOfAlignment;
	}
	/**
	 * @return the targetOfAlignment
	 */
	public String getTargetOfAlignment() {
		return targetOfAlignment;
	}
	/**
	 * @param gap the gap to set
	 */
	public void setGap(String gap) {
		this.gap = gap;
	}
	/**
	 * @return the gap
	 */
	public String getGap() {
		return gap;
	}
	/**
	 * @param derivesFrom the derivesFrom to set
	 */
	public void setDerivesFrom(String derivesFrom) {
		this.derivesFrom = derivesFrom;
	}
	/**
	 * @return the derivesFrom
	 */
	public String getDerivesFrom() {
		return derivesFrom;
	}
	/**
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}
	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}
	/**
	 * @param dbxref the dbxref to set
	 */
	public void setDbxref(String[] dbxref) {
		this.dbxref = dbxref;
	}
	/**
	 * @return the dbxref
	 */
	public String[] getDbxref() {
		return dbxref;
	}
	/**
	 * @param ontologyTerm the ontologyTerm to set
	 */
	public void setOntologyTerm(String ontologyTerm) {
		this.ontologyTerm = ontologyTerm;
	}
	/**
	 * @return the ontologyTerm
	 */
	public String getOntologyTerm() {
		return ontologyTerm;
	}
	
	@Override
	public boolean equals( Object o ) {
		boolean rtnVal = false;
		if (  o instanceof Gff3GenericModel ) {
			Gff3GenericModel model = (Gff3GenericModel)o;
			return model.makeComparisonString().equals( makeComparisonString() );
		}
		return rtnVal; 
	}

	@Override
	public int hashCode() {
		return makeComparisonString().hashCode();
	}
	
	private String makeComparisonString() {
		return getId() + "__" + getStart() + "__" + getEnd() + "__" + getLandmarkId();
	}
}
