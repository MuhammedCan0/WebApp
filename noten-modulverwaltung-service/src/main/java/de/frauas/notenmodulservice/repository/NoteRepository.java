package de.frauas.notenmodulservice.repository;

import de.frauas.notenmodulservice.model.Note;
import de.frauas.notenmodulservice.model.NotenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository für den Zugriff auf Note-Entities
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    /**
     * Findet alle Noten eines Studierenden anhand der Matrikelnummer
     */
    List<Note> findByMatrikelnummer(String matrikelnummer);

    /**
     * Findet alle PUBLISHED Noten eines Studierenden über mehrere Tenants
     */
    @Query("SELECT n FROM Note n WHERE n.matrikelnummer = :matrikelnummer " +
           "AND n.tenantId IN :tenantIds " +
           "AND n.status = 'PUBLISHED'")
    List<Note> findPublishedNotenByMatrikelnummerAndTenants(
            @Param("matrikelnummer") String matrikelnummer,
            @Param("tenantIds") List<UUID> tenantIds);

    /**
     * Prüfungsamt-Suche: Alle Noten mit Status TO_VALIDATE über Tenants mit optionalen Filtern
     */
    @Query("SELECT n FROM Note n WHERE n.tenantId IN :tenantIds " +
            "AND n.status = :status " +
            "AND (:modulId IS NULL OR n.modulId = :modulId) " +
            "AND (:matrikelnummer IS NULL OR n.matrikelnummer = :matrikelnummer) " +
            "AND (:lehrendenMatrikelnummer IS NULL OR n.lehrendenMatrikelnummer = :lehrendenMatrikelnummer) " +
            "AND (:studiengang IS NULL OR n.studiengang = :studiengang) " +
            "AND (:semester IS NULL OR n.semester = :semester)")
    List<Note> findToValidateByTenantsWithFilters(
            @Param("tenantIds") List<UUID> tenantIds,
            @Param("status") NotenStatus status,
            @Param("modulId") Integer modulId,
            @Param("matrikelnummer") String matrikelnummer,
             @Param("lehrendenMatrikelnummer") String lehrendenMatrikelnummer,
             @Param("studiengang") String studiengang,
             @Param("semester") String semester);

    /**
     * Findet alle Noten nach Status
     */
    List<Note> findByStatus(NotenStatus status);

    /**
     * Findet alle Noten eines Lehrenden anhand der Matrikelnummer
     */
    List<Note> findByLehrendenMatrikelnummer(String lehrendenMatrikelnummer);

    /**
     * Findet alle Noten eines Lehrenden für ein bestimmtes Modul und Status
     */
    List<Note> findByLehrendenMatrikelnummerAndModulIdAndStatus(
            String lehrendenMatrikelnummer, Integer modulId, NotenStatus status);

    /**
     * Findet alle Noten eines Lehrenden mit einem bestimmten Status
     */
    List<Note> findByLehrendenMatrikelnummerAndStatus(
            String lehrendenMatrikelnummer, NotenStatus status);

    /**
     * Flexible Suche nach Noten eines Lehrenden mit optionalen Filtern
     * Unterstützt Suche über mehrere Tenants
     */
    @Query("SELECT n FROM Note n WHERE n.lehrendenMatrikelnummer = :lehrendenMatrikelnummer " +
           "AND n.tenantId IN :tenantIds " +
           "AND n.status = :status " +
           "AND (:modulId IS NULL OR n.modulId = :modulId) " +
           "AND (:matrikelnummer IS NULL OR n.matrikelnummer = :matrikelnummer)")
    List<Note> findByLehrenderWithFilters(
            @Param("lehrendenMatrikelnummer") String lehrendenMatrikelnummer,
            @Param("tenantIds") List<UUID> tenantIds,
            @Param("status") NotenStatus status,
            @Param("modulId") Integer modulId,
            @Param("matrikelnummer") String matrikelnummer);

    /**
     * Findet alle Noten für ein bestimmtes Modul
     */
    List<Note> findByModulId(Integer modulId);

    /**
     * Findet alle Noten eines Studierenden mit einem bestimmten Status
     */
    List<Note> findByMatrikelnummerAndStatus(String matrikelnummer, NotenStatus status);

    /**
     * Findet alle Noten eines Studiengangs
     */
    List<Note> findByStudiengang(String studiengang);

    /**
     * Prüft, ob bereits eine Note für einen Studierenden und ein Modul existiert
     */
        boolean existsByMatrikelnummerAndModulIdAndTenantId(String matrikelnummer, Integer modulId, UUID tenantId);
}