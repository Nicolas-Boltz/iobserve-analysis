package org.iobserve.newplanning;

import java.io.File;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.uka.ipd.sdq.dsexplore.launch.DSELaunch;

/**
 * This class controls all aspects of the application's execution
 *
 * @author poeppke, weimann
 */
public class PerOpteryxHeadless {
    private static final Logger LOG = Logger.getLogger(PerOpteryxHeadless.class);

    public static final String INPUT_WORKING_DIR_OPTION = "working-dir";
    public static final String INPUT_WORKING_DIR_OPTION_SHORT = "w";

    public static final String INPUT_MODEL_NAME_OPTION = "model-name";
    public static final String INPUT_MODEL_NAME_OPTION_SHORT = "m";

    public static final String INPUT_PRODUCT_OPTION = "product";

    private IWorkspaceRoot workspaceRoot;
    private IProject project;

    // @Override
    // public Object start(String workingDir) throws Exception {
    // final String[] args = Platform.getCommandLineArgs();
    //
    // final CommandLineParser parser = new BasicParser();
    // try {
    // final CommandLine commandLine = parser.parse(PerOpteryxHeadless.createOptions(), args);
    // final String workingDir =
    // commandLine.getOptionValue(PerOpteryxHeadless.INPUT_WORKING_DIR_OPTION);
    // PerOpteryxHeadless.LOG.info("Working dir: " + workingDir);
    //
    // this.launchPeropteryx(workingDir);
    // } catch (final ParseException exp) {
    // final HelpFormatter formatter = new HelpFormatter();
    // formatter.printHelp("peropteryx-headless", PerOpteryxHeadless.createOptions());
    // return -1;
    // }
    //
    // this.project.delete(true, new NullProgressMonitor());
    //
    // return IApplication.EXIT_OK;
    // }
    ResourcesPlugin resourcesPlugin;

    public PerOpteryxHeadless() throws Exception {

        this.resourcesPlugin = new ResourcesPlugin();

        // found at https://stackoverflow.com/questions/4673406/programmatically-start-osgi-equinox,
        // also see https://www.eclipsezone.com//eclipse/forums/t93976.rhtml
        final String[] equinoxArgs = { "-console", "1234", "-noExit" };
        final BundleContext context = EclipseStarter.startup(equinoxArgs, null);

        // trying "install bundle" for jar including ResourcesPlugin, see
        // https://www.programcreek.com/java-api-examples/index.php?class=org.osgi.framework.BundleContext&method=installBundle
        File jar = new File(
                "/Users/LarsBlumke/Documents/CAU/Masterarbeit/iObserve/iobserve-analysis/lib/org.eclipse.ant.core_3.5.0.v20170509-2149.jar");
        context.installBundle(jar.toURI().toURL().toString());
        jar = new File(
                "/Users/LarsBlumke/Documents/CAU/Masterarbeit/iObserve/iobserve-analysis/lib/org.eclipse.core.expressions_3.6.0.v20170207-1037.jar");
        context.installBundle(jar.toURI().toURL().toString());
        jar = new File(
                "/Users/LarsBlumke/Documents/CAU/Masterarbeit/iObserve/iobserve-analysis/lib/org.eclipse.core.resources_3.12.0.v20170417-1558.jar");
        final Bundle resourceBundle = context.installBundle(jar.toURI().toURL().toString());
        resourceBundle.start();

        this.resourcesPlugin.start(context);

        // Main.main(equinoxArgs);

        // found at http://tux2323.blogspot.de/2011/10/osgi-how-to-get-bundle-context-in-java.html
        // but doesn't work either
        // final BundleContext ctx =
        // BundleReference.class.cast(ResourcesPlugin.class.getClassLoader()).getBundle()
        // .getBundleContext();
        // final Bundle resourcesBundle = FrameworkUtil.getBundle(ResourcesPlugin.class);
        // final BundleContext resourcesCtx = resourcesBundle.getBundleContext();
        // this.resourcesPlugin.start(resourcesCtx);

    }

    public void launchPeropteryx(final String workingDir) throws CoreException {
        PerOpteryxHeadless.LOG.info("Configuring PerOpteryx run...");
        final DSELaunch launch = new DSELaunch();

        this.configureInternalWorkspace(workingDir);

        final String projectDir = PerOpteryxLaunchConfigurationBuilder.DEFAULT_PROJECT_NAME + "/"
                + PerOpteryxLaunchConfigurationBuilder.DEFAULT_PROJECT_WORKING_DIR;

        final ILaunchConfiguration launchConfig = PerOpteryxLaunchConfigurationBuilder
                .getDefaultLaunchConfiguration(projectDir, workingDir);
        final ILaunch currentLaunch = new Launch(launchConfig, ILaunchManager.RUN_MODE, null);

        DebugPlugin.getDefault().getLaunchManager().addLaunch(currentLaunch);

        PerOpteryxHeadless.LOG.info("Launching PerOpteryx...");
        launch.launch(launchConfig, ILaunchManager.RUN_MODE, currentLaunch, new NullProgressMonitor());
    }

    private void configureInternalWorkspace(final String modelDir) throws CoreException {
        this.workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        PerOpteryxHeadless.LOG.info("Workspace root: " + this.workspaceRoot.getLocation());
        this.project = this.workspaceRoot.getProject(PerOpteryxLaunchConfigurationBuilder.DEFAULT_PROJECT_NAME);

        if (this.project.exists()) {
            this.project.close(null);
            this.project.delete(true, null);
        }

        this.project.create(null);
        this.project.open(null);

        final IFolder modelFolder = this.project
                .getFolder(PerOpteryxLaunchConfigurationBuilder.DEFAULT_PROJECT_WORKING_DIR);

        modelFolder.createLink(new Path(modelDir), IResource.BACKGROUND_REFRESH | IResource.REPLACE,
                new NullProgressMonitor());
    }

    // @Override
    // public void stop() {
    // try {
    // ResourcesPlugin.getWorkspace().save(true, null);
    // } catch (final CoreException e) {
    // e.printStackTrace();
    // }
    // }

    /**
     * Create the command line parameter setup.
     *
     * @return options for the command line parser
     */
    private static Options createOptions() {
        final Options options = new Options();

        final Option workDirOption = new Option(PerOpteryxHeadless.INPUT_WORKING_DIR_OPTION_SHORT,
                PerOpteryxHeadless.INPUT_WORKING_DIR_OPTION, true,
                "Working directory containing the model files. Note that the files may be changed in the process.");
        workDirOption.setRequired(true);

        final Option productOption = new Option(PerOpteryxHeadless.INPUT_PRODUCT_OPTION, true,
                "Eclipse product description");

        final Option helpOption = new Option("h", "help", false, "Show usage information");

        options.addOption(workDirOption);
        options.addOption(productOption);

        /** help */
        options.addOption(helpOption);

        return options;
    }
}