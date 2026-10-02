package com.example.creating_dependency_lab10;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Comanda {
    private final String rezumat;
    private final double total;
    private final BooleanProperty selectat = new SimpleBooleanProperty(this, "selectat", true);

    public Comanda(String rezumat, double total) {
        this.rezumat = rezumat;
        this.total = total;
    }

    public double getTotal() { return total; }
    public final boolean isSelectat() { return selectat.get(); }
    public BooleanProperty selectatProperty() { return selectat; }

    @Override public String toString() {
        return String.format("%s  →  de plată: %.2f lei", rezumat, total);
    }
}