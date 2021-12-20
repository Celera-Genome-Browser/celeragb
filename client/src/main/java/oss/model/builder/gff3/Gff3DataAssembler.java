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

import oss.model.builder.gff3.Gff3LineReader.Gff3LineReaderException;

import java.io.File;
import java.util.*;


/**
 * Reads the GFF3 input, and builds up intra-object relationships from it.
 * 
 * @author Leslie L Foster
 */
public class Gff3DataAssembler {
	private Gff3LineReader rdr;
	private String inputfile;
	private Map<String,ModelTreeNode> idVsNode;
	private List<ModelTreeNode> topLevelFeatures;
	private int nextUnknownId = 1;
	private boolean multiParentedFeaturesAcceptable;
	
	/** Construct with the only-ever input file to be run through this assembler. */
	public Gff3DataAssembler( String inputfile ) {
		this.inputfile = inputfile;
	}
	
	/**
	 * @param multiParentedFeaturesAcceptable the multiParentedFeaturesAcceptable to set
	 */
	public void setMultiParentedFeaturesAcceptable(
			boolean multiParentedFeaturesAcceptable) {
		this.multiParentedFeaturesAcceptable = multiParentedFeaturesAcceptable;
	}

	/**
	 * @return the multiParentedFeaturesAcceptable
	 */
	public boolean isMultiParentedFeaturesAcceptable() {
		return multiParentedFeaturesAcceptable;
	}

	/**
	 * Get the model objects representing all axes, by light scan. 
	 * @return list of models.
	 */
	public List<Gff3GenericModel> getAxisModels() {
		List<Gff3GenericModel> axisModels = new ArrayList<Gff3GenericModel>();
		List<String> existingLandmarkIds = new ArrayList<String>();
		Gff3LineReader rdr = initReader();
		try {
			Gff3GenericModel model;
			while ( null != ( model = rdr.nextLine() ) ) {
				if ( isAxis( model, existingLandmarkIds ) ) {
					existingLandmarkIds.add( model.getLandmarkId() );
					axisModels.add( model );
				}
			}

		} catch ( Gff3LineReaderException glre ) {
			throw new RuntimeException( "Failed to read file " + inputfile, glre );
		} finally {
			rdr.close();			
		}
		return axisModels;
	}

	/**
	 * Prepare models for use by caller.  Order dependency: call this before calling getTopLevelModels().
	 * 
	 * @param axisId models must refer to this as their "landmark" (gff3 term).
	 */
	public void prepareModels( String axisId ) {
		Gff3LineReader rdr = initReader();
		StringBuilder errs = new StringBuilder();
		Set<String> uniqueIds = new HashSet<>();
		try {
			Gff3GenericModel model;
			idVsNode = new HashMap<>();
			topLevelFeatures = new ArrayList<>();
			while ( null != ( model = rdr.nextLine() ) ) {
				if ( ! ( model.getLandmarkId()).equals( axisId ) ) {
					continue;						
				}

				ModelTreeNode nodeForModel = null;
				try {
					establishValidModelId( model, uniqueIds );
					String modelId = model.getId();
					nodeForModel = getNodeForModel( model, modelId );
					makeAssociations( nodeForModel );
				} catch ( BadModelException bme ) {
					backoutModel(model, nodeForModel);
					// Add to error messages.
					errs.append( bme.getMessage() + " " );
				}
			}
		} catch ( Gff3LineReaderException glre ) {
			throw new RuntimeException( "Failed to read file " + inputfile, glre );
		} finally {
			rdr.close();			
		}
		if (errs.length() < 0 ) {
			System.out.println("WARNING: the following problems occurred while reading input file " + inputfile + "\n" );
			System.out.println("         " + errs.toString() );
		}
		
	}
	
	/** Call this after "prepareModels." */
	public List<ModelTreeNode> getTopLevelFeatures() {
		return topLevelFeatures; 
	}

	/** Convenience method to get rid of a model which has been rejected for some reason. */
	private void backoutModel( Gff3GenericModel model, ModelTreeNode nodeForModel ) {
		// Cleanup.
		idVsNode.remove( model.getId() );
		topLevelFeatures.remove( nodeForModel );

		if ( nodeForModel != null ) {
			List<ModelTreeNode> childNodes = nodeForModel.getChildren();
			if ( childNodes != null ) {
				for ( ModelTreeNode childNode: childNodes ) {
					childNode.getParents().remove( nodeForModel );
				}
				childNodes.clear();
			}
			List<ModelTreeNode> parentNodes = nodeForModel.getParents();
			if ( parentNodes != null ) {
				for ( ModelTreeNode parentNode: parentNodes ) {
					parentNode.getChildren().remove( nodeForModel );
				}
				parentNodes.clear();
			}
		}
	}

