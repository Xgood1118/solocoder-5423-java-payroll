package com.hrpayroll.common;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class InMemoryRepository<T extends BaseEntity> {

    protected final Map<String, T> storage = new ConcurrentHashMap<>();

    public T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(PayrollUtils.generateId());
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(java.time.LocalDateTime.now());
        }
        entity.setUpdateTime(java.time.LocalDateTime.now());
        storage.put(entity.getId(), entity);
        return entity;
    }

    public Optional<T> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean existsById(String id) {
        return storage.containsKey(id);
    }

    public void deleteById(String id) {
        storage.remove(id);
    }

    public List<T> findAll(int page, int size) {
        List<T> all = new ArrayList<>(storage.values());
        int fromIndex = Math.min(page * size, all.size());
        int toIndex = Math.min(fromIndex + size, all.size());
        return all.subList(fromIndex, toIndex);
    }

    public long count() {
        return storage.size();
    }
}
