# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# Copyright (c) 1999 - 2006 Applera Corporation.
# 301 Merritt 7 
# P.O. Box 5435 
# Norwalk, CT 06856-5435 USA
#
# This is free software; you can redistribute it and/or modify it under the 
# terms of the GNU Lesser General Public License as published by the 
# Free Software Foundation; version 2.1 of the License.
#
# This software is distributed in the hope that it will be useful, but 
# WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE. 
# See the GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License 
# along with this software; if not, write to the Free Software Foundation, Inc.
# 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
#
# FILE:    stitch.pl
#
# AUTHOR:  L. Foster, 7/27/2000
#
# USAGE:   perl stitch.pl <GAME XML FILE>
#
# SYNOPSIS:  Use this file to turn the residues characters in a GAME XML file
#          into a single joined string.
#
# PURPOSE: The XML parser used by the Genome Browser at time of writing, has
#          a flaw whereby if the residues are spread across many lines, the production
#          of the Document Object Model's Document object takes an inordinately long time.
#          This script will eliminate this problem.  However, once this has been addressed
#          in the parser, this script should not be used.
#
local($inline);
local($in_residues_state) = 0;
local($line_accumulator) = '';

#
#  Read all lines of input from the "standard source".
#
while($inline=<>) {

    # Get into the proper "state" based on containing tag.
    #
    if (index($inline, "<residues") > -1) {
        $in_residues_state = 1;
    }

    # Switch out of the residue content state.  Write back the accumulated residues.
    #
    if (index($inline, "</residues>") > -1) {
        printf "%s\n", $line_accumulator;
        $in_residues_state = 0;
    }

    # If we are in the residues state (i.e., between start and end tag),
    # collect the residue letters into a single string.
    #
    if ($in_residues_state == 1) {
        chop($inline);
        $inline =~ s/\t//g;
        $line_accumulator .= $inline;
    }
    else {
        print $inline;
    }

}
