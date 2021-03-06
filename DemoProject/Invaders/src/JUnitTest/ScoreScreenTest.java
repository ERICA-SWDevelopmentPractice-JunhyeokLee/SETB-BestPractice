package JUnitTest;

import static org.junit.Assert.*;

import org.junit.Test;

import engine.GameState;
import engine.IGameState.Difficult;
import screen.ScoreScreen;

public class ScoreScreenTest {
	/** Width of current screen. */
	private static final int WIDTH = 448;
	/** Height of current screen. */
	private static final int HEIGHT = 520;
	/** Max fps of current screen. */
	private static final int FPS = 60;
	Difficult difficult = Difficult.NORMAL;
	GameState gameState = new GameState(1, difficult, 0, 3, 0, 0);

	@Test
	public void testScoreScreen() {
		ScoreScreen scoreScreen = new ScoreScreen(WIDTH, HEIGHT, FPS, gameState);
		assertSame(ScoreScreen.class, scoreScreen.getClass());
	}

}
