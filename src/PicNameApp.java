import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class PicNameApp {

    private List<File> imgs;
    private JFrame editorFrame;
    private File inputDirectory, outputDirectory;

    public static final ArrayList<String> inputCache = new ArrayList<String>();
    public static int cacheCursorIndex = -1;
    public static boolean navigatingCache = false;
    public static SkipListener skipListener;

    public static int maxCacheSize = 30;

    private int width;
    private int height;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Set System L&F
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    System.out.println("could not set look and feel");
                }

                PicNameApp app = new PicNameApp();
                // start frame elements
                app.editorFrame = new JFrame("Pic Name");
                app.editorFrame.setDefaultCloseOperation(WindowConstants
                        .EXIT_ON_CLOSE);

                Dimension screenSize = Toolkit.getDefaultToolkit()
                        .getScreenSize();
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
        browser.setPreferredSize(new Dimension(width / 2, height / 2));
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
        browser.setPreferredSize(new Dimension(width / 2, height / 2));
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
                extension = file.getName().substring(i + 1).toLowerCase();
                if (extension.equals("jpg") | extension.equals("png") |
                        extension.equals("jpeg")) {
                    imgs.add(file);
                }
            }
        }
        System.out.println("Found files to be renamed:");
        for (File file : imgs) {
            System.out.println(file.getName());
        }
        System.out.println("-------");
    }

    private void showNextImage() {
        boolean success = false;
        while (!success) {
            if (!imgs.isEmpty()) {
                File img = imgs.get(0);
                success = showImage(img);
                editorFrame.setTitle(img.getName());
            } else {
                JOptionPane.showMessageDialog(editorFrame, "All images done!");
                System.exit(0);
            }
        }
    }

    public boolean showImage(File img) {
        // create the layouts
        final JPanel topLayout = new JPanel(new FlowLayout());
        final JPanel bottomLayout = new JPanel(new FlowLayout());
        final JPanel contentPane = new JPanel(new BorderLayout());
        final StringBuffer finalName = new StringBuffer();

        loadMetadate(img, finalName);
        initInputForm(img, topLayout, finalName);
        try {
            loadImage(img, bottomLayout);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(editorFrame, "Could not load image");
            imgs.remove(img);
            return false;
        }
        contentPane.add(topLayout, BorderLayout.NORTH);
        contentPane.add(bottomLayout, BorderLayout.CENTER);
        refreshFrame(contentPane, topLayout);
        return true;
    }

    private void loadImage(File img, final JPanel layout) throws IOException {
        BufferedImage image = null;
        image = ImageIO.read(img);
        ImageIcon imageIcon;
        if (image.getWidth() > width) {
            Image scaledImage = image.getScaledInstance(width,
                    Math.round(image.getHeight() * (width * 1f / image
                            .getWidth())), Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaledImage);
        } else if (image.getHeight() > height) {
            Image scaledImage =
                    image.getScaledInstance(Math.round(image.getWidth() *
                                    (height * 1f / image.getHeight())),
                            height, Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(scaledImage);
        } else {
            imageIcon = new ImageIcon(image);
        }
        JLabel jLabel = new JLabel();
        jLabel.setIcon(imageIcon);
        layout.add(jLabel, BorderLayout.CENTER);
    }

    private boolean loadMetadate(File img, final StringBuffer finalName) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(img);
            if (metadata == null) {
                return false;
            }
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType
                    (ExifSubIFDDirectory.class);
            if (directory == null) {
                return false;
            }
            Date date = directory.getDate(ExifSubIFDDirectory
                    .TAG_DATETIME_ORIGINAL);
            if (date == null) {
                return false;
            }
            finalName.append((new SimpleDateFormat("yyyy MM dd")).format
                    (date) + " - ");
        } catch (ImageProcessingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void initInputForm(File img, final JPanel layout, final
    StringBuffer finalName) {
        Font font = new Font("SansSerif", Font.PLAIN, 28);
        JLabel metadate = new JLabel(finalName.toString());
        metadate.setFont(font);
        layout.add(metadate);
        final JTextField inputField = new JTextField(36);
        inputField.setFont(font);
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setToolTipText("Enter the title for the image");
        inputField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                JTextField textField = (JTextField) e.getSource();
                if (e.getKeyCode() == 38) {
                    if (cacheCursorIndex == inputCache.size() - 1) {
                        if (!inputCache.isEmpty()) {
                            if (navigatingCache) {
                                inputCache.remove(inputCache.size() - 1);
                            }
                            inputCache.add(textField.getText());
                            navigatingCache = true;
                            cacheCursorIndex = inputCache.size() - 2;
                            textField.setText(inputCache.get(cacheCursorIndex));
                        }
                    } else {
                        if (cacheCursorIndex > 0) {
                            cacheCursorIndex--;
                            textField.setText(inputCache.get(cacheCursorIndex));
                        }
                    }
                    textField.selectAll();
                } else if (e.getKeyCode() == 40) {
                    if (!navigatingCache) {
                        textField.selectAll();
                    } else if (!inputCache.isEmpty() && cacheCursorIndex <
                            inputCache.size() - 1) {
                        cacheCursorIndex++;
                        textField.setText(inputCache.get(cacheCursorIndex));
                        textField.selectAll();
                    }
                }

            }

        });
        JButton inputButton = new JButton("OK");
        inputButton.addActionListener(new RenameListener(img, finalName,
                inputField));
        layout.add(inputField);
        layout.add(inputButton);
        JButton skipButton = new JButton("Skip");
        skipListener = new SkipListener(img);
        skipButton.addActionListener(skipListener);
        layout.add(skipButton);
        //inputField.setPreferredSize(new Dimension(layout.getPreferredSize()
        // .width, inputField.getPreferredSize().height));
    }

    private class SkipListener implements ActionListener {
        private File img;

        public SkipListener(File img) {
            this.img = img;
        }

        public void actionPerformed(ActionEvent e) {
            skip();
        }

        public void skip() {
            System.out.println("SKIPPING " + img.getName());
            imgs.remove(img);
            showNextImage();
        }
    }

    private class RenameListener implements ActionListener {

        private File img;
        private StringBuffer finalName;
        private JTextField inputField;

        public RenameListener(File img, StringBuffer finalName, JTextField
                inputField) {
            this.img = img;
            this.finalName = finalName;
            this.inputField = inputField;
        }

        public void actionPerformed(ActionEvent e) {
            String text = inputField.getText();
            if (text.equals("")) {
                skipListener.skip();
                return;
            }
            String targetName = finalName.toString() + text;
            if (renameImg(img, targetName)) {
                if (!inputCache.isEmpty() && navigatingCache) {
                    inputCache.remove(inputCache.size() - 1);
                }
                inputCache.add(text);
                if (inputCache.size() > maxCacheSize) {
                    inputCache.remove(0);
                }
                cacheCursorIndex = inputCache.size() - 1;
                navigatingCache = false;
                imgs.remove(img);
                showNextImage();
            } else {
                JOptionPane.showMessageDialog(editorFrame,
                        "Could not rename image. Either input is not a valid " +
                                "file name or there is already a file with " +
                                "the same name.");
                inputField.selectAll();
            }
        }
    }

    public boolean renameImg(File image, String name) {
        int i = image.getName().lastIndexOf('.');
        String extension = image.getName().substring(i + 1).toLowerCase();
        String newPath = outputDirectory.getAbsolutePath() + File.separator +
                name + "." + extension;
        File renamed = new File(newPath);
        System.out.println(image.getAbsoluteFile() + " MOVING TO " + newPath);
        return image.renameTo(renamed);
    }

    private void refreshFrame(final JPanel layout, JPanel inputForm) {
        editorFrame.setContentPane(layout);
        editorFrame.getRootPane().setDefaultButton((JButton) inputForm
                .getComponent(2));
        inputForm.getComponent(1).requestFocus();
        editorFrame.pack();
        editorFrame.setVisible(true);
    }

}
