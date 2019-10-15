package no.ntnu.trostespel;


import com.badlogic.gdx.Game;
import no.ntnu.trostespel.entity.GameObject;
import no.ntnu.trostespel.game.MasterGameState;
import no.ntnu.trostespel.game.PlayerUpdateProcessor;
import no.ntnu.trostespel.state.MovableState;
import no.ntnu.trostespel.state.PlayerState;

import java.util.concurrent.*;

/**
 * This class is responsible for queuing and demultiplexing incoming
 * updates, and dispathching them for processing.
 */
public class PlayerUpdateDispatcher {

    private ExecutorService processors;
    private long startTime = 0;
    private MasterGameState masterGameState;


    public PlayerUpdateDispatcher() {
        this.processors = Executors.newCachedThreadPool();
        GameState gameState = new GameState();
        masterGameState = new MasterGameState(gameState);
    }

    /**
     * dispatch actions for processing and update
     * masterGameState
     * @param actions the update to queue
     */
    public void dispatch(PlayerActions actions) {
        Future f = processCMD(actions);
        updateMaster(f);
    }

    private Future<PlayerState> processCMD(PlayerActions actions) {
        Future<PlayerState> f = null;

        if (actions != null) {
            startTime = System.currentTimeMillis();
            f = processors.submit(new PlayerUpdateProcessor(
                    (PlayerState) masterGameState.getGameState().players.get(actions.pid),
                    actions,
                    startTime));
        }
        return f;
    }

    private void updateMaster(Future<PlayerState> f) {
        if (f != null) {
            try {
                PlayerState change = (PlayerState) f.get();
                long pid = change.getPid();
                masterGameState.update(pid, change);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
