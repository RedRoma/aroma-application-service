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

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.notification.service.NotificationService;
import tech.aroma.thrift.notification.service.SendNotificationRequest;
import tech.aroma.thrift.notification.service.SendNotificationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.equalTo;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class SendNotificationActionTest
{
    
    @Mock
    private NotificationService.Iface notificationService;
    
    private Message message;
    
    private SendNotificationAction instance;
    
    @Captor
    private ArgumentCaptor<SendNotificationRequest> captor;
    
    @GeneratePojo
    private SendNotificationResponse response;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
        
        instance = new SendNotificationAction(notificationService);
        verifyZeroInteractions(notificationService);
    }

    private void setupData() throws Exception
    {

        message = one(messages());
    }

    private void setupMocks() throws Exception
    {

        when(notificationService.sendNotification(any()))
            .thenReturn(response);
    }

    @Test
    @DontRepeat
    public void testConstructor()
    {
        assertThrows(() -> new SendNotificationAction(null));
    }
    
    @Test
    public void testActOnMessage() throws Exception
    {
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, notNullValue());
        assertThat(actions, is(empty()));
        
        verify(notificationService).sendNotification(captor.capture());
        
        SendNotificationRequest request = captor.getValue();
        checkThat(request).is(notNull());
        checkThat(request.event).is(notNull());
        
        checkThat(request.event.applicationId)
            .is(equalTo(message.applicationId));
    }

    @Test
    public void testToString()
    {
        assertThat(instance.toString(), not(isEmptyOrNullString()));
    }

}
