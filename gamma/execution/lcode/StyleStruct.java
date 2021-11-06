/*
 * Copyright (C) 2021 Antonio Freixas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gamma.execution.lcode;

import gamma.execution.ExecutionException;
import gamma.execution.HCodeEngine;

/**
 *
 * @author Antonio Freixas
 */
public class StyleStruct extends Struct
{
    public Color color = new Color(Color.black);
    public Color backgroundColor = new Color(Color.white);
    public int lineWidth = 2;
    public String lineStyle = "solid";
    public String arrow = "none";
    public int diameter = 2;
    public String eventShape = "circle";
    public String font = "Arial";
    public String weight = "normal";
    public String slant = "normal";
    public int size = 10;
    public int padding = 2;
    public String anchor = "MC";
    public boolean ticks = true;
    public boolean tickLabels = true;
    public int tickThickness = -9999;          // Use lineWidth
    public int majorTickThickness = -9999;     // Use lineWidth

    public StyleStruct()
    {
    }

    public StyleStruct(StyleStruct other)
    {
        this.color = new Color(other.color);
        this.backgroundColor = new Color(Color.white);
        this.lineWidth = other.lineWidth;
        this.lineStyle = other.lineStyle;
        this.arrow = other.arrow;
        this.diameter = other.diameter;
        this.eventShape = other.eventShape;
        this.font = other.font;
        this.weight = other.weight;
        this.slant = other.slant;
        this.size = other.size;
        this.padding = other.padding;
        this.anchor = other.anchor;
        this.ticks = other.ticks;
        this.tickLabels = other.tickLabels;
        this.tickThickness = other.tickThickness;          // Use lineWidth
        this.majorTickThickness = other.majorTickThickness;
    }

    public void lineWidthRangeCheck()
    {
        rangeCheck("lineWidth", lineWidth, 1, 20);
    }

    public void diameterRangeCheck()
    {
        rangeCheck("diameter", diameter, 1, 20);
    }

    public void sizeRangeCheck()
    {
        if (size < 1) {
            throw new ExecutionException("The value used for property 'size' is out of range");
        }
    }

    public void tickThicknessRangeCheck(HCodeEngine engine)
    {
        if (tickThickness == -9999) return;
        rangeCheck("tickThickness", tickThickness, 1, 20);
    }

    public void majorTickThicknessRangeCheck(HCodeEngine engine)
    {
        if (majorTickThickness == -9999) return;
        rangeCheck("majorTickThickness", majorTickThickness, 1, 20);
    }

//    public final void setFromPropertyInfo()
//    {
//        styleColor =         new Color((double)PropertyInfo.getPropertyInfo("style", "styleColor").getDefault());
//        backgroundColor =    new Color((double)PropertyInfo.getPropertyInfo("style", "backgroundColor").getDefault());
//        lineWidth =          Util.toInt((double)PropertyInfo.getPropertyInfo("style", "lineWidth").getDefault());
//        lineStyle =          (String)PropertyInfo.getPropertyInfo("style", "lineStyle").getDefault();
//        arrow =              (String)PropertyInfo.getPropertyInfo("style", "arrow").getDefault();
//        diameter =           Util.toInt((double)PropertyInfo.getPropertyInfo("style", "diameter").getDefault());
//        eventShape =         (String)PropertyInfo.getPropertyInfo("style", "eventShape").getDefault();
//        font =               (String)PropertyInfo.getPropertyInfo("style", "font").getDefault();
//        weight =             (String)PropertyInfo.getPropertyInfo("style", "weight").getDefault();
//        slant =              (String)PropertyInfo.getPropertyInfo("style", "slant").getDefault();
//        size =               Util.toInt((double)PropertyInfo.getPropertyInfo("style", "size").getDefault());
//        padding =            Util.toInt((double)PropertyInfo.getPropertyInfo("style", "padding").getDefault());
//        anchor =             (String)PropertyInfo.getPropertyInfo("style", "anchor").getDefault();
//        ticks =              Util.toBoolean((double)PropertyInfo.getPropertyInfo("style", "ticks").getDefault());
//        tickLabels =         Util.toBoolean((double)PropertyInfo.getPropertyInfo("style", "tickLabels").getDefault());
//        tickThickness =      Util.toInt((double)PropertyInfo.getPropertyInfo("style", "tickThickness").getDefault());
//        majorTickThickness = Util.toInt((double)PropertyInfo.getPropertyInfo("style", "majorTickThickness").getDefault());
//     }
//
//    @Override
//    public void setFromPropertyList(PropertyList list)
//    {
//        for (int i = 0; i < list.size(); i++) {
//            Property property = list.getProperty(i);
//            String name = property.getName();
//            PropertyInfo info = PropertyInfo.getPropertyInfo("style", name);
//            Object value = property.getValue();
//            if (info != null) {
//                switch (name) {
//                    case "styleColor" ->
//                        styleColor = new Color((Double) value);
//                    case "backgroundColor" ->
//                        backgroundColor = new Color((Double) value);
//                    case "lineWidth" ->
//                        lineWidth = Util.toInt((Double) value);
//                    case "lineStyle" ->
//                        lineStyle = (String) value;
//                    case "arrow" ->
//                        arrow = (String) value;
//                    case "diameter" ->
//                        diameter = Util.toInt((Double) value);
//                    case "eventShape" ->
//                        eventShape = (String) value;
//                    case "font" ->
//                        font = (String) value;
//                    case "weight" ->
//                        weight = (String) value;
//                    case "slant" ->
//                        slant = (String) value;
//                    case "size" ->
//                        size = Util.toInt((Double) value);
//                    case "padding" ->
//                        padding = Util.toInt((Double) value);
//                    case "anchor" ->
//                        anchor = (String) value;
//                    case "ticks" ->
//                        ticks = Util.toBoolean((Double) value);
//                    case "tickLabels" ->
//                        tickLabels = Util.toBoolean((Double) value);
//                    case "tickThickness" ->
//                        tickThickness = Util.toInt((Double) value);
//                    case "majorTickThickness" ->
//                        majorTickThickness = Util.toInt((Double) value);
//                    default -> {
//                        /* DO NOTHING */ }
//                }
//            }
//        }
//    }
}
