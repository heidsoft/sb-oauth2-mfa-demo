package io.github.cepr0.authservice;

import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;

@Component
public class OAuth2AccessTokenGenerator {

    private static final String PASSWORD_GRANT_TYPE = "password";

    public final ClientDetailsService clientDetailsService;
    public final AuthorizationServerTokenServices tokenServices;

    public OAuth2AccessTokenGenerator(ClientDetailsService clientDetailsService, AuthorizationServerEndpointsConfiguration endpoints) {
        this.clientDetailsService = clientDetailsService;
        var configurer = endpoints.getEndpointsConfigurer();
        this.tokenServices = configurer.getTokenServices();
    }

    /**
     * Creates access and refresh tokens (as well as other related data, including claims if they exists)
     * for given user details and client ID.
     * <p>
     * Checks if the given client (by its ID) exists, is not locked, expired, disabled, or invalid for any other reason.
     *
     * @param userDetails User details, must not be null
     * @param clientId    ID of the client application, must not be null
     * @return {@link OAuth2AccessToken} with access token, refresh token and other related data.
     * @throws ClientRegistrationException If the client account is locked, expired, disabled, or invalid for any other reason.
     * @throws AuthenticationException     If the user credentials are inadequate.
     */
    public OAuth2AccessToken generate(
            @NonNull UserDetails userDetails,
            @NonNull String clientId
    ) throws ClientRegistrationException, AuthenticationException {
        BaseClientDetails client = (BaseClientDetails) clientDetailsService.loadClientByClientId(clientId);
        Set<String> scopes = client.getScope();
        var tokenRequest = new TokenRequest(new HashMap<>(), clientId, scopes, PASSWORD_GRANT_TYPE);
        var authToken = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        var authentication = new OAuth2Authentication(tokenRequest.createOAuth2Request(client), authToken);
        return tokenServices.createAccessToken(authentication);
    }
}