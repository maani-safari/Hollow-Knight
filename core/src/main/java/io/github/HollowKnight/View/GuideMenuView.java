package io.github.HollowKnight.View;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.HollowKnight.Controller.GuideMenuController;
import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Cheat.CheatCatalog.CheatEntry;
import io.github.HollowKnight.Model.Guide.KnightGuideContent.GuideEntry;
import io.github.HollowKnight.Model.Guide.KnightGuideContent.GuideSection;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.KeyController.GameplayBinding;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

public class GuideMenuView extends BaseMenu {

    private final GuideMenuController controller;

    public GuideMenuView(Skin skin, Main main) {
        super(skin, main);
        this.controller = new GuideMenuController(main, main.getKeyController());
    }

    @Override
    public void show() {
        super.show();
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    controller.close();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void showUI() {
        setBackGround("BackGround/Voidheart_menu_BG.png");
        GameSettings.Language lang = main.getGameSettings().getLanguage();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(30);

        Label titleLabel = new Label(SettingsLocalization.get("guide.title", lang), skin);
        root.add(titleLabel).padBottom(20).row();

        Table contentTable = new Table();
        contentTable.top().left();
        contentTable.defaults().left().padBottom(6);

        buildControlsSection(contentTable, lang);
        buildAbilitiesSection(contentTable);
        buildCheatsSection(contentTable, lang);

        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        root.add(scrollPane).expand().fill().padBottom(20).row();

        TextButton backBtn = new TextButton(SettingsLocalization.get("back", lang), skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.close();
            }
        });
        root.add(backBtn).width(150);

        mainStack.add(root);
    }

    private void buildControlsSection(Table content, GameSettings.Language lang) {
        addSectionHeader(content, SettingsLocalization.get("guide.controls", lang));
        for (GameplayBinding binding : controller.getControlBindings()) {
            Table row = new Table();
            row.add(new Label(binding.label(), skin)).width(280).left();
            row.add(new Label(binding.keyDisplay(), skin)).left();
            content.add(row).padBottom(4).row();
        }
        content.add().padBottom(16).row();
    }

    private void buildAbilitiesSection(Table content) {
        addSectionHeader(content, SettingsLocalization.get("guide.abilities", main.getGameSettings().getLanguage()));
        for (GuideSection section : controller.getAbilitySections()) {
            content.add(new Label(section.title(), skin)).padTop(10).padBottom(4).row();
            for (GuideEntry entry : section.entries()) {
                Label entryLabel = new Label(
                    "  \u2022 " + entry.title() + " — " + entry.description(),
                    skin
                );
                entryLabel.setWrap(true);
                content.add(entryLabel).width(700).padBottom(4).row();
            }
        }
        content.add().padBottom(16).row();
    }

    private void buildCheatsSection(Table content, GameSettings.Language lang) {
        addSectionHeader(content, SettingsLocalization.get("guide.cheats", lang));
        for (CheatEntry cheat : controller.getCheatEntries()) {
            content.add(new Label(cheat.name(), skin)).padTop(8).padBottom(2).row();
            content.add(new Label(SettingsLocalization.format("guide.shortcut", lang, cheat.shortcut()), skin)).padBottom(2).row();
            Label descLabel = new Label("  " + cheat.description(), skin);
            descLabel.setWrap(true);
            content.add(descLabel).width(700).padBottom(6).row();
        }
    }

    private void addSectionHeader(Table content, String text) {
        Label header = new Label(text, skin);
        content.add(header).padTop(12).padBottom(10).row();
    }
}
