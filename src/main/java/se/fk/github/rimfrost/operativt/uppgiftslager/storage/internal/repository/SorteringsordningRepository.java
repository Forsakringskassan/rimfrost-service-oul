package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.SorteringsordningPersistenceEntity;
import java.util.UUID;

/**
 * Panache repository for {@link SorteringsordningPersistenceEntity}, keyed by {@link UUID}.
 * All CRUD operations are provided by {@link PanacheRepositoryBase}.
 */
@ApplicationScoped
public class SorteringsordningRepository
      implements PanacheRepositoryBase<SorteringsordningPersistenceEntity, UUID>
{
}
