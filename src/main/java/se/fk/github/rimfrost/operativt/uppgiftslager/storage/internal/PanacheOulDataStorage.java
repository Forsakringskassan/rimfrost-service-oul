package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.OulDataStorage;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.DefaultSorteringsordningEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.SorteringsordningPersistenceEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.SorteringsordningIsDefaultException;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.SorteringsordningNotFoundException;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository.DefaultSorteringsordningRepository;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository.SorteringsordningRepository;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository.UppgiftRepository;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA/Panache implementation of {@link OulDataStorage}.
 * All public methods participate in the caller's transaction via the class-level
 * {@link Transactional} annotation.
 */
@ApplicationScoped
@Transactional
public class PanacheOulDataStorage implements OulDataStorage
{
   @Inject
   UppgiftRepository uppgiftRepository;

   @Inject
   OulDataStorageMapper oulDataStorageMapper;

   @Inject
   SorteringsordningRepository sorteringsordningRepository;

   @Inject
   DefaultSorteringsordningRepository defaultSorteringsordningRepository;

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

   @Override
   public void saveSorteringsordning(SorteringsordningEntity entity)
   {
      var jpaEntity = new SorteringsordningPersistenceEntity();
      jpaEntity.setId(entity.id());
      jpaEntity.setCreatedAt(entity.skapad().toInstant());
      jpaEntity.setEntries(entity.entries());
      sorteringsordningRepository.persist(jpaEntity);

      defaultSorteringsordningRepository.insertIfAbsent(entity.id());
   }

   @Override
   public Optional<SorteringsordningEntity> getDefaultSorteringsordning()
   {
      return defaultSorteringsordningRepository.findByIdOptional(true)
            .flatMap(d -> sorteringsordningRepository.findByIdOptional(d.getSorteringsordningId()))
            .map(this::toSorteringsordningEntity);
   }

   @Override
   public Optional<SorteringsordningEntity> getSorteringsordningById(UUID id)
   {
      return sorteringsordningRepository.findByIdOptional(id)
            .map(this::toSorteringsordningEntity);
   }

   @Override
   public List<SorteringsordningEntity> getAllSorteringsordningar()
   {
      return sorteringsordningRepository.findAll().stream()
            .map(this::toSorteringsordningEntity)
            .toList();
   }

   /**
    * {@inheritDoc}
    *
    */
   @Override
   public void deleteSorteringsordning(UUID id)
   {
      if (sorteringsordningRepository.findByIdOptional(id).isEmpty())
      {
         throw new SorteringsordningNotFoundException(id);
      }
      defaultSorteringsordningRepository.findByIdOptional(true).ifPresent(d -> {
         if (id.equals(d.getSorteringsordningId()))
         {
            throw new SorteringsordningIsDefaultException(id);
         }
      });
      sorteringsordningRepository.deleteById(id);
   }

   /**
    * {@inheritDoc}
    * <p>
    * If a default row already exists it is updated via Hibernate dirty checking (no explicit
    * {@code persist} needed within the active transaction). Otherwise a new row is inserted.
    *
    */
   @Override
   public void setDefaultSorteringsordning(UUID id)
   {
      if (sorteringsordningRepository.findByIdOptional(id).isEmpty())
      {
         throw new SorteringsordningNotFoundException(id);
      }
      var existing = defaultSorteringsordningRepository.findByIdOptional(true);
      if (existing.isPresent())
      {
         existing.get().setSorteringsordningId(id);
      }
      else
      {
         var defaultEntity = new DefaultSorteringsordningEntity();
         defaultEntity.setSorteringsordningId(id);
         defaultSorteringsordningRepository.persist(defaultEntity);
      }
   }

   /**
    * Removes all sorteringsordningar and the default pointer.
    * Intended for use in tests only.
    */
   public void clearSorteringsordning()
   {
      defaultSorteringsordningRepository.deleteAll();
      sorteringsordningRepository.deleteAll();
   }

   /**
    * Converts a JPA persistence entity to the domain entity used by the logic layer.
    *
    * @param e the JPA entity to convert
    * @return the corresponding domain {@link SorteringsordningEntity}
    */
   private SorteringsordningEntity toSorteringsordningEntity(SorteringsordningPersistenceEntity e)
   {
      return new SorteringsordningEntity(e.getId(), e.getCreatedAt().atOffset(ZoneOffset.UTC), e.getEntries());
   }
}
