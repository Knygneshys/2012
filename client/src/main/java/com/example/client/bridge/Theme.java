package com.example.client.bridge;

import java.util.function.Supplier;
import java.util.prefs.Preferences;

/**
 * The map themes the player can pick in the menu. Each one is just a
 * {@link TileTheme} implementation handed to {@link MapElementFactoryBridge},
 * so adding a theme never touches the bridge or the map code.
 * <p>
 * The choice is remembered between launches (per user, on this computer).
 */
public enum Theme {
    CLASSIC("Classic", ClassicTileTheme::new),
    ICE("Ice", IceTileTheme::new),
    DESERT("Desert", DesertTileTheme::new);

    private static final String PREF_KEY = "mapTheme";
    private static final Theme DEFAULT = ICE;

    private final String label;
    private final Supplier<TileTheme> factory;

    Theme(String label, Supplier<TileTheme> factory) {
        this.label = label;
        this.factory = factory;
    }

    public String label() {
        return label;
    }

    public TileTheme create() {
        return factory.get();
    }

    /** The theme after (dir = 1) or before (dir = -1) this one, wrapping round. */
    public Theme step(int dir) {
        Theme[] all = values();
        return all[Math.floorMod(ordinal() + dir, all.length)];
    }

    /** The theme picked last time, or the default. Never fails: preferences may be unavailable. */
    public static Theme loadSaved() {
        try {
            return valueOf(Preferences.userNodeForPackage(Theme.class).get(PREF_KEY, DEFAULT.name()));
        } catch (Exception e) {
            return DEFAULT;
        }
    }

    public void save() {
        try {
            Preferences.userNodeForPackage(Theme.class).put(PREF_KEY, name());
        } catch (Exception ignored) {
            // not being able to remember it is harmless
        }
    }
}
