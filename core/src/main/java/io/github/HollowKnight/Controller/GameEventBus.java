package io.github.HollowKnight.Controller;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameEventBus {
    public interface Listener<T> {
        void onEvent(T event);
    }

    private final Map<GameEvent, List<Runnable>> listeners = new EnumMap<>(GameEvent.class);
    private final Map<GameEvent, List<Consumer<Object>>> payloadListeners = new EnumMap<>(GameEvent.class);

    public void subscribe(GameEvent event, Runnable listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    public <T> void subscribe(GameEvent event, Listener<T> listener) {
        payloadListeners.computeIfAbsent(event, k -> new ArrayList<>())
            .add(payload -> listener.onEvent((T) payload));
    }

    public void post(GameEvent event) {
        post(event, null);
    }

    public void post(GameEvent event, Object payload) {
        List<Runnable> list = listeners.get(event);
        if (list != null) {
            for (Runnable r : list) {
                r.run();
            }
        }
        if (payload != null) {
            List<Consumer<Object>> payloadList = payloadListeners.get(event);
            if (payloadList != null) {
                for (Consumer<Object> consumer : payloadList) {
                    consumer.accept(payload);
                }
            }
        }
    }

    public void clear() {
        listeners.clear();
        payloadListeners.clear();
    }
}
