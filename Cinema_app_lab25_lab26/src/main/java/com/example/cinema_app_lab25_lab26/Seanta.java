package com.example.cinema_app_lab25_lab26;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Modelul unei seanțe de cinema, cu proprietăți observabile JavaFX. */
public class Seanta {

    // --- câmpurile obligatorii ---
    private final IntegerProperty id           = new SimpleIntegerProperty();
    private final StringProperty  film         = new SimpleStringProperty();
    private final StringProperty  sala         = new SimpleStringProperty();
    private final StringProperty  ora          = new SimpleStringProperty();   // HH:mm
    private final DoubleProperty  pret         = new SimpleDoubleProperty();
    private final IntegerProperty locuriLibere = new SimpleIntegerProperty();

    // --- câmpuri extra pentru design ---
    private final StringProperty  gen          = new SimpleStringProperty();
    private final IntegerProperty durataMin    = new SimpleIntegerProperty();
    private final DoubleProperty  rating       = new SimpleDoubleProperty();
    private final StringProperty  descriere    = new SimpleStringProperty();
    private final StringProperty  poster       = new SimpleStringProperty();   // nume fișier din resources/posters

    /** Constructorul de bază (cele 6 câmpuri obligatorii). Rămâne neschimbat și la Lucrarea 26. */
    public Seanta(int id, String film, String sala, String ora, double pret, int locuriLibere) {
        this(id, film, sala, ora, pret, locuriLibere, "", 0, 0.0, "", "");
    }

    /** Constructorul complet, cu câmpurile extra. */
    public Seanta(int id, String film, String sala, String ora, double pret, int locuriLibere,
                  String gen, int durataMin, double rating, String descriere, String poster) {
        setId(id);
        setFilm(film);
        setSala(sala);
        setOra(ora);
        setPret(pret);
        setLocuriLibere(locuriLibere);
        setGen(gen);
        setDurataMin(durataMin);
        setRating(rating);
        setDescriere(descriere);
        setPoster(poster);
    }

    // --- id ---
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // --- film ---
    public String getFilm() { return film.get(); }
    public void setFilm(String value) { film.set(value); }
    public StringProperty filmProperty() { return film; }

    // --- sala ---
    public String getSala() { return sala.get(); }
    public void setSala(String value) { sala.set(value); }
    public StringProperty salaProperty() { return sala; }

    // --- ora ---
    public String getOra() { return ora.get(); }
    public void setOra(String value) { ora.set(value); }
    public StringProperty oraProperty() { return ora; }

    // --- pret ---
    public double getPret() { return pret.get(); }
    public void setPret(double value) { pret.set(value); }
    public DoubleProperty pretProperty() { return pret; }

    // --- locuriLibere ---
    public int getLocuriLibere() { return locuriLibere.get(); }
    public void setLocuriLibere(int value) { locuriLibere.set(value); }
    public IntegerProperty locuriLibereProperty() { return locuriLibere; }

    // --- gen ---
    public String getGen() { return gen.get(); }
    public void setGen(String value) { gen.set(value); }
    public StringProperty genProperty() { return gen; }

    // --- durataMin ---
    public int getDurataMin() { return durataMin.get(); }
    public void setDurataMin(int value) { durataMin.set(value); }
    public IntegerProperty durataMinProperty() { return durataMin; }

    // --- rating ---
    public double getRating() { return rating.get(); }
    public void setRating(double value) { rating.set(value); }
    public DoubleProperty ratingProperty() { return rating; }

    // --- descriere ---
    public String getDescriere() { return descriere.get(); }
    public void setDescriere(String value) { descriere.set(value); }
    public StringProperty descriereProperty() { return descriere; }

    // --- poster ---
    public String getPoster() { return poster.get(); }
    public void setPoster(String value) { poster.set(value); }
    public StringProperty posterProperty() { return poster; }

    @Override
    public String toString() {
        return getFilm() + " | " + getSala() + " | " + getOra()
                + " | " + String.format(java.util.Locale.US, "%.2f", getPret())
                + " | locuri: " + getLocuriLibere();
    }
}