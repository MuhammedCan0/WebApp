package com.university.tenant_service.tenant;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TenantRepository {

    private final Map<UUID, Tenant> database = new ConcurrentHashMap<>();

    public Tenant save(Tenant tenant) {
        if (tenant.getId() == null) {
            tenant.setId(UUID.randomUUID());
        }
        database.put(tenant.getId(), tenant);
        return tenant;
    }

    public Optional<Tenant> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    public Optional<Tenant> findByIdentifier(String identifier) {
        return database.values().stream()
                .filter(t -> t.getIdentifier() != null && t.getIdentifier().equals(identifier))
                .findFirst();
    }

    public Optional<Tenant> findByName(String name) {
        return database.values().stream()
                .filter(t -> t.getName() != null && t.getName().equals(name))
                .findFirst();
    }

    public List<Tenant> findAll() {
        return new ArrayList<>(database.values());
    }

    public void deleteById(UUID id) {
        database.remove(id);
    }

    public boolean existsById(UUID id) {
        return database.containsKey(id);
    }
}
