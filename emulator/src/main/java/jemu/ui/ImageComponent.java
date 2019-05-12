package jemu.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

class ImageComponent extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Image image;

	public ImageComponent(String imageResource, int width) {
		try (InputStream is = ImageComponent.class.getResourceAsStream(imageResource)) {
			image = ImageIO.read(is);
			int height= image.getHeight(this) * width / image.getWidth(this);
			setPreferredSize(new Dimension(width, height));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void paintComponent(Graphics g) {
		if (image == null) {
			return;
		}
		double imageW = image.getWidth(this);
		double imageH = image.getHeight(this);
		double screenW = getWidth();
		double screenH = getHeight();
		double zx = screenW / imageW;
		double zy = screenH / imageH;
		double z = zx < zy ? zx : zy;
		int w = (int) (imageW * z);
		int h = (int) (imageH * z);
		int x = ((int) screenW - w) / 2;
		int y = ((int) screenH - h) / 2;
		g.drawImage(image, x, y, w, h, this);
	}
}