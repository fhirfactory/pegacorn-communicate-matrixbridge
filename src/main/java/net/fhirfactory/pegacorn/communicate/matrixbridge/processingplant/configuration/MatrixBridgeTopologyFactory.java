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

import net.fhirfactory.pegacorn.core.model.component.valuesets.SoftwareComponentSystemRoleEnum;
import net.fhirfactory.pegacorn.core.model.componentid.PegacornSystemComponentTypeTypeEnum;
import net.fhirfactory.pegacorn.core.model.componentid.TopologyNodeRDN;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.adapters.HTTPClientAdapter;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.adapters.HTTPServerAdapter;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.adapters.base.IPCAdapterDefinition;
import net.fhirfactory.pegacorn.core.model.petasos.endpoint.valuesets.PetasosEndpointTopologyTypeEnum;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.issi.matrix.MatrixAPIClientEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.endpoints.issi.matrix.MatrixEventReceiverEndpoint;
import net.fhirfactory.pegacorn.core.model.topology.mode.ResilienceModeEnum;
import net.fhirfactory.pegacorn.core.model.topology.nodes.common.EndpointProviderInterface;
import net.fhirfactory.pegacorn.core.model.topology.nodes.external.ConnectedExternalSystemTopologyNode;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.connectedsystems.ConnectedSystemPort;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.connectedsystems.ConnectedSystemProperties;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.datatypes.ParameterNameValuePairType;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.ports.base.InterfaceDefinitionSegment;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.ports.http.ClusteredHTTPServerPortSegment;
import net.fhirfactory.pegacorn.deployment.properties.configurationfilebased.common.segments.ports.http.HTTPClientPortSegment;
import net.fhirfactory.pegacorn.deployment.topology.factories.archetypes.base.PetasosEnabledSubsystemTopologyFactory;

