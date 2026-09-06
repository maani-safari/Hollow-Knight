package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Save.GameData;
import io.github.HollowKnight.Model.Save.SaveManager;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

public class MainMenuScreen extends BaseMenu {

    public MainMenuScreen(Skin skin, Main main) {
        super(skin, main);
    }

    @Override
    protected void showUI() {
        setBackGround("BackGround/Voidheart_menu_BG.png");
        GameSettings.Language lang = main.getGameSettings().getLanguage();
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        Texture titleTexture = new Texture("ui/title.png");
        Image image = new Image(titleTexture);
        TextButton startGameBtn = new TextButton(SettingsLocalization.get("main.start", lang), skin);
        TextButton loadGameBtn = new TextButton(SettingsLocalization.get("main.load", lang), skin);
        TextButton optionBtn = new TextButton(SettingsLocalization.get("main.options", lang), skin);
        TextButton achievementsBtn = new TextButton(SettingsLocalization.get("main.guide", lang), skin);
        TextButton extraBtn = new TextButton(SettingsLocalization.get("main.achievements", lang), skin);
        TextButton quitBtn = new TextButton(SettingsLocalization.get("main.quit", lang), skin);
        startGameBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new ProfileScreen(skin , main));
            }
        });
        loadGameBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SaveManager saveManager = main.getSaveManager();
                int slot = saveManager.findMostRecentSlot();
                GameData data = saveManager.load(slot);
                if (data != null) {
                    main.setScreen(new GameScreen(main, data, slot));
                } else {
                    Gdx.app.log("MainMenu", "No save file found to load.");
                }
            }
        });
        optionBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new SettingsView(skin, main));
            }
        });
        achievementsBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new GuideMenuView(skin, main));
            }
        });
        extraBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new AchievementMenuView(
                    skin,
                    main,
                    main.getAchievementManager(),
                    () -> main.setScreen(new MainMenuScreen(skin, main))
                ));
            }
        });
        quitBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(image).width(1000).height(450).padBottom(40).row();
        table.add(startGameBtn).width(200).padBottom(15).row();
        table.add(loadGameBtn).width(200).padBottom(15).row();
        table.add(optionBtn).width(200).padBottom(15).row();
        table.add(achievementsBtn).width(200).padBottom(15).row();
        table.add(extraBtn).width(200).padBottom(15).row();
        table.add(quitBtn).width(200).padBottom(15);

        mainStack.add(table);
    }
}
