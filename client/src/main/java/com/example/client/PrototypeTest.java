package com.example.client;

import java.awt.Color;

public class PrototypeTest {
    public static void main(String[] args) {
        Decoration original = new Decoration(
                0, 0,
                new DecorationStyle("BARREL", Color.RED)
        );

        Decoration shallow = original.clone(true);
        Decoration deep = original.clone(false);

        System.out.printf("%-12s %-5s %-5s %-12s %-12s%n",
                "Object", "X", "Y", "Name", "Color");

        printFields("Original", original);
        printFields("Shallow", shallow);
        printFields("Deep", deep);


        System.out.printf("\n%-12s %-22s %-22s%n",
                "Object", "Decoration", "DecorationStyle");

        printIdentity("Original", original);
        printIdentity("Shallow", shallow);
        printIdentity("Deep", deep);
    }

    private static void printFields(String label, Decoration decoration) {
        System.out.printf("%-12s %-5d %-5d %-12s %-12s%n",
                label,
                decoration.getX(),
                decoration.getY(),
                decoration.getStyle().getName(),
                decoration.getStyle().getColor().equals(Color.RED)
                        ? "RED"
                        : decoration.getStyle().getColor().toString());
    }

    private static void printIdentity(String label, Decoration decoration) {
        System.out.printf("%-12s %-22d %-22d%n",
                label,
                System.identityHashCode(decoration),
                System.identityHashCode(decoration.getStyle()));
    }
}
