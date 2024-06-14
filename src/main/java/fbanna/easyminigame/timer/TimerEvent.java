package fbanna.easyminigame.timer;

import org.jetbrains.annotations.NotNull;

public class TimerEvent implements Comparable<TimerEvent>{
    private int ticks;
    private final Call callBack;

    public TimerEvent(int ticks, Call callBack) {
        this.ticks = ticks;
        this.callBack = callBack;
    }

    @Override
    public int compareTo(@NotNull TimerEvent otherEvent) {
        return this.ticks - otherEvent.ticks;
    }

    public boolean update() {
        ticks--;

        if( ticks == 0 ) {
            callBack.call();
            return true;
        }
        return false;
    }
}
