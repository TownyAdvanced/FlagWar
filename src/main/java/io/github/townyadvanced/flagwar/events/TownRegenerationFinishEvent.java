package io.github.townyadvanced.flagwar.events;

import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.objects.Battle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TownRegenerationFinishEvent extends Event {
    private static final HandlerList h = new HandlerList();

    public @NotNull HandlerList getHandlers() {return h;}
    public static HandlerList getHandlerList() {
        return h;
    }

    private final Battle battle;

    public TownRegenerationFinishEvent(Battle battle) {
        this.battle = battle;
    }

    public Battle getBattle() {
        return battle;
    }

    public Town getTown() {
        return battle.getContestedTown();
    }


}
