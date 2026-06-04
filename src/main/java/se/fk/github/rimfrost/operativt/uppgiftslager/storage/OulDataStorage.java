package se.fk.github.rimfrost.operativt.uppgiftslager.storage;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OulDataStorage
{
   void createUppgift(UppgiftEntity uppgift);

   List<UppgiftEntity> findAllUppgifter();

   UppgiftEntity findUppgiftById(UUID id);

   List<UppgiftEntity> findAllUppgifterByHandlaggarId(Idtyp handlaggarId);

   void deleteUppgift(UUID id);

   UppgiftEntity assignNewUppgift(Idtyp handlaggarId);

   UppgiftEntity unassignUppgift(UUID id);

   UppgiftEntity updateUppgift(UUID id, Idtyp handlaggarId);

    void saveSorteringsordning(SorteringsordningEntity entity);

    Optional<SorteringsordningEntity> getDefaultSorteringsordning();

    Optional<SorteringsordningEntity> getSorteringsordningById(UUID id);

    List<SorteringsordningEntity> getAllSorteringsordningar();
}
