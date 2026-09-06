package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;
import io.github.HollowKnight.Model.charm.CharmInventory;
import io.github.HollowKnight.Model.charm.CharmSlots;
import io.github.HollowKnight.Model.charm.Charms;
import io.github.HollowKnight.Model.entity.Knight;

public class CharmInventoryView {
    private final Stage stage;
    private final Skin skin;
    private final Knight knight;
    private final CharmInventory inventory;
    private final GameSettings.Language language;

    private final Table ownedTable;
    private final Table equippedTable;
    private final Label notchLabel;
    private final Label descriptionLabel;
    private final ObjectMap<Charms , Texture> icons = new ObjectMap<>();
    private boolean open;

    public CharmInventoryView(Skin skin, Knight knight, CharmInventory inventory, GameSettings.Language language) {
        this.skin = skin;
        this.knight = knight;
        this.inventory = inventory;
        this.language = language;
        this.stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.getDrawable("black_pxl"));

        Label titleLabel = new Label(SettingsLocalization.get("charms.title", language), skin);
        notchLabel = new Label("", skin);
        descriptionLabel = new Label(SettingsLocalization.get("charms.select", language), skin);
        descriptionLabel.setWrap(true);

        ownedTable = new Table();
        equippedTable = new Table();

        ScrollPane ownedScroll = new ScrollPane(ownedTable, skin);
        ownedScroll.setFadeScrollBars(false);
        ScrollPane equippedScroll = new ScrollPane(equippedTable, skin);
        equippedScroll.setFadeScrollBars(false);

        for (Charms charm : Charms.values()){
            icons.put(charm ,new Texture(Gdx.files.internal(charm.getAddress())));
        }

        Table columns = new Table();
        columns.add(new Label(SettingsLocalization.get("charms.owned", language), skin)).expandX().left().padBottom(8);
        columns.add(new Label(SettingsLocalization.get("charms.equipped", language), skin)).expandX().left().padBottom(8).padLeft(20);
        columns.row();
        columns.add(ownedScroll).width(320).height(280).expand();
        columns.add(equippedScroll).width(260).height(280).padLeft(20).expand();

        root.add(titleLabel).padTop(30).padBottom(10).row();
        root.add(notchLabel).padBottom(15).row();
        root.add(columns).padBottom(15).row();
        root.add(descriptionLabel).width(620).padBottom(20).row();
        root.add(new Label(SettingsLocalization.get("charms.closeHint", language), skin)).padBottom(25);
        root.setBackground(skin.newDrawable("white-pixel", 0f, 0f, 0f, 0.45f));

        stage.addActor(root);
        refresh();
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        setOpen(!open);
    }

    public void setOpen(boolean open) {
        this.open = open;
        Gdx.input.setInputProcessor(open ? stage : null);
        if (open) refresh();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void render(float delta) {
        if (!open) return;
        stage.act(delta);
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
    }

    private void refresh() {
        ownedTable.clear();
        for (Charms charm : Charms.values()) {
            if (!inventory.owns(charm)) continue;

            boolean equipped = knight.getCharmSlots().isEquipped(charm);
            String label = (equipped ? "[*] " : "    ") + charm.getDisplayName()
                + " (" + charm.getNotchCost() + ")";
            Image image = new Image(new TextureRegionDrawable(
                new TextureRegion(icons.get(charm))
            ));
            TextButton btn = new TextButton(label, skin);
            Table row = new Table();
            row.add(image).size(64).padRight(5);
            row.add(btn).bottom();
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onCharmClicked(charm);
                }
            });
            ownedTable.add(row).width(320).pad(4).left().row();
        }

        equippedTable.clear();
        for (Charms charm : Charms.values()) {
            if (!knight.getCharmSlots().isEquipped(charm)) continue;
            Image image = new Image(new TextureRegionDrawable(
                new TextureRegion(icons.get(charm))
            ));
            TextButton btn = new TextButton(charm.getDisplayName(), skin);
            Table row = new Table();
            row.add(image).size(64).padRight(5);
            row.add(btn).bottom();
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    knight.unequipCharm(charm);
                    descriptionLabel.setText(SettingsLocalization.format(
                        "charms.unequipped", language, charm.getDisplayName()));
                    refresh();
                }
            });
            equippedTable.add(row).width(300).pad(4).left().row();
        }

        CharmSlots slots = knight.getCharmSlots();
        notchLabel.setText(SettingsLocalization.format(
            "charms.notches", language, slots.usedNotches(), CharmSlots.MAX_NOTCHES));
    }

    private void onCharmClicked(Charms charm) {
        CharmSlots slots = knight.getCharmSlots();
        if (slots.isEquipped(charm)) {
            knight.unequipCharm(charm);
            descriptionLabel.setText(SettingsLocalization.format(
                "charms.unequipped", language, charm.getDisplayName()));
        } else if (knight.equipCharm(charm)) {
            descriptionLabel.setText(charm.getDisplayName() + " — " + charm.getDescription());
        } else {
            descriptionLabel.setText(SettingsLocalization.format(
                "charms.notEnough", language, charm.getDisplayName(), charm.getNotchCost()));
        }
        refresh();
    }
}
