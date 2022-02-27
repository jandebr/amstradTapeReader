package org.maia.amstrad.io.tape.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Viewer {

	private JComponent view;

	private String title;

	private boolean exitOnClose;

	private JFrame frame;

	public Viewer(JComponent view, String title, boolean exitOnClose) {
		this.view = view;
		this.title = title;
		this.exitOnClose = exitOnClose;
	}

	public void buildAndShow() {
		build();
		show();
	}

	public void build() {
		dispose();
		JFrame frame = new JFrame(getTitle());
		setFrame(frame);
		frame.getContentPane().add(getView());
		frame.pack();
		frame.setDefaultCloseOperation(isExitOnClose() ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
		if (isDefaultCenteredOnScreen()) {
			centerOnScreen();
		}
	}

	public void show() {
		if (getFrame() != null) {
			getFrame().setVisible(true);
		}
	}

	public void hide() {
		if (getFrame() != null) {
			getFrame().setVisible(false);
		}
	}

	public void dispose() {
		if (getFrame() != null) {
			getFrame().dispose();
		}
	}

	public void centerOnScreen() {
		if (getFrame() != null) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			getFrame().setLocation(
					new Point((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2));
		}
	}

	public boolean isDefaultCenteredOnScreen() {
		return true;
	}

	public boolean isExitOnClose() {
		return exitOnClose;
	}

	public String getTitle() {
		return title;
	}

	public JComponent getView() {
		return view;
	}

	protected JFrame getFrame() {
		return frame;
	}

	private void setFrame(JFrame frame) {
		this.frame = frame;
	}

}