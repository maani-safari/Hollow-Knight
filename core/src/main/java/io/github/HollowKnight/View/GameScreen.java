package io.github.HollowKnight.View;


import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;

import com.badlogic.gdx.graphics.GL20;

import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import com.badlogic.gdx.maps.MapLayer;

import com.badlogic.gdx.maps.MapObject;

import com.badlogic.gdx.maps.tiled.TiledMap;

import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.HollowKnight.Controller.*;

import io.github.HollowKnight.Main;

import io.github.HollowKnight.Model.Save.GameData;
import io.github.HollowKnight.Model.Save.SaveManager;
import io.github.HollowKnight.Model.Settings.KeyController;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

import io.github.HollowKnight.Model.charm.CharmInventory;

import io.github.HollowKnight.Model.World.Door;
import io.github.HollowKnight.Model.World.Room;

import io.github.HollowKnight.Model.entity.Direction;

import io.github.HollowKnight.Model.entity.Knight;

import io.github.HollowKnight.Model.entity.enemy.Enemy;
import io.github.HollowKnight.Model.entity.enemy.EnemyType;
import io.github.HollowKnight.Model.entity.enemy.FalseKnight;
import io.github.HollowKnight.Model.entity.enemy.FalseKnightState;
import io.github.HollowKnight.Model.npc.NpcDialogueState;
import io.github.HollowKnight.Model.particle.ParticleEffectRegistry;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;

public class GameScreen extends ScreenAdapter {

    private static final String DEFAULT_ROOM_MAP = "room2.tmx";
    private static final String DEFAULT_ROOM_BACKGROUND = "BackGround/forgotten.PNG";
    private static final String BOSS_ROOM_MAP = "room-test.tmx";
    private static final float DOOR_TRANSITION_COOLDOWN = 0.5f;

    private final Main main;
    private final GameData pendingLoadData;
    private final int saveSlot;

    private SpriteBatch batch;

    private TiledMap map;

    private OrthogonalTiledMapRenderer mapRenderer;

    private OrthographicCamera camera;

    private Knight knight;

    private KnightController knightController;

    private KnightView knightView;

    private Room room;

    private KeyController keys;

    private GameEventBus eventBus;



    private CombatController combatController;

    private EnemyController enemyController;

    private EnemyView enemyView;

    private FalseKnightController falseKnightController;

    private FalseKnightView falseKnightView;

    private SpellController spellController;

    private SpellView spellView;

    private CharmInventory charmInventory;

    private CharmInventoryView charmInventoryView;

    private HudRenderer hudRenderer;

    private EnvironmentalParticleController environmentalParticleController;

    private EnvironmentalParticleView environmentalParticleView;

    private SaveController saveController;
    private PauseMenuView pauseMenuView;
    private NpcView npcView;
    private DialogueView dialogueView;
    private InteractionHintView interactionHintView;
    private NpcInteractionController npcInteractionController;
    private NpcDialogueState npcDialogueState;

    private AchievementPopupView achievementPopupView;
    private AchievementController achievementController;

    private OrthographicCamera hudCamera;
    private int bgLayer;
    private int solidLayer;
    private int mainLayer;
    private int foregroundLayer;
    private float roomWidth;
    private float roomHeight;
    private float bgMinX;
    private float bgMaxX;
    private float bgMinY;
    private float bgTileWidth;
    private float bgTileHeight;

    private String currentRoomMapName = DEFAULT_ROOM_MAP;
    private boolean falseKnightDefeated;
    private float totalPlayTimeSeconds;
    private int totalDeaths;
    private int totalEnemiesKilled;
    private boolean victoryPending;
    private boolean victoryShown;
    private float saveStatusTimer;
    private String saveStatusMessage = "";
    private BitmapFont statusFont;
    private float doorTransitionCooldown;
    private Texture closedDoorTexture;

    private float shakeTimer;
    private float shakeDuration;
    private float shakeMagnitude;
    public GameScreen(Main main) {
        this(main, null, SaveManager.DEFAULT_SLOT);
    }

