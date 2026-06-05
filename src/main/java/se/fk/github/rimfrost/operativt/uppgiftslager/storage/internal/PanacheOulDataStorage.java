package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.OulDataStorage;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository.UppgiftRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class PanacheOulDataStorage implements OulDataStorage
{
   @Inject
   UppgiftRepository uppgiftRepository;

   @Inject
   OulDataStorageMapper oulDataStorageMapper;

   @Override
   public void createUppgift(UppgiftEntity uppgift)
   {
      var entity = oulDataStorageMapper.toUppgiftEntity(uppgift);
      uppgiftRepository.persist(entity);
   }

   @Override
   public List<UppgiftEntity> findAllUppgifter()
   {
      return uppgiftRepository.findAll().stream().map(oulDataStorageMapper::toUppgiftEntity).toList();
   }

   @Override
   public List<UppgiftEntity> findAllUppgifterByHandlaggarId(Idtyp handlaggarId)
   {
      return uppgiftRepository
            .find("handlaggarIdTypId = :typ_id AND handlaggarIdVarde = :varde",
                  Map.of("typ_id", handlaggarId.typId(), "varde", handlaggarId.varde()))
            .stream().map(oulDataStorageMapper::toUppgiftEntity).toList();
   }

   @Override
   public UppgiftEntity findUppgiftById(UUID id)
   {
      var entity = uppgiftRepository.findById(id);

      if (entity == null)
      {
         return null;
      }

      return oulDataStorageMapper.toUppgiftEntity(entity);
   }

   @Override
   public void deleteUppgift(UUID id)
   {
      uppgiftRepository.deleteById(id);
   }

   @Override
   public UppgiftEntity assignNewUppgift(Idtyp handlaggarId)
   {
      var uppgift = uppgiftRepository.find("handlaggarIdTypId IS NULL and handlaggarIdVarde IS NULL")
            .withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();

      if (uppgift == null)
      {
         return null;
      }

      uppgift.setStatus(UppgiftStatus.TILLDELAD);
      uppgift.setHandlaggarIdTypId(handlaggarId.typId());
      uppgift.setHandlaggarIdVarde(handlaggarId.varde());
      uppgiftRepository.persist(uppgift);

      return oulDataStorageMapper.toUppgiftEntity(uppgift);
   }

   @Override
   public UppgiftEntity unassignUppgift(UUID id)
   {
      var uppgift = uppgiftRepository.findById(id);

      if (uppgift == null)
      {
         return null;
      }

      var status = uppgift.getStatus();

      if (status == UppgiftStatus.TILLDELAD)
      {
         status = UppgiftStatus.NY;
      }

      uppgift.setHandlaggarIdTypId(null);
      uppgift.setHandlaggarIdVarde(null);
      uppgift.setStatus(status);
      uppgiftRepository.persist(uppgift);

      return oulDataStorageMapper.toUppgiftEntity(uppgift);
   }

   @Override
   public UppgiftEntity updateUppgift(UUID id, Idtyp handlaggarId)
   {
      var uppgift = uppgiftRepository.findById(id);

      if (uppgift == null)
      {
         return null;
      }

      if (handlaggarId != null)
      {
         uppgift.setStatus(UppgiftStatus.TILLDELAD);
         uppgift.setHandlaggarIdTypId(handlaggarId.typId());
         uppgift.setHandlaggarIdVarde(handlaggarId.varde());
      }

      uppgiftRepository.persist(uppgift);

      return oulDataStorageMapper.toUppgiftEntity(uppgift);
   }
}
