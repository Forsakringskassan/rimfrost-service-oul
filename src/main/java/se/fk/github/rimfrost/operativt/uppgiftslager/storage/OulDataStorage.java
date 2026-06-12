package se.fk.github.rimfrost.operativt.uppgiftslager.storage;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.UppgiftEntityPage;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage contract for all persistent operations in the operativt uppgiftslager.
 * Implementations are responsible for uppgift lifecycle as well as sorteringsordning management.
 */
public interface OulDataStorage
{
   /**
    * Persists a new uppgift.
    *
    * @param uppgift the uppgift to create
    */
   void createUppgift(UppgiftEntity uppgift);

   /**
    * Returns one page of uppgifter sorted according to the given sorteringsordning.
    * The total count reflects all rows, not just the page slice.
    *
    * @param sorteringsordning the sort specification; entries define priority groups
    * @param limit  maximum number of items to return
    * @param offset zero-based start index within the full sorted result
    * @return the page slice and total count
    */
   UppgiftEntityPage findUppgifterPage(SorteringsordningEntity sorteringsordning, int limit, int offset);

   /**
    * Returns the uppgift with the given id, or {@code null} if not found.
    *
    * @param id the uppgift UUID
    * @return the matching uppgift, or {@code null}
    */
   UppgiftEntity findUppgiftById(UUID id);

   /**
    * Returns all uppgifter assigned to the given handläggare, ordered by the given sorteringsordning.
    *
    * @param handlaggarId      the handläggare identity
    * @param sorteringsordning the sort specification; empty entries produce unspecified order
    * @return list of uppgifter for the handläggare
    */
   List<UppgiftEntity> findAllUppgifterByHandlaggarId(Idtyp handlaggarId, SorteringsordningEntity sorteringsordning);

   /**
    * Permanently removes the uppgift with the given id.
    *
    * @param id the uppgift UUID to delete
    */
   void deleteUppgift(UUID id);

   /**
    * Atomically claims the highest-priority unassigned uppgift for the given handläggare,
    * using the sorteringsordning to determine priority.
    * Returns {@code null} if no unassigned uppgift is available.
    *
    * @param handlaggarId      the handläggare to assign the uppgift to
    * @param sorteringsordning the sort specification that determines task priority
    * @return the assigned uppgift, or {@code null} if none available
    */
   UppgiftEntity assignNewUppgift(Idtyp handlaggarId, SorteringsordningEntity sorteringsordning);

   /**
    * Removes the handläggare assignment from the given uppgift.
    * Returns {@code null} if the uppgift does not exist.
    *
    * @param id the uppgift UUID to unassign
    * @return the updated uppgift, or {@code null} if not found
    */
   UppgiftEntity unassignUppgift(UUID id);

   /**
    * Updates the handläggare assignment on an existing uppgift.
    * Returns {@code null} if the uppgift does not exist.
    *
    * @param id          the uppgift UUID to update
    * @param handlaggarId the new handläggare identity, or {@code null} to clear
    * @return the updated uppgift, or {@code null} if not found
    */
   UppgiftEntity updateUppgift(UUID id, Idtyp handlaggarId);

   /**
    * Persists a new sorteringsordning and sets it as the default if none exists yet.
    *
    * @param entity the sorteringsordning to save
    */
   void saveSorteringsordning(SorteringsordningEntity entity);

   /**
    * Returns the currently designated default sorteringsordning, if one has been set.
    *
    * @return the default sorteringsordning, or empty if none is configured
    */
   Optional<SorteringsordningEntity> getDefaultSorteringsordning();

   /**
    * Looks up a sorteringsordning by its UUID.
    *
    * @param id the sorteringsordning UUID
    * @return the matching sorteringsordning, or empty if not found
    */
   Optional<SorteringsordningEntity> getSorteringsordningById(UUID id);

   /**
    * Returns all persisted sorteringsordningar.
    *
    * @return list of all sorteringsordningar
    */
   List<SorteringsordningEntity> getAllSorteringsordningar();

   /**
    * Deletes the sorteringsordning with the given id.
    *
    * @param id the sorteringsordning UUID to delete
    * @throws SorteringsordningNotFoundException if no sorteringsordning with the given id exists
    * @throws SorteringsordningIsDefaultException if the sorteringsordning is currently the default
    */
   void deleteSorteringsordning(UUID id);

   /**
    * Sets the sorteringsordning with the given id as the system default.
    *
    * @param id the sorteringsordning UUID to promote to default
    * @throws SorteringsordningNotFoundException if no sorteringsordning with the given id exists
    */
   void setDefaultSorteringsordning(UUID id);
}