    public GameScreen(Main main, GameData loadData, int saveSlot) {
        this.main = main;
        this.pendingLoadData = loadData;
        this.saveSlot = saveSlot;
    }


    @Override

    public void show() {

        batch = new SpriteBatch();

        camera = new OrthographicCamera();

        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        keys = main.getKeyController();
        saveController = new SaveController(main, main.getSaveManager());
        statusFont = new BitmapFont();
        closedDoorTexture = new Texture(Gdx.files.internal("HollowKnight/Architecture & Environment/Area specfic architecture/Forgotten Crossroads/crossroads_false_door_0000_4.png"));

        eventBus = new GameEventBus();
        main.getAudioManager().bindGameplayEvents(eventBus);

        if (pendingLoadData != null && pendingLoadData.roomMapName != null) {
            currentRoomMapName = pendingLoadData.roomMapName;
            falseKnightDefeated = pendingLoadData.falseKnightDefeated;
            totalPlayTimeSeconds = pendingLoadData.totalPlayTimeSeconds;
            totalDeaths = pendingLoadData.totalDeaths;
            totalEnemiesKilled = pendingLoadData.totalEnemiesKilled;
        }

        initializeRoom(currentRoomMapName);

        combatController = new CombatController(eventBus);

        enemyController = new EnemyController(eventBus);

        enemyView = new EnemyView();

        falseKnightController = new FalseKnightController(eventBus);
        falseKnightController.setGameScreen(this);

        falseKnightView = new FalseKnightView();

        spellController = new SpellController(eventBus);

        spellView = new SpellView(spellController);


        float spawnX = 1100;
        float spawnY = 1100;
        boolean spawnFound = false;

        MapLayer knightLayer = map.getLayers().get("Knight");
        if (knightLayer != null) {
            MapObject spawnPoint = knightLayer.getObjects().get("SpawnKnight");
            if (spawnPoint != null) {
                spawnX = spawnPoint.getProperties().get("x", Float.class);
                spawnY = spawnPoint.getProperties().get("y", Float.class);
                spawnFound = true;
            }
        }

        if (pendingLoadData != null) {
            spawnX = pendingLoadData.knightX;
            spawnY = pendingLoadData.knightY;
            camera.position.set(spawnX, spawnY + 100, 0);
        } else if (spawnFound) {
            camera.position.set(spawnX, spawnY + 100, 0);
        } else {
            camera.position.set(1100, 1100, 0);
        }

        camera.update();

        knight = new Knight(spawnX, spawnY);
        charmInventory = new CharmInventory();

        if (pendingLoadData == null) {
            charmInventory.unlockAll();
        }

        charmInventoryView = new CharmInventoryView(main.getSkin(), knight, charmInventory, main.getGameSettings().getLanguage());

        pauseMenuView = new PauseMenuView(main.getSkin(), main.getKeyController(), main.getGameSettings().getLanguage());
        pauseMenuView.setListener(new PauseMenuView.Listener() {
            @Override
            public void onResume() {
                pauseMenuView.setOpen(false);
            }

            @Override
            public void onSaveGame() {
                performSave();
            }

            @Override
            public void onQuitToMainMenu() {
                pauseMenuView.setOpen(false);
                main.setScreen(new MainMenuScreen(main.getSkin(), main));
            }

            @Override
            public void onSetting() {
                pauseMenuView.setOpen(false);
                main.setScreen(new SettingsView(main.getSkin(),main));
            }
        });

        knightController = new KnightController(knight, keys, combatController, spellController, eventBus);

        knightView = new KnightView(knight);

        npcDialogueState = new NpcDialogueState();
        npcView = new NpcView();
        dialogueView = new DialogueView(main.getSkin());
        interactionHintView = new InteractionHintView(main.getSkin(), main.getGameSettings().getLanguage());
        npcInteractionController = new NpcInteractionController(
            knight,
            keys,
            main.getAudioManager(),
            dialogueView,
            interactionHintView,
            npcDialogueState
        );
        combatController.setNpcInteractionController(npcInteractionController);

        hudRenderer = new HudRenderer();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (pendingLoadData != null) {
            saveController.applyGameData(pendingLoadData, knight, charmInventory, npcDialogueState);
            camera.position.set(knight.getPosition().x, knight.getPosition().y + 100, 0);
            camera.update();
        } else {
            knight.getSoulVessel().setCurrentSouls(66);
        }

        hudRenderer.onScreenShown(knight.getSoulVessel().getCurrentSouls(), knight.getHealthMasks());

        achievementPopupView = new AchievementPopupView(main.getSkin(), main.getGameSettings().getLanguage());
        achievementController = new AchievementController(
            main.getAchievementManager(),
            eventBus,
            achievementPopupView
        );
        achievementController.bind();
        eventBus.subscribe(GameEvent.CHEAT_BOSS_TELEPORT, this::handleBossCheatTeleport);
        eventBus.subscribe(GameEvent.ENEMY_KILLED, payload -> totalEnemiesKilled++);
        eventBus.subscribe(GameEvent.KNIGHT_DIED, () -> totalDeaths++);
        eventBus.subscribe(GameEvent.BOSS_FIGHT_STARTED, () -> shakeCamera(0.35f, 12f));
        eventBus.subscribe(GameEvent.BOSS_STUNNED, () -> shakeCamera(0.4f, 18f));
        eventBus.subscribe(GameEvent.BOSS_PHASE2, () -> shakeCamera(0.5f, 22f));
        eventBus.subscribe(GameEvent.BOSS_DEFEATED, () -> shakeCamera(0.7f, 28f));
        eventBus.subscribe(GameEvent.BOSS_HEAVY_IMPACT, () -> shakeCamera(0.3f, 20f));
        eventBus.subscribe(GameEvent.KNIGHT_TOOK_DAMAGE, () -> shakeCamera(0.2f, 8f));
        eventBus.subscribe(GameEvent.KNIGHT_CAST_SPELL, () -> shakeCamera(0.3f, 10f));

        postRoomEntered(currentRoomMapName);
    }

