package org.bonitasoft.example

import org.bonitasoft.web.client.BonitaClient
import org.bonitasoft.web.client.invoker.ApiClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers

import feign.Headers
import feign.RequestLine
import groovy.io.FileType
import spock.lang.Shared
import spock.lang.Specification
/**
 * Example of an integration test using Testcontainers, bonita-java-client and the Bonita docker image
 */
@Testcontainers
class MultiEndpointIT extends Specification {

    @Shared
    GenericContainer  bonitaContainer = new GenericContainer<>("bonita:7.11.4")
                                                .withExposedPorts(8080)
                                                .waitingFor(Wait.forHttp("/bonita"))

    def "deploy and execute all custom endpoints"() {
        given: "The built api extension archive"
        def foundArchives = []
        new File('target').eachFile(FileType.FILES) {
                    if(it.name.endsWith('.zip')) {
                        foundArchives << it
                    }
                }
        assert foundArchives.size() == 1
        def apiExtensionArchive = foundArchives[0]

        and: "a Bonita client"
        def bonitaHost = bonitaContainer.getHost();
        def bonitaPort = bonitaContainer.getFirstMappedPort();
        def bonitaClient = BonitaClient.builder("http://$bonitaHost:$bonitaPort/bonita/").build()
        // Default technical user credentials
        bonitaClient.login('install', 'install')
        
        and: "A custom service for the extension endpoints"
        def customService =  bonitaClient.get(CustomEndpointService)

        when: "Deploying the api extension"
        bonitaClient.applications().importPage(apiExtensionArchive)
        
        and: "Calling all the endpoints"
        def endpoint1Response = customService.endpoint1()
        def endpoint2Response = customService.endpoint2()
        def endpoint3Response = customService.endpoint3()

        then: "Expected result is returned"
        endpoint1Response.content == "Hello I'm Endpoint1"
        endpoint2Response.content == "Hello I'm Endpoint2"
        endpoint3Response.content == "Hello I'm Endpoint3"

        cleanup:
        bonitaClient.logout()
    }
    
    /**
     * Feign service interface use to bind our custom endpoints to the existing client.
     * The current extension mechanism in the Bonita java client is still work in progress and subject to changes !
     */
    interface CustomEndpointService extends ApiClient.Api {
        
        @RequestLine('GET /API/extension/endpoint1')
        @Headers(['Accept: application/json'])
        CustomReponse endpoint1()
        
        @RequestLine('GET /API/extension/endpoint2')
        @Headers(['Accept: application/json'])
        CustomReponse endpoint2()
        
        @RequestLine('GET /API/extension/endpoint3')
        @Headers(['Accept: application/json'])
        CustomReponse endpoint3()
    
    }
    
    /**
     * Simple POJO representing the response body used by the client for deserialization
     */
    static class CustomReponse {
        
        String content
        
    }

}





