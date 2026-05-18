package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.management;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;

@ApplicationScoped
public class ManagementMapper
{
   public OperativtUppgiftslagerAddRequest toAddRequest(CreateUppgiftRequest request)
   {
      return ImmutableOperativtUppgiftslagerAddRequest.builder()
            .version(request.getVersion())
            .handlaggningId(request.getHandlaggningId())
            .individer(toIdtyper(request.getIndivider()))
            .regel(request.getRegel())
            .beskrivning(request.getBeskrivning())
            .verksamhetslogik(request.getVerksamhetslogik())
            .roll(request.getRoll())
            .url(request.getUrl())
            .build();
   }

   private Idtyp[] toIdtyper(List<se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Idtyp> individer)
   {
      if (individer == null)
      {
         return new Idtyp[0];
      }
      return individer.stream()
            .map(i -> ImmutableIdtyp.builder().typId(i.getTypId()).varde(i.getVarde()).build())
            .toArray(Idtyp[]::new);
   }
}
