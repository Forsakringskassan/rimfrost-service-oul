package se.fk.github.rimfrost.operativt.uppgiftslager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumMapperTest
{
   private final EnumMapper mapper = new EnumMapper();

   @ParameterizedTest
   @EnumSource(UppgiftStatus.class)
   @DisplayName("FR-01.2, FR-02.3, FR-06.2: Statusvärden serialiseras korrekt till sträng och tillbaka utan förlust")
   void mapsUppgiftStatusToStatusAndBack(UppgiftStatus status)
   {
      String mapped = mapper.mapUppgiftStatusToStatus(status);
      assertEquals(status, mapper.mapStatusToUppgiftStatus(mapped));
   }
}
