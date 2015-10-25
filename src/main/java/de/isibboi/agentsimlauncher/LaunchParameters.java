package de.isibboi.agentsimlauncher;

/**
 * 
 * @author Sebastian Schmidt
 * @since 1.0.0
 */
public class LaunchParameters {
	private final boolean _enableSnapshots;

	/**
	 * Creates the launch parameters.
	 * @param enableSnapshots If snapshots should be included.
	 */
	public LaunchParameters(final boolean enableSnapshots) {
		_enableSnapshots = enableSnapshots;
	}

	/**
	 * Returns if snapshots should be included.
	 * @return True if snapshots should be included.
	 */
	public boolean isEnableSnapshots() {
		return _enableSnapshots;
	}
}
