package no.ntnu.trostespel.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Assets {

    public static Texture img;
    public static Texture lemurImage;
    public static Texture lemurRunSheet;
    public static Texture bullet;
    public static Texture attack;
    public static Texture healthbarBack;
    public static Texture healthbarFront;

    public static void load() {
        img = new Texture("badlogic.jpg");
        lemurImage = new Texture(Gdx.files.internal("lemurSideIdle.png"));
        lemurRunSheet = new Texture(Gdx.files.internal("lemurRunSheet.png"));
        attack = new Texture(Gdx.files.internal("lemur-openmouth.png"));
        bullet = new Texture(Gdx.files.internal("bullet_texture.png"));
        healthbarBack = new Texture(Gdx.files.internal("healthbar_back.png"));
        healthbarFront = new Texture(Gdx.files.internal("healthbar_front.png"));
    }
}
