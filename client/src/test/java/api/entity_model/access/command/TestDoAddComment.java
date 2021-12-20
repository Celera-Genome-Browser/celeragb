package api.entity_model.access.command;

import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.InvalidFeatureStructureException;
import api.entity_model.model.fundtype.EntityType;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import shared.util.PropertyConfigurator;

import java.util.Properties;
import java.util.Set;

import static api.entity_model.access.command.CommandTestEnvironment.mockAxis;
import static api.entity_model.access.command.CommandTestEnvironment.throwsToFalse;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        try (CommandTestEnvironment cte = new CommandTestEnvironment()) {
            EntityType entityType = Mockito.mock(EntityType.class);
            OID featureOid = Mockito.mock(OID.class);

            Feature feature = new CuratedGene(featureOid, "test-curated-gene", entityType, "test-env");

            GenomicEntityComment comment = Mockito.mock(GenomicEntityComment.class);
            given(comment.getComment()).willReturn(TEST_COMMENT_STR);
            given(comment.toString()).willReturn(TEST_COMMENT_STR);

            DoAddComment cmd = new DoAddComment(mockAxis(), feature, comment);
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
            verify(cte.getModelMgr(), times(0)).handleException(any());

        } catch (InvalidFeatureStructureException ifse) {
            ifse.printStackTrace();
            fail();
        }
    }

    private Object getCmdRoot(DoAddComment cmd) {
        return cmd.getCommandSourceRootFeatures().stream().findFirst().get();
    }

}
