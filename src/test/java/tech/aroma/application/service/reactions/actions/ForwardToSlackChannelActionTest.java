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

package tech.aroma.application.service.reactions.actions;

import java.net.URL;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.reactions.ActionForwardToSlackChannel;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.http.HttpResponse;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.mock.AlchemyHttpMock;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class ForwardToSlackChannelActionTest
{

    @Mock
    private AlchemyHttp http;

    @GeneratePojo
    private ActionForwardToSlackChannel slack;
    
    @GenerateURL
    private URL webhookUrl;
    
    private Message message;

    private ForwardToSlackChannelAction instance;
    
    @Mock
    private HttpResponse response;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new ForwardToSlackChannelAction(slack, http);
    }

    private void setupData() throws Exception
    {
        slack.webhookUrl = webhookUrl.toString();
        message = one(messages());
    }

    private void setupMocks() throws Exception
    {
        http = AlchemyHttpMock.begin()
            .whenPost()
            .anyBody()
            .at(webhookUrl)
            .thenReturnResponse(response)
            .build();
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new ForwardToSlackChannelAction(null, http));
        assertThrows(() -> new ForwardToSlackChannelAction(slack, null));
    }

    @Test
    public void testActOnMessage() throws Exception
    {
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, notNullValue());
        assertThat(actions, is(empty()));
        
        AlchemyHttpMock.verifyAllRequestsMade(http);
    }
    
    @Test
    public void testWhenHttpCallFails() throws Exception
    {
        http = AlchemyHttpMock.begin()
            .whenPost()
            .anyBody()
            .at(webhookUrl)
            .thenThrow(new AlchemyHttpException())
            .build();
        
        instance = new ForwardToSlackChannelAction(slack, http);
        
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, notNullValue());
        assertThat(actions, is(empty()));
    }

    @DontRepeat
    @Test
    public void testWithBadArgs()
    {
        assertThrows(() -> instance.actOnMessage(null))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testToString()
    {
        assertThat(instance.toString(), not(isEmptyOrNullString()));
    }

}
