package no.nav.tms.token.support.tokendings.exchange

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class TargetAppNameBuilderTest {
    private val localClusterName = "dev-local"
    private val localNamespace = "here"
    private val otherClusterName = "dev-other"
    private val otherNamespace = "there"

    private val otherApi = "other-api"

    @Test
    fun `Should use default cluster and namespace when only app name is specified`() {
        val nameBuilder = TargetAppNameBuilder(localClusterName, localNamespace)

        val expected = "$localClusterName:$localNamespace:$otherApi"
        val result = nameBuilder.buildName(otherApi)

        result `should be equal to` expected
    }

    @Test
    fun `Should use default cluster when app name and cluster is specified`() {
        val nameBuilder = TargetAppNameBuilder(localClusterName, localNamespace)

        val expected = "$localClusterName:$otherNamespace:$otherApi"
        val result = nameBuilder.buildName(otherApi, namespace = otherNamespace)

        result `should be equal to` expected
    }

    @Test
    fun `Should not use defaults when all cluster, namespace and app name are specified`() {
        val nameBuilder = TargetAppNameBuilder(localClusterName, localNamespace)

        val expected = "$otherClusterName:$otherNamespace:$otherApi"
        val result = nameBuilder.buildName(otherApi, cluster = otherClusterName, namespace = otherNamespace)

        result `should be equal to` expected
    }
}
