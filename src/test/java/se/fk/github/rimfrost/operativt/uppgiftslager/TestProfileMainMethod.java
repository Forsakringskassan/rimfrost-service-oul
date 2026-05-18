package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTestProfile;

public class TestProfileMainMethod implements QuarkusTestProfile
{
   @Override
   public boolean runMainMethod()
   {
      return true;
   }
}
