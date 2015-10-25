package de.isibboi.agentsimlauncher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Downloads and launches AgentSim.
 * 
 * @author Sebastian Schmidt
 * @since 1.0.0
 */
public class Launcher implements BoundedRangeModel, Runnable {
	private static final Logger LOG = LogManager.getLogger(Launcher.class);

	private static final String DOWNLOAD_SOURCE_DIRECTORY = "https://isibboi.de/AgentSim/";
	private static final String VERSION_FILE = DOWNLOAD_SOURCE_DIRECTORY + "current_version";

	private final List<ChangeListener> _changeListeners = new ArrayList<>();
	private LaunchParameters _launchParameters;

	private int _extent;
	private int _progress;

	private Path _rootDirectory;
	private File _jarFileLocal;

	/**
	 * Sets the launch parameters.
	 * @param launchParameters The launch parameters.
	 */
	public void setLaunchParameters(final LaunchParameters launchParameters) {
		_launchParameters = launchParameters;
	}

	@Override
	public int getMinimum() {
		return 0;
	}

	@Override
	public void setMinimum(final int newMinimum) {
		throw new RuntimeException("Cannot set minimum.");
	}

	@Override
	public int getMaximum() {
		return 100;
	}

	@Override
	public void setMaximum(final int newMaximum) {
		throw new RuntimeException("Cannot set maximum.");
	}

	@Override
	public int getValue() {
		return _progress;
	}

	@Override
	public void setValue(final int newValue) {
		throw new RuntimeException("Cannot set value.");
	}

	@Override
	public void setValueIsAdjusting(final boolean b) {
		throw new RuntimeException("Cannot set valueIsAdjusting.");
	}

	@Override
	public boolean getValueIsAdjusting() {
		return false;
	}

	@Override
	public int getExtent() {
		LOG.info("Returning extent");
		return _extent;
	}

	@Override
	public void setExtent(final int newExtent) {
		LOG.info("Setting extent to " + newExtent + ".");
		_extent = newExtent;
	}

	@Override
	public void setRangeProperties(final int value, final int extent, final int min, final int max, final boolean adjusting) {
		throw new RuntimeException("Cannot set range properties.");
	}

	@Override
	public void addChangeListener(final ChangeListener x) {
		_changeListeners.add(x);
	}

	@Override
	public void removeChangeListener(final ChangeListener x) {
		_changeListeners.remove(x);
	}

	@Override
	public void run() {
		LOG.info("Launching...");

		try {
			ensureDirectoriesExist();
			ensureCurrentVersionIsDownloaded();
			launchAgentSim();
		} catch (LauncherException e) {
			LOG.error("Could not launch AgentSim!", e);
		}
	}

	/**
	 * Launches AgentSim.
	 * @throws LauncherException If AgentSim cannot be launched.
	 */
	private void launchAgentSim() throws LauncherException {
		LOG.info("Launching AgentSim...");

		try {
			String command = "java -jar " + _jarFileLocal.getAbsolutePath();
			LOG.debug("Executing: " + command);

			Runtime.getRuntime().exec(command);
			LOG.info("AgentSim was successfully launched.");
		} catch (IOException e) {
			throw new LauncherException("Could not launch AgentSim.", e);
		}
	}

	/**
	 * Ensures that the current version of AgentSim is downloaded.
	 * @throws LauncherException If the current version of AgentSim cannot be downloaded. (And more)
	 */
	private void ensureCurrentVersionIsDownloaded() throws LauncherException {
		URL versionFile = null;

		try {
			versionFile = new URL(VERSION_FILE);
		} catch (MalformedURLException e) {
			throw new LauncherException("Version file URL is malformed.", e);
		}

		String version = null;

		try {
			URLConnection connection = versionFile.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(3000);
			connection.connect();

			BufferedReader versionReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			version = versionReader.readLine();
			versionReader.close();
		} catch (IOException e) {
			throw new LauncherException("Could not retrieve version.", e);
		}

		URL jarFile = null;

		try {
			jarFile = new URL(DOWNLOAD_SOURCE_DIRECTORY + version);
		} catch (MalformedURLException e) {
			throw new LauncherException("Version file is malformed.", e);
		}

		try {
			_jarFileLocal = _rootDirectory.resolve(version).toFile();

			if (_jarFileLocal.exists()) {
				LOG.info("Current version already exists.");
				return;
			}

			OutputStream out = new FileOutputStream(_jarFileLocal);

			URLConnection connection = jarFile.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(3000);
			connection.connect();
			int contentLength = connection.getContentLength();

			InputStream jarInputStream = new BufferedInputStream(connection.getInputStream());
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			int oldProgress = 0;
			int allBytesread = 0;

			do {
				bytesRead = jarInputStream.read(buffer);

				if (bytesRead != -1) {
					out.write(buffer, 0, bytesRead);
					allBytesread += bytesRead;
				}

				_progress = allBytesread / contentLength;
				if (_progress != oldProgress) {
					fireChangeEvent();
				}
			} while (bytesRead != -1);

			_progress = 100;
			fireChangeEvent();

			jarInputStream.close();
			out.close();
			LOG.info("Downloaded current version.");
		} catch (IOException e) {
			throw new LauncherException("Could not download and save jar.", e);
		}
	}

	/**
	 * Makes sure the necessary directories for installing AgentSim exist.
	 * @throws LauncherException When the directory cannot be created. (And more)
	 */
	private void ensureDirectoriesExist() throws LauncherException {
		_rootDirectory = FileSystems.getDefault().getPath(System.getProperty("user.home"), ".agentsim");
		File rootFile = _rootDirectory.toFile();

		if (rootFile.isDirectory()) {
			return;
		}

		if (rootFile.exists()) {
			throw new LauncherException("Root directory exists, but is no directory.");
		}

		rootFile.mkdir();
		LOG.info("Created .agentsim directory.");
	}

	/**
	 * Informs all change listeners about a state change of this object.
	 */
	private void fireChangeEvent() {
		for (ChangeListener cl : _changeListeners) {
			cl.stateChanged(new ChangeEvent(this));
		}
	}
}