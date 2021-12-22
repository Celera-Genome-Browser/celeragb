package api.entity_model.access.command;

import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.fundtype.EntityType;
import api.stub.data.OIDGenerator;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ExtendedStaticCommandTestEnvironment implements AutoCloseable {
    MockedStatic<EntityType> entityTypeStatic;
    MockedStatic<OIDGenerator> oidGeneratorMockedStatic;
    MockedStatic<PropertyMgr> propertyMgrMockedStatic;

    private CommandTestEnvironment commandTestEnvironment;

    ExtendedStaticCommandTestEnvironment() {
        commandTestEnvironment = new CommandTestEnvironment();
        entityTypeStatic = Mockito.mockStatic(EntityType.class);
        oidGeneratorMockedStatic = Mockito.mockStatic(OIDGenerator.class);
        propertyMgrMockedStatic = Mockito.mockStatic(PropertyMgr.class);
    }

    ModelMgr getModelMgr() {
        return commandTestEnvironment.getModelMgr();
    }

    MockedStatic<EntityType> getEntityType() {
        return entityTypeStatic;
    }

    GenomicEntityFactory getEntityFactory() {
        return commandTestEnvironment.getEntityFactory();
    }

    MockedStatic<OIDGenerator> getOIDGenerator() {
        return oidGeneratorMockedStatic;
    }

    MockedStatic<PropertyMgr> getPropertyMgr() {
        return propertyMgrMockedStatic;
    }

    @Override
    public void close() {
        try {
            propertyMgrMockedStatic.close();
        } catch (Exception ex) {
            System.out.println("Failed to close prop mgr mock");
            ex.printStackTrace();
        }

        try {
            oidGeneratorMockedStatic.close();
        } catch (Exception ex) {
            System.out.println("Failed to close OID Generator mock");
            ex.printStackTrace();
        }

        try {
            entityTypeStatic.close();
        } catch (Exception ex) {
            System.out.println("Failed to close entity type mock");
            ex.printStackTrace();
        }
        commandTestEnvironment.close();
    }
}
