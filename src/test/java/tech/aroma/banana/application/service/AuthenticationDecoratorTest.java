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

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.banana.thrift.application.service.ApplicationService;
import tech.aroma.banana.thrift.application.service.SendMessageRequest;
import tech.aroma.banana.thrift.application.service.SendMessageResponse;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.VerifyTokenRequest;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class AuthenticationDecoratorTest
{

    @Mock
    private AuthenticationService.Iface authenticationService;

    @Mock
    private ApplicationService.Iface delegate;

    @GeneratePojo
    private SendMessageRequest request;

    @GeneratePojo
    private SendMessageResponse response;

    private AuthenticationDecorator instance;

    @Before
    public void setUp() throws TException
    {
        instance = new AuthenticationDecorator(authenticationService, delegate);
        verifyZeroInteractions(authenticationService, delegate);

        when(delegate.sendMessage(request))
            .thenReturn(response);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new AuthenticationDecorator(null, delegate))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new AuthenticationDecorator(authenticationService, null))
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

        VerifyTokenRequest expectedRequest = new VerifyTokenRequest()
            .setTokenId(request.applicationToken.tokenId);

        verify(authenticationService).verifyToken(expectedRequest);
    }

    @Test
    public void testSendMessageWithInvalidToken() throws Exception
    {
        when(authenticationService.verifyToken(Mockito.any()))
            .thenThrow(new InvalidTokenException());

        assertThrows(() -> instance.sendMessage(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testSendMessageAsync() throws Exception
    {
        instance.sendMessageAsync(request);

        verify(delegate).sendMessageAsync(request);

        VerifyTokenRequest expectedRequest = new VerifyTokenRequest()
            .setTokenId(request.applicationToken.tokenId);

        verify(authenticationService).verifyToken(expectedRequest);

    }

    @Test
    public void testSendMessageAsyncWithInvalidToken() throws Exception
    {
        when(authenticationService.verifyToken(Mockito.any()))
            .thenThrow(new InvalidTokenException());

        assertThrows(() -> instance.sendMessageAsync(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

}
