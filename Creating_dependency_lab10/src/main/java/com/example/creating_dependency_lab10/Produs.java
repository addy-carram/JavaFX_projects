package com.example.creating_dependency_lab10;

import javafx.beans.property.*;

public class Produs {

    // 1. Proprietate de bază
    private final StringProperty nume =
            new SimpleStringProperty(this, "nume", "Produs nou");
    public final String getNume() { return nume.get(); }
    public final void setNume(String v) { nume.set(v); }
    public StringProperty numeProperty() { return nume; }

    // 2. Proprietate cu validare (suprascriem set)
    private final DoubleProperty pret = new SimpleDoubleProperty(this, "pret", 0) {
        @Override public void set(double v) { super.set(Math.max(0, v)); }
    };
    public final double getPret() { return pret.get(); }
    public final void setPret(double v) { pret.set(v); }
    public DoubleProperty pretProperty() { return pret; }

    // TODO 2.1: cantitate (0..99, implicit 1)
    private final IntegerProperty cantitate = new SimpleIntegerProperty(this, "cantitate", 1) {
        @Override public void set(int v) { super.set(Math.max(0, Math.min(99, v))); }
    };
    public final int getCantitate() { return cantitate.get(); }
    public final void setCantitate(int v) { cantitate.set(v); }
    public IntegerProperty cantitateProperty() { return cantitate; }

    // 3. Proprietate read-only calculată
    private final ReadOnlyDoubleWrapper total =
            new ReadOnlyDoubleWrapper(this, "total");
    public final double getTotal() { return total.get(); }
    public ReadOnlyDoubleProperty totalProperty() { return total.getReadOnlyProperty(); }

    // 4. Proprietate leneșă
    private StringProperty nota;
    private String _nota = "";
    public final String getNota() { return nota == null ? _nota : nota.get(); }
    public final void setNota(String v) {
        if (nota == null) _nota = v; else nota.set(v);
    }
    public StringProperty notaProperty() {
        if (nota == null) nota = new SimpleStringProperty(this, "nota", _nota);
        return nota;
    }

    public Produs(String n, double p, int c) {
        setNume(n); setPret(p); setCantitate(c);
        // TODO 2.2
        total.bind(pret.multiply(cantitate));
    }
}