package io.github.HollowKnight.Model.World;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.HollowKnight.Model.entity.Knight;
import io.github.HollowKnight.Model.entity.enemy.*;
import io.github.HollowKnight.Model.npc.Npc;
import io.github.HollowKnight.Model.npc.NpcType;


public class Room {
    private TiledMap tiledMap;
    private Array<Rectangle> platforms;
    private Array<Hazard> hazards;
    private Array<BreakableWall> breakableWalls;
    private Array<SafeSpawn> safeSpawns;
        private Array<Door> doors;
    private Array<Enemy> enemies;
    private Array<Npc> npcs;
    private Array<Vector2> returnPoints = new Array<>();
    private Texture background;
    private final String particleEffectId;
    private Vector2 BossArenaEntryPoint;
    private Vector2 bossSpawnPoint;
    private Rectangle bossTriggerZone;
    private Rectangle enterDoorRect;
    private Rectangle exitDoorRect;
    private Rectangle arenaBounds;
    private final Array<Rectangle> arenaWalls = new Array<>();
    public Room(TiledMap tiledMap, String backgroundPath) {
        this(tiledMap, backgroundPath, "floating_dust");
    }

    public Room(TiledMap tiledMap, String backgroundPath, String particleEffectId) {
        this.particleEffectId = particleEffectId;
        this.tiledMap = tiledMap;
        this.platforms = new Array<>();
        this.hazards = new Array<>();
        this.breakableWalls = new Array<>();
        this.doors = new Array<>();
        this.enemies = new Array<>();
        this.npcs = new Array<>();
        this.background = new Texture(Gdx.files.internal(backgroundPath));

        loadPhysicsLayer("Solid", platforms);
        loadHazards("Hazard");
        loadBreakableWalls("Breakable");
        loadSafeSpawns("SafeSpawn");
        loadDoorLayer("Door");
        loadEnemies("Enemy");
        loadNpcs("NPC");
        if (npcs.isEmpty()) {
            loadNpcs("Zote");
        }
        loadBossSystem("BossSystem");

    }

