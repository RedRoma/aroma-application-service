/*
 * Copyright 2016 RedRoma.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.InboxRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.notification.service.NotificationService;
import tech.aroma.thrift.reactions.ActionForwardToSlackChannel;
import tech.aroma.thrift.reactions.ActionForwardToSlackUser;
import tech.aroma.thrift.reactions.ActionSendEmail;
import tech.aroma.thrift.reactions.AromaAction;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.aroma.thrift.generators.ReactionGenerators.actions;
import static tech.aroma.thrift.generators.UserGenerators.users;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class ActionFactoryImplTest
{

    @Mock
    private NotificationService.Iface notificationService;
    
    @Mock
    private FollowerRepository followerRepo;
    
    @Mock
    private InboxRepository inboxRepo;
   
    @Mock
    private MatchAlgorithm matchAlgorithm;
  
    @Mock
    private MessageRepository messageRepo;
   
    @Mock
    private ReactionRepository reactionRepo;
   
    @Mock
    private AlchemyHttp http;

    private AromaAction action;

    private ActionFactoryImpl instance;
    
    private Message message;
    private User user;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
        
        instance = new ActionFactoryImpl(notificationService, followerRepo, inboxRepo, matchAlgorithm, messageRepo, reactionRepo, http);
        verifyZeroInteractions(notificationService, followerRepo, inboxRepo, matchAlgorithm, messageRepo, reactionRepo, http);
    }

    private void setupData() throws Exception
    {

        action = one(actions());
        message = one(messages());
        user = one(users());
    }

    private void setupMocks() throws Exception
    {

    }
    
    @DontRepeat
    @Test
    public void testConstructor() 
    {
        assertThrows(() -> new ActionFactoryImpl(null, followerRepo, inboxRepo, matchAlgorithm, messageRepo, reactionRepo, http));
        assertThrows(() -> new ActionFactoryImpl(notificationService, null, inboxRepo, matchAlgorithm, messageRepo, reactionRepo, http));
        assertThrows(() -> new ActionFactoryImpl(notificationService, followerRepo, null, matchAlgorithm, messageRepo, reactionRepo, http));
        assertThrows(() -> new ActionFactoryImpl(notificationService, followerRepo, inboxRepo, null, messageRepo, reactionRepo, http));
        assertThrows(() -> new ActionFactoryImpl(notificationService, followerRepo, inboxRepo, matchAlgorithm, null, reactionRepo, http));
        assertThrows(() -> new ActionFactoryImpl(notificationService, followerRepo, inboxRepo, matchAlgorithm, messageRepo, null, http));
        assertThrows(() -> new ActionFactoryImpl(notificationService, followerRepo, inboxRepo, matchAlgorithm, messageRepo, reactionRepo, null));
    }

    @Test
    public void testActionFor()
    {
        Action result = instance.actionFor(action);
        checkAction(result);
    }

    @Test
    public void testActionToDoNothing()
    {
        Action result = instance.actionToDoNothing();
        checkAction(result);
    }

    @Test
    public void testActionToSendToSlackChannel()
    {
        ActionForwardToSlackChannel slack = one(pojos(ActionForwardToSlackChannel.class));
        Action result = instance.actionToSendToSlackChannel(slack);
        checkAction(result);
    }

    @Test
    public void testActionToSendToSlackUser()
    {
        ActionForwardToSlackUser slack = one(pojos(ActionForwardToSlackUser.class));
        Action result = instance.actionToSendToSlackUser(slack);
        checkAction(result);
    }

    @Test
    public void testActionToSendNotifications()
    {
        Action result = instance.actionToSendNotifications(message);
        checkAction(result);
    }

    @Test
    public void testActionToRunThroughFollowerInboxes()
    {
        Action result = instance.actionToRunThroughFollowerInboxes(message);
        checkAction(result);
    }

    @Test
    public void testActionToRunThroughInbox()
    {
        Action result = instance.actionToRunThroughInbox(user);
        checkAction(result);
    }

    @Test
    public void testActionToStoreMessage()
    {
        Action result = instance.actionToStoreMessage(message);
        checkAction(result);
    }

    @Test
    public void testActionToStoreInInbox()
    {
        Action result = instance.actionToStoreMessage(message);
        checkAction(result);
    }

    @Test
    public void testActionToSendEmail()
    {
        ActionSendEmail sendEmail = one(pojos(ActionSendEmail.class));
        Action result = instance.actionToSendEmail(sendEmail);
        checkAction(result);
    }

    private void checkAction(Action result)
    {
        assertThat(result, notNullValue());
    }

}