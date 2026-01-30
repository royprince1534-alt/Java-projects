import javax.sound.sampled.*;

/**
 * Simple sound helper. Generates short tones programmatically so no external
 * audio files are needed. Uses javax.sound.sampled.SourceDataLine to play
 * a sine wave for the requested duration.
 */
public class Sound {
    public static void playTone(int hz, int msecs) {
        try {
            float sampleRate = 44100f;
            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();

            byte[] buf = new byte[1];
            int samples = (int)((msecs / 1000.0) * sampleRate);
            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * i * hz / sampleRate;
                buf[0] = (byte)(Math.sin(angle) * 100);
                sdl.write(buf, 0, 1);
            }

            sdl.drain();
            sdl.stop();
            sdl.close();
        } catch (Exception e) {
            // If sound fails for any reason, fall back to system beep
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
}