	/** Make sure that a usable ID exists in the model by return time. */
	private void establishValidModelId(Gff3GenericModel model, Set<String> uniqueIds) {
		String modelId = ensureIdGiven( model );
		model.setId( modelId );
		if ( uniqueIds.contains( model.getId() ) ) {
			model.setId( model.getId() + ":" + model.getStart() + ":" + model.getEnd() );
		}
		uniqueIds.add( model.getId() );
	}
	
	/** This will make sure the model has an ID, and return it. */
	private String ensureIdGiven(Gff3GenericModel model) {
		String nextModelId = model.getId();
		if ( nextModelId == null ) {
			model.setId( generateMockId() );
		}
		else {
			model.setId( nextModelId );
		}
		return nextModelId;
	}

	/** Establish relationships that can be deduced from the model's contained information. */
	private void makeAssociations( ModelTreeNode newNode ) throws BadModelException {
		// HERE: settle the relationships among the nodes: parent/child are the only ones as of now.
		Gff3GenericModel model = newNode.getModel();
		String[] parentIdArr = model.getParent();
		if ( parentIdArr == null  ||  parentIdArr.length == 0 ) {
			// If no parent, this is a top-level features.
			topLevelFeatures.add( newNode );
			
		}
		else {
			if ( ! multiParentedFeaturesAcceptable  &&  parentIdArr.length > 1 ) {
				throw new BadModelException( "Found multiple parent IDs found for " + model.getId() + " and that has been set unacceptable." );
			}
				
			// Set parentage for all....
			for ( String nextParentId: parentIdArr ) {
				ModelTreeNode parentNode = idVsNode.get( nextParentId );
				if ( parentNode == null ) {
					parentNode = new ModelTreeNode( nextParentId );
					idVsNode.put( model.getId(), parentNode );
				}
				parentNode.addChild( newNode );
				newNode.addParent( parentNode );
			}

		}
	}

	/** Make sure a node exists for this model.  Could be pre-existing, or may be created. */
	private ModelTreeNode getNodeForModel( Gff3GenericModel model,
			                               String nextModelId ) throws BadModelException {

		// HERE: settle the question of creation of the tree node for the model under study.
		// A model's node may have been specified by a previous addition of one of its children.
		ModelTreeNode node = idVsNode.get( nextModelId );
		if ( node != null ) {
			if ( node.getModel() != null ) {
				String message = "ID " + nextModelId + " not unique.  Dropping data for ID " + nextModelId;
				throw new BadModelException( message );
			}
			else {
				node.setModel( model );
			}
		}
		else {
			node = new ModelTreeNode( model );
			idVsNode.put( model.getId(), node );

		}
		return node;
	}

	/** Prepare means of reading the input, so models can be used. */
	private Gff3LineReader initReader() {
		// Seed the line reader for later use.
		try {
			rdr = new Gff3LineReader( new File(inputfile) );
			
		} catch ( Gff3LineReaderException glre ) {
			if ( rdr != null ) {
				rdr.close();
			}
			throw new RuntimeException( glre );
		}
		return rdr;
	}
	
	/** Axes are characterized by having either their ID's equal to their landmark ID's, or their IDs being empty. */
	private boolean isAxis( Gff3GenericModel model, List<String> existingModelIds ) {
		String landmarkId = model.getLandmarkId();
		String modelId = model.getId();
		boolean returnVal = false;
		if ( landmarkId.equals( modelId ) ) {
			returnVal = true;
		}
		if ( isEmpty( modelId ) ) {
			// Certain types can have no ID and still can NOT be axes.
			if ( ! model.getType().equalsIgnoreCase( "repeat_region" ) )
				returnVal = true;
		}
		// Don't add it more than once; first occurrence probably right.  Subsequent are alignments.
		if ( returnVal && existingModelIds.contains( model.getLandmarkId() ) ) {
			returnVal = false;
		}
		return returnVal;
	}
	
	private boolean isEmpty( String str ) {
		return ( str == null  ||  str.trim().length() == 0 );
	}
		
	/** "unique" id generator. */
	private String generateMockId() {
		return "Unknown_Feature_" + nextUnknownId++;
	}
	
	/** Throw this to indicate that a model should not make it back to the caller, due to flaws. */
	static class BadModelException extends Exception {
		public BadModelException( String message ) {
			super( message );
		}
	}
}
