/*
    Copyright 2025 Dmitry Isaenko
     
    This file is part of Tihwin.

    Tihwin is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Tihwin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Tihwin.  If not, see <https://www.gnu.org/licenses/>.
 */
package tihwin;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static tihwin.ScaleUi.ScalePolicy.DECREASE;
import static tihwin.ScaleUi.ScalePolicy.INCREASE;
import static tihwin.ScaleUi.ScalePolicy.INIT;

public class ScaleUi {

    public enum ScalePolicy {
        INCREASE,
        DECREASE,
        INIT
    }

    private static final String[] DEFAULTS = new String[] {
            "Label.font",
            "Button.font",
            "ComboBox.font",
            "TextField.font",
            "List.font",
            "Tree.font",
            "TableHeader.font"
    };

    public static void applyInitialScale(List<Component> components) {
        if (AwesomeMediator.getScaleValue() == 0)
            return;
        for (Component component : components)
            applyInitialScale(component);
        applyOnDefaults(INIT);
    }
    public static void applyInitialScale(Component component) {
        applyOn(component, AwesomeMediator.getScaleValue());
    }

    public static void increaseScale(List<Component> components) {
        for (Component component : components)
            increaseScale(component);
        applyOnDefaults(INCREASE);
    }
    private static void increaseScale(Component component) {
        applyOn(component, +1);
    }

    public static void decreaseScale(List<Component> components) {
        if (AwesomeMediator.getScaleValue() <= 0)
            return;
        for (Component component : components)
            decreaseScale(component);
        applyOnDefaults(DECREASE);
    }
    private static void decreaseScale(Component component) {
        applyOn(component, -1);
    }

    private static void applyOn(Component component, int factor) {
        Font defaultFont = component.getFont();
        component.setFont(new Font(defaultFont.getName(), defaultFont.getStyle(), defaultFont.getSize() + factor));
    }

    private static void applyOnDefaults(ScalePolicy policy){
        switch (policy){
            case INIT:
                for (String defaultElement: DEFAULTS)
                    initDefault(defaultElement);
                break;
            case INCREASE:
                for (String defaultElement: DEFAULTS)
                    increaseDefault(defaultElement);
                AwesomeMediator.increaseScaleValue();
                break;
            case DECREASE:
                for (String defaultElement: DEFAULTS)
                    decreaseDefault(defaultElement);
                AwesomeMediator.decreaseScaleValue();
                break;
        }
    }

    private static void initDefault(String name){
        applyOnDefault(name, AwesomeMediator.getScaleValue());
    }

    private static void increaseDefault(String name){
        applyOnDefault(name, +1);
    }

    private static void decreaseDefault(String name){
        applyOnDefault(name, -1);
    }

    private static void applyOnDefault(String name, int factor){
        Font defaultFont = UIManager.getDefaults().getFont(name);
        UIManager.getDefaults().put(name, new Font(defaultFont.getName(), defaultFont.getStyle(),
                defaultFont.getSize()+factor));
    }
}
