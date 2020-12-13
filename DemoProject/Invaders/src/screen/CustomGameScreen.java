package screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import engine.*;
import entity.*;

/**
 * Implements the game screen, where the action happens.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class CustomGameScreen extends IGameScreen {

    ArrayList<Player> players;
    CustomGameState.MultiMethod multimethod;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameState
     *            Current game state.
     * @param gameSettings
     *            Current game settings.
     * @param bonusLife
     *            Checks if a bonus life is awarded this level.
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     */
    public CustomGameScreen(final CustomGameState gameState,
                      final GameSettings gameSettings, final boolean bonusLife, int MAX_LIVES,
                      final int width, final int height, final int fps, int playernum) {
        super(width, height, fps);
        CustomGameState customGameState = gameState;
        this.players = gameState.getPlayers();
        this.gameSettings = gameSettings;
        this.bonusLife = bonusLife;
        this.level = customGameState.getLevel();
        this.difficult = customGameState.getDifficult();
        this.multimethod = customGameState.getMethod();
        if (this.bonusLife) {
            for( Player p :  players){
                if( p.addlive(1) == 1 )
                    p.setDie(false);
            };
        }

        this.returnCode = 1;
    }

    /**
     * Initializes basic screen properties, and adds necessary elements.
     */
    public final void initialize() {
        super.initialize();

        for( int i = 0 ; i < players.size() ; i++){
            Player p = players.get(i);
            Ship ship = new Ship((this.width / (1+players.size()))*(i+1), this.height - 30, Player.PlayerColor(i));
            ship.setPlayer(p);
            p.setShip(ship);
        }

    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();

        for (Player p : players) {
            p.addScore(LIFE_SCORE * (p.getLives() - 1));
            this.logger.info("Screen cleared with player " +p.getName() + "score of " );
        }

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        if (this.inputDelay.checkFinished() && !this.levelFinished) {
            //////////치트 영역///////////

            //점수 증가
            if (inputManager.isKeyDown(KeyEvent.VK_1)) {
                for(Player p : players)
                    p.addScore(100);
            }
            //목숨 감소
            if (inputManager.isKeyDown(KeyEvent.VK_2)) {
                for(Player p : players)
                    p.subtractlive(1);
            }
            //레벨 클리어
            if (inputManager.isKeyDown(KeyEvent.VK_3)) {
                this.levelFinished = true;
                this.screenFinishedCooldown.reset();
            }
            ///////////치트 끝//////////

            if (isPause()) {
                if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && pauseDelay.checkFinished()) {
                    setPause(false);
                    this.pauseDelay.reset();
                }
                draw();
                return;
            } else {
                if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && pauseDelay.checkFinished()) {
                    setPause(true);
                    this.pauseDelay.reset();
                    return;
                }
            }

            for(Player p : players) {
                if( p.isDie() ) continue;
                Ship ship = p.getShip();
                if (!ship.isDestroyed()) {
                    boolean isRightBorder = ship.getPositionX()
                            + ship.getWidth() + ship.getSpeed() > this.width - 1;
                    boolean isLeftBorder = ship.getPositionX()
                            - ship.getSpeed() < 1;

                    if (inputManager.isKeyDown(p.getInputs()[0]) && !isRightBorder) {
                        ship.moveRight();
                    }
                    if (inputManager.isKeyDown(p.getInputs()[1]) && !isLeftBorder) {
                        ship.moveLeft();
                    }
                    if (inputManager.isKeyDown(p.getInputs()[2]))
                        if (ship.shoot(this.bullets))
                            p.addbulletsShot(1);
                }
                p.update();
            }
            if (this.enemyShipSpecial != null) {
                if (!this.enemyShipSpecial.isDestroyed())
                    this.enemyShipSpecial.move(2, 0);
                else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
                    this.enemyShipSpecial = null;

            }
            if (this.enemyShipSpecial == null
                    && this.enemyShipSpecialCooldown.checkFinished()) {
                this.enemyShipSpecial = new EnemyShip();
                this.enemyShipSpecialCooldown.reset();
                this.logger.info("A special ship appears");
            }
            if (this.enemyShipSpecial != null
                    && this.enemyShipSpecial.getPositionX() > this.width) {
                this.enemyShipSpecial = null;
                this.logger.info("The special ship has escaped");
            }

            this.enemyShipFormation.update();
            this.enemyShipFormation.shoot(this.bullets);
        }

        manageCollisions();
        cleanBullets();
        draw();


        if ((this.enemyShipFormation.isEmpty() || allPlayerDie() )
                && !this.levelFinished) {
            this.levelFinished = true;
            this.screenFinishedCooldown.reset();
        }

        if (this.levelFinished && this.screenFinishedCooldown.checkFinished())
            this.isRunning = false;

    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        for(Player p : players) {
            Ship ship = p.getShip();
            if( !p.isDie() )
            drawManager.drawEntity(ship, ship.getPositionX(),
                    ship.getPositionY());
        }
        if (this.enemyShipSpecial != null)
            drawManager.drawEntity(this.enemyShipSpecial,
                    this.enemyShipSpecial.getPositionX(),
                    this.enemyShipSpecial.getPositionY());

        enemyShipFormation.draw();

        for (Bullet bullet : this.bullets)
            drawManager.drawEntity(bullet, bullet.getPositionX(),
                    bullet.getPositionY());

        // Interface.
        for( int i = 0 ; i < players.size() ; i++){
            drawManager.drawScore(this, players.get(i).getScore(),i);
            drawManager.drawLives(this, players.get(i).getLives(),i);
            drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
        }
        // Countdown to game start.
        if (!this.inputDelay.checkFinished()) {
            int countdown = (int) ((INPUT_DELAY
                    - (System.currentTimeMillis()
                    - this.gameStartTime)) / 1000);
            drawManager.drawCountDown(this, this.level, countdown,
                    this.bonusLife);
            drawManager.drawHorizontalLine(this, this.height / 2 - this.height
                    / 12);
            drawManager.drawHorizontalLine(this, this.height / 2 + this.height
                    / 12);
        }

        if( isPause() )
            drawManager.drawCenteredBigString(this, "PAUSE", this.height / 2);
        drawManager.completeDrawing(this);
    }



    /**
     * Manages collisions between bullets and ships.
     */
    private void manageCollisions() {
        Set<Bullet> recyclable = new HashSet<Bullet>();

        for (Bullet bullet : this.bullets) {
            //몹 발사
            if (bullet.isEnemy()) {
                for (Player p : players) {
                    Ship ship = p.getShip();
                    if (p.isDie()) continue;
                    if (checkCollision(bullet, ship) && !this.levelFinished) {
                        recyclable.add(bullet);
                        if (!ship.isDestroyed()) {
                            ship.destroy();
                            p.subtractlive(1);
                            this.logger.info("Hit on " + p.getName() + " ship, " + p.getLives()
                                    + " lives remaining.");
                        }
                    }
                }
            } else { //플레이어 발사
                for (EnemyShip enemyShip : this.enemyShipFormation) {
                    if (!enemyShip.isDestroyed()
                            && checkCollision(bullet, enemyShip)) {
                        ((Ship) bullet.getShooter()).getPlayer().addScore((int)(enemyShip.getPointValue()*getDifficultScore()));
                        ((Ship) bullet.getShooter()).getPlayer().addshipsDestroyed(1);
                        this.enemyShipFormation.destroy(enemyShip);
                        recyclable.add(bullet);
                    }
                }
                if (this.enemyShipSpecial != null
                        && !this.enemyShipSpecial.isDestroyed()
                        && checkCollision(bullet, this.enemyShipSpecial)) {
                    ((Ship)bullet.getShooter()).getPlayer().addScore((int)(this.enemyShipSpecial.getPointValue()*getDifficultScore()));
                    ((Ship)bullet.getShooter()).getPlayer().addshipsDestroyed(1);
                    this.enemyShipSpecial.destroy();
                    this.enemyShipSpecialExplosionCooldown.reset();
                    recyclable.add(bullet);
                }
            }

        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);

    }

    /**
     * Checks if two entities are colliding.
     *
     * @param a
     *            First entity, the bullet.
     * @param b
     *            Second entity, the ship.
     * @return Result of the collision test.
     */


    /**
     * Returns a GameState object representing the status of the game.
     *
     * @return Current game state.
     */
    public final CustomGameState getGameState() {
        return new CustomGameState(this.level, this.difficult, players, multimethod);
    }

    public boolean allPlayerDie(){
        for(Player p : players){
            if(! p.isDie())
                return false;
        }
        return true;
    }

}