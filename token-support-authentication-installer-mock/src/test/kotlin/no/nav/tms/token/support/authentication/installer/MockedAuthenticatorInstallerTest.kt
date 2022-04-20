package no.nav.tms.token.support.authentication.installer

import io.ktor.application.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.tms.token.support.authentication.installer.mock.MockedInstallerProxy
import no.nav.tms.token.support.authentication.installer.mock.installMockedAuthenticators
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MockedAuthenticatorInstallerTest {

    @BeforeEach
    fun setupMock() {
        mockkObject(MockedInstallerProxy)
    }

    @AfterEach
    fun cleanup() {
        unmockkObject(MockedInstallerProxy)
    }

    @Test
    fun `Should invoke only ID-porten installer when that is requested`() {
        every { MockedInstallerProxy.invokeIdPortenMockInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installMockedAuthenticators {
                installIdPortenAuthMock {  }
            }
        }

        withTestApplication({ setup() }, {})

        verify(exactly = 1) { MockedInstallerProxy.invokeIdPortenMockInstaller(any(), any(), any()) }
        verify(exactly = 0) { MockedInstallerProxy.invokeTokenXMockInstaller(any(), any(), any()) }
        verify(exactly = 0) { MockedInstallerProxy.invokeAzureMockInstaller(any(), any(), any()) }
    }

    @Test
    fun `Should invoke only TokenX installer when that is requested`() {
        every { MockedInstallerProxy.invokeTokenXMockInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installMockedAuthenticators {
                installTokenXAuthMock {  }
            }
        }

        withTestApplication({ setup() }, {})

        verify(exactly = 0) { MockedInstallerProxy.invokeIdPortenMockInstaller(any(), any(), any()) }
        verify(exactly = 1) { MockedInstallerProxy.invokeTokenXMockInstaller(any(), any(), any()) }
        verify(exactly = 0) { MockedInstallerProxy.invokeAzureMockInstaller(any(), any(), any()) }
    }

    @Test
    fun `Should invoke only Azure installer when that is requested`() {
        every { MockedInstallerProxy.invokeAzureMockInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installMockedAuthenticators {
                installAzureAuthMock {  }
            }
        }

        withTestApplication({ setup() }, {})

        verify(exactly = 0) { MockedInstallerProxy.invokeIdPortenMockInstaller(any(), any(), any()) }
        verify(exactly = 0) { MockedInstallerProxy.invokeTokenXMockInstaller(any(), any(), any()) }
        verify(exactly = 1) { MockedInstallerProxy.invokeAzureMockInstaller(any(), any(), any()) }
    }

    @Test
    fun `Should enable invoking several installers at once`() {
        every { MockedInstallerProxy.invokeIdPortenMockInstaller(any(), any(), any()) } returns Unit
        every { MockedInstallerProxy.invokeTokenXMockInstaller(any(), any(), any()) } returns Unit
        every { MockedInstallerProxy.invokeAzureMockInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installMockedAuthenticators {
                installIdPortenAuthMock {  }
                installTokenXAuthMock {  }
                installAzureAuthMock {  }
            }
        }

        withTestApplication({ setup() }, {})

        verify(exactly = 1) { MockedInstallerProxy.invokeIdPortenMockInstaller(any(), any(), any()) }
        verify(exactly = 1) { MockedInstallerProxy.invokeTokenXMockInstaller(any(), any(), any()) }
        verify(exactly = 1) { MockedInstallerProxy.invokeAzureMockInstaller(any(), any(), any()) }
    }

    @Test
    fun `Should allow one authenticator to be set as default`() {
        every { MockedInstallerProxy.invokeIdPortenMockInstaller(any(), any(), any()) } returns Unit
        every { MockedInstallerProxy.invokeTokenXMockInstaller(any(), any(), any()) } returns Unit
        every { MockedInstallerProxy.invokeAzureMockInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installMockedAuthenticators {
                installIdPortenAuthMock { setAsDefault = true }
                installTokenXAuthMock {  }
                installAzureAuthMock {  }
            }
        }

        withTestApplication({ setup() }, {})
    }

    @Test
    fun `Should throw error if more than one authenticator is set as default`() {
        every { MockedInstallerProxy.invokeIdPortenMockInstaller(any(), any(), any()) } returns Unit
        every { MockedInstallerProxy.invokeTokenXMockInstaller(any(), any(), any()) } returns Unit
        every { MockedInstallerProxy.invokeAzureMockInstaller(any(), any(), any()) } returns Unit

        val setup: Application.() -> Unit = {
            installMockedAuthenticators {
                installIdPortenAuthMock { setAsDefault = true }
                installTokenXAuthMock { setAsDefault = true }
                installAzureAuthMock {  }
            }
        }

        invoking {
            withTestApplication({ setup() }, {})
        } `should throw` IllegalArgumentException::class
    }
}
