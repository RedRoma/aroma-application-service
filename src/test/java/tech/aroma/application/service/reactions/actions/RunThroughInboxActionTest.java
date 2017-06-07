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

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.reactions.*;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.aroma.thrift.generators.UserGenerators.users;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class RunThroughInboxActionTest
{
    
    @Mock
    private ActionFactory actionFactory;
    @Mock
    private MatchAlgorithm matchAlgorithm;
    @Mock
    private ReactionRepository reactionRepo;
    
    @Mock
    private Action genericAction;
    
    @Mock
    private Action actionToStoreInInbox;
    
    @Mock
    private Action actionToSendPushNotification;
    
    private Message message;
    private User user;
    private Reaction reaction;
    private Reaction reactionThatSkipInbox;
    private Reaction reactionThatSkipPush;
    private Reaction reactionThatDontStoreMessage;
    
    private RunThroughInboxAction instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new RunThroughInboxAction(actionFactory, matchAlgorithm, reactionRepo, user);
        verifyZeroInteractions(actionFactory, matchAlgorithm, reactionRepo);
    }
    
    private void setupData() throws Exception
    {
        message = one(messages());
        user = one(users());
        
        AlchemyGenerator<Reaction> reactions = reactions();
        reaction = one(reactions);
        reactionThatSkipInbox = one(reactions);
        reactionThatDontStoreMessage = one(reactions);
        reactionThatSkipPush = one(reactions);
        
        reaction.actions = reaction.actions
            .stream()
            .filter(action -> !action.isSetSkipInbox())
            .filter(action -> !action.isSetDontStoreMessage())
            .filter(action -> !action.isSetDontSendPushNotification())
            .filter(action -> !action.isSetSendPushNotification())
            .collect(toList());
        
        reactionThatDontStoreMessage.actions = Lists.copy(reaction.actions);
        AromaAction actionToNotStore = new AromaAction();
        actionToNotStore.setDontStoreMessage(new ActionDontStoreMessage());
        reactionThatDontStoreMessage.actions.add(actionToNotStore);
        
        reactionThatSkipInbox.actions = Lists.copy(reaction.actions);
        AromaAction actionToSkipInbox = new AromaAction();
        actionToSkipInbox.setSkipInbox(new ActionSkipInbox());
        reactionThatSkipInbox.actions.add(actionToSkipInbox);
        
        reactionThatSkipPush.actions = Lists.copy(reaction.actions);
        AromaAction actionToSkipPush = new AromaAction();
        actionToSkipPush.setDontSendPushNotification(new ActionDontSendPushNotification());
        reactionThatSkipPush.addToActions(actionToSkipPush);
    }
    
    private void setupMocks() throws Exception
    {
        when(reactionRepo.getReactionsForUser(user.userId))
            .thenReturn(Lists.createFrom(reaction));
        
        when(matchAlgorithm.matches(eq(message), any()))
            .thenReturn(true);
        
        when(actionFactory.actionFor(any()))
            .thenReturn(genericAction);
        
        when(actionFactory.actionToStoreInInbox(user))
            .thenReturn(actionToStoreInInbox);
        
        when(actionFactory.actionToSendPushNotification(user.userId))
            .thenReturn(actionToSendPushNotification);
    }
    
    @Test
    public void testActOnMessage() throws Exception
    {
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, notNullValue());
        assertThat(actions, hasItems(genericAction, actionToStoreInInbox));
        
        reaction.actions.forEach(action -> verify(actionFactory).actionFor(action));
        
    }
    
    @Test
    public void testWhenDontStoreActionIncluded() throws Exception
    {
        when(reactionRepo.getReactionsForUser(user.userId))
            .thenReturn(Lists.createFrom(reactionThatDontStoreMessage));
        
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, not(hasItems(actionToStoreInInbox)));
        assertThat(actions, hasItem(genericAction));
        
        verify(actionFactory, never()).actionToStoreInInbox(any());
    }
    
    @Test
    public void testWhenSkipInboxActionIncluded() throws Exception
    {
        when(reactionRepo.getReactionsForUser(user.userId))
            .thenReturn(Lists.createFrom(reactionThatSkipInbox));
        
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, hasItem(genericAction));
        assertThat(actions, not(hasItem(actionToStoreInInbox)));
    }
    
    @Test
    public void testWhenSkipPushNotificationIncluded() throws Exception
    {
        when(reactionRepo.getReactionsForUser(user.userId))
            .thenReturn(Lists.createFrom(reactionThatSkipPush));
        
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, hasItem(genericAction));
        assertThat(actions, not(hasItem(actionToSendPushNotification)));
        
        verify(actionFactory, never()).actionToSendPushNotification(anyString());
    }
    
    @Test
    public void testWithBadArgs() throws Exception
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
