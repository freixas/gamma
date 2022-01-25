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
 * FXML Controller class
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

    private int xValue;
    private int yValue;
    private int ppiValue;

    private int lastXPixels;
    private int lastYPixels;
    private double lastXInches;
    private double lastYInches;
    private double lastXMM;
    private double lastYMM;
    private int lastPPI;


    /**
     * Initializes the controller class.
     */

    @Override
    @SuppressWarnings("unchecked")
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
            .addListener((obj, oldValue, value) -> {
                setJPGState(value);
            });

        setGIFState(imageFormat == ExportDiagramDialog.ImageType.GIF.getValue());

        radioButtons[ExportDiagramDialog.ImageType.GIF.getValue()]
            .selectedProperty()
            .addListener((obj, oldValue, value) -> {
                setGIFState(value);
            });

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

        xPixels.setTextFormatter(new TextFormatter<>((UnaryOperator)integerFormatter));
        yPixels.setTextFormatter(new TextFormatter<>((UnaryOperator)integerFormatter));
        xInches.setTextFormatter(new TextFormatter<>((UnaryOperator)doubleFormatter));
        yInches.setTextFormatter(new TextFormatter<>((UnaryOperator)doubleFormatter));
        xMM.setTextFormatter(new TextFormatter<>((UnaryOperator)doubleFormatter));
        yMM.setTextFormatter(new TextFormatter<>((UnaryOperator)doubleFormatter));
        ppi.setTextFormatter(new TextFormatter<>((UnaryOperator)integerFormatter));

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

    public void setupDimensions(int x, int y)
    {
        xValue = x;
        yValue = y;
        ppiValue = PreferencesManager.getImagePPI();

        aspectRatio = (double)x / (double)y;

        setSizes(x, y);

        ppi.setText(Integer.toString(ppiValue));
    }

    public int[] getDimensions()
    {
        int[] dimensions = { xValue, yValue };
        return dimensions;
    }

    public int getPPI()
    {
        return ppiValue;
    }

    public void saveSettings()
    {
        RadioButton[] radioButtons = { gif, jpg, png, tiff };
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
                PreferencesManager.setImageFormat(i);
                break;
            }
        }

        Double compressionValue = compression.getValue();
        PreferencesManager.setImageCompression(compressionValue.floatValue());

        PreferencesManager.setImageProgressive(progressive.isSelected());
        if (!radioButtons[ExportDiagramDialog.ImageType.GIF.getValue()].isSelected()) {
            PreferencesManager.setImagePPI(ppiValue);
        }
    }

    private void setJPGState(boolean enableJPG)
    {
        compression.setDisable(!enableJPG);
        progressive.setDisable(!enableJPG);
    }

    private void setGIFState(boolean enableGIF)
    {
        xInches.setDisable(enableGIF);
        yInches.setDisable(enableGIF);
        xMM.setDisable(enableGIF);
        yMM.setDisable(enableGIF);
        ppi.setDisable(enableGIF);
    }

    @FXML
    private void handleXPixels(ActionEvent event)
    {
        int x = getIntField(xPixels, lastXPixels);
        setSizeX(x);
    }

    @FXML
    private void handleYPixels(ActionEvent event)
    {
        int y = getIntField(yPixels, lastYPixels);
        setSizeY(y);
    }

    @FXML
    private void handleXInches(ActionEvent event)
    {
        double xI = getDoubleField(xInches, lastXInches);
        int x = Math.max(1, Util.toInt(Math.round(xI * ppiValue)));
        setSizeX(x);
    }

    @FXML
    private void handleYInches(ActionEvent event)
    {
        double yI = getDoubleField(xInches, lastYInches);
        int y = Math.max(1, Util.toInt(Math.round(yI * ppiValue)));
        setSizeY(y);
    }

    @FXML
    private void handleXMM(ActionEvent event)
    {
        double xM = getDoubleField(xMM, lastXMM) / 25.4;
        int x = Math.max(1, Util.toInt(Math.round(xM * ppiValue)));
        setSizeX(x);
    }

    @FXML
    private void handleYMM(ActionEvent event)
    {
        double yM = getDoubleField(yMM, lastYMM) / 25.4;
        int y = Math.max(1, Util.toInt(Math.round(yM * ppiValue)));
        setSizeY(y);
    }

    @FXML
    private void handlePPI(ActionEvent event)
    {
        ppiValue = Math.max(1, Integer.valueOf(ppi.getText()));
        lastPPI = ppiValue;
        setSizes(xValue, yValue);
    }

    private int getIntField(TextField field, int lastValue)
    {
        if (field.getText().length() == 0) return lastValue;
        return Math.max(1, Integer.valueOf(field.getText()));
    }

    private double getDoubleField(TextField field, double lastValue)
    {
        if (field.getText().length() == 0) return lastValue;
        return Math.max(1, Double.valueOf(field.getText()));
    }

    private void setSizeX(int x)
    {
        int y = Util.toInt(Math.round(x / aspectRatio));
        setSizes(x, y);
    }

    private void setSizeY(int y)
    {
        int x = Util.toInt(Math.round(y * aspectRatio));
        setSizes(x, y);
    }

    private void setSizes(int x, int y)
    {
        xValue = x;
        yValue = y;

        xPixels.setText(Integer.toString(x));
        yPixels.setText(Integer.toString(y));
        double xI = (double)x /(double)ppiValue;
        double yI = (double)y /(double)ppiValue;
        double xM = xI * 25.4;
        double yM = yI * 25.5;
        xInches.setText((Util.toString(xI, 2)));
        yInches.setText((Util.toString(yI, 2)));
        xMM.setText((Util.toString(xM, 2)));
        yMM.setText((Util.toString(yM, 2)));

        lastXPixels = x;
        lastYPixels = y;
        lastXInches = xI;
        lastYInches = yI;
        lastXMM = xM;
        lastYMM = yM;
    }
}
