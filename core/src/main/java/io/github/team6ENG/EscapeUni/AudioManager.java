package io.github.team6ENG.EscapeUni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * Control and play game sounds
 */
public class AudioManager {

    private final Main game;
    private final Sound torchClick;
    private final Sound honk;
    private final Sound footSteps;
    private final Music music;
    private final Music musicDungeon;
    private final Sound noAccess;
    private final Sound collect;
    private final Sound rumble;
    private final Sound impact;
    private final Sound quack;
    private final Sound thunder;


    /**
     * Initialised audio manager
     * @param game current instance of Main
     */
    public AudioManager(final Main game){
        this.game = game;

        honk = Gdx.audio.newSound(Gdx.files.internal("soundEffects/honk.mp3"));
        torchClick = Gdx.audio.newSound(Gdx.files.internal("soundEffects/click.mp3"));
        footSteps = Gdx.audio.newSound(Gdx.files.internal("soundEffects/footsteps.mp3"));
        noAccess = Gdx.audio.newSound(Gdx.files.internal("soundEffects/wrong.mp3"));
        collect = Gdx.audio.newSound(Gdx.files.internal("soundEffects/tap.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("soundEffects/music.mp3"));
        musicDungeon = Gdx.audio.newMusic(Gdx.files.internal("soundEffects/dungeon.mp3"));
        rumble = Gdx.audio.newSound(Gdx.files.internal("soundEffects/stone_falling.mp3"));
        impact = Gdx.audio.newSound(Gdx.files.internal("soundEffects/Impact.mp3"));
        quack = Gdx.audio.newSound(Gdx.files.internal("soundEffects/Quack.mp3"));
        thunder =  Gdx.audio.newSound(Gdx.files.internal("soundEffects/Thunder.mp3"));

        playMusic();
    }

    public void playThunder(){
        thunder.play(game.gameVolume);
    }
    public void playQuack(){
        quack.play(game.gameVolume);
    }
    public void playImpact(){
        impact.play(game.gameVolume);
    }
    public void playRumble(){
        rumble.play(game.gameVolume);
    }
    public void playHonk(){
        honk.play(game.gameVolume);
    }
    public void playTorch(){
        torchClick.play(game.gameVolume);
    }
    public void playNoAccess(){
        noAccess.play(game.gameVolume);
    }
    public void playCollect(){collect.play(game.gameVolume);}
    public void loopFootsteps(){
        footSteps.loop(.2f *game.gameVolume);
    }
    public void stopFootsteps(){
        footSteps.stop();
    }

    public void playDungeonMusic(){
        musicDungeon.setVolume(0.02f);
        musicDungeon.play();
        musicDungeon.setLooping(true);
    }
    public void playMusic(){
        setMusicVolume();
        music.play();
        music.setLooping(true);
    }
    public void setMusicVolume(){
        music.setVolume(0.01f * game.musicVolume);
    }
    public void stopMusic(){
        music.stop();
    }
    public void pauseMusic(){
        music.pause();
    }

    public void stopDungeonMusic(){musicDungeon.stop();}

    public void dispose(){

        if (torchClick != null) {
            torchClick.dispose();
        }
        if (honk != null) {
            honk.dispose();
        }
    }
}
