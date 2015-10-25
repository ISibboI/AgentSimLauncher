package de.isibboi.agentsimlauncher;

/**
 * Indicates that something went wrong during launching AgentSim.
 * 
 * @author Sebastian Schmidt
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class LauncherException extends Exception {
	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message The message.
	 */
	public LauncherException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message The message.
	 * @param cause The cause.
	 */
	public LauncherException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