    private void initializeRoom(String mapFileName) {
        if (map != null) {
            map.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (room != null) {
            room.dispose();
        }

        map = new TmxMapLoader().load(mapFileName);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
        room = new Room(map, backgroundForMap(mapFileName), particleForMap(mapFileName));

        environmentalParticleController = null;
        environmentalParticleView = null;
        if (ParticleEffectRegistry.has(room.getParticleEffectId())) {
            environmentalParticleController = new EnvironmentalParticleController(room.getParticleEffectId());
            environmentalParticleView = new EnvironmentalParticleView();
        }

        bgLayer = room.getTiledMap().getLayers().getIndex("BackGround");
        solidLayer = room.getTiledMap().getLayers().getIndex("Solid");
        mainLayer = room.getTiledMap().getLayers().getIndex("Main");
        foregroundLayer = room.getTiledMap().getLayers().getIndex("Foreground");

        int mapWidth = room.getTiledMap().getProperties().get("width", Integer.class);
        int mapHeight = room.getTiledMap().getProperties().get("height", Integer.class);
        int tileWidth = room.getTiledMap().getProperties().get("tilewidth", Integer.class);
        int tileHeight = room.getTiledMap().getProperties().get("tileheight", Integer.class);

        roomWidth = mapWidth * tileWidth;
        roomHeight = mapHeight * tileHeight;
        currentRoomMapName = mapFileName;
        main.getAudioManager().playMusic(musicForMap(mapFileName));
    }
    private void handleBossCheatTeleport() {
        if (!BOSS_ROOM_MAP.equals(currentRoomMapName)) {
            changeRoom(BOSS_ROOM_MAP, null);
        }
        Vector2 entry = room.getBossArenaEntryPoint();
        if (entry == null) {
            return;
        }
        knight.setPosition(new Vector2(entry.x, entry.y));
        knight.getVelocity().set(0, 0);
        knight.syncHitbox();
        knight.setLastSafeX(entry.x);
        knight.setLastSafeY(entry.y);
        camera.position.set(entry.x, entry.y + 150f, 0);
        camera.update();
        eventBus.post(GameEvent.CHEAT_ACTIVATED);
    }
    private void postRoomEntered(String roomMapName) {
        if (eventBus == null || roomMapName == null || roomMapName.isEmpty()) {
            return;
        }
        eventBus.post(GameEvent.ROOM_ENTERED, new GameEventPayload.RoomEntered(roomMapName));
    }


    private void changeRoom(String roomName, String spawnName) {
        if (roomName == null || roomName.isEmpty()) {
            return;
        }

        initializeRoom(roomName);

        Vector2 spawn = findKnightSpawn(spawnName);
        float spawnX = spawn != null ? spawn.x : knight.getPosition().x;
        float spawnY = spawn != null ? spawn.y : knight.getPosition().y;

        knight.setPosition(new Vector2(spawnX, spawnY));

        camera.position.set(spawnX, spawnY + 150f, 0);
        camera.update();

        if (knightView != null) {
            knightView.resetStateTime();
        }
        if (spellController != null) {
            spellController.clearActiveEffects();
        }

        doorTransitionCooldown = DOOR_TRANSITION_COOLDOWN;
        postRoomEntered(roomName);
    }

    private Vector2 findKnightSpawn(String spawnName) {
        if (spawnName == null || spawnName.isEmpty() || map == null) {
            return null;
        }

        MapLayer knightLayer = map.getLayers().get("Knight");
        if (knightLayer == null) {
            return null;
        }

        for (MapObject object : knightLayer.getObjects()) {
            if (!spawnName.equals(object.getName())) {
                continue;
            }
            Float x = object.getProperties().get("x", Float.class);
            Float y = object.getProperties().get("y", Float.class);
            if (x != null && y != null) {
                return new Vector2(x, y);
            }
        }
        return null;
    }
    public void shakeCamera(float duration, float magnitude) {
        if (duration > shakeTimer) {
            shakeTimer = duration;
            shakeDuration = duration;
        }
        shakeMagnitude = Math.max(shakeMagnitude, magnitude);
    }
    private void clampCameraToArenaIfLocked() {
        if (falseKnightController == null || !falseKnightController.isArenaLocked() || room == null) {
            return;
        }

        Rectangle bounds = room.getArenaBounds();
        if (bounds == null) {
            return;
        }

        float halfW = camera.viewportWidth * 0.5f;
        float halfH = camera.viewportHeight * 0.5f;

        float minX = bounds.x + halfW;
        float maxX = bounds.x + bounds.width - halfW + 150f;
        if (minX > maxX) {
            camera.position.x = bounds.x + bounds.width * 0.5f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
        }

        float minY = bounds.y + halfH;
        float maxY = bounds.y + bounds.height - halfH;
        if (minY <= maxY) {
            camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
        }
    }

    private void checkDoorTransitions() {
        if (doorTransitionCooldown > 0f || room == null || knight == null || !knight.isAlive()) {
            return;
        }

        Door door = room.findTouchingDoor(knight.getHitbox());
        if (door == null) {
            return;
        }

        changeRoom(door.getTargetRoom(), door.getTargetSpawn());
    }

    private void checkVictoryTransition() {
        if (!victoryPending || victoryShown || room == null || knight == null || !knight.isAlive()) {
            return;
        }

        Rectangle bounds = room.getArenaBounds();
        if (bounds == null) {
            return;
        }

        if (!bounds.overlaps(knight.getHitbox())) {
            showVictoryScreen();
        }
    }

    private void showVictoryScreen() {
        victoryShown = true;
        victoryPending = false;
        main.setScreen(new VictoryScreen(
            main.getSkin(),
            main,
            totalDeaths,
            totalEnemiesKilled,
            totalPlayTimeSeconds,
            new VictoryScreen.Listener() {
                @Override
                public void onRestartGame() {
                    main.getAudioManager().stopMusic();
                    main.setScreen(new GameScreen(main));
                }

                @Override
                public void onReturnToMainMenu() {
                    main.getAudioManager().stopMusic();
                    main.setScreen(new MainMenuScreen(main.getSkin(), main));
                }
            }
        ));
    }

    private static String backgroundForMap(String mapFileName) {
        if ("room-test.tmx".equals(mapFileName)){
            return DEFAULT_ROOM_BACKGROUND;
        }else if ("room2.tmx".equals(mapFileName)){
            return "BackGround/greenpath.PNG";
        }
        return DEFAULT_ROOM_BACKGROUND;
    }
    private static String musicForMap(String mapFileName) {
        if ("room2.tmx".equals(mapFileName)) {
            return "audio/S5 Green Path Bass.wav";
        }
        if ("room-test.tmx".equals(mapFileName)) {
            return "audio/S19 Crossroads Bass.wav";
        }
        return "audio/S5 Green Path Bass.wav";
    }

    private static String particleForMap(String mapFileName) {
        if ("room2.tmx".equals(mapFileName)) {
            return "greenpath_leaves";
        }
        return "floating_dust";
    }

    private void performSave() {
        boolean saved = saveController.saveGame(
            saveSlot,
            knight,
            charmInventory,
            currentRoomMapName,
            falseKnightDefeated,
            totalPlayTimeSeconds,
            totalDeaths,
            totalEnemiesKilled,
            npcDialogueState
        );
        if (saved) {
            eventBus.post(GameEvent.GAME_SAVED);
            var lang = main.getGameSettings().getLanguage();
            showSaveStatus(SettingsLocalization.format("save.statusSlot", lang, saveSlot));
            if (pauseMenuView != null) {
                pauseMenuView.showStatus(SettingsLocalization.get("pause.saved", lang));
            }
        } else {
            var lang = main.getGameSettings().getLanguage();
            showSaveStatus(SettingsLocalization.get("pause.saveFailed", lang));
            if (pauseMenuView != null) {
                pauseMenuView.showStatus(SettingsLocalization.get("pause.saveFailed", lang));
            }
        }
    }

    private void performQuickLoad() {
        GameData data = saveController.loadGame(saveSlot);
        if (data == null) {
            var lang = main.getGameSettings().getLanguage();
            showSaveStatus(SettingsLocalization.get("pause.noSave", lang));
            if (pauseMenuView != null) {
                pauseMenuView.showStatus(SettingsLocalization.get("pause.noSave", lang));
            }
            return;
        }

        if (!data.roomMapName.equals(currentRoomMapName)) {
            initializeRoom(data.roomMapName);
        }

        saveController.applyGameData(data, knight, charmInventory, npcDialogueState);
        falseKnightDefeated = data.falseKnightDefeated;
        totalPlayTimeSeconds = data.totalPlayTimeSeconds;
        totalDeaths = data.totalDeaths;
        totalEnemiesKilled = data.totalEnemiesKilled;

        camera.position.set(knight.getPosition().x, knight.getPosition().y + 100, 0);
        camera.update();
        if (knightView != null) {
            knightView.resetStateTime();
        }
        if (hudRenderer != null) {
            hudRenderer.onScreenShown(knight.getSoulVessel().getCurrentSouls(), knight.getHealthMasks());
        }

        eventBus.post(GameEvent.GAME_LOADED);
        postRoomEntered(currentRoomMapName);
        var lang = main.getGameSettings().getLanguage();
        showSaveStatus(SettingsLocalization.get("pause.loaded", lang));
        if (pauseMenuView != null) {
            pauseMenuView.showStatus(SettingsLocalization.get("pause.loaded", lang));
        }
    }

    private void showSaveStatus(String message) {
        saveStatusMessage = message;
        saveStatusTimer = 2.5f;
    }


    @Override

    public void render(float delta) {
        delta = Math.min(delta,0.05f);
        float fixedDelta = Math.min(delta, 1 / 60f);

        handleInput();

        boolean menuOpen = isAnyMenuOpen();

        if (!menuOpen && !victoryShown) {
            totalPlayTimeSeconds += delta;

            if (enemyController != null) enemyController.update(delta, room, knight);

            if (falseKnightController != null) falseKnightController.update(delta, room, knight);

            if (knightController != null) knightController.update(fixedDelta, room);

            checkDoorTransitions();
            checkVictoryTransition();

            if (knightView != null) knightView.update(fixedDelta);


            if (spellController != null) spellController.update(fixedDelta, room);

            if (npcInteractionController != null) {
                npcInteractionController.update(fixedDelta, room);
            }

        }

        if (saveStatusTimer > 0f) {
            saveStatusTimer -= delta;
        }

        if (doorTransitionCooldown > 0f) {
            doorTransitionCooldown -= delta;
        }

        if (environmentalParticleController != null) {
            environmentalParticleController.update(fixedDelta, camera);
        }


        if (knight != null) {
            camera.position.set(knight.getPosition().x,
                knight.getPosition().y + 150f,
                0);
        }

        clampCameraToArenaIfLocked();
        if (shakeTimer > 0f) {
            shakeTimer -= delta;
            float progress = Math.max(0f, shakeTimer / shakeDuration);
            float current = shakeMagnitude * progress;
            camera.position.x += MathUtils.random(-current, current);
            camera.position.y += MathUtils.random(-current, current);
            if (shakeTimer <= 0f) {
                shakeMagnitude = 0f;
            }
        }
        camera.update();


        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(
            room.getBackground(),
            0,
            0,
            roomWidth,
            roomHeight
        );
        batch.end();


        if (mapRenderer != null) {
            AnimatedTiledMapTile.updateAnimationBaseTime();
            mapRenderer.setView(camera);

            mapRenderer.render(new int[]{
                bgLayer,
                solidLayer,
                mainLayer
            });

        }

        if (falseKnightController != null && falseKnightController.isArenaLocked()) {
            Rectangle door = room.getEnterDoorRect();
            if (door != null) {
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                batch.draw(closedDoorTexture, door.x, door.y, door.width, door.height);
                batch.end();
            }
        }

        if (environmentalParticleView != null && environmentalParticleController != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            environmentalParticleView.draw(batch, environmentalParticleController);
            batch.end();
        }

        if (!menuOpen && !knight.isAlive() && knightView.isDeathAnimationFinished()) {

            knight.respawn();

            knightView.resetStateTime();

            eventBus.post(GameEvent.KNIGHT_DIED);

        }


        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (knightView != null) {
            knightView.draw(batch);
            knightView.drawSlashEffect(batch);
        }

        for (Enemy enemy : room.getEnemies()) {
            if (enemy instanceof FalseKnight) {
                continue;
            }
            enemyView.render(batch, enemy, fixedDelta);

        }

        if (npcView != null) {
            npcView.render(batch, room, fixedDelta);
        }

        batch.end();
        mapRenderer.render(new int[]{
            foregroundLayer
        });

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (falseKnightView != null && falseKnightController != null) {
            FalseKnight boss = falseKnightController.getBoss();
            if (boss != null && (boss.isAlive() || boss.getAiState() == FalseKnightState.DEAD)) {
                falseKnightView.render(batch, boss, fixedDelta);
            }
        }

        if (spellView != null) {
            spellView.draw(batch);
        }

        batch.end();

        if (hudRenderer != null && knight != null) {
            int masks = knight.getHealthMasks();
            int soul = knight.getSoulVessel().getCurrentSouls();
            hudRenderer.update(masks, soul, fixedDelta);

            hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setProjectionMatrix(hudCamera.combined);
            batch.begin();
            hudRenderer.render(batch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), masks, soul);
            drawSaveStatus(batch);
            batch.end();
        }

        charmInventoryView.render(fixedDelta);
        if (interactionHintView != null) {
            interactionHintView.render(fixedDelta);
        }
        if (dialogueView != null) {
            dialogueView.render(fixedDelta);
        }
        if (pauseMenuView != null) {
            pauseMenuView.render(fixedDelta);
        }
        if (achievementPopupView != null) {
            achievementPopupView.render(fixedDelta);
        }

        BrightnessOverlay brightnessOverlay = main.getBrightnessOverlay();
        if (brightnessOverlay != null) {
            brightnessOverlay.draw(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

    }

    private void handleInput() {
        if (npcInteractionController != null
            && npcInteractionController.isDialogueOpen()) {
            npcInteractionController.handleInput(room);
            return;
        }

        if (Gdx.input.isKeyJustPressed(keys.getInventory())) {
            if (pauseMenuView != null && pauseMenuView.isOpen()) {
                return;
            }
            charmInventoryView.toggle();
        }

        if (Gdx.input.isKeyJustPressed(keys.getPause())) {
            if (charmInventoryView.isOpen()) {
                charmInventoryView.setOpen(false);
            } else if (pauseMenuView.isOpen()) {
                pauseMenuView.setOpen(false);
            } else {
                pauseMenuView.setOpen(true);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            performSave();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F9)) {
            performQuickLoad();
        }

        if (npcInteractionController != null && room != null) {
            npcInteractionController.handleInput(room);
        }
    }

    private boolean isAnyMenuOpen() {
        return charmInventoryView.isOpen()
            || (pauseMenuView != null && pauseMenuView.isOpen())
            || (npcInteractionController != null && npcInteractionController.isDialogueOpen());
    }

    private void drawSaveStatus(SpriteBatch batch) {
        if (saveStatusTimer <= 0f || saveStatusMessage.isEmpty()) {
            return;
        }
        statusFont.draw(batch, saveStatusMessage, 20f, Gdx.graphics.getHeight() - 20f);
    }


    @Override

    public void resize(int width, int height) {

        if (charmInventoryView != null) {

            charmInventoryView.resize(width, height);

        }

        if (pauseMenuView != null) {
            pauseMenuView.resize(width, height);
        }

        if (dialogueView != null) {
            dialogueView.resize(width, height);
        }

        if (interactionHintView != null) {
            interactionHintView.resize(width, height);
        }

        if (achievementPopupView != null) {
            achievementPopupView.resize(width, height);
        }

        if (hudRenderer != null) {

            hudRenderer.resize(width, height);

        }

        if (hudCamera != null) {

            hudCamera.setToOrtho(false, width, height);

        }

        if (camera != null) {
            camera.setToOrtho(false, width, height);
        }

    }


    @Override

    public void hide() {

        if (charmInventoryView != null && charmInventoryView.isOpen()) {

            charmInventoryView.setOpen(false);

        }

        if (pauseMenuView != null && pauseMenuView.isOpen()) {
            pauseMenuView.setOpen(false);
        }

        if (npcInteractionController != null) {
            npcInteractionController.forceCloseDialogue();
        }

    }


    @Override

    public void dispose() {

        if (charmInventoryView != null) charmInventoryView.dispose();

        if (pauseMenuView != null) pauseMenuView.dispose();

        if (dialogueView != null) dialogueView.dispose();

        if (interactionHintView != null) interactionHintView.dispose();

        if (npcView != null) npcView.dispose();

        if (spellView != null) spellView.dispose();

        if (room != null) room.dispose();

        if (map != null) map.dispose();

        if (mapRenderer != null) mapRenderer.dispose();

        if (batch != null) batch.dispose();

        if (knightView != null) knightView.dispose();

        if (enemyView != null) enemyView.dispose();

        if (falseKnightView != null) falseKnightView.dispose();


        if (hudRenderer != null) hudRenderer.dispose();

        if (statusFont != null) statusFont.dispose();


        if (environmentalParticleView != null) environmentalParticleView.dispose();

        if (achievementPopupView != null) achievementPopupView.dispose();

        if (closedDoorTexture != null) closedDoorTexture.dispose();

    }

    public void markFalseKnightDefeated() {
        falseKnightDefeated = true;
        victoryPending = true;
        eventBus.post(GameEvent.BOSS_DEFEATED);
        eventBus.post(GameEvent.FALSE_KNIGHT_DEFEATED);
        eventBus.post(GameEvent.ENEMY_KILLED, new GameEventPayload.EnemyKilled(EnemyType.FALSE_KNIGHT));
        eventBus.post(GameEvent.GAME_FINISHED, new GameEventPayload.GameFinished(totalPlayTimeSeconds));
    }

    public boolean isFalseKnightDefeated() {
        return falseKnightDefeated;
    }

}

