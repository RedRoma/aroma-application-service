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

package tech.aroma.application.service.reactions;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.application.service.reactions.actions.Action;
import tech.aroma.application.service.reactions.actions.ActionFactory;
import tech.aroma.application.service.reactions.actions.ActionRunner;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.reactions.ActionDontStoreMessage;
import tech.aroma.thrift.reactions.ActionSkipInbox;
import tech.aroma.thrift.reactions.AromaAction;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class MessageReactorImplTest
{

    @Mock
    private Action genericAction;
    
    @Mock
    private Action actionRunThroughInboxes;

    @Mock
    private Action actionToStore;
    
    @Mock
    private ActionRunner actionRunner;

    @Mock
    private ActionFactory actionFactory;

    @Mock
    private MatchAlgorithm matchAlgorithm;

    @Mock
    private ReactionRepository reactionRepo;

    private Reaction reaction;
    private Reaction reactionThatSkipsInbox;
    private Reaction reactionThatSkipsStorage;

    @GeneratePojo
    private Message message;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String messageId;
    
    private MessageReactorImpl instance;
    
    @Captor
    private ArgumentCaptor<List<Action>> actionCaptor;
    
    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
        
        instance = new MessageReactorImpl(actionRunner, actionFactory, matchAlgorithm, reactionRepo);
        verifyZeroInteractions(actionFactory, actionFactory, matchAlgorithm, reactionRepo);
    }
    
    private void setupData() throws Exception
    {
        message.messageId = messageId;
        message.applicationId = appId;
        
        reaction = one(reactions());
        reaction.actions = reaction.actions
            .stream()
            .filter(action -> !action.isSetDontStoreMessage())
            .filter(action -> !action.isSetSkipInbox())
            .collect(toList());
        
        reactionThatSkipsInbox = one(reactions());
        AromaAction skipInbox = new AromaAction();
        skipInbox.setSkipInbox(new ActionSkipInbox());
        reactionThatSkipsInbox.addToActions(skipInbox);
        reactionThatSkipsInbox.actions = reactionThatSkipsInbox.actions
            .stream()
            .filter(action -> !action.isSetDontStoreMessage())
            .collect(toList());

        reactionThatSkipsStorage = one(reactions());
        AromaAction dontStore = new AromaAction();
        dontStore.setDontStoreMessage(new ActionDontStoreMessage());
        reactionThatSkipsStorage.addToActions(dontStore);
        reactionThatSkipsStorage.actions = reactionThatSkipsStorage.actions
            .stream()
            .filter(action -> !action.isSetSkipInbox())
            .collect(toList());

    }

    private void setupMocks() throws Exception
    {
        when(reactionRepo.getReactionsForApplication(appId))
            .thenReturn(Lists.createFrom(reaction));
        
        when(matchAlgorithm.matches(message, reaction.matchers))
            .thenReturn(true);
        
        when(actionFactory.actionFor(any()))
            .thenReturn(genericAction);
        
        when(actionFactory.actionToRunThroughFollowerInboxes(message))
            .thenReturn(actionRunThroughInboxes);
        
        when(actionFactory.actionToStoreMessage(message))
            .thenReturn(actionToStore);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new MessageReactorImpl(null, actionFactory, matchAlgorithm, reactionRepo));
        assertThrows(() -> new MessageReactorImpl(actionRunner, null, matchAlgorithm, reactionRepo));
        assertThrows(() -> new MessageReactorImpl(actionRunner, actionFactory, null, reactionRepo));
        assertThrows(() -> new MessageReactorImpl(actionRunner, actionFactory, matchAlgorithm, null));
    }

    @Test
    public void testReactToMessage() throws Exception
    {
        SendMessageResponse response = instance.reactToMessage(message);
        assertThat(response, notNullValue());
        assertThat(response.messageId, is(messageId));
               
        verify(actionFactory).actionToStoreMessage(message);
        verify(actionFactory).actionToRunThroughFollowerInboxes(message);
        
        verify(actionRunner).runThroughActions(eq(message), actionCaptor.capture());
        
        List<Action> actions = actionCaptor.getValue();
        assertThat(actions, hasItems(actionRunThroughInboxes, actionToStore, genericAction));
    }
    
    @Test
    public void testWhenNoReactionsForApp() throws Exception
    {
        when(reactionRepo.getReactionsForApplication(appId))
            .thenReturn(Lists.emptyList());
        
        SendMessageResponse response = instance.reactToMessage(message);
        assertThat(response, notNullValue());
        assertThat(response.messageId, is(messageId));
        
        verify(actionRunner).runThroughActions(eq(message), actionCaptor.capture());
        verify(actionFactory, never()).actionFor(any());
        
        List<Action> actions = actionCaptor.getValue();
        assertThat(actions, hasItems(actionRunThroughInboxes, actionToStore));
    }
    
    @Test
    public void testWhenNoneOfTheReactionsMatch() throws Exception
    {
        when(matchAlgorithm.matches(message, reaction.matchers))
            .thenReturn(false);
        
        SendMessageResponse response = instance.reactToMessage(message);
        assertThat(response.messageId, is(messageId));
        
        verify(actionFactory).actionToStoreMessage(message);
        verify(actionFactory).actionToRunThroughFollowerInboxes(message);
        
        verify(actionRunner).runThroughActions(eq(message), actionCaptor.capture());
        
        List<Action> actions = actionCaptor.getValue();
        assertThat(actions, hasItems(actionRunThroughInboxes, actionToStore));
    }
    
    @Test
    public void testWhenActionsIncludeSkipInbox() throws Exception
    {
        when(reactionRepo.getReactionsForApplication(appId))
            .thenReturn(Lists.createFrom(reactionThatSkipsInbox));
        
        when(matchAlgorithm.matches(message, reactionThatSkipsInbox.matchers))
            .thenReturn(true);
        
        SendMessageResponse response = instance.reactToMessage(message);
        assertThat(response.messageId, is(messageId));
        
        verify(actionFactory, never()).actionToRunThroughFollowerInboxes(any());
        verify(actionFactory).actionToStoreMessage(message);
        verify(actionFactory, atLeastOnce()).actionFor(any());
        
        verify(actionRunner).runThroughActions(eq(message), actionCaptor.capture());
        
        List<Action> actions = actionCaptor.getValue();
        assertThat(actions, hasItems(actionToStore, genericAction));
        assertThat(actions, not(hasItem(actionRunThroughInboxes)));

    }
    
    @Test
    public void testWhenActionsIncludeDontStore() throws Exception
    {
        when(reactionRepo.getReactionsForApplication(appId))
            .thenReturn(Lists.createFrom(reactionThatSkipsStorage));
        
        when(matchAlgorithm.matches(message, reactionThatSkipsStorage.matchers))
            .thenReturn(true);
        
        SendMessageResponse response = instance.reactToMessage(message);
        assertThat(response.messageId, is(messageId));
        
        verify(actionRunner).runThroughActions(eq(message), actionCaptor.capture());
        
        List<Action> actions = actionCaptor.getValue();
        assertThat(actions, hasItems(genericAction, actionRunThroughInboxes));
        assertThat(actions, not(hasItem(actionToStore)));
    }

}
