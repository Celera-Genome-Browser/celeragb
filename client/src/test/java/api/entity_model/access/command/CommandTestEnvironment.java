package api.entity_model.access.command;

import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.MutatorAccessController;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.*;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import shared.util.ThreadQueue;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

public class CommandTestEnvironment implements AutoCloseable {
    static final String CURATION_DISC_ENV = "Curation";

    private MockedStatic<ResourceBundle> resourceBundleStatic;
    private MockedStatic<FacadeManager> facadeManagerMockedStatic;
    private MockedStatic<ModelMgr> modelMgrStatic;

    private ModelMgr modelMgrInstance;
    private GenomicEntityFactory entityFactory;

    CommandTestEnvironment() {
        initResourceBundle();
        initFacadeManager();
        initModelMgr();
    }

    ModelMgr getModelMgr() {
        return modelMgrInstance;
    }

    public GenomicEntityFactory getEntityFactory() {
        return entityFactory;
    }

    @Override
    public void close() {
        try {
            resourceBundleStatic.close();
        } catch (Exception ex) {
            System.out.println("Error closing resource bundle");
            ex.printStackTrace();
        }
        try {
            facadeManagerMockedStatic.close();
        } catch (Exception ex) {
            System.out.println("Error closing facade manager");
            ex.printStackTrace();
        }
        try {
            modelMgrStatic.close();
        } catch (Exception ex) {
            System.out.println("Error closing model manager");
            ex.printStackTrace();
        }
    }

    static Axis mockAxis() {
        Axis axis = Mockito.mock(Axis.class);
        GenomeVersion genomeVersion = Mockito.mock(GenomeVersion.class);
        Workspace workspace = Mockito.mock(Workspace.class);
        given(genomeVersion.getWorkspace()).willReturn(workspace);
        given(axis.getGenomeVersion()).willReturn(genomeVersion);
        return axis;
    }

    static CuratedExon makeCuratedExon(GeometricAlignment exonAlignment) throws InvalidFeatureStructureException {
        return new CuratedExon(Mockito.mock(OID.class), "CuratedExon", Mockito.mock(EntityType.class), CURATION_DISC_ENV) {
            @Override
            public GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis() {
                return exonAlignment;
            }
        };
    }

    static CuratedExon makeCuratedExon(GeometricAlignment exonAlignment, FeatureFacade featureFacade) throws InvalidFeatureStructureException {
        return new CuratedExon(Mockito.mock(OID.class), "CuratedExon", Mockito.mock(EntityType.class), CURATION_DISC_ENV) {
            @Override
            public GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis() {
                return exonAlignment;
            }
            @Override
            public FeatureFacade getFeatureFacade() {
                return featureFacade;
            }
        };
    }

    static ComputedCodon makeComputedCodon(EntityType entityTypeInstance, GeometricAlignment computedCodonAlignment)
            throws InvalidFeatureStructureException {
        return new ComputedCodon(
                Mockito.mock(OID.class), "test-site", entityTypeInstance, CURATION_DISC_ENV, null,
                FeatureDisplayPriority.DEFAULT_PRIORITY
        ) {
            @Override
            public GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis() {
                return computedCodonAlignment;
            }
        };
    }

    static CuratedGene makeCuratedGene(EntityType entityTypeInstance, GeometricAlignment geneAlignment)
            throws InvalidFeatureStructureException {
        return new CuratedGene(Mockito.mock(OID.class), "test-curated-gene", entityTypeInstance, CURATION_DISC_ENV) {
            @Override
            public GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis() {
                return geneAlignment;
            }

            @Override
            public String toString() {
                return "Test Curated Gene";
            }
        };
    }

    static CuratedTranscript makeCuratedTranscript(
            EntityType entityTypeInstance, GeometricAlignment transcriptAlignment
    ) throws InvalidFeatureStructureException {
        return new CuratedTranscript(
                Mockito.mock(OID.class), "test-curated-transcript", entityTypeInstance, CURATION_DISC_ENV
        ) {
            @Override
            public GeometricAlignment getOnlyAlignmentToOnlyAxis() {
                return transcriptAlignment;
            }
            @Override
            public GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis() {
                return transcriptAlignment;
            }

            @Override
            public Set getAlignmentsToAxis(Axis axis) {
                Set<GeometricAlignment> alignments = new HashSet<>();
                alignments.add(transcriptAlignment);
                return alignments;
            }
        };
    }

