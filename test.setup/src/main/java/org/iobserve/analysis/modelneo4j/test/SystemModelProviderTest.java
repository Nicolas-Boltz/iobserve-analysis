/***************************************************************************
 * Copyright (C) 2017 iObserve Project (https://www.iobserve-devops.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package org.iobserve.analysis.modelneo4j.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.iobserve.analysis.modelneo4j.Graph;
import org.iobserve.analysis.modelneo4j.GraphLoader;
import org.iobserve.analysis.modelneo4j.ModelProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.io.fs.FileUtils;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.Connector;
import org.palladiosimulator.pcm.system.System;

/**
 * Test cases for the model provider using a System model.
 *
 * @author Lars Bluemke
 *
 */
public class SystemModelProviderTest implements IModelProviderTest {

    private static final File GRAPH_DIR = new File("/Users/LarsBlumke/Desktop/testdb");
    private static final Graph GRAPH = new GraphLoader(SystemModelProviderTest.GRAPH_DIR).getSystemModelGraph();

    private final Neo4jEqualityHelper equalityHelper = new Neo4jEqualityHelper();

    @Override
    @Before
    public void clearGraph() {
        new ModelProvider<>(SystemModelProviderTest.GRAPH).clearGraph();
    }

    @Override
    @Test
    public void createThenCloneThenRead() {
        final ModelProvider<System> modelProvider1 = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final ModelProvider<System> modelProvider2;
        final System writtenModel = new TestModelBuilder().getSystem();
        final System readModel;
        final Graph graph2;

        modelProvider1.createComponent(writtenModel);

        graph2 = modelProvider1.cloneNewGraphVersion(System.class);
        modelProvider2 = new ModelProvider<>(graph2);

        readModel = modelProvider2.readOnlyRootComponent(System.class);
        graph2.getGraphDatabaseService().shutdown();

        Assert.assertTrue(this.equalityHelper.equals(writtenModel, readModel));
    }

    @Override
    @Test
    public void createThenClearGraph() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();

        modelProvider.createComponent(writtenModel);

        Assert.assertFalse(IModelProviderTest.isGraphEmpty(modelProvider));

        modelProvider.clearGraph();

