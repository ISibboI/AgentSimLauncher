package de.isibboi.agentsimlauncher;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main window of the launcher.
 * 
 * @author Sebastian Schmidt
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class LauncherFrame extends JFrame implements ActionListener {
	private static final Logger LOG = LogManager.getLogger(LauncherFrame.class); 
	
	private JCheckBox _enableSnapshotsCheckBox;
	private JButton _launchButton;
	private JProgressBar _launchProgressBar;
	
	private Launcher _launcher = new Launcher();
	
	/**
	 * Starts the launcher.
	 * @param args The program arguments.
	 */
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			LOG.error("Could not set System Look and Feel.", e);
		}
		
		new LauncherFrame();
	}

	/**
	 * Creates the launcher frame.
	 */
	public LauncherFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(100, 100);
		createUI();
		pack();
		setResizable(false);
		setVisible(true);
	}

	/**
	 * Creates the UI of the launcher.
	 */
	private void createUI() {
		setLayout(new GridBagLayout());
		
		_enableSnapshotsCheckBox = new JCheckBox("Enable Snapshots", true);
		_launchButton = new JButton("Launch!");
		_launchProgressBar = new JProgressBar(_launcher);
		
		_enableSnapshotsCheckBox.setEnabled(false);
		_launchButton.addActionListener(this);
		
		Insets insets = new Insets(2, 2, 2, 2);
		
		GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0);
		add(_enableSnapshotsCheckBox, gbc);
		
		gbc = new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0);
		add(_launchButton, gbc);
		
		gbc = new GridBagConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0);
		add(_launchProgressBar, gbc);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == _launchButton) {
			_launchButton.setEnabled(false);
			
			_launcher.setLaunchParameters(new LaunchParameters(_enableSnapshotsCheckBox.isSelected()));
			
			new Thread(_launcher).start();
		}
	}
}