package no.ntnu.trostespel.udpServer;

import com.badlogic.gdx.math.Vector2;
import no.ntnu.trostespel.PlayerActions;
import no.ntnu.trostespel.config.CommunicationConfig;
import no.ntnu.trostespel.config.GameRules;
import no.ntnu.trostespel.model.Connection;
import no.ntnu.trostespel.model.Connections;
import no.ntnu.trostespel.state.MovableState;
import no.ntnu.trostespel.state.PlayerState;

import java.util.EnumSet;

public class PlayerCmdProcessor {

    private PlayerActions actions;
    private long startTime;
    private long delta;
    private long pid;
    private Vector2 displacement;
    private PlayerState playerState;
    private float playerAngle;

    private int count = 0;

    private short shouldflipCounter = 0;


    private enum Direction {
        // Angle is relative to x-axis, counterclockwise
        UP(90),
        RIGHT(0),
        DOWN(270),
        LEFT(180);
        private int dir;

        Direction(int i) {
            this.dir = i;
        }

        public int value() {
            return this.dir;
        }
    }

    /**
     * @param playerState the playerstate object that will be updated
     * @param actions     the actions to process
     */
    public PlayerCmdProcessor(PlayerState playerState, PlayerActions actions) {
        this.actions = actions;
        this.playerState = playerState;
        this.displacement = new Vector2(0, 0);
        this.pid = actions.pid;
    }

    public void run() {
        pid = actions.pid;
        if (!playerState.isDead()) {
            addUsername(actions);
            processActionButtons(actions);
            processMovement(actions);
            processAttack(actions);
        }
    }

    private void addUsername(PlayerActions actions) {
        String username = "";
        for (Connection connection : Connections.getInstance().getConnections()) {
            if (connection.getPid() == actions.pid) {
                username = connection.getUsername();
                break;
            }
        }
        playerState.setUsername(username);
    }

    private void processAttack(PlayerActions action) {
        if (playerState.getAttackTimer() <= 0) {
            MovableState projectile = new MovableState(action.pid, GameRules.Projectile.SPEED);
            EnumSet<Direction> attackDir = EnumSet.noneOf(Direction.class);

            if (action.isattackDown) {
                attackDir.add(Direction.DOWN);
            }
            if (action.isattackUp) {
                attackDir.add(Direction.UP);
            }
            if (action.isattackLeft) {
                attackDir.add(Direction.LEFT);
            }
            if (action.isattackRight) {
                attackDir.add(Direction.RIGHT);
            }

            // calculate angle of the bullet
            float direction = 0;
            if (attackDir.size() <= 2) {
                for (Direction dir : attackDir) {
                    direction += dir.value();
                    if (dir == Direction.RIGHT || dir == Direction.DOWN) {
                        // fix edge case
                        shouldflipCounter++;
                    }
                }
                direction = direction / attackDir.size();
                if(shouldflipCounter == 2) {
                    // fix edge case
                    direction += 180;
                }
                projectile.setAngle(direction);
            } else {
                projectile.setAngle(playerAngle);
            }
            // check if player and bullet is moving in the same direction
            if (!attackDir.isEmpty()) {
                double playerbulletangle = Math.abs(playerAngle - direction);
                if (playerbulletangle <= 90 || playerbulletangle >= 270) {
                    // apply players velocity to bullet
                    Vector2 heading = projectile.getHeading();
                    heading.add(displacement);
                    projectile.setHeading(heading);
                }
            }
            // add resulting projectile to spawned objects list
            if (!attackDir.isEmpty()) {
                playerState.getSpawnedObjects().add(projectile);
                // allow attacks every 0.3 seconds
                playerState.setAttackTimer(.3 * CommunicationConfig.TICKRATE + 1);
            }
        }
        double attackTimer = playerState.getAttackTimer();
        playerState.setAttackTimer(attackTimer - 1);
    }

    private void processActionButtons(PlayerActions action) {
        if (action.isaction1) {

        }
        if (action.isaction2) {

        }
        if (action.isaction3) {
        }
    }

    private void processMovement(PlayerActions action) {
        if (action.isleft) {
            displacement.x += -GameRules.Player.SPEED;
        }
        if (action.isright) {
            displacement.x += GameRules.Player.SPEED;
        }

        if (action.isup) {
            displacement.y += GameRules.Player.SPEED;
        }
        if (action.isdown) {
            displacement.y += -GameRules.Player.SPEED;
        }
        if (!displacement.isZero()) {
            Vector2 pos = playerState.getPosition();
            pos.x += displacement.x;
            pos.y += displacement.y;
            playerState.setPosition(pos);
        }
        playerAngle = displacement.angle();
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getPid() {
        return this.pid;
    }
}
