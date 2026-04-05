package org.example.audio;

import javafx.scene.media.AudioClip;
import org.example.assets.AssetRegistry;
import org.example.player.CharacterType;

import java.util.Random;

public final class AudioManager {

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

    private final AssetRegistry assets;
    private final Random random = new Random();

    public AudioManager(AssetRegistry assets) {
        this.assets = assets;
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
