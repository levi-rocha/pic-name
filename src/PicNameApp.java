import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class PicNameApp {

	private List<File> imgs;
	private JFrame editorFrame;
	private File inputDirectory, outputDirectory;

	private int width;
	private int height;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				PicNameApp app = new PicNameApp();
				// start frame elements
				app.editorFrame = new JFrame("Pic Name");
				app.editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				app.width = (int) Math.round(screenSize.getWidth()) * 5 / 6;
				app.height = (int) Math.round(screenSize.getHeight()) * 5 / 6;

				app.browseForInputDirectory();
				app.browseForOutputDirectory();
				app.loadImages();

				app.showNextImage();
			}
		});
	}

	private void browseForInputDirectory() {
		JFileChooser browser = new JFileChooser();
		browser.setPreferredSize(new Dimension(width, height));
		browser.setDialogTitle("Select input Folder");
		browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = browser.showOpenDialog(editorFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			inputDirectory = browser.getSelectedFile();
		} else {
			System.exit(1);
		}
	}

	private void browseForOutputDirectory() {
		JFileChooser browser = new JFileChooser();
		browser.setPreferredSize(new Dimension(width, height));
		browser.setDialogTitle("Select output folder");
		browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = browser.showOpenDialog(editorFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			outputDirectory = browser.getSelectedFile();
		} else {
			System.exit(1);
		}
	}

	private void loadImages() {
		imgs = new ArrayList<File>();
		for (File file : inputDirectory.listFiles()) {
			String extension = "";
			int i = file.getName().lastIndexOf('.');
			if (i > 0) {
				extension = file.getName().substring(i + 1);
				if (extension.toLowerCase().equals("jpg")) {
					imgs.add(file);
				}
			}
		}
		for (File file : imgs) {
			System.out.println(file.getName());
		}
	}

	private void showNextImage() {
		boolean success = false;
		while (!success) {
			if (!imgs.isEmpty()) {
				File img = imgs.get(0);
				success = showImage(img);
			} else {
				JOptionPane.showMessageDialog(editorFrame, "All images done!");
				System.exit(0);
			}
		}
	}

	public boolean showImage(File img) {
		// create the layout
		final JPanel layout = new JPanel(new FlowLayout());
		layout.setPreferredSize(new Dimension(width, height));
		final StringBuffer finalName = new StringBuffer();

		if (!loadMetadate(img, layout, finalName)) {
			// no metadate
			JOptionPane.showMessageDialog(editorFrame, "No metadata found for file.");
		} 
		JLabel metadate = new JLabel(finalName.toString());
		layout.add(metadate, BorderLayout.PAGE_END);
		initInputForm(img, layout, finalName);
		try {
			loadImage(img, layout);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(editorFrame, "Could not load image");
			imgs.remove(img);
			return false;
		}
		
		refreshFrame(layout);
		return true;
	}

	private void loadImage(File img, final JPanel layout) throws IOException {
		BufferedImage image = null;
		image = ImageIO.read(img);
		Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		ImageIcon imageIcon = new ImageIcon(scaledImage);
		JLabel jLabel = new JLabel();
		jLabel.setIcon(imageIcon);
		layout.add(jLabel, BorderLayout.CENTER);
	}

	private boolean loadMetadate(File img, final JPanel layout, final StringBuffer finalName) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata(img);
			if (metadata == null) {
				return false;
			}
			ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			if (directory == null) {
				return false;
			}
			Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			if (date == null) {
				return false;
			}
			finalName.append((new SimpleDateFormat("yyyy MM dd")).format(date) + " - ");
		} catch (ImageProcessingException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void initInputForm(File img, final JPanel layout, final StringBuffer finalName) {
		final JTextField inputField = new JTextField(40);
		inputField.setToolTipText("Enter the title for the image");
		JButton inputButton = new JButton("OK");
		inputButton.addActionListener(new renameListener(img, finalName, inputField));
		/*
		 * inputButton.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) {
		 * finalName.append(inputField.getText()); renameImg(img,
		 * finalName.toString()); imgs.remove(img); showNextImage(); } });
		 */
		layout.add(inputField);
		layout.add(inputButton);
	}

	private class renameListener implements ActionListener {

		private File img;
		private StringBuffer finalName;
		private JTextField inputField;

		public renameListener(File img, StringBuffer finalName, JTextField inputField) {
			this.img = img;
			this.finalName = finalName;
			this.inputField = inputField;
		}

		public void actionPerformed(ActionEvent e) {
			finalName.append(inputField.getText());
			renameImg(img, finalName.toString());
			imgs.remove(img);
			showNextImage();
		}

	}

	public void renameImg(File image, String name) {
		File renamed = new File(outputDirectory.getAbsolutePath() + "/" + name + ".jpg");
		image.renameTo(renamed);
	}

	private void refreshFrame(final JPanel layout) {
		editorFrame.setContentPane(layout);
		editorFrame.getRootPane().setDefaultButton((JButton) layout.getComponent(2));
		layout.getComponent(1).requestFocus();
		editorFrame.pack();
		editorFrame.setLocationRelativeTo(null);
		editorFrame.setVisible(true);
	}

}
