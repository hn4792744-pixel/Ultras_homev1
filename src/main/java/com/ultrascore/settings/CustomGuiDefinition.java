package com.ultrascore.settings;

import java.util.ArrayList;
import java.util.List;

public class CustomGuiDefinition {

    public final String id;
    public String title = "Menu";
    public int size = 27;
    public final List<CustomGuiButton> buttons = new ArrayList<>();

    public CustomGuiDefinition(String id) {
        this.id = id;
    }
}
