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
package api.entity_model.model.annotation;

import api.entity_model.model.fundtype.EntityType;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.List;


/**
 * Title:        GBAPI Server Project
 * Description:
 * Company:      []
 * @author Michael D. Simpson
 * @version 1.0
 */
public class PolyMorphism extends ComputedSingleAlignSingleAxisFeature {
    private short polyMutationType = PolyMutationType.UNKNOWN_SHORT;
    private short validationStatus = ValidationStatus.UNKNOWN_SHORT;
    private short[] functionalDomains = new short[0];
    private List alleleList = new ArrayList();

    /**
     * Minimal Constructor
     */
    public PolyMorphism(OID oid, String displayName, EntityType type, 
                        String discoveryEnvironment)
                 throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    /**
     * Constructor
     */
    public PolyMorphism(OID oid, String displayName, EntityType type, 
                        String discoveryEnvironment, 
                        FacadeManagerBase readFacadeManager, 
                        Feature superFeature, byte displayPriority)
                 throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
             superFeature, displayPriority, ValidationStatus.UNKNOWN_SHORT, 
             null);
    }

    /**
     * Constructor with full args.
     */
    public PolyMorphism(OID oid, String displayName, EntityType type, 
                        String discoveryEnvironment, 
                        FacadeManagerBase readFacadeManager, 
                        Feature superFeature, byte displayPriority, 
                        short validationStatus, short[] funcDomainIds)
                 throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
        this.validationStatus = validationStatus;
        this.functionalDomains = funcDomainIds;
    }

    public String getPolyMutationType() {
        return (PolyMutationType.convertPolyMutationType(this.polyMutationType));
    }

    public void setPolyMutationType(short polyMutationTypeId) {
        this.polyMutationType = polyMutationTypeId;
    }

    public void setPolyMutationType(String polyMutationTypeName) {
        this.polyMutationType = PolyMutationType.convertPolyMutationType(
                                        polyMutationTypeName);
    }

    public String getValidationStatus() {
        return (ValidationStatus.convertValidationStatus(this.validationStatus));
    }

    public void setValidationStatus(short validStatusId) {
        this.validationStatus = validStatusId;
    }

    public void setValidationStatus(String validStatusName) {
        this.validationStatus = ValidationStatus.convertValidationStatus(
                                        validStatusName);
    }

    public String getHighestRankingFunctionalDomain() {
        for (int rankIndex = 0;
             rankIndex < FunctionalDomain.RANKED_DOMAINS.length;
             rankIndex++) {
            for (int domainIndex = 0;
                 domainIndex < functionalDomains.length;
                 domainIndex++) {
                if (functionalDomains[domainIndex] == FunctionalDomain.RANKED_DOMAINS[rankIndex]) {
                    return (FunctionalDomain.convertFunctionalDomain(
                                   functionalDomains[domainIndex]));
                }
            }
        }

        return (FunctionalDomain.UNKNOWN);
    }

    public String[] getFunctionalDomains() {
        String[] domainNames = new String[0];

        for (short domainIndex = 0;
             domainIndex < functionalDomains.length;
             domainIndex++) {
            domainNames[domainIndex] = FunctionalDomain.convertFunctionalDomain(
                                               this.functionalDomains[domainIndex]);
        }

        return domainNames;
    }

    public void setFunctionalDomains(short[] funcDomainIds) {
        this.functionalDomains = funcDomainIds;
    }

    public void setFunctionalDomains(String[] funcDomainNames) {
        if ((this.functionalDomains == null) || 
                (this.functionalDomains.length == 0)) {
            this.functionalDomains = new short[funcDomainNames.length];
        }

        for (short domainIndex = 0;
             domainIndex < funcDomainNames.length;
             domainIndex++) {
            this.functionalDomains[domainIndex] = FunctionalDomain.convertFunctionalDomain(
                                                          funcDomainNames[domainIndex]);
        }
    }

    public List getAlleleList() {
        if (alleleList == null) {
            return (new ArrayList());
        }

        return alleleList;
    }

    public void addAllele(String newAllele) {
        if (alleleList == null) {
            alleleList = new ArrayList(4);
        }

        this.alleleList.add(newAllele);
    }

    /**
     * Polymorphism mutation type constants.
     */
    public static class PolyMutationType {
        public static final String UNKNOWN = "Unknown";
        public static final String SUBSTITUTION = "Substitution";
        public static final String INSERTION = "Insertion";
        public static final String DELETION = "Deletion";
        public static final short UNKNOWN_SHORT = -1;
        public static final short SUBSTITUTION_SHORT = 1;
        public static final short INSERTION_SHORT = 2;
        public static final short DELETION_SHORT = 3;

        public static String convertPolyMutationType(short mutationTypeId) {
            String mutationTypeName = UNKNOWN;

            if (mutationTypeId == SUBSTITUTION_SHORT) {
                mutationTypeName = SUBSTITUTION;
            } else if (mutationTypeId == INSERTION_SHORT) {
                mutationTypeName = INSERTION;
            } else if (mutationTypeId == DELETION_SHORT) {
                mutationTypeName = DELETION;
            }

            return mutationTypeName;
        }

        public static short convertPolyMutationType(String mutationTypeName) {
            short mutationTypeId = UNKNOWN_SHORT;

            if (mutationTypeName.equalsIgnoreCase(SUBSTITUTION)) {
                mutationTypeId = SUBSTITUTION_SHORT;
            } else if (mutationTypeName.equalsIgnoreCase(INSERTION)) {
                mutationTypeId = INSERTION_SHORT;
            } else if (mutationTypeName.equalsIgnoreCase(DELETION)) {
                mutationTypeId = DELETION_SHORT;
            }

            return mutationTypeId;
        }
    }

    /**
     * Validation status constants.
     */
    public static class ValidationStatus {
        public static final String UNKNOWN = "Unknown";
        public static final String COMPUTATIONAL = "Computational";
        public static final String VALIDATED = "Validated";
        public static final short UNKNOWN_SHORT = 0;
        public static final short COMPUTATIONAL_SHORT = 1;
        public static final short VALIDATED_SHORT = 2;

        public static String convertValidationStatus(short validStatusId) {
            String validStatusName = UNKNOWN;

            if (validStatusId == VALIDATED_SHORT) {
                validStatusName = VALIDATED;
            } else if (validStatusId == COMPUTATIONAL_SHORT) {
                validStatusName = COMPUTATIONAL;
            }

            return validStatusName;
        }

        public static short convertValidationStatus(String validStatusName) {
            short validStatusId = UNKNOWN_SHORT;

            if (validStatusName.equalsIgnoreCase(VALIDATED)) {
                validStatusId = VALIDATED_SHORT;
            } else if (validStatusName.equalsIgnoreCase(COMPUTATIONAL)) {
                validStatusId = COMPUTATIONAL_SHORT;
            }

            return validStatusId;
        }
    }

    /**
     * Functional domain constants.
     */
    public static class FunctionalDomain {
        public static final String UNKNOWN = "Unknown";
        public static final String STOP_CODON = "Stop Codon";
        public static final String SILENT = "Silent Mutation";
        public static final String MISSENSE = "Mis-sense Mutation";
        public static final String NONSENSE = "Nonsense Mutation";
        public static final String UTR_5 = "UTR 5";
        public static final String UTR_3 = "UTR 3";
        public static final String INTRON = "Intron";
        public static final String INTERGENIC = "Intergenic/Unknown";
        public static final short UNKNOWN_SHORT = -1;
        public static final short STOP_CODON_SHORT = 1;
        public static final short SILENT_SHORT = 2;
        public static final short MISSENSE_SHORT = 3;
        public static final short NONSENSE_SHORT = 4;
        public static final short UTR_5_SHORT = 5;
        public static final short UTR_3_SHORT = 6;
        public static final short INTRON_SHORT = 7;
        public static final short INTERGENIC_SHORT = 8;

        /**
         * Table to find highest rank of any functional domain.
         */
        public static final short[] RANKED_DOMAINS = new short[] {
            NONSENSE_SHORT, MISSENSE_SHORT, STOP_CODON_SHORT, SILENT_SHORT, 


            // Not represented: CODING_REGION_SHORT,
            UTR_5_SHORT, UTR_3_SHORT, INTRON_SHORT, INTERGENIC_SHORT, 
            UNKNOWN_SHORT
        };

        public static String convertFunctionalDomain(short funcDomainId) {
            String funcDomainName = UNKNOWN;

            switch (funcDomainId) {
            case STOP_CODON_SHORT:
                funcDomainName = STOP_CODON;

                break;

            case SILENT_SHORT:
                funcDomainName = SILENT;

                break;

            case MISSENSE_SHORT:
                funcDomainName = MISSENSE;

                break;

            case NONSENSE_SHORT:
                funcDomainName = NONSENSE;

                break;

            case UTR_5_SHORT:
                funcDomainName = UTR_5;

                break;

            case UTR_3_SHORT:
                funcDomainName = UTR_3;

                break;

            case INTRON_SHORT:
                funcDomainName = INTRON;

                break;

            case INTERGENIC_SHORT:
                funcDomainName = INTERGENIC;

                break;
            }

            return funcDomainName;
        }

        public static short convertFunctionalDomain(String funcDomainName) {
            short funcDomainId = UNKNOWN_SHORT;

            if (funcDomainName.equals(STOP_CODON)) {
                funcDomainId = STOP_CODON_SHORT;
            } else if (funcDomainName.equals(SILENT)) {
                funcDomainId = SILENT_SHORT;
            } else if (funcDomainName.equals(MISSENSE)) {
                funcDomainId = MISSENSE_SHORT;
            } else if (funcDomainName.equals(NONSENSE)) {
                funcDomainId = NONSENSE_SHORT;
            } else if (funcDomainName.equals(UTR_5)) {
                funcDomainId = UTR_5_SHORT;
            } else if (funcDomainName.equals(UTR_3)) {
                funcDomainId = UTR_3_SHORT;
            } else if (funcDomainName.equals(INTRON)) {
                funcDomainId = INTRON_SHORT;
            } else if (funcDomainName.equals(INTERGENIC)) {
                funcDomainId = INTERGENIC_SHORT;
            }

            return funcDomainId;
        }
    }
}