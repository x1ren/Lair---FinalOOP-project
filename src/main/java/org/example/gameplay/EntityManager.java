package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Generic manager for gameplay objects.
 */
public class EntityManager<T extends GameObject> implements Iterable<T> {

    private final List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<T> view() {
        return Collections.unmodifiableList(items);
    }

    public void removeIf(Predicate<? super T> predicate) {
        items.removeIf(predicate);
    }

    public void renderAll(GraphicsContext gc) {
        for (T item : items) {
            item.render(gc);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }
}
