package api.entity_model.access.command;

import api.entity_model.management.ModelMgr;
import api.entity_model.management.MutatorAccessController;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.InvalidFeatureStructureException;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import shared.util.PropertyConfigurator;
import shared.util.ThreadQueue;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({DoAddComment.class})
public class TestDoAddComment {

    private static final String TEST_COMMENT_STR = "test-comment";
    private static final String COMMENT_TEST_FMT = "Added Comment =%s on Entity %s id %s";

    @Before
    public void setup() {
        Properties mockProps = new Properties();
        mockProps.setProperty("x.genomebrowser.ModelMgrProperties", "testprop");
        mockProps.setProperty("x.genomebrowser.ServerConnectionProperties", "testserverconprop");
        PropertyConfigurator.add(mockProps);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteNoUndo() {
        try (MockedStatic<ResourceBundle> resourceBundleStatic = Mockito.mockStatic(ResourceBundle.class)) {
            ResourceBundle modelMgrResourceBundle = Mockito.mock(ResourceBundle.class);
            given(modelMgrResourceBundle.getString("Factory")).willReturn("api.entity_model.management.StandardEntityFactory");
            resourceBundleStatic.when(() -> ResourceBundle.getBundle("testprop")).thenReturn(modelMgrResourceBundle);

            ResourceBundle serverConResourceBundle = Mockito.mock(ResourceBundle.class);
            given(serverConResourceBundle.getString("HttpServer")).willReturn("localhost");
            resourceBundleStatic.when(() -> ResourceBundle.getBundle("testserverconprop")).thenReturn(serverConResourceBundle);

            ResourceBundle annotationResourceBundle = Mockito.mock(ResourceBundle.class);
            given(annotationResourceBundle.getKeys()).willReturn(Collections.enumeration(Arrays.asList()));
            resourceBundleStatic.when(() -> ResourceBundle.getBundle("resource.shared.DataLayerToFeatureGroup")).thenReturn(annotationResourceBundle);

            try (MockedStatic<FacadeManager> facadeManagerMockedStatic = Mockito.mockStatic(FacadeManager.class)) {
                FacadeManagerBase facadeManagerInstance = Mockito.mock(FacadeManagerBase.class);
                facadeManagerMockedStatic.when(() -> FacadeManager.getFacadeManager()).thenReturn(facadeManagerInstance);

                try (MockedStatic<ModelMgr> modelMgr = Mockito.mockStatic(ModelMgr.class)) {

                    ModelMgr modelMgrInstance = Mockito.mock(ModelMgr.class);
                    modelMgr.when(() -> ModelMgr.getModelMgr())
                            .thenReturn(modelMgrInstance);

                    MutatorAccessController accessController = Mockito.mock(MutatorAccessController.class);
                    given(modelMgrInstance.getMutatorAccessController()).willReturn(accessController);
                    given(accessController.allowAccessToMutator(any(), any())).willReturn(true);

                    ThreadQueue threadQueue = Mockito.mock(ThreadQueue.class);
                    given(modelMgrInstance.getNotificationQueue()).willReturn(threadQueue);

                    EntityType entityType = Mockito.mock(EntityType.class);
                    OID featureOid = Mockito.mock(OID.class);

                    Feature feature = new CuratedGene(featureOid, "test-curated-gene", entityType, "test-env");
                    Axis axis = Mockito.mock(Axis.class);
                    GenomeVersion genomeVersion = Mockito.mock(GenomeVersion.class);
                    Workspace workspace = Mockito.mock(Workspace.class);
                    given(genomeVersion.getWorkspace()).willReturn(workspace);
                    given(axis.getGenomeVersion()).willReturn(genomeVersion);

                    GenomicEntityComment comment = Mockito.mock(GenomicEntityComment.class);
                    given(comment.getComment()).willReturn(TEST_COMMENT_STR);
                    given(comment.toString()).willReturn(TEST_COMMENT_STR);

                    DoAddComment cmd = new DoAddComment(axis, feature, comment);
                    // Assignment to generic type of bare type is "unsafe operation".  Carrying that out
                    // as part of the test in anticipation of moving to generics.
                    Set<Feature> rootFeatures = cmd.getCommandSourceRootFeatures();
                    assertTrue("No root features returned prior to command exec",rootFeatures.size() == 1);
                    assertEquals("Different root before command", getCmdRoot(cmd), feature);
                    assertTrue("Exception thrown by the command", throwsToFalse(() -> cmd.executeWithNoUndo()));
                    assertEquals("Different root after command", getCmdRoot(cmd), feature);

                    assertEquals("Result occurred on different feature", cmd.getCommandResultsRootFeatures().stream().findFirst().get(), feature);
                    assertTrue(cmd.getCommandLogMessage().contains(String.format(
                        COMMENT_TEST_FMT, TEST_COMMENT_STR, feature.getEntityType().toString(), feature.getOid()))
                    );
                    // Assignment to generic type from bare type.  Unsafe.  Part of test.
                    final Set<GenomicEntityComment> loadedComments = feature.getLoadedComments();

                    // Finally:  Did the change actually happen?
                    assertTrue("Added comment not found in feature comments",
                            loadedComments.stream().anyMatch(lc -> lc.getComment().equals(TEST_COMMENT_STR)));

                } catch (InvalidFeatureStructureException ifse) {
                    ifse.printStackTrace();
                    fail();
                }
            }
        }
    }

    private Object getCmdRoot(DoAddComment cmd) {
        return cmd.getCommandSourceRootFeatures().stream().findFirst().get();
    }

    private boolean throwsToFalse(Exceptable r) {
        try {
            r.exec();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private interface Exceptable {
        void exec() throws Exception;
    }

}
