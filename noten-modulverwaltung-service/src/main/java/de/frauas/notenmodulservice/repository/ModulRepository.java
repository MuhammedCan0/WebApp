package de.frauas.notenmodulservice.repository;

import de.frauas.notenmodulservice.model.Modul;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für den Zugriff auf Modul-Entities
 */
@Repository
public interface ModulRepository extends JpaRepository<Modul, UUID> {

    /**
     * Findet ein Modul anhand der Modul-ID
     */
    Optional<Modul> findByModulId(Integer modulId);

    /**
     * Findet ein Modul anhand der Modul-ID und Tenant-ID
     */
    Optional<Modul> findByModulIdAndTenantId(Integer modulId, UUID tenantId);

    /**
     * Findet Module anhand der Modul-ID in erlaubten Tenants
     */
    List<Modul> findByModulIdAndTenantIdIn(Integer modulId, List<UUID> tenantIds);

    /**
     * Findet alle Module in erlaubten Tenants
     */
    List<Modul> findByTenantIdIn(List<UUID> tenantIds);

    /**
     * Findet alle Module eines Studiengangs
     */
    List<Modul> findByStudiengang(String studiengang);

    /**
     * Findet alle Module eines Studiengangs in erlaubten Tenants
     */
    List<Modul> findByStudiengangAndTenantIdIn(String studiengang, List<UUID> tenantIds);

    /**
     * Findet alle Module eines bestimmten Semesters
     */
    List<Modul> findBySemester(Integer semester);

    /**
     * Findet alle Module eines Studiengangs und Semesters
     */
    List<Modul> findByStudiengangAndSemester(String studiengang, Integer semester);

    /**
     * Prüft, ob ein Modul mit der Modul-ID bereits existiert
     */
    boolean existsByModulId(Integer modulId);

    /**
     * Prüft, ob ein Modul mit der Modul-ID in einem Tenant existiert
     */
    boolean existsByModulIdAndTenantId(Integer modulId, UUID tenantId);

    /**
     * Löscht ein Modul anhand der Modul-ID
     */
    void deleteByModulId(Integer modulId);

    /**
     * Löscht ein Modul anhand der Modul-ID und Tenant-ID
     */
    void deleteByModulIdAndTenantId(Integer modulId, UUID tenantId);
}