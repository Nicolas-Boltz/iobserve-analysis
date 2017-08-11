package org.iobserve.analysis.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.core.Is;
import org.iobserve.analysis.model.ResourceEnvironmentModelBuilder;
import org.iobserve.analysis.model.ResourceEnvironmentModelProvider;
import org.iobserve.analysis.modelneo4j.ModelProvider;
import org.iobserve.common.record.ContainerAllocationEvent;
import org.iobserve.common.record.IAllocationRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import teetime.framework.test.StageTester;

/**
 * Tests for TAllocation filter, when the resource container does not exist yet.
 *
 * @author jweg
 *
 */
@RunWith(PowerMockRunner.class)
// write all final classes here
@PrepareForTest({ ResourceEnvironmentModelBuilder.class, ResourceEnvironmentModelProvider.class })
public class TAllocationNoResourceContainerTest {

    /** mocks */
    private static ModelProvider<ResourceEnvironment> mockedResourceEnvironmentModelGraphProvider;

    /** data for generating test container allocation event */
    private static final String SERVICE = "test-service";
    private static final String CONTEXT = "/path/test";
    private static final String URL = "http://" + TAllocationNoResourceContainerTest.SERVICE + '/'
            + TAllocationNoResourceContainerTest.CONTEXT;

    /** test event */
    private static ContainerAllocationEvent allocationEvent;

    /** test resource containers */
    private static Optional<ResourceContainer> optTestNullResourceContainer;
    private static ResourceContainer testResourceContainer;

    private static ResourceEnvironment testResourceEnvironment;

    /***/
    private TAllocation tAllocation;

    private static List<IAllocationRecord> inputEvents = new ArrayList<>();

    /**
     * Initialize test data.
     */
    @BeforeClass
    public static void initializeTAllocationAndMock() {

        /** test event */
        TAllocationNoResourceContainerTest.allocationEvent = new ContainerAllocationEvent(
                TAllocationNoResourceContainerTest.URL);

        /** input allocation event */
        TAllocationNoResourceContainerTest.inputEvents.add(TAllocationNoResourceContainerTest.allocationEvent);

        /** optional test resource container without value */
        TAllocationNoResourceContainerTest.optTestNullResourceContainer = Optional.ofNullable(null);
        // /** test resource container with value */
        TAllocationNoResourceContainerTest.testResourceContainer = ResourceenvironmentFactory.eINSTANCE
                .createResourceContainer();
        TAllocationNoResourceContainerTest.testResourceContainer.setEntityName("TestResourceContainer");
        TAllocationNoResourceContainerTest.testResourceContainer.setId("_resourcecontainer_test_id");

        /** test resource environment */
        TAllocationNoResourceContainerTest.testResourceEnvironment = ResourceenvironmentFactory.eINSTANCE
                .createResourceEnvironment();

    }

    /**
     * Define the test situation in which a container allocation event is defined as input and the
     * specified resource container does not exist in the resource environment model.
     */
    @Before
    public void stubMocksNoResourceContainer() {

        /** mock for ResourceEnvironmentModelBuilder */
        // use PowerMockito for calling static methods of this final class
        PowerMockito.mockStatic(ResourceEnvironmentModelBuilder.class);
        /** mock for new graph provider */
        TAllocationNoResourceContainerTest.mockedResourceEnvironmentModelGraphProvider = Mockito
                .mock(ModelProvider.class);

        this.tAllocation = new TAllocation(
                TAllocationNoResourceContainerTest.mockedResourceEnvironmentModelGraphProvider);

        // for new model graph provider
        Mockito.when(TAllocationNoResourceContainerTest.mockedResourceEnvironmentModelGraphProvider
                .readOnlyRootComponent(ResourceEnvironment.class))
                .thenReturn(TAllocationNoResourceContainerTest.testResourceEnvironment);

        Mockito.when(ResourceEnvironmentModelBuilder.getResourceContainerByName(
                TAllocationNoResourceContainerTest.testResourceEnvironment, TAllocationNoResourceContainerTest.SERVICE))
                .thenReturn(TAllocationNoResourceContainerTest.optTestNullResourceContainer);

        Mockito.when(ResourceEnvironmentModelBuilder.createResourceContainer(
                TAllocationNoResourceContainerTest.testResourceEnvironment, TAllocationNoResourceContainerTest.SERVICE))
                .thenReturn(TAllocationNoResourceContainerTest.testResourceContainer);

        Mockito.doNothing().when(TAllocationNoResourceContainerTest.mockedResourceEnvironmentModelGraphProvider)
                .updateComponent(ResourceEnvironment.class, TAllocationNoResourceContainerTest.testResourceEnvironment);
    }

    /**
     * Check whether the allocation event is forwarded to the next stage after the allocation is
     * finished.
     */
    @Test
    public void checkAllocationFinished() {

        final List<IAllocationRecord> allocationFinishedEvents = new ArrayList<>();

        StageTester.test(this.tAllocation).and().send(TAllocationNoResourceContainerTest.inputEvents)
                .to(this.tAllocation.getInputPort()).and().receive(allocationFinishedEvents)
                .from(this.tAllocation.getAllocationFinishedOutputPort()).start();

        Assert.assertThat(allocationFinishedEvents.get(0), Is.is(TAllocationNoResourceContainerTest.allocationEvent));
    }

    /**
     * Check whether the allocation event is forwarded to the next stage after the allocation model
     * is updated.
     */
    @Test
    public void checkAllocationUpdate() {

        final List<IAllocationRecord> allocationEvents = new ArrayList<>();

        StageTester.test(this.tAllocation).and().send(TAllocationNoResourceContainerTest.inputEvents)
                .to(this.tAllocation.getInputPort()).and().receive(allocationEvents)
                .from(this.tAllocation.getAllocationOutputPort()).start();

        Assert.assertThat(allocationEvents.get(0), Is.is(TAllocationNoResourceContainerTest.allocationEvent));

    }

}
