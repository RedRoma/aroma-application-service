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
import java.util.Objects;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.reactions.AromaAction;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validUser;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This step runs the message through a follower's Inbox and any Reactions it may have.
 *
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
@Internal
final class RunThroughInboxAction implements Action
{

    private final static Logger LOG = LoggerFactory.getLogger(RunThroughInboxAction.class);

    private final ActionFactory actionFactory;
    private final MatchAlgorithm matchAlgorithm;
    private final ReactionRepository reactionRepo;
    private final User user;

    RunThroughInboxAction(ActionFactory actionFactory,
                          MatchAlgorithm matchAlgorithm,
                          ReactionRepository reactionRepo,
                          User user)
    {
        checkThat(actionFactory, matchAlgorithm, reactionRepo, user)
            .are(notNull());
        
        checkThat(user).is(validUser());

        this.actionFactory = actionFactory;
        this.matchAlgorithm = matchAlgorithm;
        this.reactionRepo = reactionRepo;
        this.user = user;
    }

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        Action.checkMessage(message);

        List<AromaAction> applicableActions = getApplicationActionsFor(message, user);
        
        LOG.debug("Found {} application actions for message {} through Inbox of user {}",
                  applicableActions.size(), 
                  message.messageId,
                  user.userId);
        
        //This Action is assumed true unless otherwise excluded.
        boolean shouldStoreInInbox = true;
        boolean shouldSendPushNotification = true;

        List<Action> newActions = Lists.create();
        
        for (AromaAction action : applicableActions)
        {
            if (action.isSetDontStoreMessage())
            {
                shouldStoreInInbox = false;
            }
            else if (action.isSetSkipInbox())
            {
                shouldStoreInInbox = false;
                shouldSendPushNotification = false;
            }
            else if (action.isSetDontSendPushNotification())
            {
                shouldSendPushNotification = false;
            }
            else if (action.isSetSendPushNotification())
            {
                shouldSendPushNotification = true;
            }
            else
            {
                Action newAction = actionFactory.actionFor(action);
                newActions.add(newAction);
            }
        }

        if (shouldStoreInInbox)
        {
            Action actionToSaveInInbox = actionFactory.actionToStoreInInbox(user);
            newActions.add(actionToSaveInInbox);
        }
        
        if (shouldSendPushNotification)
        {
            Action actionToSendPushNotification = actionFactory.actionToSendPushNotification(user.userId);
            newActions.add(actionToSendPushNotification);
            LOG.debug("Sending Push Notifications to {}", user);
        }
        else
        {
            LOG.debug("Not Sending Push Notification to {}", user);
        }

        return newActions;
    }

  
    private List<AromaAction> getApplicationActionsFor(Message message, User user) throws TException
    {
        List<Reaction> reactions = reactionRepo.getReactionsForUser(user.userId);
        reactions = Lists.nullToEmpty(reactions);

        return reactions.stream()
            .filter(reaction -> matchAlgorithm.matches(message, reaction.matchers))
            .map(reaction -> reaction.getActions())
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .collect(toList());
    }

    @Override
    public String toString()
    {
        return "RunThroughInboxAction{" + "actionFactory=" + actionFactory + ", matchAlgorithm=" + matchAlgorithm + ", reactionRepo=" + reactionRepo + ", user=" + user + '}';
    }
}
