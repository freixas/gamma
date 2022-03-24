/*
 *  Gamma - A Minkowski Spacetime Diagram Generator
 *  Copyright (C) 2021-2022  by Antonio Freixas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.freixas.gamma.file;

import org.freixas.gamma.Gamma;
import org.freixas.gamma.MainWindow;
import org.freixas.gamma.math.Util;
import org.freixas.gamma.preferences.PreferencesManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.stream.ImageOutputStream;
import org.w3c.dom.Element;

/**
 * Export diagrams in GIF, JPG, PNG, or TIFF formats.
 *
 * @author Antonio Freixas
 */
public class ExportDiagramDialog extends Dialog<ButtonType>
{
    public enum ImageType  {
        GIF(0), JPG(1), PNG(2), TIFF(3);

        private final int value;
        ImageType(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    static final String[] fileTypes = { "GIF Files", "JPEG Files", "PNG Files", "TIFF Files" };
    static final String[] fileExtensions = { "*.gif", "*.jpg", "*.png", "*.tif" };
    static final String[] extraExtensions = { null, "*.jpeg", null, "*.tiff" };

    static final String[] formatNames = { "gif", "jpg", "png", "tiff" };

    private final MainWindow window;
    private final ExportDiagramDialogController controller;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create an export images dialog.
     *
     * @param window The main window associated with this dialog.
     */
    public ExportDiagramDialog(MainWindow window) throws Exception
    {
        this.window = window;

        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/ExportDiagramDialog.fxml"));
        DialogPane dialogPane = loader.load();
        controller = loader.getController();
        setDialogPane(dialogPane);

        initOwner(window);
        initModality(Modality.APPLICATION_MODAL);

        setResizable(true);
        setTitle("Export Diagram");
    }

    // **********************************************************************
    // *
    // * Show
    // *
    // **********************************************************************

    /**
     * Show the dialog. This method displays the dialog, manages the user's
     * selections, and executes the export.
     */
    public void showDialog()
    {
        // Get the real pixel size of the canvas

        Canvas canvas = window.getCanvas();

        Screen screen = window.getScreen();

        int width = Util.toInt(canvas.getWidth() * screen.getOutputScaleX());
        int height = Util.toInt(canvas.getHeight() * screen.getOutputScaleY());

        // Set the dimensions in the dialog

        controller.setupDimensions(width, height);

        showAndWait()
            .filter(response -> response == ButtonType.NEXT)
            .ifPresent(response -> saveSettingsAndExport());
    }

    // **********************************************************************
    // *
    // * Private methods
    // *
    // **********************************************************************

    /**
     * Collect all the settings and export the image. We save some settings
     * to use as default the next time around.
     */
    private void saveSettingsAndExport() {
        controller.saveSettings();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Diagram");

        // Set up the file chooser

        int imageFormat = PreferencesManager.getImageFormat();

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(fileTypes[imageFormat], fileExtensions[imageFormat]));
        if (extraExtensions[imageFormat] != null) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(fileTypes[imageFormat], extraExtensions[imageFormat]));
        }

        fileChooser.setInitialDirectory(window.getDefaultDirectory(Gamma.FileType.IMAGE));
        File selectedFile = fileChooser.showSaveDialog(window);

