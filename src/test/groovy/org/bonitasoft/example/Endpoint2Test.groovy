package org.bonitasoft.example;

import groovy.json.JsonSlurper

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import spock.lang.Specification
import org.bonitasoft.web.extension.rest.RestAPIContext

import java.time.LocalDate;

class Endpoint2Test extends Specification {

    // Declare mocks here
    // Mocks are used to simulate external dependencies behavior
    def httpRequest = Mock(HttpServletRequest)
    def context = Mock(RestAPIContext)

    def should_return_a_json_representation_as_result() {
        given: "The endpoint controller"
        def controller = new Endpoint2()
        
        when: "Invoking the REST API"
        def apiResponse = controller.doHandle(httpRequest, new RestApiResponseBuilder(), context)

        then: "A JSON representation is returned in response body"
        def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
        // Validate returned response
        apiResponse.httpStatus == 200
        jsonResponse.content == "Hello I'm Endpoint2"
    }

}