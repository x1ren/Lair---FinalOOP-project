package org.example.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.example.assets.AssetRegistry;
import org.example.player.CharacterType;

import java.net.URL;
import java.util.Random;

public final class AudioManager {

    private static final String BGM_GENERAL = "/assets/The Lair.mp3";
    private static final String BGM_SIR_KHAI_MIMIC_BOSS = "/assets/The Lair Mimic (Sir Khai).mp3";
    private static final double BGM_VOLUME = 0.38;

    private static final String[] MALE_JUMP_IDS = {
            "audio.jump.masc_jump",
            "audio.jump.masc_multi_1",
            "audio.jump.masc_multi_2",
            "audio.jump.masc_multi_3",
            "audio.jump.retro_1",
            "audio.jump.retro_4",
            "audio.jump.boing",
            "audio.jump.woop"
    };

    private static final String[] FEMALE_JUMP_IDS = {
            "audio.jump.fem_1",
            "audio.jump.fem_2",
            "audio.jump.bup",
            "audio.jump.retro_2",
            "audio.jump.retro_3"
    };

    private static final String[] LAND_IDS = {
            "audio.jump.masc_land",
            "audio.jump.retro_land_1",
            "audio.jump.retro_land_2",
            "audio.jump.bum"
    };

    private static final String[] JIMENEZ_WEAPON_IDS = {
            "audio.weapon.jimenez_rifle_1",
            "audio.weapon.jimenez_rifle_2",
            "audio.weapon.jimenez_rifle_3"
    };
    private static final String[] IBEN_WEAPON_IDS = {
            "audio.weapon.iben_lmg_1",
            "audio.weapon.iben_lmg_2",
            "audio.weapon.iben_lmg_3"
    };
    private static final String[] ILDE_WEAPON_IDS = {
            "audio.weapon.ilde_smg_1",
            "audio.weapon.ilde_smg_2",
            "audio.weapon.ilde_smg_3"
    };
    private static final String[] GAILE_WEAPON_IDS = {
            "audio.weapon.gaille_shotgun_1",
            "audio.weapon.gaille_shotgun_2",
            "audio.weapon.gaille_shotgun_3"
    };
    private static final String[] BACUS_WEAPON_IDS = {
            "audio.weapon.bacus_sniper_1",
            "audio.weapon.bacus_sniper_2",
            "audio.weapon.bacus_sniper_3"
    };

    private static final double WEAPON_FIRE_VOLUME = 0.48;

    private final AssetRegistry assets;
    private final Random random = new Random();

    private MediaPlayer backgroundMusic;
    private String backgroundMusicResource;

    public AudioManager(AssetRegistry assets) {
        this.assets = assets;
    }

    /** Loops story BGM; switches to the Sir Khai mimic track on the final courtyard stage. */
    public void setStoryBackgroundMusic(boolean sirKhaiMimicBossStage) {
        String resource = sirKhaiMimicBossStage ? BGM_SIR_KHAI_MIMIC_BOSS : BGM_GENERAL;
        if (resource.equals(backgroundMusicResource)
                && backgroundMusic != null
                && backgroundMusic.getStatus() == MediaPlayer.Status.PLAYING) {
            return;
        }
        stopBackgroundMusic();
        backgroundMusicResource = resource;
        URL url = AudioManager.class.getResource(resource);
        if (url == null) {
            backgroundMusicResource = null;
            return;
        }
        try {
            Media media = new Media(url.toExternalForm());
            backgroundMusic = new MediaPlayer(media);
            backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusic.setVolume(BGM_VOLUME);
            backgroundMusic.play();
        } catch (RuntimeException ignored) {
            backgroundMusicResource = null;
            backgroundMusic = null;
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
        backgroundMusicResource = null;
    }

    public void playJump(CharacterType character) {
        playRandom(character.isFemaleVoice() ? FEMALE_JUMP_IDS : MALE_JUMP_IDS, 0.30);
    }

    public void playLand() {
        playRandom(LAND_IDS, 0.28);
    }

    public void playFootstep() {
        play("audio.walk." + random.nextInt(10), 0.12);
    }

    public void playDeath() {
        play("audio.death", 0.35);
    }

    /** One-shot gunfire SFX matched to the playable character roster. */
    public void playWeaponFire(CharacterType character) {
        String[] ids = switch (character) {
            case JOSEPH_JIMENEZ -> JIMENEZ_WEAPON_IDS;
            case IBEN_ANOOS -> IBEN_WEAPON_IDS;
            case ILDE_JAN_FIGUERAS -> ILDE_WEAPON_IDS;
            case GAILE_AMOLONG -> GAILE_WEAPON_IDS;
            case JAMUEL_BACUS -> BACUS_WEAPON_IDS;
        };
        playRandom(ids, WEAPON_FIRE_VOLUME);
    }

    private void playRandom(String[] ids, double volume) {
        play(ids[random.nextInt(ids.length)], volume);
    }

    private void play(String id, double volume) {
        AudioClip clip = assets.audio(id);
        if (clip == null) {
            return;
        }
        clip.play(volume);
    }
}
