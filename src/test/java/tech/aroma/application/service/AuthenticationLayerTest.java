/*
 * Copyright 2017 RedRoma, Inc.
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

package tech.aroma.application.service;

import java.util.function.Function;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import tech.aroma.thrift.application.service.*;
import tech.aroma.thrift.authentication.*;
import tech.aroma.thrift.authentication.service.*;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.functions.TokenFunctions;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class AuthenticationLayerTest
{

    @Mock
    private AuthenticationService.Iface authenticationService;

    @Mock
    private ApplicationService.Iface delegate;
    
    @Mock
    private Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @GeneratePojo
    private SendMessageRequest request;

    @GeneratePojo
    private SendMessageResponse response;
    
    @GeneratePojo
    private ApplicationToken appToken;
    
    private AuthenticationToken authToken;
    
    @GenerateString(UUID)
    private String appId;
    
    private String tokenId;
    
    private VerifyTokenRequest expectedVerifyRequest;
    
    private GetTokenInfoRequest expectedGetTokenRequest;

    private AuthenticationLayer instance;

    @Before
    public void setUp() throws TException
    {
        instance = new AuthenticationLayer(authenticationService, delegate, tokenMapper);
        verifyZeroInteractions(authenticationService, delegate);
        
        setupData();
        setupMocks();
    }

    private void setupData()
    {
        appToken.applicationId = appId;
        
        authToken = TokenFunctions.appTokenToAuthTokenFunction().apply(appToken);
        
        request.applicationToken = appToken;
        
        tokenId = appToken.tokenId;
        
        expectedVerifyRequest = new VerifyTokenRequest()
            .setTokenId(tokenId)
            .setOwnerId(appId);
        
        expectedGetTokenRequest = new GetTokenInfoRequest()
            .setTokenId(tokenId)
            .setTokenType(TokenType.APPLICATION);
    }
    
    private void setupMocks() throws TException
    {
        when(delegate.sendMessage(request)).thenReturn(response);
        when(tokenMapper.apply(authToken)).thenReturn(appToken);
        when(authenticationService.getTokenInfo(expectedGetTokenRequest)).thenReturn(new GetTokenInfoResponse(authToken));
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new AuthenticationLayer(null, delegate, tokenMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AuthenticationLayer(authenticationService, null, tokenMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AuthenticationLayer(authenticationService, delegate, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetApiVersion() throws Exception
    {
        double expected = one(doubles(10, 1000));
        when(delegate.getApiVersion())
            .thenReturn(expected);

        double result = instance.getApiVersion();
        assertThat(result, is(expected));
        verify(delegate).getApiVersion();

    }

    @Test
    public void testGetApiVersionWhenFails() throws Exception
    {
        when(delegate.getApiVersion())
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.getApiVersion())
            .isInstanceOf(OperationFailedException.class);

    }

    @Test
    public void testSendMessage() throws Exception
    {
        SendMessageResponse result = instance.sendMessage(request);

        assertThat(result, is(response));
        verify(delegate).sendMessage(request);

        verify(authenticationService).verifyToken(expectedVerifyRequest);
    }
    
    @Test
    public void testSendMessageWhenAppIdMissing() throws Exception
    {
        ApplicationToken completeToken = new ApplicationToken(appToken);
        
        appToken.unsetApplicationId();
        expectedVerifyRequest.unsetOwnerId();
        
        when(tokenMapper.apply(authToken)).thenReturn(completeToken);
        
        
        SendMessageResponse result = instance.sendMessage(request);
        
        assertThat(result, is(response));
        
        verify(authenticationService).verifyToken(expectedVerifyRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenRequest);
        
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(delegate).sendMessage(captor.capture());

        ApplicationToken expectedToken = new ApplicationToken(appToken).setApplicationId(appId);
        SendMessageRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.applicationToken, is(expectedToken));
    }

    @Test
    public void testSendMessageWithInvalidToken() throws Exception
    {
        setupForBadToken();
        
        assertThrows(() -> instance.sendMessage(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testSendMessageAsync() throws Exception
    {
        instance.sendMessageAsync(request);

        verify(delegate).sendMessageAsync(request);

        verify(authenticationService).verifyToken(expectedVerifyRequest);
    }

    @Test
    public void testSendMessageAsyncWithInvalidToken() throws Exception
    {
        setupForBadToken();

        assertThrows(() -> instance.sendMessageAsync(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    private void setupForBadToken() throws InvalidTokenException, TException
    {
        when(authenticationService.verifyToken(expectedVerifyRequest))
            .thenThrow(new InvalidTokenException());
    }

}
