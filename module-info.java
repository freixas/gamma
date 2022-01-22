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

module gamma
{
    requires java.prefs;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    opens gamma to javafx.fxml, javafx.graphics;
    opens gamma.execution to javafx.fxml;
    opens gamma.file to javafx.fxml;
    opens gamma.preferences to javafx.fxml;
//    opens gamma.print to javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;
    requires java.base;

}
