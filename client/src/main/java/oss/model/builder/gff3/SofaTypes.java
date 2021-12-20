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
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Can help tell the category for a SOFA type; or tell the SOFA type for a category (or, here GFF3 data type).
 *
 * @author Leslie L Foster
 */
public class SofaTypes {
	private static final String MAPPING_FILE_NAME = "SOFA.txt";
	
	private Map<String,String> sofaToCategory;
	private Map<String,String> categoryToSofa;
	
	public SofaTypes() throws Exception {
		init();
	}
	
	public boolean isSofa( String type ) {
		return categoryToSofa.containsKey( type );
	}
	
	public String getCategoryForSofa( String type ) {
		return categoryToSofa.get( type );
	}
	
	public String getSofaForCategory( String sofa ) {
		return sofaToCategory.get( sofa );
	}
	
	private void init() throws Exception {
		sofaToCategory = new HashMap<String,String>();
		categoryToSofa = new HashMap<String,String>();
		
		InputStream is = this.getClass().getResourceAsStream( getMappingLocation() );
		BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
		String inline = null;
		
		while( null != ( inline = br.readLine() )) {
			if ( inline.startsWith( "#" ) ) {
				continue;
			}
			
			int firstSpacePos = inline.indexOf( ' ' );
			String sofaId = inline.substring( 0, firstSpacePos );
			String category = inline.substring( firstSpacePos + 1 );
			
			sofaToCategory.put( sofaId, category );
			categoryToSofa.put( category, sofaId );
		}

		br.close();
	}
	
	private String getMappingLocation() {
		String thisPackageName = getClass().getPackage().getName();
		thisPackageName = thisPackageName.replaceAll( "[.]", "/" );
		return thisPackageName + "/" + MAPPING_FILE_NAME;
	}
}
