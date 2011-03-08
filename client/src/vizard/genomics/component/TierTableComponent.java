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
package vizard.genomics.component;

import vizard.genomics.model.GenomicAxisViewModel;

import javax.swing.*;
import java.awt.*;


/**
 * The purpose of the TierTableComponent class is to provide
 * a reusable swing component for viewing rows of tiers in two columns
 * (a tier being an arbitrary grouping of genomic entities).
 *
 * Each row in the first column is the graphic visualization of a tier.
 * Each row in the second column shows the name of the tier.
 */
public class TierTableComponent extends JPanel
{
    private TiersComponent tiersComponent;
    private TierNamesComponent tierNamesComponent;

    public TierTableComponent(GenomicAxisViewModel axisViewModel) {
	super(new BorderLayout());

	tiersComponent = new TiersComponent(axisViewModel);
	tierNamesComponent = new TierNamesComponent(tiersComponent);
	add("Center", tiersComponent);
	add("East", tierNamesComponent);
    }

    public void setTierNamesWidth(int width) {
        if (width < 10)
            width = 10;
        if (getWidth() > 10 && width > getWidth() - 10)
            width = getWidth() - 10;
        tierNamesComponent.setPreferredSize(new Dimension(width, tierNamesComponent.getHeight()));
        tierNamesComponent.invalidate();
        if (getParent() != null &&
            getParent().getParent() != null &&
            getParent().getParent().getParent() != null)
            getParent().getParent().getParent().validate();
    }

    /**
     * Return the component that renders the tiers.
     */
    public TiersComponent tiersComponent() {
	return tiersComponent;
    }

    /**
     * Return the component that renders the tier names.
     */
    public TierNamesComponent tierNamesComponent() {
	return tierNamesComponent;
    }

    public void reset() {
	tiersComponent.reset();
    }

    public void delete() {
	reset();
	tiersComponent.delete();
    }
}
