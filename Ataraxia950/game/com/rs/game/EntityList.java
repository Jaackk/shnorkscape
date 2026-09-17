package com.rs.game;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

public class EntityList<T extends Entity> extends AbstractCollection<T> implements Iterable<T> {

    public T[] entities;
    public int lowestFreeIndex;
    private int size;

    @SuppressWarnings("unchecked")
    public EntityList(int capacity, boolean player) {
        entities = (T[]) (player ? new Player[capacity] : new NPC[capacity]);
    }

    public boolean add(T entity) {
        synchronized (this) {
            entity.setIndex(lowestFreeIndex + 1);
            entities[lowestFreeIndex] = entity;
            size++;
            for (int i = lowestFreeIndex + 1; i < entities.length; i++) {
                if (entities[i] == null) {
                    lowestFreeIndex = i;
                    break;
                }
            }
            return true;
        }
    }

    /**
     * Registers {@code entity} at an exact index instead of the lowest free one.
     *
     * <p>The native 947 world hands a player index to the login response before the
     * Player object exists ({@code Native950World.reserve}), so its admission cannot
     * use {@link #add(Entity)}, which assigns {@code lowestFreeIndex + 1} at insert
     * time. Legacy callers are unaffected: nothing but the native admission path
     * calls this method, and {@code lowestFreeIndex} is advanced exactly as
     * {@link #add(Entity)} advances it when the requested slot was the free one.
     *
     * @return false when the index is out of range or already occupied; the entity
     *         is then left unregistered with its previous index.
     */
    public boolean addAt(T entity, int index) {
        synchronized (this) {
            int listIndex = index - 1;
            if (entity == null || listIndex < 0 || listIndex >= entities.length || entities[listIndex] != null)
                return false;
            entity.setIndex(index);
            entities[listIndex] = entity;
            size++;
            if (lowestFreeIndex == listIndex) {
                for (int i = listIndex + 1; i < entities.length; i++) {
                    if (entities[i] == null) {
                        lowestFreeIndex = i;
                        break;
                    }
                }
            }
            return true;
        }
    }

    public void remove(T entity) {
        synchronized (this) {
            int listIndex = entity.getIndex() - 1;
            entities[listIndex] = null;
            size--;
            if (listIndex < lowestFreeIndex)
                lowestFreeIndex = listIndex;
        }
    }

    public T get(int index) {
        if (index >= entities.length || index == 0)
            return null;
        return entities[index - 1];
    }

    public boolean contains(T entity) {
        return entity.getIndex() != 0 && entities[entity.getIndex() - 1] == entity;
    }

    public int indexOf(T entity) {
        for (int i = 0; i < entities.length; i++) {
            if (entities[i] != null && entities[i].equals(entity))
                return i;
        }
        return -1;
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new EntityIterator();
    }

    private final class EntityIterator implements Iterator<T> {

        /**
         * The previous index of this iterator.
         */
        private int previousIndex = -1;

        /**
         * The current index of this iterator.
         */
        private int index = 0;

        @Override
        public boolean hasNext() {
            for (int i = index; i < entities.length; i++) {
                if (entities[i] != null) {
                    index = i;
                    return true;
                }
            }
            return false;
        }

        @Override
        public T next() {
            T entity = null;
            for (int i = index; i < entities.length; i++) {
                if (entities[i] != null) {
                    entity = entities[i];
                    index = i;
                    break;
                }
            }
            if (entity == null)
                throw new NoSuchElementException();
            previousIndex = index;
            index++;
            return entity;
        }

        @Override
        public void remove() {
            if (previousIndex == -1) {
                throw new IllegalStateException();
            }
            EntityList.this.remove(entities[previousIndex]);
            previousIndex = -1;
        }

    }
}