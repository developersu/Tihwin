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
        applyOnAll(INIT, components);
    }
    public static void applyInitialScale(Component component) {
        applyOn(component, AwesomeMediator.getScaleValue());
    }

    public static void increaseScale(List<Component> components) {
        applyOnAll(INCREASE, components);
    }

    public static void decreaseScale(List<Component> components) {
        applyOnAll(DECREASE, components);
    }

    private static void applyOnAll(ScalePolicy policy, List<Component> components) {
        switch (policy) {
            case INIT:
                if (AwesomeMediator.getScaleValue() == 0)
                    return;
                for (Component component : components)
                    applyInitialScale(component);
                applyOnDefaults(INIT);
                break;
            case INCREASE:
                for (Component component : components)
                    applyOn(component, +1);
                applyOnDefaults(INCREASE);
                break;
            case DECREASE:
                if (AwesomeMediator.getScaleValue() <= 0)
                    return;
                for (Component component : components)
                    applyOn(component, -1);
                applyOnDefaults(DECREASE);
                break;
        }
    }
    private static void applyOn(Component component, int factor) {
        Font defaultFont = component.getFont();
        component.setFont(new Font(defaultFont.getName(), defaultFont.getStyle(), defaultFont.getSize() + factor));
    }

    private static void applyOnDefaults(ScalePolicy policy){
        switch (policy){
            case INIT:
                for (String defaultElement: DEFAULTS)
                    applyOnDefault(defaultElement, AwesomeMediator.getScaleValue());
                break;
            case INCREASE:
                for (String defaultElement: DEFAULTS)
                    applyOnDefault(defaultElement, +1);
                AwesomeMediator.increaseScaleValue();
                break;
            case DECREASE:
                for (String defaultElement: DEFAULTS)
                    applyOnDefault(defaultElement, -1);
                AwesomeMediator.decreaseScaleValue();
                break;
        }
    }
    private static void applyOnDefault(String componentName, int factor){
        Font defaultFont = UIManager.getDefaults().getFont(componentName);
        UIManager.getDefaults().put(componentName, new Font(defaultFont.getName(), defaultFont.getStyle(),
                defaultFont.getSize()+factor));
    }
}
