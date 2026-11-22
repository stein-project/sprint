/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2026 Breno Xavier
 */

package dev.xavier.stein.sprint;

import dev.xavier.stein.loader.api.Chat;
import dev.xavier.stein.loader.api.Entities;
import dev.xavier.stein.loader.api.Gameplay;
import dev.xavier.stein.loader.api.Hud;
import dev.xavier.stein.loader.api.Keys;
import dev.xavier.stein.loader.api.Lang;
import dev.xavier.stein.loader.api.ModConfig;
import dev.xavier.stein.loader.api.ModContext;
import dev.xavier.stein.loader.api.Page;
import dev.xavier.stein.loader.api.Screens;
import dev.xavier.stein.loader.api.Server;
import dev.xavier.stein.loader.api.SteinMod;

import java.io.File;

/**
 * Sprint: toggle sprint and toggle sneak, and the movement state on the HUD.
 *
 * <p>The mod only holds the game's keys ({@link HeldKey}); the game decides, by its own rules,
 * whether the player sprints or sneaks. Nothing about the movement changes, so it plays the same on
 * any server — and a server can still turn either toggle off ({@code toggleSprint}, {@code
 * toggleSneak} in steinmod.json's features).
 *
 * <p>Every tick, in this order: the key presses, toggle sprint, toggle sneak. Mods that depend on
 * Sprint (Combat's W-tap, through {@link dev.xavier.stein.sprint.api.Sprint}) tick after it.
 */
public final class SteinSprint implements SteinMod {

    static final String MOD_ID = "steinsprint";

    private static final String CATEGORY = "key.categories.steinsprint";

    private static SteinSprint instance;

    private final ModContext ctx = ModContext.of(this);

    private ModConfig<SprintConfig> config;

    final HeldKey sprint = new HeldKey(Keys.GameKey.SPRINT);
    final HeldKey sneak = new HeldKey(Keys.GameKey.SNEAK);

    Keys.Binding sprintKey;
    Keys.Binding sneakKey;

    /**
     * The game's own keys were physically down last tick (for {@link SprintConfig#gameKeysToggle}).
     */
    private boolean gameSprintWasDown;

    private boolean gameSneakWasDown;

    /** The player was flying last tick (for {@link SprintConfig#unsneakOnFly}). */
    private boolean wasFlying;

    /** The running mod, or null before it starts. */
    public static SteinSprint instance() {
        return instance;
    }

    SprintConfig config() {
        return config.get();
    }

    void save() {
        config.save();
    }

    @Override
    public void afterStartGame() {
        File file = ctx.configFile();
        boolean firstStart = !file.isFile();
        config = ctx.config(SprintConfig.class);
        config().normalize();
        sprintKey = Keys.bind("key.steinsprint.toggleSprint", Keys.KEY_G, CATEGORY);
        sneakKey = Keys.bind("key.steinsprint.toggleSneak", Keys.KEY_NONE, CATEGORY);
        if (firstStart) {
            CombatMigration.run(file.getParentFile().getParentFile(), config(), sprintKey, log());
            save();
        }
        Hud.register(new StatusHud(this));
        instance = this;
    }

    dev.xavier.stein.loader.api.Log log() {
        return ctx.log();
    }

    @Override
    public void onTickEnd() {
        if (config == null) {
            return;
        }
        SprintConfig c = config();
        handleKeys(c);
        Object me = Entities.self();
        boolean inGame = me != null && !Screens.isOpen();
        boolean flying = me != null && Entities.isFlying(me);
        if (flying && !wasFlying && c.unsneakOnFly && c.toggleSneak) {
            c.toggleSneak = false;
            save();
        }
        wasFlying = flying;
        // Each tick: a server can take a toggle away, and give it back, at any moment.
        sprint.tick(inGame && c.toggleSprint && Server.allows("toggleSprint"));
        sneak.tick(inGame && c.toggleSneak && Server.allows("toggleSneak"));
        Gameplay.setDoubleTapSprint(c.doubleTapSprint);
    }

    /** The presses since the last tick: the mod's keys, and the game's when they also toggle. */
    private void handleKeys(SprintConfig c) {
        while (sprintKey.consumeClick()) {
            toggleSprint(c);
        }
        while (sneakKey.consumeClick()) {
            toggleSneak(c);
        }
        boolean sprintDown = sprint.physicallyDown();
        boolean sneakDown = sneak.physicallyDown();
        if (c.gameKeysToggle && !Screens.isOpen() && Entities.self() != null) {
            if (sprintDown && !gameSprintWasDown) {
                toggleSprint(c);
            }
            if (sneakDown && !gameSneakWasDown) {
                toggleSneak(c);
            }
        }
        gameSprintWasDown = sprintDown;
        gameSneakWasDown = sneakDown;
    }

    private void toggleSprint(SprintConfig c) {
        if (!Server.allows("toggleSprint")) {
            return;
        }
        c.toggleSprint = !c.toggleSprint;
        save();
        Chat.actionBar(
                Lang.tr(
                        c.toggleSprint ? "steinsprint.on" : "steinsprint.off",
                        Lang.tr("steinsprint.toggleSprint")));
    }

    private void toggleSneak(SprintConfig c) {
        if (!Server.allows("toggleSneak")) {
            return;
        }
        c.toggleSneak = !c.toggleSneak;
        save();
        Chat.actionBar(
                Lang.tr(
                        c.toggleSneak ? "steinsprint.on" : "steinsprint.off",
                        Lang.tr("steinsprint.toggleSneak")));
    }

    /** Toggle sprint is holding the game's sprint key (for the public API). */
    public boolean sprintHolding() {
        return sprint.holding();
    }

    /**
     * The sprint reset for the public API: stop sprinting now and let go of the key until the next
     * tick, so the client tells the server it stopped and, a tick later, that it started.
     */
    public boolean resetSprint() {
        if (!sprint.holding()) {
            return false;
        }
        Gameplay.stopSprinting();
        return sprint.release();
    }

    @Override
    public void onSuspend() {
        // The Loader already let go of the keys; the double tap goes back to the game's.
        Gameplay.setDoubleTapSprint(true);
        sprint.tick(false);
        sneak.tick(false);
    }

    @Override
    public void onPage(Page page) {
        SprintPage.build(this, page);
    }
}
