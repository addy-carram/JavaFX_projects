package com.example.cinema_app_lab25_lab26;

import java.util.List;

/** Contractul de acces la date. Interfața grafică lucrează DOAR cu această interfață. */
public interface SeantaProvider {

    List<Seanta> findAll();

    /** id == 0 -> INSERT (se setează id-ul generat); id != 0 -> UPDATE. Întoarce aceeași seanță. */
    Seanta save(Seanta s);

    void delete(int id);
}