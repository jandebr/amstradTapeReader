package org.maia.amstrad.io.tape.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Viewer {

	private String title;

	private JComponent view;

	private JFrame frame;

	protected Viewer(String title) {
		this(null, title);
	}

	public Viewer(JComponent view, String title) {
		this.view = view;
		this.title = title;
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
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

	public String getTitle() {
		return title;
	}

	public JComponent getView() {
		return view;
	}

	protected void setView(JComponent view) {
		this.view = view;
	}

	protected JFrame getFrame() {
		return frame;
	}

	private void setFrame(JFrame frame) {
		this.frame = frame;
	}

}