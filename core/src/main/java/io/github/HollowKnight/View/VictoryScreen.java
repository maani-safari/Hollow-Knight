package io.github.HollowKnight.View;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.HollowKnight.Main;
import io.github.HollowKnight.Model.Settings.GameSettings;
import io.github.HollowKnight.Model.Settings.SettingsLocalization;

public class VictoryScreen extends BaseMenu {

    public interface Listener {
        void onRestartGame();
        void onReturnToMainMenu();
    }
    @Override
    public void show() {
        main.getAudioManager().playMusic("audio/hans_zimmer_interstellar_day one.mp3");
        super.show();
    }
    private final int totalDeaths;
    private final int totalEnemiesKilled;
    private final float totalPlayTimeSeconds;
    private final Listener listener;

    public VictoryScreen(
        Skin skin,
        Main main,
        int totalDeaths,
        int totalEnemiesKilled,
        float totalPlayTimeSeconds,
        Listener listener
    ) {
        super(skin, main);
        this.totalDeaths = totalDeaths;
        this.totalEnemiesKilled = totalEnemiesKilled;
        this.totalPlayTimeSeconds = totalPlayTimeSeconds;
        this.listener = listener;
    }

    @Override
    protected void showUI() {
        setBackGround("BackGround/Voidheart_menu_BG.png");
        GameSettings.Language lang = main.getGameSettings().getLanguage();

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label(SettingsLocalization.get("victory.title", lang), skin);
        titleLabel.setAlignment(1);

        Label deathsLabel = new Label(SettingsLocalization.format("victory.deaths", lang, totalDeaths), skin);
        deathsLabel.setAlignment(1);

        Label killsLabel = new Label(SettingsLocalization.format("victory.kills", lang, totalEnemiesKilled), skin);
        killsLabel.setAlignment(1);

        Label timeLabel = new Label(
            SettingsLocalization.format("victory.time", lang, formatPlayTime(totalPlayTimeSeconds)),
            skin
        );
        timeLabel.setAlignment(1);

        TextButton restartBtn = new TextButton(SettingsLocalization.get("victory.restart", lang), skin);
        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    listener.onRestartGame();
                }
            }
        });
        TextButton menuBtn = new TextButton(SettingsLocalization.get("victory.menu", lang), skin);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    listener.onReturnToMainMenu();
                }
            }
        });

        table.add(titleLabel).padBottom(40).row();
        table.add(deathsLabel).padBottom(12).row();
        table.add(killsLabel).padBottom(12).row();
        table.add(timeLabel).padBottom(40).row();
        table.add(restartBtn).width(280).padBottom(15).row();
        table.add(menuBtn).width(280);

        mainStack.add(table);
    }

    public static String formatPlayTime(float totalSeconds) {
        int total = Math.max(0, (int) totalSeconds);
        int hours = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
