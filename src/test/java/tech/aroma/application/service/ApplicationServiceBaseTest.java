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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import tech.aroma.thrift.AromaConstants;
import tech.aroma.thrift.application.service.SendMessageRequest;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.*;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class ApplicationServiceBaseTest
{

    @GeneratePojo
    private SendMessageRequest request;

    @GeneratePojo
    private SendMessageResponse response;

    @Mock
    private ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;

    private ApplicationServiceBase instance;

    @Mock
    private ExecutorService executor;

    @Captor
    private ArgumentCaptor<Callable> callableCaptor;

    @Before
    public void setUp() throws TException
    {
        instance = new ApplicationServiceBase(sendMessageOperation, executor);

        verifyZeroInteractions(sendMessageOperation);

        when(sendMessageOperation.process(request))
            .thenReturn(response);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new ApplicationServiceBase(null, executor))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new ApplicationServiceBase(sendMessageOperation, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetApiVersion() throws Exception
    {
        double result = instance.getApiVersion();

        assertThat(result, is(AromaConstants.API_VERSION));
    }

    @Test
    public void testSendMessage() throws Exception
    {
        SendMessageResponse result = instance.sendMessage(request);

        assertThat(result, is(response));
        verify(sendMessageOperation).process(request);
    }

    @DontRepeat
    @Test
    public void testSendMessageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.sendMessage(null))
            .isInstanceOf(InvalidArgumentException.class);

        verifyZeroInteractions(sendMessageOperation);
    }

    @Test
    public void testSendMessageAsync() throws Exception
    {
        instance.sendMessageAsync(request);

        verify(executor).submit(callableCaptor.capture());

        Callable callable = callableCaptor.getValue();
        assertThat(callable, notNullValue());

        //Actually run the callable
        callable.call();

        verify(sendMessageOperation).process(request);
    }

    @DontRepeat
    @Test
    public void testSendMessageAsyncWithBadArgs() throws Exception
    {
        //No exception expected, since it's an Async operation, just fail quietly.
        instance.sendMessageAsync(null);
    }

}
