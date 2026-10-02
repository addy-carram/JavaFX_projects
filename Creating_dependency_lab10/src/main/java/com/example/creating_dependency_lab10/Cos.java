package com.example.creating_dependency_lab10;

import javafx.beans.binding.*;
import javafx.beans.property.*;

public class Cos {
    public static final double COTA_TVA = 0.20;
    public static final double PRAG_LIVRARE = 500;

    private final Produs produs;

    // procent reducere 0..50
    private final DoubleProperty reducere = new SimpleDoubleProperty(this, "reducere", 0) {
        @Override public void set(double v) { super.set(Math.max(0, Math.min(50, v))); }
    };
    public DoubleProperty reducereProperty() { return reducere; }

    private final NumberBinding subtotal;
    private final NumberBinding sumaReducere;
    private final DoubleBinding tva;
    private final NumberBinding totalDePlata;
    private final BooleanBinding livrareGratuita;
    private final StringBinding rezumat;

    public Cos(Produs produs) {
        this.produs = produs;

        // a) API fluent
        subtotal = produs.totalProperty().add(0);

        // 3.3: varianta cu clasa Bindings (rezultat identic cu subtotal.multiply(reducere).divide(100))
        sumaReducere = Bindings.divide(Bindings.multiply(subtotal, reducere), 100);

        // b) legătură personalizată (subclasă DoubleBinding)
        tva = new DoubleBinding() {
            { super.bind(subtotal, sumaReducere); }
            @Override protected double computeValue() {
                System.out.println("computeValue() TVA");
                return (subtotal.doubleValue() - sumaReducere.doubleValue()) * COTA_TVA;
            }
        };

        // 3.1
        totalDePlata = subtotal.subtract(sumaReducere).add(tva);

        // 3.2
        livrareGratuita = totalDePlata.greaterThanOrEqualTo(PRAG_LIVRARE);

        // c) Bindings.createStringBinding cu dependențe explicite
        rezumat = Bindings.createStringBinding(
                () -> String.format("%s × %d = %.2f lei",
                        produs.getNume(), produs.getCantitate(), produs.getTotal()),
                produs.numeProperty(), produs.cantitateProperty(), produs.totalProperty());
    }

    public Produs getProdus() { return produs; }
    public NumberBinding subtotalBinding() { return subtotal; }
    public NumberBinding sumaReducereBinding() { return sumaReducere; }
    public DoubleBinding tvaBinding() { return tva; }
    public NumberBinding totalDePlataBinding() { return totalDePlata; }
    public BooleanBinding livrareGratuitaBinding() { return livrareGratuita; }
    public StringBinding rezumatBinding() { return rezumat; }
}