    private void loadPhysicsLayer(String layerName, Array<Rectangle> targetList) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    targetList.add(rect);
                }
            }
        }
    }

    private void loadSafeSpawns(String layerName) {
        safeSpawns = new Array<>();
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject rectObject) {
                safeSpawns.add(new SafeSpawn(rectObject.getRectangle()));
            }
        }
    }

    private void loadHazards(String layerName) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    hazards.add(new Hazard(rect.x, rect.y, rect.width, rect.height));
                }
            }
        }
    }

    private void loadBreakableWalls(String layerName) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    breakableWalls.add(new BreakableWall(rect.x, rect.y, rect.width, rect.height));
                }
            }
        }
    }

    private void loadEnemies(String layerName) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof PointMapObject point)) continue;

            if ("ReturnPoint".equals(object.getName())) {
                returnPoints.add(new Vector2(point.getPoint().x, point.getPoint().y));
            }
        }
        for (MapObject object : layer.getObjects()) {
            if (object instanceof PointMapObject) {
                PointMapObject point = (PointMapObject) object;
                String type = object.getName();
                int mapHeight = tiledMap.getProperties().get("height", Integer.class);
                int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
                float x = point.getPoint().x;
                float y = point.getPoint().y;
                MapProperties props = object.getProperties();
                if (type == null) continue;
                int maxHp = props.get("maxHp", 20, Integer.class);
                int soulReward = props.get("soulReward", 3, Integer.class);
                int contactDamage = props.get("contactDamage", 1, Integer.class);
                boolean movingRight = props.get("movingRight", true, Boolean.class);
                switch (type.toLowerCase()) {
                    case "mosquito":
                        enemies.add(new FlyingEnemy(x, y,
                            maxHp, soulReward, contactDamage, movingRight, EnemyType.MOSQUITO));
                        break;
                    case "mosscreep":
                        enemies.add(new GroundEnemy(x, y, maxHp, soulReward, contactDamage, movingRight, EnemyType.MOSSCREEP));
                        break;
                    case "hornhead":
                        enemies.add(new HornHeadEnemy(x, y, maxHp, soulReward, contactDamage,
                            movingRight, EnemyType.HORNHEAD));
                        break;
                    case "guardian":
                    case "crystallized":
                        enemies.add(new CrystallizedEnemy(x, y, maxHp, soulReward, contactDamage,
                            movingRight, EnemyType.CRYSTALLIZED));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void loadNpcs(String layerName) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null) {
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof PointMapObject point)) {
                continue;
            }

            String typeName = object.getName();
            if (typeName == null || typeName.isEmpty()) {
                continue;
            }

            float x = point.getPoint().x;
            float y = point.getPoint().y;
            MapProperties props = object.getProperties();
            String npcId = typeName;
            String displayName = props.get("displayName", typeName, String.class);
            boolean facingRight = props.get("facingRight", true, Boolean.class);
            float interactRadius = props.get("interactRadius", Npc.DEFAULT_INTERACT_RADIUS, Float.class);

            Npc npc = createNpc(typeName, npcId, displayName, x, y, facingRight, interactRadius);
            if (npc != null) {
                npcs.add(npc);
            }
        }
    }

    private Npc createNpc(
        String typeName,
        String npcId,
        String displayName,
        float x,
        float y,
        boolean facingRight,
        float interactRadius
    ) {
        if ("Zote".equalsIgnoreCase(typeName)) {
            return new Npc(npcId, displayName, NpcType.ZOTE, x, y, facingRight, interactRadius);
        }
        return null;
    }

    private void loadBossSystem(String layerName) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            String name = object.getName();
            if (name == null) continue;

            switch (name) {
                case "enterDoor" -> {
                    if (object instanceof RectangleMapObject rectObj) {
                        Rectangle r = rectObj.getRectangle();
                        enterDoorRect = new Rectangle(r);
                        BossArenaEntryPoint = new Vector2(
                            r.x + r.width / 2f,
                            r.y + r.height / 2f
                        );
                    }
                }
                case "exitDoor" -> {
                    if (object instanceof RectangleMapObject rectObj) {
                        exitDoorRect = new Rectangle(rectObj.getRectangle());
                    }
                }
                case "BossSpown", "BossSpawn" -> {
                    if (object instanceof PointMapObject point) {
                        bossSpawnPoint = new Vector2(
                            point.getPoint().x,
                            point.getPoint().y
                        );
                    }
                }
                case "BossTriger", "BossTrigger" -> {
                    if (object instanceof RectangleMapObject rectObj) {
                        bossTriggerZone = rectObj.getRectangle();
                    }
                }
            }
        }
        computeArenaBounds();
    }

    private void computeArenaBounds() {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        boolean found = false;

        for (Rectangle rect : new Rectangle[]{enterDoorRect, exitDoorRect, bossTriggerZone}) {
            if (rect == null) continue;
            found = true;
            minX = Math.min(minX, rect.x);
            minY = Math.min(minY, rect.y);
            maxX = Math.max(maxX, rect.x + rect.width);
            maxY = Math.max(maxY, rect.y + rect.height);
        }

        if (found) {
            arenaBounds = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
    }
    private void loadDoorLayer(String layerName) {
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            MapProperties props = object.getProperties();

            String targetRoom = props.get("targetRoom", "", String.class);
            String targetSpawn = props.get("targetSpawn", "", String.class);

            if (targetRoom == null || targetRoom.isEmpty()) {
                continue;
            }

            doors.add(new Door(rect, targetRoom, targetSpawn));
        }
    }


    public Array<Enemy> getEnemies() {
        return enemies;
    }

    public Array<Npc> getNpcs() {
        return npcs;
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void removeEnemyAt(int index) {
        enemies.removeIndex(index);
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public Array<Rectangle> getPlatforms() {
        return platforms;
    }

    public Array<Hazard> getHazards() {
        return hazards;
    }

    public Array<BreakableWall> getBreakableWalls() {
        return breakableWalls;
    }

    public Array<SafeSpawn> getSafeSpawns() {
        return safeSpawns;
    }

    public Array<Vector2> getReturnPoints() {
        return returnPoints;
    }

    public Texture getBackground() {
        return background;
    }

    public String getParticleEffectId() {
        return particleEffectId;
    }

    public Vector2 getBossArenaEntryPoint() {
        return BossArenaEntryPoint;
    }

    public Vector2 getBossSpawnPoint() {
        return bossSpawnPoint;
    }

    public Rectangle getBossTriggerZone() {
        return bossTriggerZone;
    }

    public Rectangle getEnterDoorRect() {
        return enterDoorRect;
    }

    public Rectangle getExitDoorRect() {
        return exitDoorRect;
    }

    public Rectangle getArenaBounds() {
        return arenaBounds;
    }

    public Array<Rectangle> getArenaWalls() {
        return arenaWalls;
    }

    public void clearArenaWalls() {
        arenaWalls.clear();
    }

    public void addArenaWall(Rectangle wall) {
        arenaWalls.add(new Rectangle(wall));
    }
        public Array<Door> getDoors() { return doors; }

    public Door findTouchingDoor(Rectangle entityHitbox) {
        for (Door door : doors) {
            if (door.getHitbox().overlaps(entityHitbox)) return door;
        }
        return null;
    }

    public void dispose() {
        if (background != null) {
            background.dispose();
            background = null;
        }
    }

}
