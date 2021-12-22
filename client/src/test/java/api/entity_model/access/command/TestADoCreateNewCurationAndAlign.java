package api.entity_model.access.command;

import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.InvalidFeatureStructureException;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.geometry.Range;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import shared.util.PropertyConfigurator;
import shared.util.ThreadQueue;

import java.util.Properties;

import static api.entity_model.access.command.CommandTestEnvironment.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Order Dependency!  This test must be run early in the suite.
 * This is owing to the mock-detection in the statis ModelMgr mock failing
 * to cause the proper entity to be returned.
 * As other tests are added, changing the order like this may even prove insufficient.
 */
public class TestADoCreateNewCurationAndAlign {

    @Before
    public void setup() {
        Properties mockProps = new Properties();
        mockProps.setProperty("x.genomebrowser.ModelMgrProperties", "testprop");
        mockProps.setProperty("x.genomebrowser.ServerConnectionProperties", "testserverconprop");
        mockProps.setProperty("x.shared.PropertySettings", "/test_props.properties");
        PropertyConfigurator.add(mockProps);
    }

    @Test
    public void testExecuteNoUndo() {
        try (ExtendedStaticCommandTestEnvironment testEnvironment = new ExtendedStaticCommandTestEnvironment()) {
            // Order dependency!  This thread queue mock must be applied before the GenomicEntity _class_ is loaded.
            ThreadQueue loaderThreadQueue = Mockito.mock(ThreadQueue.class);
            given(testEnvironment.getModelMgr().getLoaderThreadQueue()).willReturn(loaderThreadQueue);

            final EntityType entityTypeInstance = Mockito.mock(EntityType.class);
            Axis.AxisMutator axisMutator = Mockito.mock(Axis.AxisMutator.class);
            Axis anAxis = makeAxis(axisMutator);
            OIDGenerator oidGeneratorInstance = Mockito.mock(OIDGenerator.class);
            OID newTranscriptOID = Mockito.mock(OID.class);
            GenomeVersion axisGenomeVersion = anAxis.getGenomeVersion();
            given(oidGeneratorInstance.generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, anAxis.getGenomeVersion().hashCode())).willReturn(newTranscriptOID);
            OID newExonOID = Mockito.mock(OID.class);
            given(oidGeneratorInstance.generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, anAxis.getGenomeVersion().getID())).willReturn(newExonOID);

            GeometricAlignment exonAlignment = Mockito.mock(GeometricAlignment.class);
            FeatureFacade featureFacade = Mockito.mock(FeatureFacade.class);
            OID[] evOIDs = new OID[] {
                    Mockito.mock(OID.class)
            };
            given(featureFacade.retrieveEvidence(any())).willReturn(evOIDs);
            CuratedExon curatedExon = makeCuratedExon(exonAlignment, featureFacade);
            given(testEnvironment.getEntityFactory().create(newExonOID, "Exon", entityTypeInstance, CURATION_DISC_ENV))
                    .willReturn(curatedExon);
            Range exonRangeOnAxis = Mockito.mock(Range.class);
            given(exonRangeOnAxis.getStart()).willReturn(1);
            given(exonAlignment.getRangeOnAxis()).willReturn(exonRangeOnAxis);

            testEnvironment.getEntityType()
                    .when(() -> EntityType.getEntityTypeForValue(EntityTypeConstants.NonPublic_Transcript))
                    .thenReturn(entityTypeInstance);
            testEnvironment.getEntityType()
                    .when(() -> EntityType.getEntityTypeForValue(EntityTypeConstants.Exon))
                    .thenReturn(entityTypeInstance);

            GeometricAlignment evidence = Mockito.mock(GeometricAlignment.class);
            Range evidenceRange = Mockito.mock(Range.class);
            given(evidence.getRangeOnAxis()).willReturn(evidenceRange);
            Range.Orientation orientation = Mockito.mock(Range.Orientation.class);
            given(evidence.getOrientationOnAxis()).willReturn(orientation);

            CuratedTranscript curatedTranscript = makeCuratedTranscript(entityTypeInstance, evidence, anAxis.getGenomeVersion());
            given(evidence.getEntity()).willReturn(curatedTranscript);
            given(testEnvironment.getEntityFactory().create(newTranscriptOID, "Transcript", entityTypeInstance, CURATION_DISC_ENV)).willReturn(curatedTranscript);

            testEnvironment.getOIDGenerator().when(OIDGenerator::getOIDGenerator).thenReturn(oidGeneratorInstance);

            given(evidence.getAxis()).willReturn(anAxis);

            DoCreateNewCurationAndAlign cmd = new DoCreateNewCurationAndAlign(evidence);
            assertEquals("Wrong number of root features before exec",0, cmd.getCommandSourceRootFeatures().size());
            assertTrue("Preconditions failed", throwsToFalse(cmd::validatePreconditions));

            given(testEnvironment.getModelMgr().getGenomeVersionContaining(any())).willReturn(axisGenomeVersion);
            PropertyMgr propMgrInstance = Mockito.mock(PropertyMgr.class);
            testEnvironment.getPropertyMgr().when(PropertyMgr::getPropertyMgr).thenReturn(propMgrInstance);

            assertTrue("Exception thrown by the command", throwsToFalse(cmd::executeWithNoUndo));
            assertEquals("Wrong number of result features after exec",1,
                    cmd.getCommandResultsRootFeatures().size());
        } catch (InvalidFeatureStructureException ifse) {
            ifse.printStackTrace();
            fail();
        } catch (NoData nd) {
            nd.printStackTrace();
            fail();
        }
    }
}
