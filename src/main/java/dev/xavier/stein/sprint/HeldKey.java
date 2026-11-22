/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2026 Breno Xavier
 */

package dev.xavier.stein.sprint;

import dev.xavier.stein.loader.api.Keys;

/**
 * One of the game's keys held by the mod, as if a finger were on it: the game still decides what
 * the key does (sprinting forward only, with its hunger, item and blindness rules; sneaking).
 * Nothing about the movement changes — the client sends what a player holding the key sends.
 */
final class HeldKey {

    private final Keys.GameKey key;

    /** The mod is holding the key. */
    private boolean holding;

    HeldKey(Keys.GameKey key) {
        this.key = key;
    }

    /** The mod is holding the key right now. */
    boolean holding() {
        return holding;
    }

    /** The physical key (keyboard or mouse) is down, whoever else holds the game's key. */
    boolean physicallyDown() {
        int code = Keys.game(key).key();
        return code != Keys.KEY_NONE && Keys.isDown(code);
    }

    /**
     * Every tick: holds the key while {@code want}, lets go once when it stops wanting it (the key
     * goes back to what the keyboard says, so a key held by hand stays the player's).
     */
    void tick(boolean want) {
        if (want) {
            Keys.game(key).hold(true);
            holding = true;
        } else if (holding) {
            holding = false;
            Keys.game(key).hold(false);
        }
    }

    /**
     * Lets go of the key until the next {@link #tick}, which holds it again. Called after this
     * mod's tick (a mod that depends on Sprint ticks after it), so the next game tick runs with the
     * key up. Only while holding: a key held by hand is not the mod's to let go.
     *
     * @return true when the key was let go
     */
    boolean release() {
        if (!holding) {
            return false;
        }
        Keys.game(key).hold(false);
        return true;
    }
}
