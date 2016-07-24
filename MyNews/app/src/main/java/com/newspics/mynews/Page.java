package com.newspics.mynews;

/**
 * Created by Abhi on 2/27/2016.
 */
public class Page {
    private int id;
    private  int type;//type => 0=text; 1=video; 2=only image

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
