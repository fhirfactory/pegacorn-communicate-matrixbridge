/*
 * Copyright (c) 2021 Mark A. Hunter
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
package net.fhirfactory.pegacorn.communicate.matrixbridge.workshops.matrixbridge.common;

import net.fhirfactory.pegacorn.communicate.matrix.credentials.MatrixAccessToken;
import net.fhirfactory.pegacorn.communicate.matrix.methods.MatrixApplicationServiceMethods;
import net.fhirfactory.pegacorn.communicate.matrix.model.r110.api.common.MAPIResponse;
import net.fhirfactory.pegacorn.communicate.synapse.credentials.SynapseAdminAccessToken;
import net.fhirfactory.pegacorn.communicate.synapse.methods.SynapseUserMethods;
import net.fhirfactory.pegacorn.communicate.synapse.model.SynapseAdminProxyInterface;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SynapseServerConnectionInitialisation {
    private static final Logger LOG = LoggerFactory.getLogger(SynapseServerConnectionInitialisation.class);

    @Inject
    private MatrixAccessToken matrixAccessToken;

    @Inject
    private SynapseAdminAccessToken synapseAccessToken;

    @Inject
    private MatrixApplicationServiceMethods matrixApplicationServiceMethods;

    @Inject
    private SynapseAdminProxyInterface synapseAdminProxy;

    @Inject
    private SynapseUserMethods synapseUserAPI ;


    //
    // Business Methods
    //

    public void initialiseConnection(){
        getLogger().debug(".initialiseConnection(): Entry");
        // 1st, Always check we have an access token
        if (StringUtils.isEmpty(getMatrixAccessToken().getSessionAccessToken())) {
            getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] Start...");
            MAPIResponse mapiResponse = getMatrixApplicationServiceMethods().registerApplicationService();
            if(mapiResponse.getResponseCode() != 200){
                getMatrixApplicationServiceMethods().loginApplicationService();
            }
//            getMatrixApplicationServiceMethods().setApplicationServiceDisplayName("ITOps-Agent");
//            getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] Finish...");
        }
        if(StringUtils.isEmpty(getSynapseAccessToken().getSessionAccessToken())){
            getLogger().info(".topologyReplicationSynchronisationDaemon(): [Synapse Login] Start...");
            getSynapseAdminProxy().executeLogin();
            getLogger().info(".topologyReplicationSynchronisationDaemon(): [Synapse Login] Start...");
        }
        synapseUserAPI.overrideRateLimit(synapseAccessToken.getUserId(), 0,0);
        synapseUserAPI.overrideRateLimit(matrixAccessToken.getUserId(), 0, 0);
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] Finish...");
        getLogger().debug(".initialiseConnection(): Exit");
    }

    public boolean isConnectionEstablished(){
        getLogger().debug(".isConnectionEstablished(): Entry");

        if (StringUtils.isEmpty(getMatrixAccessToken().getSessionAccessToken())) {
            getLogger().debug(".isConnectionEstablished(): Exit, matrixAccessToken is empty, returning -false-");
            return(false);
        }
        if(StringUtils.isEmpty(getSynapseAccessToken().getSessionAccessToken())){
            getLogger().debug(".isConnectionEstablished(): Exit, synapseAccessToken is empty, returning -false-");
            return(false);
        }
        getLogger().debug(".isConnectionEstablished(): Exit, both synapseAccessToken and matrixAccessToken are set, returning -true-");
        return(true);
    }

    //
    // Getters (and Setters)
    //

    protected MatrixAccessToken getMatrixAccessToken(){
        return(matrixAccessToken);
    }

    protected SynapseAdminAccessToken getSynapseAccessToken(){
        return(synapseAccessToken);
    }

    protected Logger getLogger(){
        return(LOG);
    }

    protected MatrixApplicationServiceMethods getMatrixApplicationServiceMethods(){
        return(matrixApplicationServiceMethods);
    }

    protected SynapseAdminProxyInterface getSynapseAdminProxy(){
        return(synapseAdminProxy);
    }
}
