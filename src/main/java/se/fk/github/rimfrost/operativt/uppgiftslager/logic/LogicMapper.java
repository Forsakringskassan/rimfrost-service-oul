package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.*;

@ApplicationScoped
public class LogicMapper
{
   @Inject
   EnumMapper enumMapper;

   public OperativtUppgiftslagerStatusMessagePayload toOperativtUppgiftslagerStatusMessagePayload(UppgiftEntity uppgift)
   {
      var data = new OperativtUppgiftslagerStatusMessagePayload();
      data.setStatus(enumMapper.mapUppgiftStatusToStatus(uppgift.status()));
      data.setUppgiftId(uppgift.uppgiftId().toString());
      data.setProcessId(uppgift.processId().toString());
      if (uppgift.personnummer() != null)
         data.setPersonnummer(uppgift.personnummer());
      return data;
   }
}