    /** Overload required for creating and aligning. */
    static CuratedTranscript makeCuratedTranscript(
            EntityType entityTypeInstance, GeometricAlignment transcriptAlignment, GenomeVersion genomeVersion
    ) throws InvalidFeatureStructureException {
        return new CuratedTranscript(
                Mockito.mock(OID.class), "test-curated-transcript", entityTypeInstance, CURATION_DISC_ENV
        ) {
            @Override
            public GeometricAlignment getOnlyAlignmentToOnlyAxis() {
                return transcriptAlignment;
            }
            @Override
            public GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis() {
                return transcriptAlignment;
            }

            @Override
            public Set getAlignmentsToAxis(Axis axis) {
                Set<GeometricAlignment> alignments = new HashSet<>();
                alignments.add(transcriptAlignment);
                return alignments;
            }

            @Override
            public GenomeVersion getGenomeVersion() {
                return genomeVersion;
            }
        };
    }

    /** If exception thrown, return the exception. Expected behavior. */
    static boolean throwsToException(Exceptable r, Class clazz) {
        try {
            r.exec();
            return false;
        } catch (Exception e) {
            return clazz.isInstance(e);
        }
    }

    static boolean throwsToFalse(Exceptable r) {
        try {
            r.exec();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static Axis makeAxis(Axis.AxisMutator axisMutator) {
        OID axisOID = Mockito.mock(OID.class);
        return new GenomicAxis(axisOID, "The Axis") {
            private GenomeVersion genomeVersion = Mockito.mock(GenomeVersion.class);
            @Override
            protected GenomicEntityMutator constructMyMutator() {
                return axisMutator;
            }

            @Override
            public GenomeVersion getGenomeVersion() {
                Workspace workspace = Mockito.mock(Workspace.class);
                given(genomeVersion.getWorkspace()).willReturn(workspace);
                return genomeVersion;
            }
        };
    }

    private final void initResourceBundle() {
        resourceBundleStatic = Mockito.mockStatic(ResourceBundle.class);
        ResourceBundle modelMgrResourceBundle = Mockito.mock(ResourceBundle.class);
        given(modelMgrResourceBundle.getString("Factory")).willReturn("api.entity_model.management.StandardEntityFactory");
        resourceBundleStatic.when(() -> ResourceBundle.getBundle("testprop")).thenReturn(modelMgrResourceBundle);

        ResourceBundle annotationResourceBundle = Mockito.mock(ResourceBundle.class);
        given(annotationResourceBundle.getKeys()).willReturn(Collections.enumeration(Arrays.asList()));
        resourceBundleStatic.when(() -> ResourceBundle.getBundle("resource.shared.DataLayerToFeatureGroup")).thenReturn(annotationResourceBundle);

        ResourceBundle serverConResourceBundle = Mockito.mock(ResourceBundle.class);
        given(serverConResourceBundle.getString("HttpServer")).willReturn("localhost");
        resourceBundleStatic.when(() -> ResourceBundle.getBundle("testserverconprop")).thenReturn(serverConResourceBundle);

    }

    private final void initFacadeManager() {
        facadeManagerMockedStatic = Mockito.mockStatic(FacadeManager.class);
        FacadeManagerBase facadeManagerInstance = Mockito.mock(FacadeManagerBase.class);
        facadeManagerMockedStatic.when(() -> FacadeManager.getFacadeManager()).thenReturn(facadeManagerInstance);
    }

    private final void initModelMgr() {
        modelMgrStatic = Mockito.mockStatic(ModelMgr.class);

        modelMgrInstance = Mockito.mock(ModelMgr.class);
        modelMgrStatic.when(() -> ModelMgr.getModelMgr()).thenReturn(modelMgrInstance);

        entityFactory = Mockito.mock(GenomicEntityFactory.class);
        given(modelMgrInstance.getEntityFactory()).willReturn(entityFactory);

        MutatorAccessController accessController = Mockito.mock(MutatorAccessController.class);
        given(modelMgrInstance.getMutatorAccessController()).willReturn(accessController);
        given(accessController.allowAccessToMutator(any(), any())).willReturn(true);

        ThreadQueue threadQueue = Mockito.mock(ThreadQueue.class);
        given(modelMgrInstance.getNotificationQueue()).willReturn(threadQueue);
    }

    interface Exceptable {
        void exec() throws Exception;
    }

}
