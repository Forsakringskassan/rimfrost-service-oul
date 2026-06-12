package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.util.List;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;

/**
 * A paginated slice of {@link UppgiftEntity} items together with the total row count.
 * Returned by the storage layer so the service can apply DTO mapping before building
 * the presentation-layer {@link SortedUppgiftPage}.
 *
 * @param total total number of rows matching the query (without LIMIT/OFFSET)
 * @param items the page slice
 */
// @formatter:off
public record UppgiftEntityPage(int total, List<UppgiftEntity> items)
{
   public UppgiftEntityPage
   {
      items = List.copyOf(items);
   }
}
// @formatter:on
