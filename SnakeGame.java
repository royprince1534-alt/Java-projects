import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.*;

/**
 * Single-file Snake game including `SnakeGame` (main), `GamePanel` (gameplay),
 * and `Sound` (tone generator). This keeps everything in one source for
 * simple compilation and running in editors like VS Code.
 */
public class SnakeGame {
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Snake Game");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.add(new GamePanel());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setResizable(false);
			frame.setVisible(true);
		});
	}
}

/**
 * GamePanel contains the gameplay logic and rendering.
 * - Uses a Swing Timer for continuous movement
 * - Arrow keys control the snake
 * - Generates random food
 * - Detects collisions (walls and self)
 * - Displays score and Game Over with restart option
 */
class GamePanel extends JPanel implements ActionListener, KeyListener {
	static final int SCREEN_WIDTH = 600;
	static final int SCREEN_HEIGHT = 600;
	static final int UNIT_SIZE = 25;
	static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

	final int x[] = new int[GAME_UNITS];
	final int y[] = new int[GAME_UNITS];
	int bodyParts = 6;
	int applesEaten = 0;
	int appleX;
	int appleY;
	char direction = 'R'; // U, D, L, R
	boolean running = false;
	Timer timer;
	int delay = 150; // milliseconds between moves; will decrease as score increases
	Random random;

	public GamePanel() {
		random = new Random();
		this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
		this.setBackground(Color.BLACK);
		this.setFocusable(true);
		this.addKeyListener(this);
		startGame();
	}

	// Initialize and start the game
	public void startGame() {
		newApple();
		running = true;
		timer = new Timer(delay, this);
		timer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	// Draw game elements
	private void draw(Graphics g) {
		if (running) {
			// optional grid for clarity (subtle)
			for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
				g.setColor(new Color(40, 40, 40));
				g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
				g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
			}

			// draw food
			g.setColor(Color.RED);
			g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

			// draw snake
			for (int i = 0; i < bodyParts; i++) {
				if (i == 0) {
					g.setColor(Color.GREEN.brighter());
					g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
				} else {
					g.setColor(new Color(45, 180, 0));
					g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
				}
			}

			// draw score
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, 20));
			FontMetrics metrics = getFontMetrics(g.getFont());
			g.drawString("Score: " + applesEaten, 10, metrics.getHeight());
		} else {
			gameOver(g);
		}
	}

	// Place a new apple at a random grid location
	private void newApple() {
		appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
		appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
	}

	// Move the snake by updating the coordinates array
	private void move() {
		for (int i = bodyParts; i > 0; i--) {
			x[i] = x[i - 1];
			y[i] = y[i - 1];
		}

		switch (direction) {
			case 'U': y[0] = y[0] - UNIT_SIZE; break;
			case 'D': y[0] = y[0] + UNIT_SIZE; break;
			case 'L': x[0] = x[0] - UNIT_SIZE; break;
			case 'R': x[0] = x[0] + UNIT_SIZE; break;
		}
	}

	// Check if snake ate the apple
	private void checkApple() {
		if ((x[0] == appleX) && (y[0] == appleY)) {
			bodyParts++;
			applesEaten++;
			Sound.playTone(900, 100); // play eat sound
			// Increase speed every 3 apples, with a lower bound
			if (applesEaten % 3 == 0 && delay > 50) {
				delay -= 15;
				timer.setDelay(delay);
			}
			newApple();
		}
	}

	// Detect collisions with self or walls
	private void checkCollisions() {
		// Self collision
		for (int i = bodyParts; i > 0; i--) {
			if ((x[0] == x[i]) && (y[0] == y[i])) {
				running = false;
			}
		}

		// Wall collisions
		if (x[0] < 0) running = false;
		if (x[0] >= SCREEN_WIDTH) running = false;
		if (y[0] < 0) running = false;
		if (y[0] >= SCREEN_HEIGHT) running = false;

		if (!running) {
			timer.stop();
			Sound.playTone(200, 400); // game over sound
		}
	}

	// Draw Game Over and restart instructions
	private void gameOver(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 28));
		FontMetrics metrics1 = getFontMetrics(g.getFont());
		g.drawString("Score: " + applesEaten,
				(SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2,
				SCREEN_HEIGHT / 2 - 50);

		g.setColor(Color.RED);
		g.setFont(new Font("Arial", Font.BOLD, 48));
		FontMetrics metrics2 = getFontMetrics(g.getFont());
		g.drawString("Game Over",
				(SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2,
				SCREEN_HEIGHT / 2);

		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		FontMetrics metrics3 = getFontMetrics(g.getFont());
		String s = "Press R to Restart or ESC to Exit";
		g.drawString(s, (SCREEN_WIDTH - metrics3.stringWidth(s)) / 2, SCREEN_HEIGHT / 2 + 50);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (running) {
			move();
			checkApple();
			checkCollisions();
		}
		repaint();
	}

	// Keyboard controls
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				if (direction != 'R') direction = 'L';
				break;
			case KeyEvent.VK_RIGHT:
				if (direction != 'L') direction = 'R';
				break;
			case KeyEvent.VK_UP:
				if (direction != 'D') direction = 'U';
				break;
			case KeyEvent.VK_DOWN:
				if (direction != 'U') direction = 'D';
				break;
			case KeyEvent.VK_R:
				if (!running) {
					// Reset game state and restart
					bodyParts = 6;
					applesEaten = 0;
					direction = 'R';
					for (int i = 0; i < x.length; i++) { x[i] = 0; y[i] = 0; }
					delay = 150;
					startGame();
				}
				break;
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
				break;
		}
	}

	@Override public void keyReleased(KeyEvent e) {}
	@Override public void keyTyped(KeyEvent e) {}
}

/**
 * Simple sound helper. Generates short tones programmatically so no external
 * audio files are needed. Uses javax.sound.sampled.SourceDataLine to play
 * a sine wave for the requested duration.
 */
class Sound {
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


