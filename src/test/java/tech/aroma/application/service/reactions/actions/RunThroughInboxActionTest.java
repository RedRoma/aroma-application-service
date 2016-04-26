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
import tech.aroma.thrift.reactions.ActionDontStoreMessage;
import tech.aroma.thrift.reactions.ActionSkipInbox;
import tech.aroma.thrift.reactions.AromaAction;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.aroma.thrift.generators.UserGenerators.users;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

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
    
    private Message message;
    private User user;
    private Reaction reaction;
    private Reaction reactionThatSkipInbox;
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
        reaction = one(reactions());
        reactionThatSkipInbox = one(reactions());
        reactionThatDontStoreMessage = one(reactions());
        
        reaction.actions = reaction.actions
            .stream()
            .filter(action -> !action.isSetSkipInbox())
            .filter(action -> !action.isSetDontStoreMessage())
            .collect(toList());
        
        reactionThatDontStoreMessage.actions = Lists.copy(reaction.actions);
        AromaAction actionToNotStore = new AromaAction();
        actionToNotStore.setDontStoreMessage(new ActionDontStoreMessage());
        reactionThatDontStoreMessage.actions.add(actionToNotStore);
        
        reactionThatSkipInbox.actions = Lists.copy(reaction.actions);
        AromaAction actionToSkipInbox = new AromaAction();
        actionToSkipInbox.setSkipInbox(new ActionSkipInbox());
        reactionThatSkipInbox.actions.add(actionToSkipInbox);
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
