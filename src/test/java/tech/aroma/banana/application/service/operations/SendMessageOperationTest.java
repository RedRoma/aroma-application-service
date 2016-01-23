/*
 * Copyright 2016 Aroma Tech.
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

package tech.aroma.banana.application.service.operations;

import junit.framework.AssertionFailedError;
import org.apache.thrift.TApplicationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.banana.thrift.functions.TokenFunctions;
import tech.aroma.banana.thrift.notification.service.NotificationService;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.aroma.banana.data.assertions.RequestAssertions.validMessageId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;


/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class SendMessageOperationTest 
{
    @Mock
    private AuthenticationService.Iface authenticationService;
    
    @Mock
    private MessageRepository messageRepository;
    
    @Mock
    private NotificationService.Iface notificationService;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    private SendMessageOperation instance;
    
    private GetTokenInfoRequest expectedAuthenticationRequest;
    
    private ApplicationToken appToken;
    
    
    @GeneratePojo
    private SendMessageRequest request;
    
    @Before
    public void setUp() throws Exception
    {
        instance = new SendMessageOperation(authenticationService, messageRepository, notificationService);
        
        appToken = request.applicationToken;
        
        setupExpectedAuthRequest();
        setupAuthServiceResponse();
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new SendMessageOperation(null, messageRepository, notificationService))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new SendMessageOperation(authenticationService, null, notificationService))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new SendMessageOperation(authenticationService, messageRepository, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
        SendMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        checkThat(response.messageId)
            .throwing(AssertionFailedError.class)
            .is(validMessageId());
        
        verify(messageRepository).saveMessage(messageCaptor.capture(), Mockito.any());
        
        Message message = messageCaptor.getValue();
        assertThat(message, notNullValue());
        assertThat(message.messageId, is(response.messageId));
        assertThat(message.body, is(request.message));
        assertThat(message.urgency, is(request.urgency));
        assertThat(message.timeOfCreation, is(request.timeOfMessage));
    }
    
    @Test
    public void testProcessWhenAuthenticationFails() throws Exception
    {
        when(authenticationService.getTokenInfo(expectedAuthenticationRequest))
            .thenThrow(new TApplicationException());
    }

    private void setupExpectedAuthRequest()
    {
        expectedAuthenticationRequest = new GetTokenInfoRequest()
            .setTokenId(appToken.tokenId)
            .setTokenType(TokenType.APPLICATION);

    }

    private void setupAuthServiceResponse() throws Exception
    {
        AuthenticationToken authToken = TokenFunctions.appTokenToAuthTokenFunction().apply(appToken);

        GetTokenInfoResponse response = new GetTokenInfoResponse()
            .setToken(authToken);
        
        when(authenticationService.getTokenInfo(expectedAuthenticationRequest))
            .thenReturn(response);
    }

}