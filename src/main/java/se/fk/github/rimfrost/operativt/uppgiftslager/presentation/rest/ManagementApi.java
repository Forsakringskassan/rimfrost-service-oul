package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(
{
      ElementType.METHOD, ElementType.TYPE
})
public @interface ManagementApi
{
}
