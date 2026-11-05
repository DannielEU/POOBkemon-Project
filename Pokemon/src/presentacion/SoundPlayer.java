package presentacion;

import domain.Log;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * Reproduce una pista en bucle a la vez. Si ya hay una sonando, la reemplaza.
 * Un fallo de audio se registra pero nunca interrumpe el juego.
 */
final class SoundPlayer {

    private Clip clip;

    /**
     * Reproduce en bucle un archivo de {@link Theme#SONGS}.
     */
    void loop(String fileName) {
        stop();
        try (AudioInputStream input = AudioSystem.getAudioInputStream(new File(Theme.SONGS + fileName))) {
            clip = AudioSystem.getClip();
            clip.open(input);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException | RuntimeException e) {
            Log.record(e);
            clip = null;
        }
    }

    void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
