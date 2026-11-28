package com.example.client.command;

import com.example.client.InGameMenu;

/** Opens the in-game menu overlay. Purely client side: nothing is sent to the server. */
public class OpenPauseMenuCommand implements Command {
    private final InGameMenu menu;

    public OpenPauseMenuCommand(InGameMenu menu) {
        this.menu = menu;
    }

    @Override
    public void execute() {
        menu.open();
    }
}
