package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.IdentityInvalidAuthCredentialsException;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.IdentityStatusException;
import se.fk.rimfrost.adapter.identity.adapter.IdentityAdapter;
import se.fk.rimfrost.adapter.identity.exception.IdentityException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class IdentityUtilTest
{
   @InjectMock
   IdentityAdapter identityAdapter;

   @Inject
   IdentityUtil identityUtil;

   @Test
   void should_return_identity_on_get_identity_success() throws IdentityException
   {
      var expectedTypeId = UUID.randomUUID().toString();
      var expectedValue = UUID.randomUUID().toString();
      var adapterResponse = se.fk.rimfrost.adapter.identity.model.ImmutableIdtyp.builder().typId(expectedTypeId)
            .varde(expectedValue).build();

      Mockito.when(identityAdapter.getIdentity(Mockito.any())).thenReturn(adapterResponse);

      var identity = identityUtil.getIdentity("header value");
      assertEquals(expectedTypeId, identity.typId());
      assertEquals(expectedValue, identity.varde());
   }

   @Test
   void should_throw_identity_invalid_auth_credentials_exception_on_unauthorized() throws IdentityException
   {
      Mockito.when(identityAdapter.getIdentity(Mockito.any()))
            .thenThrow(new IdentityException(IdentityException.ErrorType.UNAUTHORIZED, ""));
      assertThrows(IdentityInvalidAuthCredentialsException.class, () -> identityUtil.getIdentity("header value"));
   }

   @Test
   void should_throw_identity_status_exception_on_other_adapter_exception() throws IdentityException
   {
      Mockito.when(identityAdapter.getIdentity(Mockito.any()))
            .thenThrow(new IdentityException(IdentityException.ErrorType.SERVICE_UNAVAILABLE, ""));
      assertThrows(IdentityStatusException.class, () -> identityUtil.getIdentity("header value"));
   }
}
