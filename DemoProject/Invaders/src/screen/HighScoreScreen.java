package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import engine.Core;
import engine.IGameState;
import engine.Score;

/**
 * Implements the high scores screen, it shows player records.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class HighScoreScreen extends Screen {

	/** List of past high scores. */
	private List<Score> highScores;

	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	String difficultstr = "";
	public HighScoreScreen(final int width, final int height, final int fps, IGameState.Difficult difficult) {
		super(width, height, fps);

		this.returnCode = -1;

		try {
			switch (difficult){
				case EASY -> {difficultstr = "easy";}
				case NORMAL -> {difficultstr = "normal";}
				case HARD -> {difficultstr = "hard";}
			}
			this.highScores = Core.getFileManager().loadHighScores(difficultstr+"scores");
		} catch (NumberFormatException | IOException e) {
			logger.warning("Couldn't load high scores!");
		}
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final int run() {
		super.run();

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		draw();
		if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)
				&& this.inputDelay.checkFinished()) {
			highScores.clear();
			try {
				Core.getFileManager().saveHighScores(highScores,difficultstr+"scores");
			} catch (IOException e) {
				logger.warning("Couldn't load high scores!");
			}
		}
		if (inputManager.isKeyDown(KeyEvent.VK_SPACE)
				&& this.inputDelay.checkFinished())
			this.isRunning = false;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawHighScoreMenu(this, difficultstr);
		drawManager.drawHighScores(this, this.highScores);

		drawManager.completeDrawing(this);
	}
}
