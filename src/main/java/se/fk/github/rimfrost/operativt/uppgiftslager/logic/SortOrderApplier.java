package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SortBy;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningField;

@ApplicationScoped
public class SortOrderApplier
{
   private final ConstraintMatcher constraintMatcher;

   @Inject
   public SortOrderApplier(ConstraintMatcher constraintMatcher)
   {
      this.constraintMatcher = constraintMatcher;
   }

   public SortedUppgiftPage apply(List<UppgiftDto> uppgifter, SorteringsordningEntity sorteringsordning,
         int limit, int offset)
   {
      SequencedSet<UppgiftDto> remaining = new LinkedHashSet<>(uppgifter);
      List<UppgiftDto> ordered = new ArrayList<>();

      for (var entry : sorteringsordning.entries())
      {
         var matched = remaining.stream()
               .filter(u -> constraintMatcher.matches(u, entry.getConstraints()))
               .sorted(comparatorFor(entry.getSortBy()))
               .toList();

         matched.forEach(remaining::remove);
         ordered.addAll(matched);
      }

      ordered.addAll(remaining);

      int total = ordered.size();
      var items = ordered.stream().skip(offset).limit(limit).toList();
      return new SortedUppgiftPage(total, items);
   }

   @SuppressWarnings(
   {
         "unchecked", "rawtypes"
   })
   private Comparator<UppgiftDto> comparatorFor(SortBy sortBy)
   {
      if (sortBy == null)
      {
         return Comparator.comparingInt(u -> 0);
      }

      Comparator<UppgiftDto> comparator = Comparator.comparing(
            u -> (Comparable) resolveField(u, sortBy.getField()),
            Comparator.nullsLast(Comparator.naturalOrder()));

      return sortBy.getDirection() == SortBy.DirectionEnum.DESC ? comparator.reversed() : comparator;
   }

   private Object resolveField(UppgiftDto uppgift, SorteringsordningField field)
   {
      return switch (field)
      {
         case UPPGIFT_ID -> uppgift.uppgiftId().toString();
         case SKAPAD -> uppgift.skapad();
         case PLANERAD_TILL -> uppgift.planeradTill();
         case STATUS -> uppgift.status().name();
         case REGEL -> uppgift.regel();
         case ROLL -> uppgift.roll();
         case VERKSAMHETSLOGIK -> uppgift.verksamhetslogik();
         case BESKRIVNING -> uppgift.beskrivning();
      };
   }
}
