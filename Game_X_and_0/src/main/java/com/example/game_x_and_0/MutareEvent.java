package com.example.game_x_and_0;

import javafx.event.Event;
import javafx.event.EventType;

public class MutareEvent extends Event {
    public static final EventType<MutareEvent> ANY    = new EventType<>(Event.ANY, "MUTARE_ANY");
    public static final EventType<MutareEvent> MUTARE = new EventType<>(ANY, "MUTARE_MUTARE");
    public static final EventType<MutareEvent> CASTIG = new EventType<>(ANY, "MUTARE_CASTIG");
    public static final EventType<MutareEvent> REMIZA = new EventType<>(ANY, "MUTARE_REMIZA");

    private final int index;        // celula 0-8
    private final String simbol;    // "X" sau "0"

    public MutareEvent(EventType<MutareEvent> tip, int index, String simbol) {
        super(tip);                // ce transmiți clasei părinte?
        this.index = index;
        this.simbol = simbol;
    }

    public int getIndex() { return index; }
    public String getSimbol() { return simbol; }
}