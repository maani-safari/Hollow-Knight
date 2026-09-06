package io.github.HollowKnight.View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.HollowKnight.Main;

public abstract class BaseMenu implements Screen {
    protected Stage stage;
    protected final Skin skin;
    protected final Main main;
    protected Stack mainStack;
    protected Image bgImage;
    public BaseMenu(Skin skin, Main main) {
        this.stage = new Stage(new ScreenViewport());
        this.skin = skin;
        this.main = main;
        this.mainStack = new Stack();
        this.mainStack.setFillParent(true);
        this.stage.addActor(mainStack);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        showUI();
    }
    protected abstract void showUI();
    protected void setBackGround(String addres){
        Texture texture = new Texture(Gdx.files.internal(addres));
        bgImage = new Image(texture);
        bgImage.setScaling(Scaling.fill);
        mainStack.addActorAt(0 , bgImage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
        drawBrightnessOverlay();
    }

    private void drawBrightnessOverlay() {
        BrightnessOverlay overlay = main.getBrightnessOverlay();
        if (overlay != null) {
            overlay.draw(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width , height ,true);
        mainStack.setSize(width , height);
        if (bgImage != null) {
            bgImage.setSize(width, height);
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
