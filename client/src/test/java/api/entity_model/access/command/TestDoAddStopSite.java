package api.entity_model.access.command;

import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.InvalidFeatureStructureException;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.geometry.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import shared.util.PropertyConfigurator;

import java.util.Arrays;
import java.util.Properties;

import static api.entity_model.access.command.CommandTestEnvironment.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TestDoAddStopSite {

    @Before
    public void setup() {
        Properties mockProps = new Properties();
        mockProps.setProperty("x.genomebrowser.ModelMgrProperties", "testprop");
        mockProps.setProperty("x.genomebrowser.ServerConnectionProperties", "testserverconprop");
        mockProps.setProperty("x.shared.PropertySettings", "/test_props.properties");
        PropertyConfigurator.add(mockProps);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteNoUndo() {
        try (CommandTestEnvironment cte = new CommandTestEnvironment();
             MockedStatic<EntityType> entityTypeStatic = Mockito.mockStatic(EntityType.class);
             MockedStatic<OIDGenerator> oidGeneratorMockedStatic = Mockito.mockStatic(OIDGenerator.class);
             MockedStatic<PropertyMgr> propertyMgrMockedStatic = Mockito.mockStatic(PropertyMgr.class)) {

            final EntityType entityTypeInstance = Mockito.mock(EntityType.class);
            entityTypeStatic.when(() -> EntityType.getEntityTypeForName(any())).thenReturn(entityTypeInstance);
            final GeometricAlignment computedCodonAlignment = Mockito.mock(GeometricAlignment.class);

            Axis.AxisMutator axisMutator = Mockito.mock(Axis.AxisMutator.class);
            final Axis anAxis = makeAxis(axisMutator);
            given(computedCodonAlignment.getAxis()).willReturn(anAxis);

            final Range onAxisRange = Mockito.mock(Range.class);
            given(computedCodonAlignment.getRangeOnAxis()).willReturn(onAxisRange);

            final CuratedTranscript transcript = Mockito.mock(CuratedTranscript.class);

            final GeometricAlignment exonAlignment = Mockito.mock(GeometricAlignment.class);
            final Range exonRange = Mockito.mock(Range.class);
            given(exonAlignment.getRangeOnAxis()).willReturn(exonRange);
            given(exonRange.contains(onAxisRange)).willReturn(true);

            given(transcript.getSubFeatures()).willReturn(Arrays.asList(makeCuratedExon(exonAlignment)));
            GenomicEntityFactory entityFactory = Mockito.mock(GenomicEntityFactory.class);

            CuratedCodon curatedCodon = new CuratedCodon(Mockito.mock(OID.class), "StartCodon", Mockito.mock(EntityType.class), CURATION_DISC_ENV);

            OIDGenerator oidGeneratorInstance = Mockito.mock(OIDGenerator.class);
            oidGeneratorMockedStatic.when(OIDGenerator::getOIDGenerator).thenReturn(oidGeneratorInstance);

            final GenomeVersion genomeVersion = anAxis.getGenomeVersion();
            given(transcript.getGenomeVersion()).willReturn(genomeVersion);

            OID generatedOID = Mockito.mock(OID.class);
            given(oidGeneratorInstance.generateScratchOIDForGenomeVersion(genomeVersion.hashCode())).willReturn(generatedOID);
            given(entityFactory.create(generatedOID, "StopCodon", null, CURATION_DISC_ENV, null, transcript, FeatureDisplayPriority.DEFAULT_PRIORITY)).willReturn(curatedCodon);

            given(cte.getModelMgr().getEntityFactory()).willReturn(entityFactory);
            PropertyMgr propMgrInstance = Mockito.mock(PropertyMgr.class);
            propertyMgrMockedStatic.when(PropertyMgr::getPropertyMgr).thenReturn(propMgrInstance);

            // Preconditions are that there is an exon whose range overlaps the start codon target location.
            DoAddStopSite cmd = new DoAddStopSite(anAxis, makeComputedCodon(entityTypeInstance, computedCodonAlignment), transcript);
            assertTrue("Preconditions failed", throwsToFalse(cmd::validatePreconditions));
            assertTrue("Exception thrown by the command", throwsToFalse(cmd::executeWithNoUndo));

            // Doing post-checks: did the command have the effect?
            verify(cte.getModelMgr(), times(0)).handleException(any());
            verify(propMgrInstance, times(1)).handleProperties(PropertyMgr.NEW_ENTITY, curatedCodon, false);
            verify(axisMutator, times(1)).addAlignmentToEntity(any());
        } catch (AlignmentNotAllowedException | InvalidFeatureStructureException ex) {
            ex.printStackTrace();
            fail();
        }
    }

}
