package fbanna.easyminigame.timer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Timer {

    private Set<TimerEvent> queue;

    public Timer() {
        queue = new HashSet<TimerEvent>();
    }

    public void register(TimerEvent event) {
        this.queue.add(event);
    }

    public void update() {

        Set<TimerEvent> copy = new HashSet<>(this.queue);

        for (Iterator<TimerEvent> iterator = copy.iterator(); iterator.hasNext();) {
            TimerEvent event = iterator.next();

            boolean result = event.update();
            if(result) {
                iterator.remove();
            }

        }
    }

}
