import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class BigDumbTestMain {

	static JLabel name;
	static List<File> imgs;

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// start frame elements
				final JFrame editorFrame = new JFrame("Pic Name");
				editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				name = new JLabel();

				// get file
				File dir = new File("img");
				//final File img = new File("img/DSC01219.JPG");
				for (File img : dir.listFiles()) {
					//imgs.add(img);
					
				}

				final StringBuffer finalName = new StringBuffer();

				// get image
				BufferedImage image = null;
				try {
					image = ImageIO.read(img);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				Image scaledImage = image.getScaledInstance(800, 600, Image.SCALE_SMOOTH);
				ImageIcon imageIcon = new ImageIcon(scaledImage);
				JLabel jLabel = new JLabel();
				jLabel.setIcon(imageIcon);
				editorFrame.getContentPane().add(jLabel, BorderLayout.CENTER);

				// get date
				try {
					Metadata metadata = ImageMetadataReader.readMetadata(img);
					ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
					Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
					finalName.append((new SimpleDateFormat("yyyy MM dd")).format(date));
				} catch (ImageProcessingException e) {
					e.printStackTrace();
					System.exit(1);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				JLabel metadate = new JLabel(finalName.toString());
				editorFrame.getContentPane().add(metadate, BorderLayout.PAGE_END);

				// get title input
				final JTextField inputField = new JTextField();
				inputField.setToolTipText("Enter the title for the image");
				JButton inputButton = new JButton("OK");
				inputButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String title = inputField.getText();
						finalName.append(" - " + title);
						name.setText(finalName.toString());
						// TODO refresh frame
						editorFrame.getContentPane().add(name, BorderLayout.SOUTH);
						editorFrame.revalidate();
						editorFrame.repaint();
						
					}
				});
				editorFrame.getContentPane().add(inputField, BorderLayout.NORTH);
				editorFrame.getContentPane().add(inputButton, BorderLayout.EAST);

				// show name
				name.setText(finalName.toString());
				editorFrame.getContentPane().add(name, BorderLayout.SOUTH);

				// show frame
				editorFrame.pack();
				editorFrame.setLocationRelativeTo(null);
				editorFrame.setVisible(true);

			}
		});
	}
	
	public void renameImg(File image, String name) {
		File renamed = new File(name+".jpg");
		image.renameTo(renamed);
	}
}
