package no.nav.tms.token.support.user.token.verification

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.*


internal class TokenVerifierTest {

    private val jwk = JwkBuilder.generateJwk()

    private val mockIssuer = "http://issuer"
    private val mockAudience = UUID.randomUUID().toString()
    private val mockJwkProvider: JwkProvider = mockk()

    private val now = Date()
    private val hourFromNow = Date() + Duration.ofHours(1)

    @AfterEach
    fun cleanUp() {
        clearMocks(mockJwkProvider)
    }

    @Test
    fun `godtar gyldig token`() {

        val testIssuer = "http://valid-issuer"
        val testAudience = UUID.randomUUID().toString()

        val acrClaim = "high"
        val acrMapper = { acr: String -> if (acr == acrClaim) LevelOfAssurance.High else throw IllegalArgumentException() }

        val verifier = tokenVerifier(
            issuer = testIssuer,
            audience = testAudience,
            acrMapper = acrMapper,
            minLevelOfAssurance = LevelOfAssurance.High
        )

        val token = generateJwt(
            issueTime = now,
            expiryTime = hourFromNow,
            audience = testAudience,
            issuer = testIssuer,
            levelOfAssurance = acrClaim,
            rsaKey = jwk
        )

        every { mockJwkProvider.get(any()) } returns jwk.toJwk()

        shouldNotThrow<Exception> {
            verifier.verify(token)
        }
    }

    @Test
    fun `godtar ikke token med ukjent issuer`() {

        val validIssuer = "http://valid-issuer"

        val verifier = tokenVerifier(
            issuer = validIssuer,
        )

        val token = generateJwt(
            issuer = "http://invalid-issuer",
        )

        every { mockJwkProvider.get(any()) } returns jwk.toJwk()

        shouldThrow<Exception> {
            verifier.verify(token)
        }
    }

    @Test
    fun `godtar ikke token med feil audience`() {

        val riktigAudience = "riktig_audience"
        val feilAudience = "feil_audience"

        val verifier = tokenVerifier(
            audience = riktigAudience,
        )

        val token = generateJwt(
            audience = feilAudience,
        )

        every { mockJwkProvider.get(any()) } returns jwk.toJwk()

        shouldThrow<Exception> {
            verifier.verify(token)
        }
    }

    @Test
    fun `godtar ikke tokens hvor expiryTime er passert`() {

        val verifier = tokenVerifier()

        val token = generateJwt(
            issueTime = now - Duration.ofHours(2),
            expiryTime = now - Duration.ofHours(1),
        )

        every { mockJwkProvider.get(any()) } returns jwk.toJwk()

        shouldThrow<Exception> {
            verifier.verify(token)
        }
    }

    @Test
    fun `godtar ikke tokens med lavere enn påkrevd level of assurance`() {
        val loaSubstantial = "sub"
        val loaHigh = "high"
        val acrMapper = { acr: String ->
            when (acr) {
                loaSubstantial -> LevelOfAssurance.Substantial
                loaHigh -> LevelOfAssurance.High
                else -> throw IllegalArgumentException()
            }
        }


        val verifier = tokenVerifier(
            acrMapper = acrMapper,
            minLevelOfAssurance = LevelOfAssurance.High,
        )

        val token = generateJwt(
            levelOfAssurance = loaSubstantial
        )

        every { mockJwkProvider.get(any()) } returns jwk.toJwk()

        shouldThrow<Exception> {
            verifier.verify(token)
        }
    }

    @Test
    fun `godtar tokens med høyere enn påkrevd level of assurance`() {
        val loaSubstantial = "sub"
        val loaHigh = "high"
        val acrMapper = { acr: String ->
            when (acr) {
                loaSubstantial -> LevelOfAssurance.Substantial
                loaHigh -> LevelOfAssurance.High
                else -> throw IllegalArgumentException()
            }
        }


        val verifier = tokenVerifier(
            acrMapper = acrMapper,
            minLevelOfAssurance = LevelOfAssurance.High,
        )

        val token = generateJwt(
            levelOfAssurance = loaHigh
        )

        every { mockJwkProvider.get(any()) } returns jwk.toJwk()

        shouldNotThrow<Exception> {
            verifier.verify(token)
        }
    }

    private fun tokenVerifier(
        issuer: String = mockIssuer,
        jwkProvider: JwkProvider = mockJwkProvider,
        audience: String = mockAudience,
        acrMapper: (String) -> LevelOfAssurance = { _: String -> LevelOfAssurance.High },
        minLevelOfAssurance: LevelOfAssurance = LevelOfAssurance.Substantial,
    ) = TokenVerifier(
        issuer = issuer,
        jwkProvider = jwkProvider,
        audience = audience,
        acrMapper = acrMapper,
        minLevelOfAssurance = minLevelOfAssurance
    )

    private fun generateJwt(
        issueTime: Date = now,
        expiryTime: Date = hourFromNow,
        issuer: String = mockIssuer,
        audience: String = mockAudience,
        levelOfAssurance: String = "mock",
        rsaKey: RSAKey = jwk
    ): DecodedJWT {
        return JWTClaimsSet.Builder()
            .audience(audience)
            .issuer(issuer)
            .issueTime(issueTime)
            .expirationTime(expiryTime)
            .claim("acr", levelOfAssurance)
            .claim("pid", "<ident>")
            .jwtID(UUID.randomUUID().toString())
            .build()
            .sign(rsaKey)
            .serialize()
            .let(JWT::decode)
    }

    private fun JWTClaimsSet.sign(rsaKey: RSAKey): SignedJWT =
        SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaKey.keyID)
                .type(JOSEObjectType.JWT).build(),
            this
        ).apply {
            sign(RSASSASigner(rsaKey.toPrivateKey()))
        }
}

private fun RSAKey.toJwk() = toJSONObject()
    .run { toMap() }
    .let { Jwk.fromValues(it) }
