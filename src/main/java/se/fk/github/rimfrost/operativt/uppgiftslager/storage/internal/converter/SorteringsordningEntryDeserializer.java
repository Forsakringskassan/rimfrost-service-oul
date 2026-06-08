package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Constraint;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintBetween;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintContains;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintEq;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.ConstraintOffsetToNow;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SortBy;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson deserializer for {@link SorteringsordningEntry}.
 * <p>
 * The generated OpenAPI model does not establish Java inheritance between
 * {@link Constraint} and its concrete subtypes ({@link ConstraintEq}, {@link ConstraintBetween},
 * {@link ConstraintContains}, {@link ConstraintOffsetToNow}), so Jackson's standard polymorphic
 * dispatch cannot be used. This deserializer reads the {@code operator} field and routes each
 * constraint object to its concrete type manually.
 * <p>
 * The resulting list is built as a raw {@link java.util.ArrayList} and cast via
 * {@code (List<Constraint>)(List<?>)} to avoid a JVM {@code checkcast} on individual elements,
 * which would fail at runtime since the elements do not extend {@link Constraint}.
 */
@SuppressWarnings("unchecked")
class SorteringsordningEntryDeserializer extends StdDeserializer<SorteringsordningEntry>
{
   /**
    * Registers this deserializer for {@link SorteringsordningEntry}.
    */
   SorteringsordningEntryDeserializer()
   {
      super(SorteringsordningEntry.class);
   }

   /**
    * Deserializes a single {@link SorteringsordningEntry} from JSON, dispatching each
    * constraint object to its concrete subtype based on the {@code operator} field.
    *
    * @param p    the JSON parser positioned at the start of the entry object
    * @param ctxt the deserialization context
    * @return the fully populated {@link SorteringsordningEntry}
    * @throws IOException if the JSON is malformed or contains an unknown operator
    */
   @Override
   public SorteringsordningEntry deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
   {
      ObjectMapper mapper = (ObjectMapper) p.getCodec();
      JsonNode node = mapper.readTree(p);
      SorteringsordningEntry entry = new SorteringsordningEntry();
      if (node.has("constraints") && node.get("constraints").isArray())
      {
         List rawList = new ArrayList<>();
         for (JsonNode cn : node.get("constraints"))
         {
            rawList.add(deserializeConstraint(mapper, cn));
         }
         entry.setConstraints((List<Constraint>) (List<?>) rawList);
      }
      if (node.has("sort_by") && !node.get("sort_by").isNull())
      {
         entry.setSortBy(mapper.treeToValue(node.get("sort_by"), SortBy.class));
      }
      return entry;
   }

   /**
    * Maps a single constraint JSON node to its concrete Java type based on the {@code operator} value.
    *
    * @param mapper the ObjectMapper used for tree-to-value conversion
    * @param cn     the JSON node representing one constraint
    * @return an instance of the appropriate concrete constraint type
    * @throws IOException if the operator value is unrecognised
    */
   private Object deserializeConstraint(ObjectMapper mapper, JsonNode cn) throws IOException
   {
      String operator = cn.path("operator").asText();
      if ("eq".equals(operator))
      {
         return mapper.treeToValue(cn, ConstraintEq.class);
      }
      if ("between".equals(operator))
      {
         return mapper.treeToValue(cn, ConstraintBetween.class);
      }
      if ("contains".equals(operator))
      {
         return mapper.treeToValue(cn, ConstraintContains.class);
      }
      if ("offset_to_now".equals(operator))
      {
         return mapper.treeToValue(cn, ConstraintOffsetToNow.class);
      }
      throw new IOException("Unknown constraint operator: " + operator);
   }
}
