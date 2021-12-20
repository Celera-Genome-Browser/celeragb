package oss.model.builder.gff3;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * This will allow modeling of parent-child relationships among features from GFF3 reads.
 * 
 * @author Leslie L Foster
 */
public class ModelTreeNode {
	private Gff3GenericModel model;
	private String id;
	private List<ModelTreeNode> parents;
	private List<ModelTreeNode> children;
	
	/** This variant is needed, because sometimes the relationship to a model can be seen before the actual model's data. */
	public ModelTreeNode( String id ) {
		this.id = id;
	}

	/** When the model is known, construct from it. */
	public ModelTreeNode( Gff3GenericModel model ) {
		this.model = model;
		this.id = model.getId();
	}
	
	
	/** 
	 * Children are other tree nodes containing models that have this one as parent.
	 * 
	 * @param childNode some node whose model calls this one parent.
	 */
	public void addChild( ModelTreeNode childNode ) {
		if ( children == null ) {
			children = new ArrayList<ModelTreeNode>();
		}
		children.add( childNode );
	}
	
	/** 
	 * May need to eliminate things if there is dynamic addition.
	 * 
	 * @param childNode some node whose model calls this one parent.
	 */
	public boolean removeChild( ModelTreeNode childNode ) {
		boolean foundNode = false;
		if ( children != null ) {
			foundNode = children.remove( childNode );
		}
		
		return foundNode;
	}
	
	public List<ModelTreeNode> getChildren() { return children; }
	
	/**
	 * Gff3 supports multi-parent models (features calling more than one thing parent).
	 * 
	 * @param parentNode some node called parent by this one's model.
	 */
	public void addParent( ModelTreeNode parentNode ) {
		if ( parents == null ) {
			parents = new ArrayList<ModelTreeNode>();
			parents.add( parentNode );
		}
	}
	
	/** 
	 * Set parents.  Do not add new ones.
	 * 
	 * @param parents to add
	 */
	public void setParents( ModelTreeNode[] parents ) {
		this.parents = Arrays.asList(parents);
	}
	
	public List<ModelTreeNode> getParents() { return parents; }
	
	/** This is what this node represents. */
	public Gff3GenericModel getModel() { return model; }
	public void setModel( Gff3GenericModel model ) { this.model = model; }
	
	public String getId() { return id; }
}
