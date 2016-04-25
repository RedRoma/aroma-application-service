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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.application.service.reactions.MessageReactor;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.reactions.AromaAction;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static java.util.stream.Collectors.toList;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;

/**
 * This step runs the message through a follower's Inbox and any 
 * Reactions it may have.
 * 
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
@Internal
class RunThroughInboxAction implements Action
{

    private final static Logger LOG = LoggerFactory.getLogger(RunThroughInboxAction.class);

    private ActionFactory actionFactory;
    private MessageReactor creator;
    private MatchAlgorithm matchAlgorithm;
    private ReactionRepository reactionRepo;
    private User user;

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        Action.checkMessage(message);
        
        List<Action> newActions = Lists.create();
        List<Reaction> reactions = reactionRepo.getReactionsForUser(user.userId);
        
        boolean shouldStoreInInbox = true;
        
        List<AromaAction> matchingActions = reactions.stream()
            .filter(r -> matchAlgorithm.matches(message, r.matchers))
            .flatMap(r -> r.getActions().stream())
            .distinct()
            .collect(toList());
        
        for (AromaAction action : matchingActions)
        {
            if (action.isSetSkipInbox())
            {
                shouldStoreInInbox = false;
                continue;
            }
            
        }
        
        
        if (shouldStoreInInbox)
        {
            Action actionToSaveInInbox = actionFactory.actionToStoreInInbox(message, user);
            newActions.add(actionToSaveInInbox);
        }
        
        return newActions;
    }

}
