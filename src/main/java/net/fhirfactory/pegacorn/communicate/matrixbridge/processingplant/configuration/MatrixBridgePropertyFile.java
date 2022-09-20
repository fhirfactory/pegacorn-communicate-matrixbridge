package net.fhirfactory.pegacorn.communicate.matrixbridge.processingplant.configuration;

import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.archetypes.PetasosEnabledSubsystemPropertyFile;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.ports.http.ClusteredHTTPServerPortSegment;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.ports.http.HTTPClientPortSegment;

public abstract class MatrixBridgePropertyFile extends PetasosEnabledSubsystemPropertyFile {
    private ClusteredHTTPServerPortSegment interactIngressMatrixEvents;
    private HTTPClientPortSegment interactEgressMatrixActions;
    private HTTPClientPortSegment interactEgressMatrixQuery;
    private HTTPClientPortSegment InteractEgressSynapseAPIClient;

    //
    // Constructor(s)
    //

    public MatrixBridgePropertyFile(){
        super();
        interactIngressMatrixEvents = new ClusteredHTTPServerPortSegment();
        interactEgressMatrixActions = new HTTPClientPortSegment();
        InteractEgressSynapseAPIClient = new HTTPClientPortSegment();
        interactEgressMatrixQuery = new HTTPClientPortSegment();
    }

    //
    // Getters and Setters
    //

    public HTTPClientPortSegment getInteractEgressMatrixQuery() {
        return interactEgressMatrixQuery;
    }

    public void setInteractEgressMatrixQuery(HTTPClientPortSegment interactEgressMatrixQuery) {
        this.interactEgressMatrixQuery = interactEgressMatrixQuery;
    }


    public HTTPClientPortSegment getInteractEgressMatrixActions() {
        return interactEgressMatrixActions;
    }

    public void setInteractEgressMatrixActions(HTTPClientPortSegment interactEgressMatrixActions) {
        this.interactEgressMatrixActions = interactEgressMatrixActions;
    }

    public HTTPClientPortSegment getInteractEgressSynapseAPIClient() {
        return InteractEgressSynapseAPIClient;
    }

    public void setInteractEgressSynapseAPIClient(HTTPClientPortSegment interactEgressSynapseAPIClient) {
        InteractEgressSynapseAPIClient = interactEgressSynapseAPIClient;
    }

    public ClusteredHTTPServerPortSegment getInteractIngressMatrixEvents() {
        return interactIngressMatrixEvents;
    }

    public void setInteractIngressMatrixEvents(ClusteredHTTPServerPortSegment interactIngressMatrixEvents) {
        this.interactIngressMatrixEvents = interactIngressMatrixEvents;
    }
}
