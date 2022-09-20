/*
 * Copyright (c) 2021 Mark A. Hunter (ACT Health)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.communicate.matrixbridge.processingplant.configuration;

import net.fhirfactory.pegacorn.core.model.topology.endpoints.adapters.HTTPClientAdapter;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.adapters.HTTPServerAdapter;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.adapters.base.IPCAdapterDefinition;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.issi.matrix.MatrixAPIClientEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.issi.matrix.MatrixEventReceiverEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.mode.ResilienceModeEnum;
import net.fhirfactory.pegacorn.core.model.topology.nodes.common.EndpointProviderInterface;
import net.fhirfactory.pegacorn.core.model.topology.nodes.external.ConnectedExternalSystemTopologyNode;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.ports.http.ClusteredHTTPServerPortSegment;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.ports.http.HTTPClientPortSegment;
import net.fhirfactory.pegacorn.deployment.topology.factories.archetypes.base.PetasosEnabledSubsystemTopologyFactory;

public abstract class MatrixBridgeTopologyFactory extends PetasosEnabledSubsystemTopologyFactory {

    //
    // Constructor(s)
    //

    public MatrixBridgeTopologyFactory(){
        super();
    }

    //
    // Getters and Setters
    //

    private MatrixBridgePropertyFile getMatrixBridgePropertyFile(){
        return((MatrixBridgePropertyFile)getPropertyFile());
    }

    //
    // Business Methods
    //

    public void addMatrixEventsReceiver(EndpointProviderInterface endpointProvider) {
        getLogger().debug(".addMatrixEventsReceiver(): Entry");

        ClusteredHTTPServerPortSegment interactIngressMatrixEventsProperties = getMatrixBridgePropertyFile().getInteractIngressMatrixEvents();

        getHTTPTopologyEndpointFactory().newHTTPServerTopologyEndpoint(getMatrixBridgePropertyFile(),endpointProvider,"MatrixEventsReceiver", interactIngressMatrixEventsProperties);

        getLogger().debug(".addMatrixEventsReceiver(): Exit");
    }

    public void addMatrixActionsClient(EndpointProviderInterface endpointProvider) {
        getLogger().debug(".addMatrixActionsClient(): Entry");

        HTTPClientPortSegment matrixActionsClientProperties = getMatrixBridgePropertyFile().getInteractEgressMatrixActions();

        getHTTPTopologyEndpointFactory().newHTTPClientTopologyEndpoint(getMatrixBridgePropertyFile(), endpointProvider, "MatrixActions", matrixActionsClientProperties);

        getLogger().debug(".addMatrixActionsClient(): Exit");
    }

    public void addSynapseAdminClientEndpoint( EndpointProviderInterface endpointProvider) {
        getLogger().debug(".addSynapseAdminClientEndpoint(): Entry");

        HTTPClientPortSegment interactEgressSynapseClientProperties = getMatrixBridgePropertyFile().getInteractEgressSynapseAPIClient();

        getHTTPTopologyEndpointFactory().newHTTPClientTopologyEndpoint(getMatrixBridgePropertyFile(), endpointProvider, "SynapseAPIClient", interactEgressSynapseClientProperties);

        getLogger().debug(".addSynapseAdminClientEndpoint(): Exit");
    }

    public void addMatrixQueryClient(EndpointProviderInterface endpointProvider) {
        getLogger().debug(".addMatrixQueryClient(): Entry");

        HTTPClientPortSegment interactEgressMatrixQueryProperties = getMatrixBridgePropertyFile().getInteractEgressMatrixQuery();

        getHTTPTopologyEndpointFactory().newHTTPClientTopologyEndpoint(getMatrixBridgePropertyFile(),endpointProvider, "MatrixQuery", interactEgressMatrixQueryProperties);

        getLogger().debug(".addMatrixQueryClient(): Exit");
    }


}
