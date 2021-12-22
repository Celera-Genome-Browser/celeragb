package api.entity_model.access.command;

import api.entity_model.management.CommandPreconditionException;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.InvalidFeatureStructureException;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.geometry.Range;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import shared.util.PropertyConfigurator;

import java.util.*;

import static api.entity_model.access.command.CommandTestEnvironment.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestDoDetachTranscriptFromGene {

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
        try (ExtendedStaticCommandTestEnvironment testEnvironment = new ExtendedStaticCommandTestEnvironment()) {

            final EntityType entityTypeInstance = Mockito.mock(EntityType.class);
            testEnvironment.getEntityType()
                    .when(() -> EntityType.getEntityTypeForName(any())).thenReturn(entityTypeInstance);
            final GeometricAlignment childEntityAlignment = Mockito.mock(GeometricAlignment.class);

            Axis.AxisMutator axisMutator = Mockito.mock(Axis.AxisMutator.class);
            final Axis anAxis = makeAxis(axisMutator);
            given(childEntityAlignment.getAxis()).willReturn(anAxis);

            // Setup for pre-validation.
            final Range onAxisRange = Mockito.mock(Range.class);
            given(childEntityAlignment.getRangeOnAxis()).willReturn(onAxisRange);

            OIDGenerator oidGeneratorInstance = Mockito.mock(OIDGenerator.class);
            testEnvironment.getOIDGenerator().when(OIDGenerator::getOIDGenerator).thenReturn(oidGeneratorInstance);

            final GenomeVersion genomeVersion = anAxis.getGenomeVersion();

            OID generatedOID = Mockito.mock(OID.class);
            given(oidGeneratorInstance.generateScratchOIDForGenomeVersion(genomeVersion.hashCode())).willReturn(generatedOID);
            PropertyMgr propMgrInstance = Mockito.mock(PropertyMgr.class);
            testEnvironment.getPropertyMgr().when(PropertyMgr::getPropertyMgr).thenReturn(propMgrInstance);

            final GeometricAlignment parentEntityAlignment = Mockito.mock(GeometricAlignment.class);
            given(parentEntityAlignment.getAxis()).willReturn(anAxis);
            CuratedGene gene = makeCuratedGene(entityTypeInstance, parentEntityAlignment);

            final CuratedTranscript transcript = makeCuratedTranscript(entityTypeInstance, childEntityAlignment);
            DoAttachTranscriptToGene preCmd = new DoAttachTranscriptToGene(
                    anAxis, gene, new ArrayList(Arrays.asList(transcript))
            );
            try {
                preCmd.executeWithNoUndo();
            } catch (Exception ex) {
                fail("Failed to setup test with detachable transcript");
            }
            assertTrue("Transcript was not on gene before detaching",
                    gene.getSubFeatures().stream().findAny().get().equals(transcript));

            DoDetachTranscriptFromGene cmd = new DoDetachTranscriptFromGene(anAxis, transcript);
            assertEquals("No root feature prior to exec",1, cmd.getCommandSourceRootFeatures().size());

            assertTrue("Preconditions failed", throwsToFalse(cmd::validatePreconditions));
            assertTrue("Exception thrown by the command", throwsToFalse(cmd::executeWithNoUndo));
            assertEquals("No result root features after exec",1, cmd.getCommandResultsRootFeatures().size());

            // Doing post-checks: did the command have the effect?
            verify(testEnvironment.getModelMgr(), times(0)).handleException(any());
            verify(propMgrInstance, times(2)).handleProperties(PropertyMgr.UPDATE_ENTITY, gene, false);
            assertEquals("Subfeatures remain after removing", 0, gene.getSubFeatures().size());
        } catch (InvalidFeatureStructureException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteNoUndo_failedPreCond() {
        try (ExtendedStaticCommandTestEnvironment testEnvironment = new ExtendedStaticCommandTestEnvironment()) {

            final EntityType entityTypeInstance = Mockito.mock(EntityType.class);
            testEnvironment.getEntityType()
                    .when(() -> EntityType.getEntityTypeForName(any())).thenReturn(entityTypeInstance);
            final GeometricAlignment childEntityAlignment = Mockito.mock(GeometricAlignment.class);

            Axis.AxisMutator axisMutator = Mockito.mock(Axis.AxisMutator.class);
            final Axis anAxis = makeAxis(axisMutator);
            given(childEntityAlignment.getAxis()).willReturn(anAxis);

            // Setup for pre-validation.
            final Range onAxisRange = Mockito.mock(Range.class);
            given(childEntityAlignment.getRangeOnAxis()).willReturn(onAxisRange);

            OIDGenerator oidGeneratorInstance = Mockito.mock(OIDGenerator.class);
            testEnvironment.getOIDGenerator().when(OIDGenerator::getOIDGenerator).thenReturn(oidGeneratorInstance);

            final GenomeVersion genomeVersion = anAxis.getGenomeVersion();

            OID generatedOID = Mockito.mock(OID.class);
            given(oidGeneratorInstance.generateScratchOIDForGenomeVersion(genomeVersion.hashCode())).willReturn(generatedOID);
            PropertyMgr propMgrInstance = Mockito.mock(PropertyMgr.class);
            testEnvironment.getPropertyMgr().when(PropertyMgr::getPropertyMgr).thenReturn(propMgrInstance);

            final CuratedTranscript transcript = makeCuratedTranscript(entityTypeInstance, childEntityAlignment);
            DoDetachTranscriptFromGene cmd = new DoDetachTranscriptFromGene(anAxis, transcript);

            assertTrue("Preconditions not enforced", throwsToException(cmd::validatePreconditions, CommandPreconditionException.class));

        } catch (InvalidFeatureStructureException ex) {
            ex.printStackTrace();
            fail();
        }
    }

}
