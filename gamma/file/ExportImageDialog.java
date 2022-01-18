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
package gamma.file;

import gamma.Gamma;
import gamma.MainWindow;
import gamma.math.Util;
import gamma.preferences.PreferencesManager;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
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
import javafx.stage.Stage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

/**
 *
 * @author Antonio Freixas
 */
public class ExportImageDialog extends Dialog<ButtonType>
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

    private final DialogPane dialogPane;
    private final ExportImageDialogController controller;

    // **********************************************************************
    // *
    // * Constructor
    // *
    // **********************************************************************

    /**
     * Create an export images dialog.
     */
    public ExportImageDialog() throws Exception
    {
        // Load the view (FXML file) and controller. Get a reference to the controller.

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/gamma/resources/ExportImageDialog.fxml"));
        dialogPane = loader.load();
        controller = (ExportImageDialogController)loader.getController();
        setDialogPane(dialogPane);
    }

    public void show(MainWindow window)
    {
        initOwner(window);
        initModality(Modality.APPLICATION_MODAL);

        setResizable(true);
        setTitle("Set Preferences");

        showAndWait()
            .filter(response -> response == ButtonType.NEXT)
            .ifPresent(response -> saveSettingsAndExport(window));
    }

    private void saveSettingsAndExport(MainWindow window) {
        controller.saveSettings();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Image");

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
            export(window, imageFormat, selectedFile);
        }
    }

    @SuppressWarnings("null")
    private void export(MainWindow window, int imageFormat, File exportFile)
    {
        // Capture the canvas

        Canvas canvas = window.getCanvas();

        // We need to scale our capture so that it comes out correctly in
        // pixel size

        Stage stage = (Stage)canvas.getScene().getWindow();
        Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1., 1.).get(0);

        int width = Util.toInt(canvas.getWidth() * screen.getOutputScaleX());
        int height = Util.toInt(canvas.getHeight() * screen.getOutputScaleY());

        SnapshotParameters snapshotParams = new SnapshotParameters();
        snapshotParams.setTransform(new Scale(screen.getOutputScaleX(), screen.getOutputScaleY()));

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

        // JPEG files need parameters

        ImageWriteParam param = null;
        if (imageFormat == ExportImageDialog.ImageType.JPG.getValue()) {
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

        // Write the file

        try {

            // Tell the writer to write to our file

            ImageOutputStream imageOutputStream;
            imageOutputStream = ImageIO.createImageOutputStream(exportFile);
            writer.setOutput(imageOutputStream);

            writer.write(null, new IIOImage(bufferedImage, null, null), param);

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
    }

}
