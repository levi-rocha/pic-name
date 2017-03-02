import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class BigDumbTestMain {

  public static void main(String[] args) {

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame editorFrame = new JFrame("Pic Name");
        editorFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        File img = new File("img/DSC01219.JPG");
        
        StringBuffer finalName = new StringBuffer();
        
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
        
        editorFrame.pack();
        editorFrame.setLocationRelativeTo(null);
        editorFrame.setVisible(true);
      }
    });
  }
}
