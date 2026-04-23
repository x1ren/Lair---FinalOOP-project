package org.example.assets;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AssetRegistry {

    private final Map<String, String> imagePaths = new LinkedHashMap<>();
    private final Map<String, String> audioPaths = new LinkedHashMap<>();
    private final Map<String, Image> imageCache = new HashMap<>();
    private final Map<String, AudioClip> audioCache = new HashMap<>();
    private final Map<String, SpriteSheet> spriteCache = new HashMap<>();

    private volatile boolean preloadStarted;
    private volatile boolean preloadComplete;

    public AssetRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        registerImage("character.joseph", "/assets/characters/jimenez.png");
        registerImage("character.iben", "/assets/characters/iben.png");
        registerImage("character.ilde", "/assets/characters/ilde.png");
        registerImage("character.gaille", "/assets/characters/gaille.png");
        registerImage("character.jamuel", "/assets/characters/bacus.png");
        registerImage("character.preview", "/assets/characters/preview.gif");

        registerImage("stage.library", "/assets/stages/library.png");
        registerImage("stage.canteen", "/assets/stages/canteen.png");
        registerImage("stage.gym", "/assets/stages/gym.png");
        registerImage("stage.courtyard", "/assets/stages/courtyard.png");

        registerImage("enemy.librarian", "/assets/stages/enemies/librarian.png");
        registerImage("enemy.security_guard", "/assets/stages/enemies/security_guard.png");
        registerImage("enemy.student_f", "/assets/stages/enemies/student_f.png");
        registerImage("enemy.student_m", "/assets/stages/enemies/student_m.png");
        registerImage("enemy.vendor", "/assets/stages/enemies/vendor.png");
        registerImage("enemy.janitor", "/assets/stages/enemies/janitor.png");
        registerImage("character.sir_khai", "/assets/stages/enemies/khai_with_zombified.png");
        registerImage("enemy.khai_mimic", "/assets/stages/enemies/khai_boss_mimic.png");
        registerImage("enemy.caesar_hunos", "/assets/stages/enemies/caesar_hunos_idle.png");
        registerImage("intro.caesar_human_idle", "/assets/stages/enemies/caesar_human_idle.png");
        registerImage("intro.caesar_human_walk", "/assets/stages/enemies/caesar_human_walk.png");

        registerImage("ui.skill.jimenez", "/assets/ui/skills/Jimenez_Skill.png");
        registerImage("ui.skill.anoos", "/assets/ui/skills/Anoos_Skill.png");
        registerImage("ui.skill.figueras", "/assets/ui/skills/Figueras_Skill.png");
        registerImage("ui.skill.amolong", "/assets/ui/skills/Amolong_Skill.png");
        registerImage("ui.skill.bacus", "/assets/ui/skills/Bacus_Skill.png");

        registerImage("effect.muzzle_flash", "/assets/effects/muzzle_flash.png");
        registerImage("weapon.smg", "/assets/effects/muzzle_flash.png");

        registerAudio("audio.death", "/assets/audio/death.mp3");
        registerAudio("audio.jump.boing", "/assets/audio/jump/Boing.mp3");
        registerAudio("audio.jump.bum", "/assets/audio/jump/Bum.mp3");
        registerAudio("audio.jump.bup", "/assets/audio/jump/Bup.mp3");
        registerAudio("audio.jump.fall_funny", "/assets/audio/jump/Fall to Death Funny Ps1 Style.mp3");
        registerAudio("audio.jump.fem_1", "/assets/audio/jump/Fem Jump 1.mp3");
        registerAudio("audio.jump.fem_2", "/assets/audio/jump/Fem Jump 2.mp3");
        registerAudio("audio.jump.masc_fall_1", "/assets/audio/jump/Masc Fall 1.mp3");
        registerAudio("audio.jump.masc_fall_2", "/assets/audio/jump/Masc Fall 2.mp3");
        registerAudio("audio.jump.masc_fall_3", "/assets/audio/jump/Masc Fall 3.mp3");
        registerAudio("audio.jump.masc_jump", "/assets/audio/jump/Masc Jump.mp3");
        registerAudio("audio.jump.masc_land", "/assets/audio/jump/Masc Land.mp3");
        registerAudio("audio.jump.masc_multi_1", "/assets/audio/jump/Masc Multi Jump 1.mp3");
        registerAudio("audio.jump.masc_multi_2", "/assets/audio/jump/Masc Multi Jump 2.mp3");
        registerAudio("audio.jump.masc_multi_3", "/assets/audio/jump/Masc Multi Jump 3.mp3");
        registerAudio("audio.jump.retro_1", "/assets/audio/jump/Retro Jump 1.mp3");
        registerAudio("audio.jump.retro_2", "/assets/audio/jump/Retro Jump 2.mp3");
        registerAudio("audio.jump.retro_3", "/assets/audio/jump/Retro Jump 3.mp3");
        registerAudio("audio.jump.retro_4", "/assets/audio/jump/Retro Jump 4.mp3");
        registerAudio("audio.jump.retro_land_1", "/assets/audio/jump/Retro Land 1.mp3");
        registerAudio("audio.jump.retro_land_2", "/assets/audio/jump/Retro Land 2.mp3");
        registerAudio("audio.jump.woop", "/assets/audio/jump/Woop.mp3");

        for (int i = 0; i < 10; i++) {
            registerAudio("audio.walk." + i, String.format("/assets/audio/walk/footstep%02d.ogg", i));
        }
    }

    private void registerImage(String id, String path) {
        imagePaths.put(id, path);
    }

    private void registerAudio(String id, String path) {
        audioPaths.put(id, path);
    }

    public Image image(String id) {
        if (imageCache.containsKey(id)) {
            return imageCache.get(id);
        }
        Image image = loadImage(id);
        imageCache.put(id, image);
        return image;
    }

    public AudioClip audio(String id) {
        if (audioCache.containsKey(id)) {
            return audioCache.get(id);
        }
        AudioClip clip = loadAudio(id);
        audioCache.put(id, clip);
        return clip;
    }

    public SpriteSheet sheet(String imageId, int frameWidth, int frameHeight) {
        String cacheKey = imageId + "@" + frameWidth + "x" + frameHeight;
        if (spriteCache.containsKey(cacheKey)) {
            return spriteCache.get(cacheKey);
        }

        Image image = image(imageId);
        SpriteSheet sheet = image == null ? null : new SpriteSheet(image, frameWidth, frameHeight);
        spriteCache.put(cacheKey, sheet);
        return sheet;
    }

    private Image loadImage(String id) {
        String path = imagePaths.get(id);
        if (path == null) {
            return null;
        }

        try (InputStream stream = AssetRegistry.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            return new Image(stream);
        } catch (Exception ignored) {
            return null;
        }
    }

    private AudioClip loadAudio(String id) {
        String path = audioPaths.get(id);
        if (path == null) {
            return null;
        }

        try {
            URL url = AssetRegistry.class.getResource(path);
            return url == null ? null : new AudioClip(url.toExternalForm());
        } catch (Exception ignored) {
            return null;
        }
    }

    public void preloadAll() {
        preloadStarted = true;
        for (String imageId : imagePaths.keySet()) {
            image(imageId);
        }
        for (String audioId : audioPaths.keySet()) {
            audio(audioId);
        }
    }

    public void markPreloadComplete() {
        preloadComplete = true;
    }

    public boolean isPreloadStarted() {
        return preloadStarted;
    }

    public boolean isPreloadComplete() {
        return preloadComplete;
    }
}
