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
import net.fhirfactory.pegacorn.communicate.synapse.model.SynapseUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

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
        // 1st, Establish Synapse Admin Login
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Synapse Login] Start...");
        if(StringUtils.isEmpty(getSynapseAccessToken().getSessionAccessToken())){
            getLogger().info(".topologyReplicationSynchronisationDaemon(): [Synapse Login] Session Token is Empty, Logging In...");
            getSynapseAdminProxy().executeLogin();
        } else {
            getLogger().error(".topologyReplicationSynchronisationDaemon(): [Synapse Login] Session Token was already populated...");
        }
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Synapse Login] Finish...");

        // 2nd, Override any rate-limiting for our synapse user
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Synapse User Rate Limit Override] Start...");
        synapseUserAPI.overrideRateLimit(synapseAccessToken.getUserId(), 0,0);
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Synapse User Rate Limit Override] Finish...");

        // 3rd, Register Application Service User (if not already registered) and/or login
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] Start...");
        if (StringUtils.isEmpty(getMatrixAccessToken().getSessionAccessToken())) {
            getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] Session Token is Empty, Logging In");
            getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] First Checking to see if User Exists");
            List<SynapseUser> usersByNameList = synapseUserAPI.getUsersByName(getMatrixApplicationServiceMethods().getMatrixApplicationServiceName());
            boolean isAlreadyRegistered = false;
            for(SynapseUser currentUser: usersByNameList){
                if(currentUser.getName().contains(getMatrixApplicationServiceMethods().getMatrixApplicationServiceName())){
                    getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] User ({}) Exists", currentUser.getName());
                    isAlreadyRegistered = true;
                }
            }
            if(!isAlreadyRegistered) {
                getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] User does not Exist, so Registering");
                MAPIResponse mapiResponse = getMatrixApplicationServiceMethods().registerApplicationService();
            } else {
                getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] User Exists, so Logging in");
                getMatrixApplicationServiceMethods().loginApplicationService();
            }
        }
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix Login] Finish...");

        // 4th, Override any rate-limiting for our matrix application service
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix User Rate Limit Override] Start...");
        synapseUserAPI.overrideRateLimit(matrixAccessToken.getUserId(), 0, 0);
        getLogger().info(".topologyReplicationSynchronisationDaemon(): [Matrix User Rate Limit Override] Finish...");

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