        if (selectedFile != null) {
            export(imageFormat, selectedFile);
        }
    }

    /**
     *  Export the image.
     *
     * @param imageFormat The desired image format.
     * @param exportFile The file to export to.
     */
    @SuppressWarnings("null")
    private void export(int imageFormat, File exportFile)
    {
        // Capture the canvas

        Canvas canvas = window.getCanvas();

        int[] dimensions = controller.getDimensions();
        int width = dimensions[0];
        int height = dimensions[1];

        double scale = width / canvas.getWidth();

        SnapshotParameters snapshotParams = new SnapshotParameters();
        snapshotParams.setTransform(new Scale(scale, scale));

        // Capture the image

        WritableImage image = new WritableImage(width, height);
        canvas.snapshot(snapshotParams, image);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        // Select a writer by type

        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(formatNames[imageFormat]);
        ImageWriter writer = null;
        if (iter.hasNext()) {
             writer = iter.next();
        }
        if (writer == null) {
            window.showTextAreaAlert(
                AlertType.ERROR, "Format Unavailable", "Format Unavailable",
                "The format you selected in not available on this system",
                true);
            return;
        }

        // Add parameters if needed

        ImageWriteParam param = null;
        if (imageFormat == ExportDiagramDialog.ImageType.JPG.getValue()) {
            param = new JPEGImageWriteParam(null);
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(PreferencesManager.getImageCompression() / 100.0F);
            if (PreferencesManager.getImageProgressive()) {
                param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            }
            else {
                param.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
            }

            if (bufferedImage.getColorModel().hasAlpha()) {
                BufferedImage newImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = newImage.createGraphics();
                g.drawImage(bufferedImage, 0, 0, null);
                bufferedImage = newImage;
                g.dispose();
            }
        }
        else if (imageFormat == ExportDiagramDialog.ImageType.TIFF.getValue()) {
            param = writer.getDefaultWriteParam();
        }

        // Write the file

        try {

            // Tell the writer to write to our file

            ImageOutputStream imageOutputStream;
            imageOutputStream = ImageIO.createImageOutputStream(exportFile);
            writer.setOutput(imageOutputStream);

            IIOMetadata metadata = writer.getDefaultImageMetadata(
                ImageTypeSpecifier.createFromBufferedImageType(
                    bufferedImage.getType()), param);

            // Save the DPI

            int ppi = controller.getPPI();
            switch (imageFormat) {
                case 0 -> { }
                case 1 -> setJPGPPI(metadata, ppi);
                case 2 -> setPNGPPI(metadata, ppi);
                case 3 -> metadata = setTIFFPPI(metadata, ppi);
            }

            writer.write(null, new IIOImage(bufferedImage, null, metadata), param);

            imageOutputStream.close();
            writer.dispose();
        }
        catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Write Error");
            alert.setHeaderText("Write Error");
            alert.setContentText("Write failed!\n" + e.getLocalizedMessage());
            alert.showAndWait();
        }

        // If we succeeded, change the default directory for images for the
        // window parent

        window.setDefaultDirectory(Gamma.FileType.IMAGE, exportFile);
    }

    /**
     * Set some metadata for JPG images.
     *
     * @param metadata The metadata structure to write to.
     * @param ppi The PPI of the image.
     *
     * @throws IIOInvalidTreeException if the IIO tree is invalid.
     */
    private void setJPGPPI(IIOMetadata metadata, int ppi) throws IIOInvalidTreeException
    {
        Element tree = (Element)metadata.getAsTree("javax_imageio_jpeg_image_1.0");
        Element jfif = (Element)tree.getElementsByTagName("app0JFIF").item(0);
        jfif.setAttribute("Xdensity", Integer.toString(ppi));
        jfif.setAttribute("Ydensity", Integer.toString(ppi));
        jfif.setAttribute("resUnits", "1"); // density is dots per inch
        metadata.mergeTree("javax_imageio_jpeg_image_1.0", tree);
    }

    /**
     *  Set some metadata for PNG images.
     *
     * @param metadata The metadata structure to write to.
     * @param ppi The PPI of the image.
     *
     * @throws IIOInvalidTreeException if the IIO tree is invalid.
     */
    private void setPNGPPI(IIOMetadata metadata, int ppi) throws IIOInvalidTreeException
    {
        // for PNG, it's dots per mm

        double dotsPerMilli = ppi / 25.4;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

    /**
     *  Set some metadata for TIFF images.
     *
     * @param metadata The metadata structure to write to.
     * @param ppi The PPI of the image.
     *
     * @return The revised metadata.
     * @throws IIOInvalidTreeException if the IIO tree is invalid.
     */
    private IIOMetadata setTIFFPPI(IIOMetadata metadata, int ppi) throws IIOInvalidTreeException
    {
        // Convert default metadata to TIFF metadata

        TIFFDirectory dir = TIFFDirectory.createFromMetadata(metadata);

        // Get {X,Y} resolution tags

        BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
        TIFFTag tagXRes = base.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION);
        TIFFTag tagYRes = base.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION);

        // Create {X,Y} resolution fields

        TIFFField fieldXRes = new TIFFField(tagXRes, TIFFTag.TIFF_RATIONAL, 1, new long[][] { { ppi, 1 } });
        TIFFField fieldYRes = new TIFFField(tagYRes, TIFFTag.TIFF_RATIONAL, 1, new long[][] { { ppi, 1 } });

        // Add {X,Y} resolution fields to TIFFDirectory

        dir.addTIFFField(fieldXRes);
        dir.addTIFFField(fieldYRes);

        // Add unit field to TIFFDirectory (change to RESOLUTION_UNIT_CENTIMETER if necessary)

        dir.addTIFFField(new TIFFField(base.getTag(BaselineTIFFTagSet.TAG_RESOLUTION_UNIT), BaselineTIFFTagSet.RESOLUTION_UNIT_INCH));

        // Return TIFF metadata so that it can be picked up by the IIOImage

        return dir.getAsMetadata();
    }
}
