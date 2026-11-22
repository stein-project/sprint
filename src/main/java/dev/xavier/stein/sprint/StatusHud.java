/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2026 Breno Xavier
 */

package dev.xavier.stein.sprint;

import dev.xavier.stein.loader.api.Entities;
import dev.xavier.stein.loader.api.HudElement;
import dev.xavier.stein.loader.api.HudPlacement;
import dev.xavier.stein.loader.api.Keys;
import dev.xavier.stein.loader.api.Lang;
import dev.xavier.stein.loader.api.Option;
import dev.xavier.stein.loader.api.Render;

import java.util.List;

/**
 * The movement state on the HUD: {@code [Sprinting (Toggled)]}, {@code [Sneaking (Key Held)]},
 * {@code [Flying + Sprinting (Toggled)]}, {@code [Descending (Toggled)]}, {@code [Riding]}. The
 * first that applies wins, in that order: flying, riding, sneaking, sprinting.
 *
 * <p>Each state says who is behind it: the mod's toggle, the key held by hand, or the game itself
 * (a double tap on forward). Everything is read from the current player every frame, never kept
 * across worlds, so a state cannot outlive the world it came from. The text is only rebuilt when
 * the state changes.
 */
final class StatusHud implements HudElement {

    private static final int NONE = 0;
    private static final int SPRINTING = 1;
    private static final int SNEAKING = 2;
    private static final int FLYING = 3;
    private static final int DESCENDING = 4;
    private static final int RIDING = 5;

    private static final String[] STATE_KEYS = {
        "",
        "steinsprint.hud.sprinting",
        "steinsprint.hud.sneaking",
        "steinsprint.hud.flying",
        "steinsprint.hud.descending",
        "steinsprint.hud.riding"
    };

    private static final int BY_GAME = 0;
    private static final int BY_KEY = 1;
    private static final int BY_TOGGLE = 2;

    private static final String[] SOURCE_KEYS = {
        "steinsprint.hud.vanilla", "steinsprint.hud.keyHeld", "steinsprint.hud.toggled"
    };

    private final SteinSprint mod;

    /** What the text was built from: a change in any of them rebuilds it. */
    private int lastState = -1;

    private int lastSource = -1;
    private int lastSprintSource = -1;
    private int lastStyle = -1;
    private String lastLanguage = "";

    private String text = "";

    StatusHud(SteinSprint mod) {
        this.mod = mod;
    }

    @Override
    public String id() {
        return "steinsprint.status";
    }

    @Override
    public String name() {
        return Lang.tr("steinsprint.hud.name");
    }

    @Override
    public boolean layout(boolean preview, float partialTicks) {
        SprintConfig c = mod.config();
        Object me = Entities.self();
        int state = NONE;
        int source = BY_GAME;
        int sprintSource = -1; // the "+ Sprinting" part while flying; -1 = none
        if (me != null) {
            boolean sprinting = Entities.isSprinting(me) || c.hudAlways && mod.sprint.holding();
            boolean sneaking = Entities.isSneaking(me) || c.hudAlways && mod.sneak.holding();
            if (Entities.isFlying(me)) {
                boolean jumping = Keys.game(Keys.GameKey.JUMP).isDown();
                if (Entities.isSneaking(me) && !jumping) {
                    state = DESCENDING;
                    source = source(mod.sneak);
                } else {
                    state = FLYING;
                    if (sprinting && c.hudSprintWhileFlying) {
                        sprintSource = source(mod.sprint);
                    }
                }
            } else if (Entities.isRiding(me)) {
                state = RIDING;
            } else if (sneaking) {
                state = SNEAKING;
                source = source(mod.sneak);
            } else if (sprinting) {
                state = SPRINTING;
                source = source(mod.sprint);
            }
        }
        if (state == NONE && preview) {
            state = SPRINTING;
            source = BY_TOGGLE;
        }
        if (state == NONE) {
            return false;
        }
        String language = Lang.language();
        if (state != lastState
                || source != lastSource
                || sprintSource != lastSprintSource
                || c.hudStyle != lastStyle
                || !language.equals(lastLanguage)) {
            lastState = state;
            lastSource = source;
            lastSprintSource = sprintSource;
            lastStyle = c.hudStyle;
            lastLanguage = language;
            text = build(state, source, sprintSource, c.hudStyle);
        }
        return true;
    }

    private static int source(HeldKey key) {
        if (key.holding()) {
            return BY_TOGGLE;
        }
        return key.physicallyDown() ? BY_KEY : BY_GAME;
    }

    /**
     * The text for a state. Flying and riding have no source of their own: flying shows the
     * sprint's, when it sprints.
     */
    private static String build(int state, int source, int sprintSource, int style) {
        StringBuilder b = new StringBuilder(48);
        b.append(Lang.tr(STATE_KEYS[state]));
        if (state == FLYING) {
            if (sprintSource >= 0) {
                b.append(" + ").append(Lang.tr(STATE_KEYS[SPRINTING]));
                appendSource(b, sprintSource, style);
            }
        } else if (state != RIDING) {
            appendSource(b, source, style);
        }
        return style == SprintConfig.STYLE_BRACKETS ? "[" + b + "]" : b.toString();
    }

    private static void appendSource(StringBuilder b, int source, int style) {
        if (style != SprintConfig.STYLE_SHORT) {
            b.append(" (").append(Lang.tr(SOURCE_KEYS[source])).append(')');
        }
    }

    @Override
    public int width() {
        return Render.textWidth(text) + 2;
    }

    @Override
    public int height() {
        return 12;
    }

    @Override
    public void draw(boolean alignRight, float partialTicks) {
        Render.text(text, 1, 2, 0xFFFFFFFF, true);
    }

    @Override
    public HudPlacement defaultPlacement() {
        // Left edge, middle: clear of the chat (bottom left) and of the HUD's corners.
        return new HudPlacement(0, 1, 2, 0, 1.0F, true, false);
    }

    @Override
    public float defaultBackground() {
        return 0F;
    }

    @Override
    public void options(List<Option> out) {
        SprintConfig c = mod.config();
        out.add(
                Option.cycle(
                        Lang.tr("steinsprint.hud.style"),
                        new String[] {
                            Lang.tr("steinsprint.hud.style.brackets"),
                            Lang.tr("steinsprint.hud.style.plain"),
                            Lang.tr("steinsprint.hud.style.short")
                        },
                        () -> c.hudStyle,
                        v -> c.hudStyle = v));
        out.add(
                Option.toggle(
                        Lang.tr("steinsprint.hud.always"),
                        () -> c.hudAlways,
                        v -> c.hudAlways = v));
        out.add(
                Option.toggle(
                        Lang.tr("steinsprint.hud.sprintWhileFlying"),
                        () -> c.hudSprintWhileFlying,
                        v -> c.hudSprintWhileFlying = v));
    }

    @Override
    public void restoreOptions() {
        SprintConfig c = mod.config();
        SprintConfig d = new SprintConfig();
        c.hudStyle = d.hudStyle;
        c.hudAlways = d.hudAlways;
        c.hudSprintWhileFlying = d.hudSprintWhileFlying;
    }

    @Override
    public void optionsChanged() {
        mod.save();
    }
}
