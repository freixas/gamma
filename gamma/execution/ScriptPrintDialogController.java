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
package gamma.execution;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

/**
 * FXML Controller class
 *
 * @author Antonio Freixas
 */
public class ScriptPrintDialogController implements Initializable
{
    @FXML
    private TextArea textArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button printButton;
    @FXML
    private Button closeButton;
    @FXML
    private GridPane buttonBar;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        textArea.setWrapText(false);
        textArea.setEditable(false);

        // Future enhancement: add save, print

        buttonBar.getChildren().remove(saveButton);
        buttonBar.getChildren().remove(printButton);
    }

    public void appendText(String str)
    {
        textArea.appendText(str);
    }

    public void clear()
    {
        textArea.clear();
    }

    @FXML
    private void handleSave(ActionEvent event)
    {
    }

    @FXML
    private void handlePrint(ActionEvent event)
    {
    }

    @FXML
    private void handleClose(ActionEvent event)
    {
        closeButton.getScene().getWindow().hide();
    }

//    private void print(String printStr)
//    {
//        // Create a default print job and get the default page size
//
//        PrinterJob job = PrinterJob.createPrinterJob();
//        PageLayout pgLayout = job.getJobSettings().getPageLayout();
//        double pageHeight = pgLayout.getPrintableHeight();
//        double pageWidth = pgLayout.getPrintableWidth();
//
//        // We need to paginate, but this only works for the default printer
//
//        Font font = new Font("System", 12);
//        Text textNode = new Text();
//        textNode.setBoundsType(TextBoundsType.LOGICAL);
//
//        textNode.setFont(font);
//        textNode.setText("X");
//        Bounds bounds = textNode.getLayoutBounds();
//
//        double lineHeight = bounds.getHeight();
//        double linesPerPage = pageHeight / lineHeight;
//
//        // Break up the printStr into chunks of linesPerPage lines or less
//
//        int numLines = 0; // ???
//        int numPages = Util.toInt((numLines - 1) / linesPerPage) + 1;
//
//        PageRange pgRange = new PageRange(1, numPages);
//        JobSettings jobSettings = job.getJobSettings();
//        jobSettings.setPageRanges(pgRange);
//
//        boolean printed = false;
//        for (PageRange pr : jobSettings.getPageRanges()) {                           // You could have multiple ranges (e.g. 1-5; 10-12), so you need this loop (this is an array of page ranges)
//            for (int p = pr.getStartPage(); p <= pr.getEndPage(); p++) {        // This loops through the selected page range
//                TextFlow text = new TextFlow(new Text(parsedText[p - 1]));     // Each page of the parsed String[] to pages is dumped into a TextFlow
//                text.setPrefHeight(pgLayout.getPrintableHeight());            // Each TextFlow is fit to the selected printers printable area (your page array should have parsed to this already)
//                text.setPrefWidth(pgLayout.getPrintableWidth());
//                printed = job.printPage(pgLayout, text);                     // The page is printed ... the key is not to end the job after each page if you want to print all pages and to a file... or you will have a file for each page.
//                if (!printed) {
//                    System.out.println("Printing failed."); // for testing
//                    break;
//                }
//            }
//        }
//        if (printed) {
//            job.endJob();        // The key to printing multiple pages... endJob() should only be called when all pages have been sent.
//        }
//    }
//
//    private void printPage(Node node)
//    {
//        jobStatus.textProperty().bind(pJob.jobStatusProperty().asString());   // Optional: part of message label bind for printer status messages
//
//        PrinterJob pJob = PrinterJob.createPrinterJob();
//        jobStatus.textProperty().bind(pJob.jobStatusProperty().asString());
//        PageLayout pageLayout = pJob.getJobSettings().getPageLayout();
//
//        boolean printed = pJob.printPage(pageLayout, node);
//        if (printed) {
//            pJob.endJob();
//        }
//        else {
//            jobStatus.textProperty().unbind();    // part of the message label binding if you so choose.
//        }
//    }

}
