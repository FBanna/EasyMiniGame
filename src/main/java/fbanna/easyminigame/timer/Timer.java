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

        if(queue.size() != 0) {

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

    public void clear() {

        queue = new HashSet<>();

    }

}
