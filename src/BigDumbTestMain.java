import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
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

public class BigDumbTestMain {

  static List<File> imgs;
  static JFrame editorFrame;
  static File inputDirectory, outputDirectory;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // start frame elements
        editorFrame = new JFrame("Pic Name");
        editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        browseForInputDirectory();
        browseForOutputDirectory();
        loadImages();

        // show first image
        if (!imgs.isEmpty()) {
          File img = imgs.get(0);
          showImage(img);
        }
      }
    });
  }
  
  private static void browseForInputDirectory() {
    JFileChooser browser = new JFileChooser();
    browser.setDialogTitle("Select input Folder");
    browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = browser.showOpenDialog(editorFrame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      inputDirectory = browser.getSelectedFile();
    } else {
      System.exit(1);
    }
  }

  private static void browseForOutputDirectory() {
    JFileChooser browser = new JFileChooser();
    browser.setDialogTitle("Select output folder");
    browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int returnVal = browser.showOpenDialog(editorFrame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      outputDirectory = browser.getSelectedFile();
    } else {
      System.exit(1);
    }
  }
  
  private static void loadImages() {
    imgs = new ArrayList<File>();
    for (File file : inputDirectory.listFiles()) {
      String extension = "";
      int i = file.getName().lastIndexOf('.');
      if (i > 0) {
          extension = file.getName().substring(i+1);
          if (extension.toLowerCase().equals("jpg")) {
            imgs.add(file);
          }
      }
    }
  }
  
  public static void showImage(final File img) {
    // create the layout
    final JPanel layout = new JPanel(new FlowLayout());
    layout.setPreferredSize(new Dimension(800, 640));
    final StringBuffer finalName = new StringBuffer();

    loadImage(img, layout);
    loadMetadate(img, layout, finalName);
    initInputForm(img, layout, finalName);
    refreshFrame(layout);
  }

  private static void loadImage(final File img, final JPanel layout) {
    BufferedImage image = null;
    try {
      image = ImageIO.read(img);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(editorFrame, "This is a useless, non-descriptive error message");
      System.exit(1);
    }
    Image scaledImage = image.getScaledInstance(800, 600, Image.SCALE_SMOOTH);
    ImageIcon imageIcon = new ImageIcon(scaledImage);
    JLabel jLabel = new JLabel();
    jLabel.setIcon(imageIcon);
    layout.add(jLabel, BorderLayout.CENTER);
  }

  private static void loadMetadate(final File img, final JPanel layout,
      final StringBuffer finalName) {
    try {
      Metadata metadata = ImageMetadataReader.readMetadata(img);
      ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
      Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
      finalName.append((new SimpleDateFormat("yyyy MM dd")).format(date) + " - ");
    } catch (ImageProcessingException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    JLabel metadate = new JLabel(finalName.toString());
    layout.add(metadate, BorderLayout.PAGE_END);
  }

  private static void initInputForm(final File img, final JPanel layout,
      final StringBuffer finalName) {
    final JTextField inputField = new JTextField(40);
    inputField.setToolTipText("Enter the title for the image");
    JButton inputButton = new JButton("OK");
    inputButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        finalName.append(inputField.getText());
        renameImg(img, finalName.toString());
        imgs.remove(img);
        if (!imgs.isEmpty()) {
          File next = imgs.get(0);
          showImage(next);
        } else {
          JOptionPane.showMessageDialog(editorFrame, "All images done!");
          System.exit(0);
        }
      }
    });
    layout.add(inputField);
    layout.add(inputButton);
  }
  
  public static void renameImg(File image, String name) {
    File renamed = new File(outputDirectory.getAbsolutePath() + "/" + name + ".jpg");
    image.renameTo(renamed);
  }

  private static void refreshFrame(final JPanel layout) {
    editorFrame.setContentPane(layout);
    editorFrame.pack();
    editorFrame.setLocationRelativeTo(null);
    editorFrame.setVisible(true);
  }

}
