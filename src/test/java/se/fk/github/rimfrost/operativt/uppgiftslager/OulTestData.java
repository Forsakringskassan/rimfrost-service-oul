package se.fk.github.rimfrost.operativt.uppgiftslager;

import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.Erbjudande;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.ProcessInfo;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.Idtyp;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Constraint;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SortBy;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningField;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningFieldEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OulTestData
{
   public static final String oulHandlaggareTypId = "116759e4-18fd-4209-849c-90abbd257d22";

   public static CreateUppgiftRequest newCreateUppgiftRequest(UUID handlaggningId)
   {
      var individ = new Idtyp();
      individ.setTypId("d8bc00b6-445e-4085-ac31-d743cfb5f303");
      individ.setVarde("19900101-1234");

      var erbjudande = new Erbjudande();
      erbjudande.setId("55f84389-8e66-4122-8f07-c157141d5b1d");
      erbjudande.setNamn("Erbjudande namn");

      var request = new CreateUppgiftRequest();
      request.setVersion("1.0");
      request.setHandlaggningId(handlaggningId);
      request.setIndivider(List.of(individ));
      request.setRegel("Test Regel");
      request.setRoll("Test Roll");
      request.setBeskrivning("Test Beskrivning");
      request.setVerksamhetslogik("Test Verksamhetslogik");
      request.setUrl("/test/url/");
      request.setSubTopic("test-sub-topic");
      var processInfo = new ProcessInfo();
      processInfo.setReplyTopic("test-reply-topic");
      processInfo.setCloudeventAttributes(Map.of("kogitoprocinstanceid", "test-proc-instance-id"));
      request.setProcessInfo(processInfo);
      request.setErbjudande(erbjudande);

      return request;
   }

   public static EndUppgiftRequest newEndUppgiftRequest(String reason)
   {
      var endUppgiftRequest = new EndUppgiftRequest();
      endUppgiftRequest.setReason(reason);
      return endUppgiftRequest;
   }

   public static SorteringsordningSpec newSorteringsordningSpec()
   {
      var entry = new SorteringsordningEntry();
      var spec = new SorteringsordningSpec();
      spec.setEntries(List.of(entry));
      return spec;
   }

   @SuppressWarnings("unchecked")
   public static SorteringsordningSpec newSorteringsordningSpecWithConstraints()
   {
      var constraint = new ConstraintEq();
      constraint.setField(SorteringsordningFieldEq.STATUS);
      constraint.setOperator(ConstraintEq.OperatorEnum.EQ);
      constraint.setValue("NY");

      var sortBy = new SortBy();
      sortBy.setField(SorteringsordningField.SKAPAD);
      sortBy.setDirection(SortBy.DirectionEnum.ASC);

      var entry = new SorteringsordningEntry();
      entry.setConstraints((List<Constraint>) (List<?>) List.of(constraint));
      entry.setSortBy(sortBy);

      var spec = new SorteringsordningSpec();
      spec.setEntries(List.of(entry));
      return spec;
   }

   /**
    * Creates a sorteringsordning spec with two entries: an eq-constraint entry that
    * matches {@code value} on {@code field}, followed by a catch-all entry.
    * Tasks matching the constraint land in group 0; all others in group 1.
    *
    * @param field the field to match on
    * @param value the exact value to match
    * @return a two-entry sorteringsordning spec
    */
   @SuppressWarnings("unchecked")
   public static SorteringsordningSpec newSorteringsordningSpecWithEqConstraint(SorteringsordningFieldEq field,
         String value)
   {
      var constraint = new ConstraintEq();
      constraint.setField(field);
      constraint.setOperator(ConstraintEq.OperatorEnum.EQ);
      constraint.setValue(value);

      var priorityEntry = new SorteringsordningEntry();
      priorityEntry.setConstraints((List<Constraint>) (List<?>) List.of(constraint));

      var catchAllEntry = new SorteringsordningEntry();

      var spec = new SorteringsordningSpec();
      spec.setEntries(List.of(priorityEntry, catchAllEntry));
      return spec;
   }

   /**
    * Creates a sorteringsordning spec with a single catch-all entry (no constraints)
    * that sorts all tasks by the given field and direction.
    *
    * @param field     the field to sort on
    * @param direction sort direction
    * @return a single-entry catch-all sorteringsordning spec with sort_by configured
    */
   public static SorteringsordningSpec newSorteringsordningSpecWithSortBy(SorteringsordningField field,
         SortBy.DirectionEnum direction)
   {
      var sortBy = new SortBy();
      sortBy.setField(field);
      sortBy.setDirection(direction);

      var entry = new SorteringsordningEntry();
      entry.setSortBy(sortBy);

      var spec = new SorteringsordningSpec();
      spec.setEntries(List.of(entry));
      return spec;
   }
}
