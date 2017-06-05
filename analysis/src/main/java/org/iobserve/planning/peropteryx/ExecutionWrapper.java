package org.iobserve.planning.peropteryx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.emf.common.util.URI;

/**
 * Wrapper for executing headless PerOpteryx.
 *
 * @author Tobias Pöppke
 * @author Philipp Weimann
 *
 */
public class ExecutionWrapper extends AbstractExecutionWrapper {

	private String execEnvironment;
	private String execEnvironmentParam;
	private String execCommand;

	public ExecutionWrapper(URI inputModelDir, URI perOpteryxDir, URI lqnsDir) throws IOException {
		super(inputModelDir, perOpteryxDir, lqnsDir);
		
		if (isWindows()) {
			this.execEnvironment = "cmd.exe";
			this.execEnvironmentParam = "/C";
			this.execCommand = "java  -jar .\\plugins\\org.eclipse.equinox.launcher_1.3.201.v20161025-1711.jar";
		} else {
			this.execEnvironment = "/bin/bash";
			this.execEnvironmentParam = "-c";
			this.execCommand = "./peropteryx-headless";
		}

	}

	private boolean isWindows() {
		boolean isWindows = System.getProperty("os.name").startsWith("Windows");
		return isWindows;
	}

	@Override
	public void watch(final Process process) throws InterruptedException {
		Thread watcherThread = new Thread(new Runnable() {
			@Override
			public void run() {
				BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				try {
					while ((line = input.readLine()) != null) {
						System.err.println("PerOpteryx Output: " + line);
						// LOG.info("PerOpteryx Output: " + line);
					}
				} catch (IOException e) {
					System.err.println("Watcher Thread terminated");
					// LOG.error("IOException during PerOpteryx run: " +
					// e.getStackTrace());
				}
			}
		});
		synchronized (watcherThread) {
			System.out.println("Starting Watcher Thread!");
			watcherThread.start();
			watcherThread.wait();
			System.out.println("Watcher Thread terminated");
		}
	}

	@Override
	public ProcessBuilder createProcess() {
		LOG.info("Starting optimization process...");
		String modelDir = this.getInputModelDir().toFileString();

		ProcessBuilder builder = new ProcessBuilder(this.execEnvironment, this.execEnvironmentParam,
				this.execCommand + " -w " + modelDir);

		String perOpteryxDir = this.getPerOpteryxDir().toFileString();
		Map<String, String> env = builder.environment();
		
		String path;
		if (isWindows()) {
			path = env.get("Path");
			path = this.getLQNSDir().toFileString()+ ";" + path;
			env.put("Path", path);
		} else {
			path = env.get("PATH");
			path = this.getLQNSDir().toFileString()+ ":" + path;
			env.put("PATH", path);
		}
		
		LOG.info("Environment PATH: " + path);
		builder.directory(new File(perOpteryxDir));
		builder.redirectOutput();
		builder.redirectErrorStream(true);

		return builder;
	}
}
