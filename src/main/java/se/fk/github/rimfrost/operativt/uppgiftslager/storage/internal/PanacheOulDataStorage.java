package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.SorteringsordningEntityPage;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.UppgiftEntityPage;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

   @Inject
   SorteringsordningQueryBuilder queryBuilder;

   @ConfigProperty(name = "oul.uppgift.count-cache-ttl-ms", defaultValue = "5000")
   long countCacheTtlMs;

   private volatile long cachedTotal = -1;
   private volatile long cacheTimestamp = 0L;

   @Override
   public void createUppgift(UppgiftEntity uppgift)
   {
      var entity = oulDataStorageMapper.toUppgiftEntity(uppgift);
      uppgiftRepository.persist(entity);
      cachedTotal = -1L;
   }

   /**
    * {@inheritDoc}
    * <p>
    * Executes one sorted native SQL query (ORDER BY sort_group + per-entry sort_by + created_at) and
    * one {@code COUNT(*)} query. Pagination is applied via {@code setFirstResult}/{@code setMaxResults}.
    */
   @Override
   public UppgiftEntityPage findUppgifterPage(SorteringsordningEntity sorteringsordning, int limit, int offset)
   {
      var built = queryBuilder.build(sorteringsordning);
      var em = uppgiftRepository.getEntityManager();

      var pageQuery = em.createNativeQuery(
            built.pageSql(),
            se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity.class);
      built.params().forEach(pageQuery::setParameter);
      pageQuery.setFirstResult(offset);
      pageQuery.setMaxResults(limit);

      // FQN is required: logic UppgiftEntity is already imported under the same simple name
      @SuppressWarnings("unchecked")
      List<se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity> resultList = pageQuery
            .getResultList();
      var items = resultList.stream().map(oulDataStorageMapper::toUppgiftEntity).toList();

      var total = fetchTotal(em, built.countSql());

      return new UppgiftEntityPage((int) total, items);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Executes a native SQL query ordered by the sorteringsordning sort groups and per-entry
    * sort_by fields. Falls back to {@code created_at ASC} when no entries are configured.
    */
   @Override
   public List<UppgiftEntity> findAllUppgifterByHandlaggarId(Idtyp handlaggarId, SorteringsordningEntity sorteringsordning)
   {
      var built = queryBuilder.buildHandlaggareListQuery(sorteringsordning, handlaggarId.typId(), handlaggarId.varde());
      var em = uppgiftRepository.getEntityManager();
      var query = em.createNativeQuery(built.pageSql(),
            se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity.class);
      built.params().forEach(query::setParameter);
      @SuppressWarnings("unchecked")
      List<se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity> results = query
            .getResultList();
      return results.stream().map(oulDataStorageMapper::toUppgiftEntity).toList();
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
      cachedTotal = -1L;
   }

   /**
    * {@inheritDoc}
    * <p>
    * Uses a two-phase native SQL approach: an inner subquery computes priority order without
    * locking; the outer query re-selects the chosen row by id and applies
    * {@code FOR UPDATE SKIP LOCKED}. If the row was concurrently claimed the outer SELECT
    * returns empty and the method returns {@code null}.
    */
   @Override
   public UppgiftEntity assignNewUppgift(Idtyp handlaggarId, SorteringsordningEntity sorteringsordning)
   {
      var built = queryBuilder.buildAssignQuery(sorteringsordning);
      var em = uppgiftRepository.getEntityManager();
      var selectQuery = em.createNativeQuery(built.pageSql(),
            se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity.class);
      built.params().forEach(selectQuery::setParameter);

      @SuppressWarnings("unchecked")
      List<se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity> results = selectQuery
            .getResultList();

      if (results.isEmpty())
      {
         return null;
      }

      var uppgift = results.getFirst();
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
            .map(oulDataStorageMapper::toSorteringsordningEntity);
   }

   @Override
   public Optional<SorteringsordningEntity> getSorteringsordningById(UUID id)
   {
      return sorteringsordningRepository.findByIdOptional(id)
            .map(oulDataStorageMapper::toSorteringsordningEntity);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Executes one {@code COUNT} query and one range query ordered by {@code createdAt} descending.
    */
   @Override
   public SorteringsordningEntityPage findSorteringsordningarPage(int limit, int offset)
   {
      var query = sorteringsordningRepository.findAll(Sort.by("createdAt", Sort.Direction.Descending));
      int total = (int) query.count();
      var items = query.range(offset, offset + limit - 1)
            .stream()
            .map(oulDataStorageMapper::toSorteringsordningEntity)
            .toList();
      return new SorteringsordningEntityPage(total, items);
   }

   @Override
   public void deleteSorteringsordning(UUID id)
   {
      if (sorteringsordningRepository.findByIdOptional(id).isEmpty())
      {
         throw new SorteringsordningNotFoundException(id);
      }
      defaultSorteringsordningRepository.findForUpdate().ifPresent(d -> {
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
    * Resets the in-process count cache so that the next read triggers a fresh {@code COUNT(*)}.
    * Package-private: only {@link se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.StorageTestCleaner}
    * should call this.
    */
   void invalidateCountCache()
   {
      cachedTotal = -1L;
   }

   /**
    * Returns the total number of uppgifter, using a short-lived in-process cache to avoid
    * a full table scan on every paginated request.
    * <p>
    * The cached value is considered fresh for {@code oul.uppgift.count-cache-ttl-ms} milliseconds
    * (default 5 000 ms). At most two concurrent threads can miss the cache simultaneously and
    * both run COUNT(*); this is intentional and harmless given the TTL semantics.
    *
    * @param em       the entity manager to use for the query if the cache is cold
    * @param countSql the COUNT(*) SQL to execute on a cache miss
    * @return the total row count, possibly from cache
    */
   private long fetchTotal(jakarta.persistence.EntityManager em, String countSql)
   {
      long now = System.currentTimeMillis();
      if (cachedTotal >= 0 && now - cacheTimestamp < countCacheTtlMs)
      {
         return cachedTotal;
      }
      long total = ((Number) em.createNativeQuery(countSql).getSingleResult()).longValue();
      cacheTimestamp = now;
      cachedTotal = total;
      return total;
   }

}
