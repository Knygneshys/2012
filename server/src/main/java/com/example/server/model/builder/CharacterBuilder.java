package com.example.server.model.builder;

import com.example.server.model.ServerCharacter;

/** Construction steps shared by server-side characters. */
public interface CharacterBuilder<T extends ServerCharacter> {
    CharacterBuilder<T> id(int id);

    CharacterBuilder<T> position(int x, int y);

    CharacterBuilder<T> color(String colorHex);

    T build();
}
