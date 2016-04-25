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

package tech.aroma.application.service.operations;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.application.service.reactions.MatchAlgorithm;
import tech.aroma.application.service.reactions.actions.Action;
import tech.aroma.application.service.reactions.actions.ActionMapper;
import tech.aroma.application.service.reactions.actions.ActionRunner;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.reactions.AromaAction;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

import tech.aroma.application.service.reactions.actions.ActionFactory;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 *
 * @author SirWellington
 */
@Internal
final class MessageReactorImpl implements MessageReactor
{

    private final static Logger LOG = LoggerFactory.getLogger(MessageReactorImpl.class);
    
    private ActionMapper actionMapper;
    private ActionRunner actionRunner;
    private ActionFactory factory;
    private MatchAlgorithm matchAlgorithm;
    private ReactionRepository reactionRepo;

    @Override
    public SendMessageResponse reactToMessage(@Required Message message) throws TException
    {
        checkThat(message)
            .throwing(InvalidArgumentException.class)
            .is(notNull());
        
        checkThat(message.messageId)
            .throwing(InvalidArgumentException.class)
            .is(validApplicationId());
        
        String appId = message.applicationId;
        List<Reaction> reactions = reactionRepo.getReactionsForApplication(appId);
       
        List<AromaAction> applicableActions = Lists.nullToEmpty(reactions)
            .stream()
            .filter((reaction) -> matchAlgorithm.matches(message, reaction.matchers))
            .flatMap(reaction -> Lists.nullToEmpty(reaction.actions).stream())
            .distinct()
            .collect(Collectors.toList());
        
        boolean runThroughInbox = true;
        boolean storeMessage = true;
        
        List<Action> initialActions = Lists.create();
      
        for (AromaAction action : applicableActions)
        {
            if (action.isSetSkipInbox())
            {
                runThroughInbox = false;
                continue;
            }
            
            if (action.isSetDontStoreMessage())
            {
                storeMessage = false;
            }
            
            Action newAction = actionMapper.create(message, action);
            initialActions.add(newAction);
        }
        
        if (storeMessage)
        {
            Action actionToStoreMessage = factory.actionToStoreMessage(message);
            initialActions.add(actionToStoreMessage);
        }
        
        if (runThroughInbox)
        {
            Action actionToRunThroughFollowerInboxes = factory.actionToRunThroughFollowerInboxes(message, this);
            initialActions.add(actionToRunThroughFollowerInboxes);
        }
        
        LOG.debug("Beginning Message Processing with {} initial actions", initialActions.size());
        int totalActions = actionRunner.runThroughActions(message, initialActions);
        LOG.debug("Ran through {} total actions for Message {}", totalActions, message);
        
        return new SendMessageResponse().setMessageId(message.messageId);
    }

}
