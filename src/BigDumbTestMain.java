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

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // start frame elements
        editorFrame = new JFrame("Pic Name");
        editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // get file
        imgs = new ArrayList<File>();
        File dir = new File("img");
        // final File img = new File("img/DSC01219.JPG");
        for (File img : dir.listFiles()) {
          imgs.add(img);
        }

        // show first image
        if (!imgs.isEmpty()) {
          File img = imgs.get(0);
          System.out.println(img.toString());
          showImage(img);
        }
      }
    });
  }

  public static void renameImg(File image, String name) {
    File renamed = new File("img/" + name + ".jpg");
    image.renameTo(renamed);
  }

  public static void showImage(final File img) {
    final JPanel layout = new JPanel(new FlowLayout());
    layout.setPreferredSize(new Dimension(800, 640));
    final StringBuffer finalName = new StringBuffer();

    // get image
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

    // get date
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

    // get title input
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

    // show frame
    editorFrame.setContentPane(layout);
    editorFrame.pack();
    editorFrame.setLocationRelativeTo(null);
    editorFrame.setVisible(true);
  }
}
