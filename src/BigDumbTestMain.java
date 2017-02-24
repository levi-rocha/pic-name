import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class BigDumbTestMain {

	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame editorFrame = new JFrame("Pic Name");
				editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

				BufferedImage image = null;
				try {
					image = ImageIO.read(new File("img/pino.jpg"));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				ImageIcon imageIcon = new ImageIcon(image);
				JLabel jLabel = new JLabel();
				jLabel.setIcon(imageIcon);
				editorFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

				editorFrame.pack();
				editorFrame.setLocationRelativeTo(null);
				editorFrame.setVisible(true);
			}
		});
	}
}
