/*
 * Copyright 2015 Aroma Tech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.banana.application.service;

import decorice.DecoratedBy;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.banana.thrift.authentication.service.VerifyTokenRequest;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;

import static tech.aroma.banana.data.assertions.AuthenticationAssertions.completeToken;
import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.CONCRETE_DECORATOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * This class Decorates an existing Application Service, providing Authentication of incoming requests against an
 * {@linkplain AuthenticationService.Iface Authentication Service}. It also enriches request
 * to make sure that the Application ID is contained in the Token.
 *
 * @author SirWellington
 */
@Internal
@DecoratorPattern(role = CONCRETE_DECORATOR)
final class AuthenticationLayer implements ApplicationService.Iface
{

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationLayer.class);

    private final AuthenticationService.Iface authenticationService;
    private final ApplicationService.Iface delegate;
    private final Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @Inject
    AuthenticationLayer(AuthenticationService.Iface authenticationService,
                        @DecoratedBy(AuthenticationLayer.class) ApplicationService.Iface delegate,
                        Function<AuthenticationToken, ApplicationToken> tokenMapper)
    {
        checkThat(delegate, authenticationService, tokenMapper)
            .are(notNull());

        this.authenticationService = authenticationService;
        this.delegate = delegate;
        this.tokenMapper = tokenMapper;
    }

    @Override
    public double getApiVersion() throws TException
    {
        return delegate.getApiVersion();
    }

    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              TException
    {
        checkThat(request).is(notNull());
        checkTokenIsValid(request.applicationToken);

        if(!request.applicationToken.isSetApplicationId())
        {
            ApplicationToken newToken = getAdditionalTokenInfo(request.applicationToken);
            request.setApplicationToken(newToken);
        }
        
        return delegate.sendMessage(request);
    }

    @Override
    public void sendMessageAsync(SendMessageRequest request) throws TException
    {
        checkThat(request).is(notNull());
        checkTokenIsValid(request.applicationToken);

        delegate.sendMessageAsync(request);
    }

    private ApplicationToken getAdditionalTokenInfo(ApplicationToken applicationToken) throws TException
    {
        GetTokenInfoRequest request = new GetTokenInfoRequest()
            .setTokenId(applicationToken.tokenId)
            .setTokenType(TokenType.APPLICATION);
        
        GetTokenInfoResponse response = tryToGetTokenInfo(request);
        
        checkThat(response)
            .usingMessage("Auth Service returned null response")
            .throwing(OperationFailedException.class)
            .is(notNull());
            
        checkThat(response.token)
            .usingMessage("Auth Service returned incomplete token")
            .throwing(OperationFailedException.class)
            .is(completeToken());

        ApplicationToken newAppToken = convertToAppToken(response.token);
        return newAppToken;
    }

    private GetTokenInfoResponse tryToGetTokenInfo(GetTokenInfoRequest request) throws OperationFailedException
    {
        try
        {
            return authenticationService.getTokenInfo(request);
        }
        catch(TException ex)
        {
            LOG.error("Failed to get Additional token info for: {}", request);
            throw new OperationFailedException("Could not get token infO: " + ex.getMessage());
        }
    }

    private ApplicationToken convertToAppToken(AuthenticationToken token)
    {
        return tokenMapper.apply(token);
    }
    
    private void checkTokenIsValid(ApplicationToken token) throws TException
    {
        checkThat(token)
            .throwing(InvalidTokenException.class)
            .usingMessage("Request missing token")
            .is(notNull());
        
        checkThat(token.tokenId)
            .throwing(InvalidTokenException.class)
            .usingMessage("Request missing tokenId")
            .is(nonEmptyString());
        
        VerifyTokenRequest request = new VerifyTokenRequest()
            .setTokenId(token.tokenId)
            .setOwnerId(token.applicationId);
        
        try
        {
            authenticationService.verifyToken(request);
        }
        catch (TException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            LOG.error("Authentication Service call failed", ex);
            throw new OperationFailedException("Could not reach Authentication Service: " + ex.getMessage());
        }
    }


}
