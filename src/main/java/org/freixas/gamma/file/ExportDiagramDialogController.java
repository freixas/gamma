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

import org.freixas.gamma.math.Util;
import org.freixas.gamma.preferences.PreferencesManager;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
/**
 * FXML Controller for Export Diagrams.
 *
 * @author Antonio Freixas
 */
public class ExportDiagramDialogController implements Initializable
{

    @FXML
    private DialogPane dialogPane;
    @FXML
    private ToggleGroup format;
    @FXML
    private RadioButton gif;
    @FXML
    private RadioButton jpg;
    @FXML
    private RadioButton png;
    @FXML
    private RadioButton tiff;
    @FXML
    private Slider compression;
    @FXML
    private CheckBox progressive;
    @FXML
    private TextField xPixels;
    @FXML
    private TextField yPixels;
    @FXML
    private TextField xInches;
    @FXML
    private TextField yInches;
    @FXML
    private TextField xMM;
    @FXML
    private TextField yMM;
    @FXML
    private TextField ppi;

    private double aspectRatio;

    private int width;
    private int height;
    private int ppiValue;

    private int lastWidth;
    private int lastHeight;
    private double lastXInches;
    private double lastYInches;
    private double lastXMM;
    private double lastYMM;


    /**
     * Initializes the controller class.
     */

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        dialogPane.setExpandableContent(null);

        RadioButton[] radioButtons = { gif, jpg, png, tiff };
        int imageFormat = PreferencesManager.getImageFormat();
        radioButtons[imageFormat].setSelected(true);

        compression.setValue(PreferencesManager.getImageCompression());
        progressive.setSelected(PreferencesManager.getImageProgressive());

        setJPGState(imageFormat == ExportDiagramDialog.ImageType.JPG.getValue());

        radioButtons[ExportDiagramDialog.ImageType.JPG.getValue()]
            .selectedProperty()
            .addListener((obj, oldValue, value) -> setJPGState(value));

        setGIFState(imageFormat == ExportDiagramDialog.ImageType.GIF.getValue());

        radioButtons[ExportDiagramDialog.ImageType.GIF.getValue()]
            .selectedProperty()
            .addListener((obj, oldValue, value) -> setGIFState(value));

        UnaryOperator<TextFormatter.Change> integerFormatter =
            change -> {
                if (change.getControlNewText().length() == 0) return change;
                if (change.getControlNewText().matches("\\d+")) return change;
                change.setText("");
                change.setRange(change.getRangeStart(), change.getRangeStart());
                return change;
            };

        UnaryOperator<TextFormatter.Change> doubleFormatter =
            change -> {
                if (change.getControlNewText().length() == 0) return change;
                if (change.getControlNewText().matches("(\\d+\\.?)|(\\d*\\.\\d+)")) return change;
                change.setText("");
                change.setRange(change.getRangeStart(), change.getRangeStart());
                return change;
            };

        xPixels.setTextFormatter(new TextFormatter<Integer>(integerFormatter));
        yPixels.setTextFormatter(new TextFormatter<Integer>(integerFormatter));
        xInches.setTextFormatter(new TextFormatter<Double>(doubleFormatter));
        yInches.setTextFormatter(new TextFormatter<Double>(doubleFormatter));
        xMM.setTextFormatter(new TextFormatter<Double>(doubleFormatter));
        yMM.setTextFormatter(new TextFormatter<Double>(doubleFormatter));
        ppi.setTextFormatter(new TextFormatter<Integer>(integerFormatter));

