package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import java.util.List;

/**
 * JPA {@link AttributeConverter} that serializes a list of {@link SorteringsordningEntry}
 * objects to a JSON TEXT column and back.
 * <p>
 * Deserialization uses {@link SorteringsordningEntryDeserializer} to handle the
 * {@link se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Constraint}
 * subtypes, which are not connected by Java inheritance in the generated OpenAPI model.
 */
@Converter
public class SorteringsordningEntriesConverter
      implements AttributeConverter<List<SorteringsordningEntry>, String>
{
   private static final ObjectMapper MAPPER = JsonMapper.builder()
         .addModule(new JavaTimeModule())
         .addModule(new SimpleModule().addDeserializer(SorteringsordningEntry.class, new SorteringsordningEntryDeserializer()))
         .build();
   private static final TypeReference<List<SorteringsordningEntry>> TYPE = new TypeReference<>()
   {
   };

   /**
    * Serializes the entry list to a JSON string for storage in the TEXT column.
    *
    * @param entries the list of entries to serialize
    * @return JSON representation of the list
    */
   @Override
   public String convertToDatabaseColumn(List<SorteringsordningEntry> entries)
   {
      try
      {
         return MAPPER.writeValueAsString(entries);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Failed to serialize SorteringsordningEntry list", e);
      }
   }

   /**
    * Deserializes a JSON string from the TEXT column back to the entry list.
    *
    * @param json the JSON string from the database column
    * @return the deserialize list of entries
    */
   @Override
   public List<SorteringsordningEntry> convertToEntityAttribute(String json)
   {
      try
      {
         return MAPPER.readValue(json, TYPE);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Failed to deserialize SorteringsordningEntry list", e);
      }
   }
}
