package se.fk.github.rimfrost.operativt.uppgiftslager;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.Status;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumMapperTest
{
   private final EnumMapper mapper = new EnumMapper();

   @ParameterizedTest
   @EnumSource(UppgiftStatus.class)
   void mapsUppgiftStatusToStatusAndBack(UppgiftStatus status)
   {
      Status mapped = mapper.mapUppgiftStatusToStatus(status);
      assertEquals(status, mapper.mapStatusToUppgiftStatus(mapped));
   }
}
