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

package tech.aroma.application.service.operations;

import junit.framework.AssertionFailedError;
import org.apache.thrift.TApplicationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.aroma.application.service.reactions.MessageReactor;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.thrift.functions.TokenFunctions;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.data.assertions.RequestAssertions.validMessageId;
import static tech.aroma.thrift.application.service.ApplicationServiceConstants.MAX_CHARACTERS_IN_BODY;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
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

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    private SendMessageOperation instance;

    private GetTokenInfoRequest expectedAuthenticationRequest;

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
        instance = new SendMessageOperation(authenticationService, messageReactor);

        verifyZeroInteractions(authenticationService, messageReactor);

        setupData();
        setupMocks();
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new SendMessageOperation(null, messageReactor));
        assertThrows(() -> new SendMessageOperation(authenticationService, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        SendMessageResponse result = instance.process(request);
        assertThat(result, notNullValue());

        checkThat(result.messageId)
            .throwing(AssertionFailedError.class)
            .is(validMessageId());

        verify(messageReactor).reactToMessage(messageCaptor.capture());

        Message message = messageCaptor.getValue();
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
    public void testWhenBodyExceedsLimit() throws Exception
    {
        String extraLongBody = one(alphabeticString(MAX_CHARACTERS_IN_BODY * 2));
        String truncatedBody = extraLongBody.substring(0, MAX_CHARACTERS_IN_BODY);

        request.setBody(extraLongBody);

        SendMessageResponse result = instance.process(request);
        assertThat(result, notNullValue());

        checkThat(result.messageId)
            .throwing(AssertionFailedError.class)
            .is(validMessageId());

        verify(messageReactor).reactToMessage(messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();

        assertThat(savedMessage.body, is(truncatedBody));
        assertThat(savedMessage.messageId, is(result.messageId));
    }

    private void setupMocks() throws Exception
    {
        when(messageReactor.reactToMessage(any()))
            .thenReturn(response);
        
        setupExpectedAuthRequest();
        setupAuthServiceResponse();
    }

    private void setupData()
    {
        appToken = request.applicationToken;
        appToken.applicationId = appId;
        app.applicationId = appId;
        response.messageId = messageId;
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
