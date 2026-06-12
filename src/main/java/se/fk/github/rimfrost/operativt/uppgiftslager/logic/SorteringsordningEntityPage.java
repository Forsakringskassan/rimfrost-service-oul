package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.util.List;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;

/**
 * A paginated slice of {@link SorteringsordningEntity} items together with the total row count.
 * Returned by the storage layer so the service can apply DTO mapping before building
 * the presentation-layer response.
 *
 * @param total total number of rows (without LIMIT/OFFSET)
 * @param items the page slice
 */
// @formatter:off
public record SorteringsordningEntityPage(int total, List<SorteringsordningEntity> items)
{
   public SorteringsordningEntityPage
   {
      items = List.copyOf(items);
   }
}
// @formatter:on