import java.util.HashMap;
import java.util.List;

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

        MatrixEventReceiverEndpoint matrixEventsReceiver = new MatrixEventReceiverEndpoint();

        ClusteredHTTPServerPortSegment interactIngressMatrixEventsProperties = getMatrixBridgePropertyFile().getInteractIngressMatrixEvents();

        String name = interactIngressMatrixEventsProperties.getName();
        TopologyNodeRDN nodeRDN = createNodeRDN(name, getMatrixBridgePropertyFile().getSubsystemInstant().getProcessingPlantVersion(), PegacornSystemComponentTypeTypeEnum.ENDPOINT);
        matrixEventsReceiver.setComponentRDN(nodeRDN);

        matrixEventsReceiver.setEndpointConfigurationName(interactIngressMatrixEventsProperties.getName());

        matrixEventsReceiver.constructFDN(endpointProvider.getComponentFDN(),nodeRDN);

        matrixEventsReceiver.constructFunctionFDN(endpointProvider.getNodeFunctionFDN(),nodeRDN);

        matrixEventsReceiver.setEndpointType(PetasosEndpointTopologyTypeEnum.HTTP_API_SERVER);

        matrixEventsReceiver.setServer(true);

        matrixEventsReceiver.constructFunctionFDN(endpointProvider.getNodeFunctionFDN(), nodeRDN);

        matrixEventsReceiver.setConnectedSystemName(interactIngressMatrixEventsProperties.getConnectedSystem().getSubsystemName());

        matrixEventsReceiver.setContainingNodeFDN(endpointProvider.getComponentFDN());

        HTTPServerAdapter httpServer = new HTTPServerAdapter();
        httpServer.setHostName(interactIngressMatrixEventsProperties.getServerHostname());
        httpServer.setServicePortValue(interactIngressMatrixEventsProperties.getServicePort());
        httpServer.setPortNumber(interactIngressMatrixEventsProperties.getServerPort());
        httpServer.setServiceDNSName(interactIngressMatrixEventsProperties.getServiceDNS());
        httpServer.setEncrypted(interactIngressMatrixEventsProperties.isEncrypted());
        if(!interactIngressMatrixEventsProperties.getOtherConfigurationParameters().isEmpty()){
            List<ParameterNameValuePairType> otherConfigurationParameters = interactIngressMatrixEventsProperties.getOtherConfigurationParameters();
            if(httpServer.getAdditionalParameters() == null){
                httpServer.setAdditionalParameters(new HashMap<>());
            }
            for(ParameterNameValuePairType currentNameValuePair: otherConfigurationParameters){
                httpServer.getAdditionalParameters().put(currentNameValuePair.getParameterName(), currentNameValuePair.getParameterValue());
            }
        }
        httpServer.setEnablingTopologyEndpoint(matrixEventsReceiver.getComponentID());
        httpServer.setGroupName(name);
        for(InterfaceDefinitionSegment currentInterface: interactIngressMatrixEventsProperties.getSupportedInterfaceProfiles()){
            IPCAdapterDefinition interfaceDef = new IPCAdapterDefinition();
            interfaceDef.setInterfaceFormalVersion(currentInterface.getInterfaceDefinitionVersion());
            interfaceDef.setInterfaceFormalName(currentInterface.getInterfaceDefinitionName());
            httpServer.getSupportedInterfaceDefinitions().add(interfaceDef);
            httpServer.getSupportedDeploymentModes().add(endpointProvider.getResilienceMode());
        }
        httpServer.getSupportedDeploymentModes().add(ResilienceModeEnum.RESILIENCE_MODE_KUBERNETES_MULTISITE_CLUSTERED);
        httpServer.getSupportedDeploymentModes().add(ResilienceModeEnum.RESILIENCE_MODE_KUBERNETES_MULTISITE);
        httpServer.getSupportedDeploymentModes().add(ResilienceModeEnum.RESILIENCE_MODE_KUBERNETES_STANDALONE);
        httpServer.getSupportedDeploymentModes().add(ResilienceModeEnum.RESILIENCE_MODE_KUBERNETES_CLUSTERED);
        httpServer.getSupportedDeploymentModes().add(ResilienceModeEnum.RESILIENCE_MODE_MULTISITE);
        httpServer.getSupportedDeploymentModes().add(ResilienceModeEnum.RESILIENCE_MODE_STANDALONE);
        httpServer.getSupportedDeploymentModes().add(ResilienceModeEnum.RESILIENCE_MODE_CLUSTERED);
        httpServer.getSupportedDeploymentModes().add(ResilienceModeEnum.RESILIENCE_MODE_MULTISITE_CLUSTERED);

        matrixEventsReceiver.setComponentType(PegacornSystemComponentTypeTypeEnum.ENDPOINT);

        matrixEventsReceiver.setComponentSystemRole(SoftwareComponentSystemRoleEnum.COMPONENT_ROLE_INTERACT_INGRES);

        matrixEventsReceiver.getAdapterList().add(httpServer);

        endpointProvider.addEndpoint(matrixEventsReceiver.getComponentFDN());
        getLogger().trace(".addMatrixEventsReceiver(): Add the MatrixApplicationServicesServer Port to the Topology Cache");
        getTopologyIM().addTopologyNode(endpointProvider.getComponentFDN(), matrixEventsReceiver);
        getLogger().debug(".addMatrixEventsReceiver(): Exit");
    }

    public void addMatrixActionsClient(EndpointProviderInterface endpointProvider) {
        getLogger().debug(".addMatrixActionsClient(): Entry");

        HTTPClientPortSegment matrixActionsClientProperties = getMatrixBridgePropertyFile().getInteractEgressMatrixActions();
        addMatrixBridgeClientEndpoint(endpointProvider, matrixActionsClientProperties);

        getLogger().debug(".addMatrixActionsClient(): Exit");
    }

    public void addMatrixBridgeClientEndpoint(EndpointProviderInterface endpointProvider, HTTPClientPortSegment matrixClientProperties){
        getLogger().debug(".addMatrixBridgeClientEndpoint(): Entry, endpointProvider->{}, matrixClientProperties->{}", endpointProvider, matrixClientProperties);

        MatrixAPIClientEndpoint matrixActionsClient = new MatrixAPIClientEndpoint();

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Set the componentRDN");
        String clientName = matrixClientProperties.getName();
        TopologyNodeRDN nodeRDN = createNodeRDN(clientName, getMatrixBridgePropertyFile().getSubsystemInstant().getProcessingPlantVersion(), PegacornSystemComponentTypeTypeEnum.ENDPOINT);
        matrixActionsClient.setComponentRDN(nodeRDN);

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Set Configuration Name");
        matrixActionsClient.setEndpointConfigurationName(matrixClientProperties.getName());

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Construct the componentFDN");
        matrixActionsClient.constructFDN(endpointProvider.getComponentFDN(),nodeRDN);

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Set the endpointType");
        matrixActionsClient.setEndpointType(PetasosEndpointTopologyTypeEnum.HTTP_API_CLIENT);

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Construct the functionFDN");
        matrixActionsClient.constructFunctionFDN(endpointProvider.getNodeFunctionFDN(),nodeRDN);

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Set the componentSystemRole");
        matrixActionsClient.setComponentSystemRole(SoftwareComponentSystemRoleEnum.COMPONENT_ROLE_INTERACT_EGRESS);

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Set the containingNodeFDN");
        matrixActionsClient.setContainingNodeFDN(endpointProvider.getComponentFDN());

        ConnectedSystemProperties connectedSystem = matrixClientProperties.getConnectedSystem();

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Set the connectedSystemName");
        matrixActionsClient.setConnectedSystemName(connectedSystem.getSubsystemName());

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Construct the Connected System details");
        ConnectedExternalSystemTopologyNode synapseSystem = new ConnectedExternalSystemTopologyNode();
        synapseSystem.setSubsystemName(connectedSystem.getSubsystemName());

        ConnectedSystemPort targetPort1 = connectedSystem.getTargetPort1();
        HTTPClientAdapter clientAdapter1 = getHTTPTopologyEndpointFactory().newHTTPClientAdapter(targetPort1);
        synapseSystem.getTargetPorts().add(clientAdapter1);

        if(connectedSystem.getTargetPort2() != null){
            ConnectedSystemPort targetPort2 = connectedSystem.getTargetPort2();
            HTTPClientAdapter clientAdapter2 = getHTTPTopologyEndpointFactory().newHTTPClientAdapter(targetPort2);
            synapseSystem.getTargetPorts().add(clientAdapter2);
        }

        if(connectedSystem.getTargetPort3() != null) {
            ConnectedSystemPort targetPort3 = connectedSystem.getTargetPort3();
            HTTPClientAdapter clientAdapter3 = getHTTPTopologyEndpointFactory().newHTTPClientAdapter(targetPort3);
            synapseSystem.getTargetPorts().add(clientAdapter3);
        }

        matrixActionsClient.setTargetSystem(synapseSystem);

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Set the componentType");
        matrixActionsClient.setComponentType(PegacornSystemComponentTypeTypeEnum.ENDPOINT);

        getLogger().trace(".addMatrixBridgeClientEndpoint(): Add the Endpoint to the endpointProvider");
        endpointProvider.addEndpoint(matrixActionsClient.getComponentFDN());

        getLogger().trace(".addMatrixActionsClient(): Add the MatrixApplicationServicesClientEndpoint Port to the Topology Cache");
        getTopologyIM().addTopologyNode(endpointProvider.getComponentFDN(), matrixActionsClient);
    }

    public void addSynapseAdminClientEndpoint( EndpointProviderInterface endpointProvider) {
        getLogger().debug(".addSynapseAdminClientEndpoint(): Entry");

        HTTPClientPortSegment interactEgressSynapseClientProperties = getMatrixBridgePropertyFile().getInteractEgressSynapseAPIClient();

        addMatrixBridgeClientEndpoint(endpointProvider, interactEgressSynapseClientProperties);

        getLogger().debug(".addSynapseAdminClientEndpoint(): Exit");
    }

    public void addMatrixQueryClient(EndpointProviderInterface endpointProvider) {
        getLogger().debug(".addMatrixQueryClient(): Entry");

        HTTPClientPortSegment interactEgressMatrixQueryProperties = getMatrixBridgePropertyFile().getInteractEgressMatrixQuery();

        addMatrixBridgeClientEndpoint(endpointProvider, interactEgressMatrixQueryProperties);

        getLogger().debug(".addMatrixQueryClient(): Exit");
    }


}
