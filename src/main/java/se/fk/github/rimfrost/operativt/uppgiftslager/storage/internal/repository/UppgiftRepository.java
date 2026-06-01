package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity;

import java.util.UUID;

@ApplicationScoped
public class UppgiftRepository implements PanacheRepositoryBase<UppgiftEntity, UUID>
{
}
