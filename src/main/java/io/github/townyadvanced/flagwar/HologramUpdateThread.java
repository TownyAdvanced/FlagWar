/*
 * Copyright (c) 2021 TownyAdvanced
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.townyadvanced.flagwar;

import io.github.townyadvanced.flagwar.objects.CellUnderAttack;

import java.util.TimerTask;

/**
 * Each {@link CellUnderAttack}'s hologram timer thread, extending the {@link TimerTask}.
 */
public class HologramUpdateThread extends TimerTask {

    /** Holds the relevant {@link CellUnderAttack}, assigned by the constructor. */
    private final CellUnderAttack cell;

    /**
     * Constructs the {@link HologramUpdateThread} for a given {@link CellUnderAttack}.
     * @param cellUnderAttack to assign the CellAttackThread to.
     */
    public HologramUpdateThread(final CellUnderAttack cellUnderAttack) {

        this.cell = cellUnderAttack;
    }

    /**
     * Updates the hologram of the war flag within the {@link CellUnderAttack}.
     */
    @Override
    public void run() {

        cell.updateHologram();
    }
}
