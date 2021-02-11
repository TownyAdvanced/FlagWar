/*
 * Copyright 2021 TownyAdvanced
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.github.townyadvanced.flagwar;

import io.github.townyadvanced.flagwar.objects.CellUnderAttack;
import java.util.TimerTask;

/**
 * Each {@link CellUnderAttack}'s attack thread, extending the {@link TimerTask}
 */
public class CellAttackThread extends TimerTask {

	final CellUnderAttack cell;

    /**
     * Constructs the {@link CellAttackThread} for a given {@link CellUnderAttack}
     * @param cellUnderAttack to assign the CellAttackThread to.
     */
	public CellAttackThread(CellUnderAttack cellUnderAttack) {

		this.cell = cellUnderAttack;
	}

    /**
     * Updates the war flag within the {@link CellUnderAttack}, and if {@link CellUnderAttack#hasEnded()} becomes true,
     * runs {@link FlagWar#attackWon(CellUnderAttack)}
     */
	@Override
	public void run() {

		cell.changeFlag();
		if (cell.hasEnded())
			FlagWar.attackWon(cell);
	}
}
