package io.github.HollowKnight.Model.World;

import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.entity.enemy.Enemy;

import java.util.ArrayList;
import java.util.List;

public class World {
    private final Knight knight;
    private final Room room;
    private final List<Enemy> enemies;

    public World(Knight knight, Room room) {
        this.knight = knight;
        this.room = room;
        this.enemies = new ArrayList<>();
    }

    public Room getRoom() {
        return room;
    }
}
