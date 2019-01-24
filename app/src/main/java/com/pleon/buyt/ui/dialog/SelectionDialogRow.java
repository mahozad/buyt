package com.pleon.buyt.ui.dialog;

import java.io.Serializable;

import androidx.annotation.DrawableRes;

public class SelectionDialogRow implements Serializable {

    private String name;
    @DrawableRes private int image;

    public SelectionDialogRow(String name, int image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
