package no.ntnu.trostespel;


import no.ntnu.trostespel.game.MasterGameState;
import no.ntnu.trostespel.game.PlayerUpdateProcessor;
import no.ntnu.trostespel.state.GameState;
import no.ntnu.trostespel.state.PlayerState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * This class is responsible for queuing and demultiplexing incoming
 * updates, and dispathching them for processing.
 */
public class PlayerUpdateDispatcher extends ThreadPoolExecutor {

    private MasterGameState masterGameState;

    private Map<Long, Long> workers;

    public PlayerUpdateDispatcher() {
        super(1, 8, 0, TimeUnit.HOURS, new LinkedBlockingQueue<>(8));
        masterGameState = MasterGameState.getInstance();
        this.workers = new HashMap<>();
    }

    /**
     * dispatch actions for processing and update
     * masterGameState
     *
     * @param actions the update to queue
     */
    public void dispatch(PlayerActions actions) {
        long currentTick = GameServer.getTickcounter();
        long pid = actions.pid;
        if (!workers.containsKey(pid)) {
            workers.put(actions.pid, currentTick);

        }

        if (workers.get(pid) < currentTick) {
            executeCMD(actions, currentTick);
            updateMaster(actions.pid);
        } else {
            System.out.println("OOPS! TOO FAST " + actions.pid);
            System.out.println(workers.toString());
        }
    }


    private void executeCMD(PlayerActions actions, long startTime) {
        PlayerState playerState = (PlayerState) masterGameState.getGameState().players.get(actions.pid);
        if (playerState == null) {
            playerState = new PlayerState(actions.pid);
            masterGameState.getGameState().players.put(actions.pid, playerState);
        }
        PlayerUpdateProcessor processor = new PlayerUpdateProcessor(playerState, actions, startTime);
        execute(processor);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        this.remove(r)
        workers.put(((PlayerUpdateProcessor) r).getPid(), ((PlayerUpdateProcessor) r).getStartTime());
        //remove(r);
    }

    private void updateMaster(long pid) {
        masterGameState.update(pid);
    }
}
