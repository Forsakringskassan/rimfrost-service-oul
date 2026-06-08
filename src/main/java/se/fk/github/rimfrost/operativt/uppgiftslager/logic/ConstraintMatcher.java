package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintBetween;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintContains;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintOffsetToNow;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldDate;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldString;

@ApplicationScoped
public class ConstraintMatcher
{
   public boolean matches(UppgiftDto uppgift, List<?> constraints)
   {
      if (constraints == null || constraints.isEmpty())
      {
         return true;
      }
      return constraints.stream().allMatch(c -> matchesSingle(uppgift, c));
   }

   private boolean matchesSingle(UppgiftDto uppgift, Object constraint)
   {
      return switch(constraint){case ConstraintEq eq->matchesEq(uppgift,eq);case ConstraintContains contains->matchesContains(uppgift,contains);case ConstraintBetween between->matchesBetween(uppgift,between);case ConstraintOffsetToNow offsetToNow->matchesOffsetToNow(uppgift,offsetToNow);default->throw new IllegalArgumentException("Unknown constraint type: "+constraint.getClass());};
   }

   private boolean matchesEq(UppgiftDto uppgift, ConstraintEq constraint)
   {
      var actual = resolveStringField(uppgift, constraint.getField());
      return actual != null && actual.equals(constraint.getValue());
   }

   private boolean matchesContains(UppgiftDto uppgift, ConstraintContains constraint)
   {
      var actual = resolveStringField(uppgift, constraint.getField());
      return actual != null && actual.contains(constraint.getValue());
   }

   private boolean matchesBetween(UppgiftDto uppgift, ConstraintBetween constraint)
   {
      var date = resolveDateField(uppgift, constraint.getField());
      if (date == null)
      {
         return false;
      }
      return !date.isBefore(constraint.getFrom()) && !date.isAfter(constraint.getTo());
   }

   private boolean matchesOffsetToNow(UppgiftDto uppgift, ConstraintOffsetToNow constraint)
   {
      var date = resolveDateField(uppgift, constraint.getField());
      if (date == null)
      {
         return false;
      }
      var from = LocalDate.now().plus(parseOffset(constraint.getOffset()));
      var to = LocalDate.now();
      return !date.isBefore(from) && !date.isAfter(to);
   }

   private String resolveStringField(UppgiftDto uppgift, SorteringsordningFieldEq field)
   {
      return switch (field)
      {
         case UPPGIFT_ID -> uppgift.uppgiftId().toString();
         case STATUS -> uppgift.status().name();
         case REGEL -> uppgift.regel();
         case ROLL -> uppgift.roll();
         case VERKSAMHETSLOGIK -> uppgift.verksamhetslogik();
         case BESKRIVNING -> uppgift.beskrivning();
      };
   }

   private String resolveStringField(UppgiftDto uppgift, SorteringsordningFieldString field)
   {
      return switch (field)
      {
         case STATUS -> uppgift.status().name();
         case REGEL -> uppgift.regel();
         case ROLL -> uppgift.roll();
         case VERKSAMHETSLOGIK -> uppgift.verksamhetslogik();
         case BESKRIVNING -> uppgift.beskrivning();
      };
   }

   private LocalDate resolveDateField(UppgiftDto uppgift, SorteringsordningFieldDate field)
   {
      return switch (field)
      {
         case SKAPAD -> uppgift.skapad();
         case PLANERAD_TILL -> uppgift.planeradTill();
      };
   }

   private Period parseOffset(String offset)
   {
      if(offset==null||offset.isBlank()){return Period.ZERO;}var s=offset.strip();var negative=s.startsWith("-");if(negative){s=s.substring(1);}var unit=s.charAt(s.length()-1);var amount=Integer.parseInt(s.substring(0,s.length()-1));var period=switch(unit){case'd'->Period.ofDays(amount);case'w'->Period.ofWeeks(amount);case'm'->Period.ofMonths(amount);case'y'->Period.ofYears(amount);default->throw new IllegalArgumentException("Unknown offset unit: "+unit);};return negative?period.negated():period;
   }
}
