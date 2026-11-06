package se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka;

public class StatusNotificationException extends RuntimeException
{
   public StatusNotificationException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
