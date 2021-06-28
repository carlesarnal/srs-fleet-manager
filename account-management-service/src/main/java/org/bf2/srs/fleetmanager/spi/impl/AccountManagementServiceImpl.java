package org.bf2.srs.fleetmanager.spi.impl;

import org.b2f.ams.client.AccountManagementSystemRestClient;
import org.b2f.ams.client.model.request.ClusterAuthorization;
import org.b2f.ams.client.model.request.TermsReview;
import org.b2f.ams.client.model.response.ClusterAuthorizationResponse;
import org.b2f.ams.client.model.response.ResponseTermsReview;
import org.bf2.srs.fleetmanager.spi.AccountManagementService;
import org.bf2.srs.fleetmanager.spi.model.AccountInfo;

/**
 * This service is in charge of check if a given user has the appropriate situation in order to ask for the requested resource
 */
public class AccountManagementServiceImpl implements AccountManagementService {

    private final AccountManagementSystemRestClient restClient;

    public AccountManagementServiceImpl(AccountManagementSystemRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String createResource(AccountInfo accountInfo, String resourceType, String clusterId, String productId) {

        final ClusterAuthorization clusterAuthorization = ClusterAuthorization.builder()
                .accountUsername(accountInfo.getAccountUsername())
                .clusterId(clusterId)
                .productId(productId)
                .reserve(true)
                .build();

        final ClusterAuthorizationResponse clusterAuthorizationResponse = restClient.clusterAuthorization(clusterAuthorization);

        return clusterAuthorizationResponse.getSubscriptionId();
    }

    @Override
    public boolean hasEntitlements(AccountInfo accountInfo, String resourceType, String clusterId) {

        boolean termsAccepted = true;
        final TermsReview termsReview = new TermsReview();
        termsReview.setAccountUsername(accountInfo.getAccountUsername());
        final ResponseTermsReview responseTermsReview = restClient.termsReview(termsReview);
        termsAccepted = !responseTermsReview.getTermsRequired();

        if (termsAccepted) {
            final ClusterAuthorization clusterAuthorization = ClusterAuthorization.builder()
                    .clusterId(clusterId)
                    .reserve(false)
                    .accountUsername(accountInfo.getAccountUsername())
                    .build();

            final ClusterAuthorizationResponse clusterAuthorizationResponse = restClient.clusterAuthorization(clusterAuthorization);
            if (clusterAuthorizationResponse.getExcessResources() == null || clusterAuthorizationResponse.getExcessResources().isEmpty()) {
                return clusterAuthorizationResponse.getAllowed();
            }
        }
        //TODO throw specific exception when terms not accepted
        return false;
    }
}