        Assert.assertTrue(IModelProviderTest.isGraphEmpty(modelProvider));
    }

    @Override
    @Test
    public void createThenReadById() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();
        final System readModel;

        modelProvider.createComponent(writtenModel);
        readModel = modelProvider.readOnlyComponentById(System.class, writtenModel.getId());

        Assert.assertTrue(this.equalityHelper.equals(writtenModel, readModel));
    }

    @Override
    @Test
    public void createThenReadByName() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();
        final List<System> readModels;

        modelProvider.createComponent(writtenModel);
        readModels = modelProvider.readOnlyComponentByName(System.class, writtenModel.getEntityName());

        for (final System readModel : readModels) {
            Assert.assertTrue(this.equalityHelper.equals(writtenModel, readModel));
        }
    }

    @Override
    @Test
    public void createThenReadByType() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();
        final List<String> readIds;

        modelProvider.createComponent(writtenModel);
        readIds = modelProvider.readComponentByType(System.class);

        for (final String readId : readIds) {
            Assert.assertTrue(writtenModel.getId().equals(readId));
        }

    }

    @Override
    @Test
    public void createThenReadRoot() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();
        final System readModel;

        modelProvider.createComponent(writtenModel);
        readModel = modelProvider.readOnlyRootComponent(System.class);

        Assert.assertTrue(this.equalityHelper.equals(writtenModel, readModel));
    }

    @Override
    @Test
    public void createThenReadContaining() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();
        final System readModel;
        final AssemblyContext ac = writtenModel.getAssemblyContexts__ComposedStructure().get(0);

        modelProvider.createComponent(writtenModel);
        readModel = (System) modelProvider.readOnlyContainingComponentById(AssemblyContext.class, ac.getId());

        Assert.assertTrue(this.equalityHelper.equals(writtenModel, readModel));
    }

    @Override
    @Test
    public void createThenReadReferencing() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();
        AssemblyContext businessOrderContext = null;
        AssemblyConnector businessPayConnector = null;
        List<EObject> readReferencingComponents;

        for (final AssemblyContext ac : writtenModel.getAssemblyContexts__ComposedStructure()) {
            if (ac.getEntityName().equals("busisnessOrderContext_org.mybookstore.orderComponent")) {
                businessOrderContext = ac;
            }
        }

        for (final Connector c : writtenModel.getConnectors__ComposedStructure()) {
            if (c.getEntityName().equals("businessPayment")) {
                businessPayConnector = (AssemblyConnector) c;
            }
        }

        modelProvider.createComponent(writtenModel);

        readReferencingComponents = modelProvider.readOnlyReferencingComponentsById(AssemblyContext.class,
                businessOrderContext.getId());

        // Only the businessPay connector is referencing the businessOrder context
        Assert.assertTrue(readReferencingComponents.size() == 1);

        Assert.assertTrue(this.equalityHelper.equals(businessPayConnector, readReferencingComponents.get(0)));

    }

    @Override
    @Test
    public void createThenUpdateThenReadUpdated() {
        // final ModelProvider<System> modelProvider = new
        // ModelProvider<>(SystemModelProviderTest.GRAPH);
        // final System writtenModel = new TestModelBuilder().getSystem();
        // Interface payInterface = null;
        // SystemComponent paymentComponent = null;
        // System readModel;
        //
        // modelProvider.createComponent(writtenModel);
        //
        // // Update the model by renaming and replacing payment the method
        // writtenModel.setEntityName("MyVideoOnDemandService");
        //
        // for (final Interface i : writtenModel.getInterfaces__System()) {
        // if (i.getEntityName().equals("IPay")) {
        // payInterface = i;
        // }
        // }
        //
        // for (final SystemComponent c : writtenModel.getComponents__System()) {
        // if (c.getEntityName().equals("org.mybookstore.paymentComponent")) {
        // paymentComponent = c;
        // }
        // }
        //
        // final OperationProvidedRole providedPayOperation =
        // SystemFactory.eINSTANCE.createOperationProvidedRole();
        // providedPayOperation.setEntityName("payPalPayment");
        // providedPayOperation.setProvidedInterface__OperationProvidedRole((OperationInterface)
        // payInterface);
        //
        // paymentComponent.getProvidedRoles_InterfaceProvidingEntity().clear();
        // paymentComponent.getProvidedRoles_InterfaceProvidingEntity().add(providedPayOperation);
        //
        // modelProvider.updateComponent(System.class, writtenModel);
        //
        // readModel = modelProvider.readOnlyRootComponent(System.class);
        //
        // Assert.assertTrue(this.equalityHelper.equals(writtenModel, readModel));
    }

    @Override
    @Test
    public void createThenDeleteComponent() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();

        modelProvider.createComponent(writtenModel);

        Assert.assertFalse(IModelProviderTest.isGraphEmpty(modelProvider));

        modelProvider.deleteComponent(System.class, writtenModel.getId());

        // Manually delete the proxy nodes from the repository model
        try (Transaction tx = SystemModelProviderTest.GRAPH.getGraphDatabaseService().beginTx()) {
            SystemModelProviderTest.GRAPH.getGraphDatabaseService()
                    .execute("MATCH (n:OperationProvidedRole) DELETE (n)");
            SystemModelProviderTest.GRAPH.getGraphDatabaseService()
                    .execute("MATCH (n:OperationRequiredRole) DELETE (n)");
            tx.success();
        }

        Assert.assertTrue(IModelProviderTest.isGraphEmpty(modelProvider));
    }

    @Override
    @Test
    public void createThenDeleteComponentAndDatatypes() {
        final ModelProvider<System> modelProvider = new ModelProvider<>(SystemModelProviderTest.GRAPH);
        final System writtenModel = new TestModelBuilder().getSystem();

        modelProvider.createComponent(writtenModel);

        Assert.assertFalse(IModelProviderTest.isGraphEmpty(modelProvider));

        modelProvider.deleteComponentAndDatatypes(System.class, writtenModel.getId());

        // Manually delete the proxy nodes from the repository model
        try (Transaction tx = SystemModelProviderTest.GRAPH.getGraphDatabaseService().beginTx()) {
            SystemModelProviderTest.GRAPH.getGraphDatabaseService()
                    .execute("MATCH (n:OperationProvidedRole) DELETE (n)");
            SystemModelProviderTest.GRAPH.getGraphDatabaseService()
                    .execute("MATCH (n:OperationRequiredRole) DELETE (n)");
            tx.success();
        }

        Assert.assertTrue(IModelProviderTest.isGraphEmpty(modelProvider));
    }

    @AfterClass
    public static void cleanUp() throws IOException {
        SystemModelProviderTest.GRAPH.getGraphDatabaseService().shutdown();
        FileUtils.deleteRecursively(SystemModelProviderTest.GRAPH_DIR);
    }

}
