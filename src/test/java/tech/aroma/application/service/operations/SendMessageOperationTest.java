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

package tech.aroma.application.service.operations;

import java.util.function.Function;

import junit.framework.AssertionFailedError;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import tech.aroma.application.service.reactions.MessageReactor;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.authentication.*;
import tech.aroma.thrift.authentication.service.*;
import tech.aroma.thrift.functions.TokenFunctions;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validMessageId;
import static tech.aroma.thrift.application.service.ApplicationServiceConstants.MAX_CHARACTERS_IN_BODY;
import static tech.aroma.thrift.application.service.ApplicationServiceConstants.MAX_TITLE_LENGTH;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

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
    private MessageReactor messageReactor;

    @Mock
    private Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @Captor
    private ArgumentCaptor<Message> captor;

    private SendMessageOperation instance;

    private GetTokenInfoRequest expectedAuthenticationRequest;

    private AuthenticationToken authToken;

    private ApplicationToken appToken;

    @GeneratePojo
    private Application app;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String messageId;

    @GeneratePojo
    private SendMessageRequest request;

    @GeneratePojo
    private SendMessageResponse response;

    @Before
    public void setUp() throws Exception
    {
        instance = new SendMessageOperation(authenticationService, messageReactor, tokenMapper);

        verifyZeroInteractions(authenticationService, messageReactor);

        setupData();
        setupMocks();
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new SendMessageOperation(null, messageReactor, tokenMapper));
        assertThrows(() -> new SendMessageOperation(authenticationService, null, tokenMapper));
        assertThrows(() -> new SendMessageOperation(authenticationService, messageReactor, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        SendMessageResponse result = instance.process(request);
        assertThat(result, notNullValue());

        checkThat(result.messageId)
            .throwing(AssertionFailedError.class)
            .is(validMessageId());

        verify(messageReactor).reactToMessage(captor.capture());

        Message message = captor.getValue();
        assertThat(message, notNullValue());
        assertThat(message.messageId, is(result.messageId));
        assertThat(message.body, is(request.body));
        assertThat(message.title, is(request.title));
        assertThat(message.urgency, is(request.urgency));
        assertThat(message.macAddress, is(request.macAddress));
        assertThat(message.hostname, is(request.hostname));
        assertThat(message.timeOfCreation, is(request.timeOfMessage));

    }

    @Test
    public void testProcessWhenAuthenticationFails() throws Exception
    {
        when(authenticationService.getTokenInfo(expectedAuthenticationRequest))
            .thenThrow(new TApplicationException());
    }
    
    @Test
    public void testWhenTitleExceedsLimit() throws Exception
    {
        String extraLongTitle = one(alphabeticStrings(MAX_TITLE_LENGTH * 2));
        String truncatedTitle = extraLongTitle.substring(0, MAX_TITLE_LENGTH);
        
        request.setTitle(extraLongTitle);
        
        SendMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        checkThat(response.messageId).is(validApplicationId());
        
        verify(messageReactor).reactToMessage(captor.capture());
        Message savedMessage = captor.getValue();
        
        assertThat(savedMessage, notNullValue());
        assertThat(savedMessage.title, is(truncatedTitle));
        assertThat(savedMessage.messageId, is(response.messageId));
    }

    @Test
    public void testWhenBodyExceedsLimit() throws Exception
    {
        String extraLongBody = one(alphabeticStrings(MAX_CHARACTERS_IN_BODY * 2));
        String truncatedBody = extraLongBody.substring(0, MAX_CHARACTERS_IN_BODY);

        request.setBody(extraLongBody);

        SendMessageResponse result = instance.process(request);
        assertThat(result, notNullValue());

        checkThat(result.messageId)
            .throwing(AssertionFailedError.class)
            .is(validMessageId());

        verify(messageReactor).reactToMessage(captor.capture());

        Message savedMessage = captor.getValue();

        assertThat(savedMessage.body, is(truncatedBody));
        assertThat(savedMessage.messageId, is(result.messageId));
    }
    
    @DontRepeat
    @Test
    public void testWhenTokenMapperFails() throws Exception
    {
        when(tokenMapper.apply(authToken)).thenThrow(new RuntimeException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(TException.class);
    }
    
    @DontRepeat
    @Test
    public void testWhenTokenMapperReturnsNull() throws Exception
    {
        when(tokenMapper.apply(authToken)).thenReturn(null);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(TException.class);
    }

    private void setupData()
    {
        appToken = request.applicationToken;
        appToken.applicationId = appId;

        authToken = TokenFunctions.appTokenToAuthTokenFunction().apply(appToken);

        app.applicationId = appId;
        response.messageId = messageId;
    }

    private void setupMocks() throws Exception
    {
        when(messageReactor.reactToMessage(any())).thenReturn(response);
        when(tokenMapper.apply(authToken)).thenReturn(appToken);

        setupExpectedAuthRequest();
        setupAuthServiceResponse();

    }

    private void setupExpectedAuthRequest()
    {
        expectedAuthenticationRequest = new GetTokenInfoRequest()
            .setTokenId(appToken.tokenId)
            .setTokenType(TokenType.APPLICATION);

    }

    private void setupAuthServiceResponse() throws Exception
    {

        GetTokenInfoResponse response = new GetTokenInfoResponse()
            .setToken(authToken);

        when(authenticationService.getTokenInfo(expectedAuthenticationRequest))
            .thenReturn(response);
    }

}
