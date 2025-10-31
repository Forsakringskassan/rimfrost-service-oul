package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OperativtUppgiftslagerService
{
   @Inject
   LogicMapper logicMapper;


   public void addOperativeTask() {
      // TODO: Logik för att lägga till i DB osv
   }
}
