package se.fk.github.rimfrost.operativt.uppgiftslager.integration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;

/**
 * Registers custom Jackson deserializers with the Quarkus ObjectMapper.
 * <p>
 * The generated {@link Constraint} model declares {@code @JsonTypeInfo} and {@code @JsonSubTypes}
 * pointing to concrete subtypes ({@code ConstraintEq}, etc.) that do not extend {@code Constraint}
 * in Java. When Jackson's {@code TypeDeserializer} dispatches to a concrete subtype and returns it
 * as {@code Constraint}, the JVM throws {@code ClassCastException} because the concrete type is
 * not assignment-compatible.
 * <p>
 * Registering {@link SorteringsordningEntryDeserializer} at the {@link SorteringsordningEntry}
 * level bypasses this: the deserializer builds a raw list of concrete constraint objects and casts
 * the list (not individual elements) to {@code List<Constraint>}, avoiding any JVM checkcast.
 */
@Singleton
public class JacksonConfiguration implements ObjectMapperCustomizer
{
   /**
    * Registers {@link SorteringsordningEntryDeserializer} on the shared ObjectMapper so that
    * {@link SorteringsordningEntry} objects are deserialized correctly at the REST layer.
    *
    * @param mapper the Quarkus-managed ObjectMapper to customise
    */
   @Override
   public void customize(ObjectMapper mapper)
   {
      SimpleModule module = new SimpleModule();
      module.addDeserializer(SorteringsordningEntry.class, new SorteringsordningEntryDeserializer());
      mapper.registerModule(module);
   }
}
