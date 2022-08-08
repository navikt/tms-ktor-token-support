package no.nav.tms.token.support.authentication.installer

import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.tms.token.support.idporten.sidecar.IdPortenRoutesConfig
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


internal class AuthenticatorInstallerTest {

    @BeforeEach
    fun setupMock() {
        mockkObject(InstallerProxy)
    }

    @AfterEach
    fun cleanup() {
        unmockkObject(InstallerProxy)
    }

    @Test
    fun `Should invoke only ID-porten installer when that is requested`() {
        every { InstallerProxy.invokeIdPortenInstaller(any(), any(), any()) } returns IdPortenRoutesConfig {  }

        val setup: Application.() -> Unit = {
            installAuthenticators {
                installIdPortenAuth {  }
            }
        }

        testApplication {
            application {
                setup()
            }
        }

        verify(exactly = 1) { InstallerProxy.invokeIdPortenInstaller(any(), any(), any()) }
        verify(exactly = 0) { InstallerProxy.invokeTokenXInstaller(any(), any(), any()) }
        verify(exactly = 0) { InstallerProxy.invokeAzureInstaller(any(), any(), any()) }
    }

    @Test
    fun `Should invoke only TokenX installer when that is requested`() {
        every { InstallerProxy.invokeTokenXInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installAuthenticators {
                installTokenXAuth {  }
            }
        }

        testApplication {
            application {
                setup()
            }
        }

        verify(exactly = 0) { InstallerProxy.invokeIdPortenInstaller(any(), any(), any()) }
        verify(exactly = 1) { InstallerProxy.invokeTokenXInstaller(any(), any(), any()) }
        verify(exactly = 0) { InstallerProxy.invokeAzureInstaller(any(), any(), any()) }
    }

    @Test
    fun `Should invoke only Azure installer when that is requested`() {
        every { InstallerProxy.invokeAzureInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installAuthenticators {
                installAzureAuth {  }
            }
        }

        testApplication {
            application {
                setup()
            }
        }

        verify(exactly = 0) { InstallerProxy.invokeIdPortenInstaller(any(), any(), any()) }
        verify(exactly = 0) { InstallerProxy.invokeTokenXInstaller(any(), any(), any()) }
        verify(exactly = 1) { InstallerProxy.invokeAzureInstaller(any(), any(), any()) }
    }

    @Test
    fun `Should enable invoking several installers at once`() {
        every { InstallerProxy.invokeIdPortenInstaller(any(), any(), any()) } returns IdPortenRoutesConfig {  }
        every { InstallerProxy.invokeTokenXInstaller(any(), any(), any()) } returns Unit
        every { InstallerProxy.invokeAzureInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installAuthenticators {
                installIdPortenAuth {  }
                installTokenXAuth {  }
                installAzureAuth {  }
            }
        }

        testApplication {
            application {
                setup()
            }
        }

        verify(exactly = 1) { InstallerProxy.invokeIdPortenInstaller(any(), any(), any()) }
        verify(exactly = 1) { InstallerProxy.invokeTokenXInstaller(any(), any(), any()) }
        verify(exactly = 1) { InstallerProxy.invokeAzureInstaller(any(), any(), any()) }
    }

    @Test
    fun `Should allow one authenticator to be set as default`() {
        every { InstallerProxy.invokeIdPortenInstaller(any(), any(), any()) } returns IdPortenRoutesConfig {  }
        every { InstallerProxy.invokeTokenXInstaller(any(), any(), any()) } returns Unit
        every { InstallerProxy.invokeAzureInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installAuthenticators {
                installIdPortenAuth { setAsDefault = true }
                installTokenXAuth {  }
                installAzureAuth {  }
            }
        }

        testApplication {
            application {
                setup()
            }
        }
    }

    @Test
    fun `Should throw error if more than one authenticator is set as default`() {
        every { InstallerProxy.invokeIdPortenInstaller(any(), any(), any()) } returns IdPortenRoutesConfig {  }
        every { InstallerProxy.invokeTokenXInstaller(any(), any(), any()) } returns Unit
        every { InstallerProxy.invokeAzureInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installAuthenticators {
                installIdPortenAuth { setAsDefault = true }
                installTokenXAuth { setAsDefault = true }
                installAzureAuth {  }
            }
        }

        invoking {
            testApplication {
                application {
                    setup()
                }
            }
        } `should throw` IllegalArgumentException::class
    }
}
