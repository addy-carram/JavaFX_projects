package com.example.clients_row_lab14;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuItem;

public class Command {
    private final String nume;
    private final Runnable actiune;
    private final BooleanProperty activa = new SimpleBooleanProperty(true);

    public Command(String nume, Runnable actiune) {
        this.nume = nume;
        this.actiune = actiune;
    }

    public void execute() {
        if (activa.get()) actiune.run();
    }

    public BooleanProperty activaProperty() { return activa; }
    public String getNume() { return nume; }

    public void bindTo(ButtonBase b) {
        b.setOnAction(e -> execute());
        b.disableProperty().bind(activa.not());
    }

    public void bindTo(MenuItem m) {
        m.setOnAction(e -> execute());
        m.disableProperty().bind(activa.not());
    }
}