        xPixels.focusedProperty().addListener( (obj, oldValue, newValue) -> {
            if (!newValue) handleXPixels(null);
        });
        yPixels.focusedProperty().addListener( (obj, oldValue, newValue) -> {
            if (!newValue) handleYPixels(null);
        });
        xInches.focusedProperty().addListener( (obj, oldValue, newValue) -> {
            if (!newValue) handleXInches(null);
        });
        yInches.focusedProperty().addListener( (obj, oldValue, newValue) -> {
            if (!newValue) handleYInches(null);
        });
        xMM.focusedProperty().addListener( (obj, oldValue, newValue) -> {
            if (!newValue) handleXMM(null);
        });
        yMM.focusedProperty().addListener( (obj, oldValue, newValue) -> {
            if (!newValue) handleYMM(null);
        });
        ppi.focusedProperty().addListener( (obj, oldValue, newValue) -> {
            if (!newValue) handlePPI(null);
        });
    }

    /**
     * In the dialog, display the initial dimensions of the exported image
     * using a variety of units (pixels, inches, mm).
     *
     * @param width The screen pixel width.
     * @param height The screen pixel height.
     */
    public void setupDimensions(int width, int height)
    {
        this.width = width;
        this.height = height;
        ppiValue = PreferencesManager.getImagePPI();

        aspectRatio = (double)width / (double)height;

        setSizes(width, height);

        ppi.setText(Integer.toString(ppiValue));
    }

    /**
     * Get the dimensions of the image in pixels.
     *
     * @return The dimensions of the image in pixels.
     */
    public int[] getDimensions()
    {
        return new int[] { width, height };
    }

    /**
     * Get the PPI of the image.
     *
     * @return The PPI of the image.
     */
    public int getPPI()
    {
        return ppiValue;
    }

    /**
     * Save the current settings as a future default.
     */
    public void saveSettings()
    {
        RadioButton[] radioButtons = { gif, jpg, png, tiff };
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
                PreferencesManager.setImageFormat(i);
                break;
            }
        }

        double compressionValue = compression.getValue();
        PreferencesManager.setImageCompression((float) compressionValue);

        PreferencesManager.setImageProgressive(progressive.isSelected());
        if (!radioButtons[ExportDiagramDialog.ImageType.GIF.getValue()].isSelected()) {
            PreferencesManager.setImagePPI(ppiValue);
        }
    }

    /**
     * Enable or disable settings based on whether JPG is chosen as the image
     * format.
     *
     * @param enableJPG True if JPG is the image format.
     */
    private void setJPGState(boolean enableJPG)
    {
        compression.setDisable(!enableJPG);
        progressive.setDisable(!enableJPG);
    }

    /**
     * Enable or disable settings based on whether GIF is chosen as the image
     * format.
     *
     * @param enableGIF True if GIF is the image format.
     */
    private void setGIFState(boolean enableGIF)
    {
        xInches.setDisable(enableGIF);
        yInches.setDisable(enableGIF);
        xMM.setDisable(enableGIF);
        yMM.setDisable(enableGIF);
        ppi.setDisable(enableGIF);
    }

    /**
     * Handle events for the xPixels field.
     *
     * @param event The event to handle.
     */
    @FXML
    private void handleXPixels(ActionEvent event)
    {
        int x = getIntField(xPixels, lastWidth);
        setWidth(x);
    }

    /**
     * Handle events for the yPixels field.
     *
     * @param event The event to handle.
     */
    @FXML
    private void handleYPixels(ActionEvent event)
    {
        int y = getIntField(yPixels, lastHeight);
        setHeight(y);
    }

    /**
     * Handle events for the xInches field.
     *
     * @param event The event to handle.
     */
    @FXML
    private void handleXInches(ActionEvent event)
    {
        double xI = getDoubleField(xInches, lastXInches);
        int x = Math.max(1, Util.toInt(Math.round(xI * ppiValue)));
        setWidth(x);
    }

    /**
     * Handle events for the yInches field.
     *
     * @param event The event to handle.
     */
    @FXML
    private void handleYInches(ActionEvent event)
    {
        double yI = getDoubleField(xInches, lastYInches);
        int y = Math.max(1, Util.toInt(Math.round(yI * ppiValue)));
        setHeight(y);
    }

    /**
     * Handle events for the xMM field.
     *
     * @param event The event to handle.
     */
    @FXML
    private void handleXMM(ActionEvent event)
    {
        double xM = getDoubleField(xMM, lastXMM) / 25.4;
        int x = Math.max(1, Util.toInt(Math.round(xM * ppiValue)));
        setWidth(x);
    }

    /**
     * Handle events for the yMM field.
     *
     * @param event The event to handle.
     */
    @FXML
    private void handleYMM(ActionEvent event)
    {
        double yM = getDoubleField(yMM, lastYMM) / 25.4;
        int y = Math.max(1, Util.toInt(Math.round(yM * ppiValue)));
        setHeight(y);
    }

    /**
     * Handle events for the PPI field.
     *
     * @param event The event to handle.
     */
    @FXML
    private void handlePPI(ActionEvent event)
    {
        ppiValue = Math.max(1, Integer.parseInt(ppi.getText()));
        setSizes(width, height);
    }

    /**
     * Get the value of a text field that should hold an integer.
     *
     * @param field The text field whose value we want.
     * @param lastValue The last valid value for the field.
     *
     * @return The integer value of the field. If the field is blank, we use
     * the last valid value; otherwise, we get whatever integer value the string
     * converts to, but not less than 1.
     */
    private int getIntField(TextField field, int lastValue)
    {
        if (field.getText().length() == 0) return lastValue;
        return Math.max(1, Integer.parseInt(field.getText()));
    }

    /**
     * Get the value of a text field that should hold a double.
     *
     * @param field The text field whose value we want.
     * @param lastValue The last valid value for the field.
     *
     * @return The double value of the field. If the field is blank, we use
     * the last valid value; otherwise, we get whatever double value the string
     * converts to, but not less than 1.
     */
    private double getDoubleField(TextField field, double lastValue)
    {
        if (field.getText().length() == 0) return lastValue;
        return Math.max(1, Double.parseDouble(field.getText()));
    }

    /**
     * Given a width in pixels, find the corresponding height, and then set
     * all the other equivalent sizes.
     *
     * @param width A width in pixels.
     */
    private void setWidth(int width)
    {
        int height = Util.toInt(Math.round(width / aspectRatio));
        setSizes(width, height);
    }

    /**
     * Given a height in pixels, find the corresponding width, and then set
     * all the other equivalent sizes.
     *
     * @param height A height in pixels.
     */
    private void setHeight(int height)
    {
        int x = Util.toInt(Math.round(height * aspectRatio));
        setSizes(x, height);
    }

    /**
     * Given a width and height in pixels, set the equivalent sizes in inches
     * and mm using the PPI to calculate.
     *
     * @param width The width in pixels.
     * @param height The height in pixels.
     */
    private void setSizes(int width, int height)
    {
        this.width = width;
        this.height = height;

        xPixels.setText(Integer.toString(width));
        yPixels.setText(Integer.toString(height));
        double xI = (double)width /(double)ppiValue;
        double yI = (double)height /(double)ppiValue;
        double xM = xI * 25.4;
        double yM = yI * 25.5;
        xInches.setText((Util.toString(xI, 2)));
        yInches.setText((Util.toString(yI, 2)));
        xMM.setText((Util.toString(xM, 2)));
        yMM.setText((Util.toString(yM, 2)));

        lastWidth = width;
        lastHeight = height;
        lastXInches = xI;
        lastYInches = yI;
        lastXMM = xM;
        lastYMM = yM;
    }
}
