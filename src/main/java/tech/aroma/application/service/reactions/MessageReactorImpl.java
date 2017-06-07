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

package tech.aroma.application.service.reactions;

import java.util.List;
import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.application.service.reactions.actions.*;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.reactions.AromaAction;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;

import static java.util.stream.Collectors.toList;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This Message Reactor creates the initial {@linkplain Action Actions}
 * for an incoming message, and begins the 
 * {@linkplain ActionRunner#runThroughActions(tech.aroma.thrift.Message, java.util.List) execution process}.
 * 
 * @author SirWellington
 */
@Internal
@ThreadSafe
final class MessageReactorImpl implements MessageReactor
{
    
    private final static Logger LOG = LoggerFactory.getLogger(MessageReactorImpl.class);
    
    private final ActionRunner actionRunner;
    private final ActionFactory actionFactory;
    private final MatchAlgorithm matchAlgorithm;
    private final ReactionRepository reactionRepo;
    
    @Inject
    MessageReactorImpl(ActionRunner actionRunner,
                       ActionFactory actionFactory,
                       MatchAlgorithm matchAlgorithm,
                       ReactionRepository reactionRepo)
    {
        checkThat(actionRunner, actionFactory, matchAlgorithm, reactionRepo)
            .are(notNull());
        
        this.actionRunner = actionRunner;
        this.actionFactory = actionFactory;
        this.matchAlgorithm = matchAlgorithm;
        this.reactionRepo = reactionRepo;
    }
    
    @Override
    public SendMessageResponse reactToMessage(@Required Message message) throws TException
    {
        Action.checkMessage(message);
        
        List<AromaAction> applicableActions = getAllActionsApplicableToMessage(message);
        
        LOG.debug("Found {} applicable actions for Message {}", applicableActions.size(), message.messageId);
        
        //Unlike other Actions, these are assumed to be true unless otherwise excluded.
        boolean shouldRunThroughInboxes = true;
        boolean shouldStoreMessage = true;
        
        List<Action> initialActions = Lists.create();
        
        for (AromaAction action : applicableActions)
        {
            if (action.isSetSkipInbox())
            {
                shouldRunThroughInboxes = false;
                continue;
            }
            
            if (action.isSetDontStoreMessage())
            {
                shouldStoreMessage = false;
                continue;
            }
            
            Action newAction = actionFactory.actionFor(action);
            initialActions.add(newAction);
        }
        
        if (shouldStoreMessage)
        {
            Action actionToStoreMessage = actionFactory.actionToStoreMessage(message);
            initialActions.add(actionToStoreMessage);
        }
        
        if (shouldRunThroughInboxes)
        {
            Action actionToRunThroughFollowerInboxes = actionFactory.actionToRunThroughFollowerInboxes(message);
            initialActions.add(actionToRunThroughFollowerInboxes);
        }
        
        LOG.debug("Processing Message with {} initial actions: [{}]", initialActions.size(), message.messageId);
        int totalActions = actionRunner.runThroughActions(message, initialActions);
        LOG.debug("Ran through {} total actions for Message {}", totalActions, message.messageId);
        
        return new SendMessageResponse().setMessageId(message.messageId);
    }

    private List<AromaAction> getAllActionsApplicableToMessage(Message message) throws TException
    {
        String appId = message.applicationId;

        List<Reaction> reactions = reactionRepo.getReactionsForApplication(appId);

        return Lists.nullToEmpty(reactions)
            .stream()
            .filter(reaction -> matchAlgorithm.matches(message, reaction.matchers))
            .filter(reaction -> !Lists.isEmpty(reaction.actions))
            .flatMap(reaction -> reaction.actions.stream())
            .distinct()
            .collect(toList());
    }

}
