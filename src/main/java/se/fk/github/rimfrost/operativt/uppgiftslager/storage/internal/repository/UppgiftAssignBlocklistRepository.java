package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftAssignBlocklistEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftAssignBlocklistId;

@ApplicationScoped
public class UppgiftAssignBlocklistRepository
      implements PanacheRepositoryBase<UppgiftAssignBlocklistEntity, UppgiftAssignBlocklistId>
{
}
