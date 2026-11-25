package com.example.cinema_app_lab25_lab26;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;

/** Contextul datelor: lista observabilă a seanțelor + seanța selectată (current). */
public class DataModel {

    /** Lista ORIGINALĂ. Adăugările și ștergerile se fac doar aici. */
    private final ObservableList<Seanta> seante = FXCollections.observableArrayList(
            // EXTRACTOR: spune listei ce proprietăți ale unui element trebuie urmărite
            s -> new Observable[] {
                    s.filmProperty(),
                    s.salaProperty(),
                    s.oraProperty(),
                    s.pretProperty(),
                    s.locuriLibereProperty(),
                    s.genProperty(),
                    s.ratingProperty()
            });

    /** Seanța selectată în acest moment (poate fi null). */
    private final ObjectProperty<Seanta> current = new SimpleObjectProperty<>();

    public ObservableList<Seanta> getSeante() {
        return seante;
    }

    /** Încarcă datele de la provider (înlocuiește tot conținutul printr-o singură notificare). */
    public void setAll(Collection<Seanta> data) {
        seante.setAll(data);
    }

    // --- current ---
    public Seanta getCurrent() { return current.get(); }
    public void setCurrent(Seanta value) { current.set(value); }
    public ObjectProperty<Seanta> currentProperty() { return current; }
}