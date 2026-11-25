package com.example.cinema_app_lab25_lab26;

import java.util.ArrayList;
import java.util.List;

/** Implementare în memorie: cerută de barem și folosită ca rezervă dacă nu pornește baza. */
public class MemorySeantaProvider implements SeantaProvider {

    private final List<Seanta> store = new ArrayList<>();
    private int nextId = 1;

    public MemorySeantaProvider() {
        add(new Seanta(0, "Dune: Partea a Doua", "Sala 1", "18:30", 90, 120, "SF", 166, 8.5,
                "Paul Atreides se alătură fremenilor și pornește pe calea răzbunării împotriva celor care i-au distrus familia.",
                "dune.jpg"));
        add(new Seanta(0, "Oppenheimer", "Sala 2", "19:00", 85, 0, "Dramă", 180, 8.3,
                "Povestea fizicianului care a condus proiectul Manhattan și a schimbat cursul istoriei.",
                "oppenheimer.jpg"));
        add(new Seanta(0, "Inside Out 2", "Sala 3", "16:00", 60, 45, "Animație", 96, 7.6,
                "Riley devine adolescentă, iar în mintea ei apar emoții noi, care răstoarnă tot ce știa.",
                "insideout2.jpg"));
        add(new Seanta(0, "Joker", "Sala 2", "21:30", 80, 8, "Thriller", 122, 8.4,
                "Un comedian eșuat din Gotham alunecă treptat spre nebunie și crimă.",
                "joker.jpg"));
        add(new Seanta(0, "Interstellar", "Sala 1", "21:45", 95, 200, "SF", 169, 8.7,
                "Un grup de exploratori călătorește prin gaura de vierme pentru a găsi o nouă casă omenirii.",
                "interstellar.jpg"));
        add(new Seanta(0, "Mad Max: Fury Road", "Sala 4", "20:15", 75, 60, "Acțiune", 120, 8.1,
                "O urmărire nebună prin deșert, în care Furiosa încearcă să scape de un tiran.",
                "madmax.jpg"));
        add(new Seanta(0, "Kung Fu Panda 4", "Sala 3", "14:00", 55, 150, "Animație", 94, 6.9,
                "Po trebuie să-și aleagă un succesor și se confruntă cu o vrăjitoare care schimbă înfățișarea.",
                "kungfupanda4.jpg"));
        add(new Seanta(0, "The Batman", "Sala 4", "22:45", 100, 5, "Acțiune", 176, 7.8,
                "Batman anchetează o serie de crime și descoperă corupția din inima orașului Gotham.",
                "batman.jpg"));
    }

    private void add(Seanta s) {
        s.setId(nextId++);
        store.add(s);
    }

    @Override
    public List<Seanta> findAll() {
        return new ArrayList<>(store);   // copie a listei, ca apelantul să nu modifice depozitul
    }

    @Override
    public Seanta save(Seanta s) {
        if (s.getId() == 0) {                       // INSERT
            add(s);
            return s;
        }
        Seanta stored = store.stream()              // UPDATE
                .filter(x -> x.getId() == s.getId())
                .findFirst()
                .orElseThrow(() -> new ProviderException("Seanța cu id " + s.getId() + " nu există."));
        if (stored != s) {                          // dacă e alt obiect, copiem valorile
            stored.setFilm(s.getFilm());
            stored.setSala(s.getSala());
            stored.setOra(s.getOra());
            stored.setPret(s.getPret());
            stored.setLocuriLibere(s.getLocuriLibere());
            stored.setGen(s.getGen());
            stored.setDurataMin(s.getDurataMin());
            stored.setRating(s.getRating());
            stored.setDescriere(s.getDescriere());
            stored.setPoster(s.getPoster());
        }
        return stored;
    }

    @Override
    public void delete(int id) {
        store.removeIf(x -> x.getId() == id);
    }
}