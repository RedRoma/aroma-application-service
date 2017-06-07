/*
 * Copyright 2017 RedRoma.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.reactions.ActionForwardToGitter;
import tech.sirwellington.alchemy.http.*;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;
import tech.sirwellington.alchemy.http.mock.AlchemyHttpMock;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticStrings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class ForwardToGitterActionTest 
{

    private AlchemyHttp http;
    
    private ActionForwardToGitter gitter;
    
    @Mock
    private HttpRequest request;
    
    @Mock
    private HttpResponse response;
    
    @GenerateURL
    private URL url;
    
    private Message message;
    
    private ForwardToGitterAction instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new ForwardToGitterAction(http, gitter);
    }


    private void setupData() throws Exception
    {
        gitter = new ActionForwardToGitter()
            .setGitterWebhookUrl(url.toString());
        
        message = one(messages());
    }

    private void setupMocks() throws Exception
    {
        http = AlchemyHttpMock.begin()
            .whenPost()
            .anyBody()
            .at(url)
            .thenReturnResponse(response)
            .build();
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new ForwardToGitterAction(null, gitter));
        assertThrows(() -> new ForwardToGitterAction(http, null));
        
        assertThrows(() -> new ForwardToGitterAction(http, new ActionForwardToGitter()));
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
    public void testWhenHttpCallFail() throws Exception
    {
        http = AlchemyHttpMock.begin()
            .whenPost()
            .anyBody()
            .at(url)
            .thenThrow(new AlchemyHttpException())
            .build();
        
        instance = new ForwardToGitterAction(http, gitter);
        
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, notNullValue());
        AlchemyHttpMock.verifyAllRequestsMade(http);
    }
    
    @Test
    public void testWithBadUrl()
    {
        String badUrl = one(alphabeticStrings());
        gitter.gitterWebhookUrl = badUrl;
        
        assertThrows(() -> instance.actOnMessage(message))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testWithBadArgs()
    {
        assertThrows(() -> instance.actOnMessage(null));
        
        String badId = one(alphabeticStrings());
        message.messageId = badId;
        assertThrows(() -> instance.actOnMessage(message));
    }

    @Repeat(10)
    @Test
    public void testToString()
    {
        assertThat(instance.toString(), not(isEmptyOrNullString()));
    }

}