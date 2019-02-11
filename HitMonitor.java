import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class HitMonitor {
	static int[] mask = { -1, 0, 1, -1, 0, 1, -1, 0, 1 };
	static int bufferPixels = 10;
	static int p1Hits = 0;
	static int p1Line = 0;
	static int p2Hits = 0;
	static int p2Line = 0;

	public static void main(String[] Args) {
		try {

			// Draw frame
			JFrame frame = new JFrame("Player1");
			frameDraw(frame);

			while (true) {
				//Sleepy time
				Thread.sleep(500);

				// Check for hits
				BufferedImage[] p1Frames = new BufferedImage[10];
				BufferedImage[] p2Frames = new BufferedImage[10];
				for (int i = 0; i < 10; i++) {
					p1Frames[i] = blackWhiteTransform(take2pP1ScreenShot());
					p2Frames[i] = blackWhiteTransform(take2pP2ScreenShot());
				}
				BufferedImage inputP1 = blackerWhiteTransform(averageTransform(p1Frames));
				BufferedImage inputP2 = blackerWhiteTransform(averageTransform(p2Frames));

				// Update Counters
				int imEdgeP1 = vertEdge(inputP1);
				if (imEdgeP1 < p1Line) {
					p1Hits++;
				}
				p1Line = imEdgeP1;
				System.out.println(p1Line + "," + p1Hits);

				int imEdgeP2 = vertEdge(inputP2);
				if (imEdgeP2 < p2Line) {
					p2Hits++;
				}
				p2Line = imEdgeP2;

				// Update frame
				frameUpdate(frame);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage averageTransform(BufferedImage[] p1Frames) {
		// Copy first image to have an image to change
		BufferedImage imageOut = deepCopy(p1Frames[0]);

		for (int x = 0; x < imageOut.getWidth(); x++) {
			for (int y = 0; y < imageOut.getHeight(); y++) {
				int pixelSum = 0;
				for (BufferedImage frame : p1Frames) {
//					pixelSum += frame.getRGB(x, y);
					imageOut.setRGB(x, y, Math.max(frame.getRGB(x,y),imageOut.getRGB(x,y)));
				}
//				imageOut.setRGB(x, y, pixelSum / p1Frames.length);
			}
		}
		return imageOut;
	}

	public static int vertEdge(BufferedImage imageIn) {
		int prevSum = 0;
		for (int y = bufferPixels; y < imageIn.getHeight() - bufferPixels; y++) {
			prevSum += Math.abs(imageIn.getRGB(imageIn.getWidth() - 1, y));
		}
		// loop through image
		for (int x = imageIn.getWidth() - 1; x >= 0; x--) {
			int sum = 0;
			for (int y = bufferPixels; y < imageIn.getHeight() - bufferPixels; y++) {
				sum += Math.abs(imageIn.getRGB(x, y));
			}
			if (sum < prevSum || x == 0) {
				return (x);
			}
			prevSum = sum;
		}

		return 0;
	}

	public static BufferedImage blackWhiteTransform(BufferedImage imageIn) {
		BufferedImage imageOut = deepCopy(imageIn);

		// compare
		BufferedImage compareImage = deepCopy(imageIn);
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		compareImage = op.filter(compareImage, null);

		// loop through image
		for (int y = 0; y < imageIn.getHeight(); y++) {
			for (int x = 0; x < imageIn.getWidth(); x++) {
				if (Math.abs(compareImage.getRGB(x, y)) <= (5000000)) {
					imageOut.setRGB(x, y, 16777215);
				} else {
					imageOut.setRGB(x, y, 0);
				}
			}
		}
		return imageOut;
	}

	public static BufferedImage blackerWhiteTransform(BufferedImage imageIn) {
		BufferedImage imageOut = deepCopy(imageIn);

		// compare
		BufferedImage compareImage = deepCopy(imageIn);
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(cs, null);
		compareImage = op.filter(compareImage, null);

		// loop through image
		for (int y = 0; y < imageIn.getHeight(); y++) {
			for (int x = 0; x < imageIn.getWidth(); x++) {
				if (Math.abs(compareImage.getRGB(x, y)) <= (16000000)) {
					imageOut.setRGB(x, y, 16777215);
				} else {
					imageOut.setRGB(x, y, 0);
				}
			}
		}
		return imageOut;
	}

	static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	static BufferedImage take2pP1ScreenShot() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int h = screenSize.height;
		int w = screenSize.width;
		int minMonitorH = (int) Math.round(h * 1 / 17);
		int minMonitorW = (int) Math.round(w * 4.52 / 23);
		try {
			Robot robot = new Robot();
			BufferedImage screenShot = robot.createScreenCapture(new Rectangle(minMonitorW, minMonitorH, 275, 25));
			return screenShot;
		} catch (Exception e) {
			return null;
		}

	}

	static BufferedImage take2pP2ScreenShot() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int h = screenSize.height;
		int w = screenSize.width;
		int minMonitorH = (int) Math.round(h * 1 / 17);
		int minMonitorW = (int) Math.round(w * 16.02 / 23);
		try {
			Robot robot = new Robot();
			BufferedImage screenShot = robot.createScreenCapture(new Rectangle(minMonitorW, minMonitorH, 275, 25));
			return screenShot;
		} catch (Exception e) {
			return null;
		}

	}

	static void frameDraw(JFrame frame) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int h = screenSize.height;
		int w = screenSize.width;
		int minMonitorH = (int) Math.round(h * 1 / 17);
		int minMonitorWP1 = (int) Math.round(w * 4.5 / 23);
		int minMonitorWP2 = (int) Math.round(w * 16.02 / 23);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(new Rectangle(0, 0, w, h));
		frame.setUndecorated(true);
		frame.setBackground(new Color(0, 0, 0, 0));
		frame.setAlwaysOnTop(true);
		frame.getContentPane().setLayout(null);
		JLabel p1Box = createTextBox(minMonitorWP1 + 285, minMonitorH + 1, "Hits: 0", 20, Color.BLACK);
		frame.getContentPane().add(p1Box);
		JLabel p2Box = createTextBox(minMonitorWP2 + 285, minMonitorH + 1, "Hits: 0", 20, Color.BLACK);
		frame.getContentPane().add(p2Box);
		frame.setVisible(true);
		frame.setResizable(false);

		JButton button = new JButton("Reset");
		button.setLocation(w - 100, 25);
		button.setSize(button.getPreferredSize());
		button.setEnabled(true);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				p1Hits = 0;
				p2Hits = 0;
			}
		});
		frame.add(button);
	}

	private static void frameUpdate(JFrame frame) {
		((JLabel2D) frame.getContentPane().getComponent(0)).setText("Hits: " + String.valueOf(p1Hits));
		((JLabel2D) frame.getContentPane().getComponent(1)).setText("Hits: " + String.valueOf(p2Hits));
		SwingUtilities.updateComponentTreeUI(frame);
	}

	static JLabel2D createTextBox(int posX, int posY, String text, int size, Color color) {
		JLabel2D textBox = new JLabel2D(text);
		textBox.setOpaque(false);
		textBox.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		textBox.setOutlineColor(Color.WHITE);
		textBox.setStroke(new BasicStroke(4.2f));
		textBox.setFont(new Font("SansSerif", Font.BOLD, size));
		textBox.setForeground(color);
		textBox.setHorizontalAlignment(JTextField.CENTER);
		textBox.setSize(75, 25);
		textBox.setLocation(posX, posY);
		return textBox;
	}

}