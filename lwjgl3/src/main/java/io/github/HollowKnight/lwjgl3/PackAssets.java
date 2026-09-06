package io.github.HollowKnight.lwjgl3;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
public class PackAssets {
    public static void main(String[] args){
//        TexturePacker.Settings settings = new TexturePacker.Settings();
//        settings.maxHeight = 2048;
//        settings.maxWidth = 2048;

        String  inputDir = "assets/Zote";
        String outputDir = "assets/atlas";
        String packName = "Zote";
        TexturePacker.process(inputDir,outputDir,packName);
    }
}
