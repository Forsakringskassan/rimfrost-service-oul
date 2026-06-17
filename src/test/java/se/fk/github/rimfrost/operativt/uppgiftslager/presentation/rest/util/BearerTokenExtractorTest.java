package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.MalformedBearerTokenException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

/**
 * Unit tests for {@link BearerTokenExtractor}.
 */
class BearerTokenExtractorTest
{
   private BearerTokenExtractor extractor;

   @BeforeEach
   void setUp()
   {
      extractor = new BearerTokenExtractor();
   }

   @Test
   @DisplayName("Valid token returns Idtyp with correct typId and varde")
   void validToken_returnsCorrectIdtyp()
   {
      Idtyp result = extractor.extract("Bearer abc123:xyz789");

      assertEquals("abc123", result.typId());
      assertEquals("xyz789", result.varde());
   }

   @Test
   @DisplayName("Valid token with UUID values parses correctly")
   void validTokenWithUuids_returnsCorrectIdtyp()
   {
      var handlaggarId = UUID.randomUUID();

      Idtyp result = extractor.extract("Bearer " + oulHandlaggareTypId + ":" + handlaggarId);

      assertEquals(oulHandlaggareTypId, result.typId());
      assertEquals(handlaggarId.toString(), result.varde());
   }

   @Test
   @DisplayName("Null header throws MalformedBearerTokenException")
   void nullHeader_throwsMalformedBearerTokenException()
   {
      assertThrows(MalformedBearerTokenException.class, () -> extractor.extract(null));
   }

   @Test
   @DisplayName("Header without Bearer prefix throws MalformedBearerTokenException")
   void missingBearerPrefix_throwsMalformedBearerTokenException()
   {
      assertThrows(MalformedBearerTokenException.class, () -> extractor.extract("abc123:xyz789"));
   }

   @Test
   @DisplayName("Token without colon separator throws MalformedBearerTokenException")
   void tokenWithoutColon_throwsMalformedBearerTokenException()
   {
      assertThrows(MalformedBearerTokenException.class, () -> extractor.extract("Bearer nocolon"));
   }

   @Test
   @DisplayName("Token with multiple colons splits on first colon only")
   void vardeContainingColon_parsesOnFirstColon()
   {
      Idtyp result = extractor.extract("Bearer typ:val:extra");

      assertEquals("typ", result.typId());
      assertEquals("val:extra", result.varde());
   }
}
