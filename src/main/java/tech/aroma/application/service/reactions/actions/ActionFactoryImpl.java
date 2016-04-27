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

import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern;
import tech.sirwellington.alchemy.http.AlchemyHttp;

import static tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern.Role.FACTORY;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
@FactoryPattern(role = FACTORY)
final class ActionFactoryImpl implements ActionFactory
{

    private final static Logger LOG = LoggerFactory.getLogger(ActionFactoryImpl.class);

    private final NotificationService.Iface notificationService;
    private final FollowerRepository followerRepo;
    private final InboxRepository inboxRepo;
    private final MatchAlgorithm matchAlgorithm;
    private final MessageRepository messageRepo;
    private final ReactionRepository reactionRepo;
    private final AlchemyHttp http;

    @Inject
    ActionFactoryImpl(NotificationService.Iface notificationService,
                      FollowerRepository followerRepo,
                      InboxRepository inboxRepo,
                      MatchAlgorithm matchAlgorithm,
                      MessageRepository messageRepo,
                      ReactionRepository reactionRepo,
                      AlchemyHttp http)
    {
        checkThat(notificationService, followerRepo, inboxRepo, matchAlgorithm, messageRepo, reactionRepo, http)
            .are(notNull());
        
        this.notificationService = notificationService;
        this.followerRepo = followerRepo;
        this.inboxRepo = inboxRepo;
        this.matchAlgorithm = matchAlgorithm;
        this.messageRepo = messageRepo;
        this.reactionRepo = reactionRepo;
        this.http = http;
    }

    @Override
    public Action actionFor(AromaAction action)
    {
        if (action == null)
        {
            return this.actionToDoNothing();
        }

        if (action.isSetForwardToSlackChannel())
        {
            return this.actionToSendToSlackChannel(action.getForwardToSlackChannel());
        }

        if (action.isSetForwardToSlackUser())
        {
            return this.actionToSendToSlackUser(action.getForwardToSlackUser());
        }

        if (action.isSetSendEmail())
        {
            return this.actionToSendEmail(action.getSendEmail());
        }

        return this.actionToDoNothing();
    }

    @Override
    public Action actionToDoNothing()
    {
        return new DoNothingAction();
    }

    @Override
    public Action actionToSendToSlackChannel(ActionForwardToSlackChannel slack)
    {
        return new ForwardToSlackChannelAction(slack, http);
    }

    @Override
    public Action actionToSendToSlackUser(ActionForwardToSlackUser slack)
    {
        return actionToDoNothing();
    }

    @Override
    public Action actionToSendNotifications(Message message)
    {
        return new SendNotificationAction(notificationService);
    }

    @Override
    public Action actionToRunThroughFollowerInboxes(Message message)
    {
        return new RunThroughFollowerInboxesAction(this, followerRepo);
    }

    @Override
    public Action actionToRunThroughInbox(User user)
    {
        return new RunThroughInboxAction(this, matchAlgorithm, reactionRepo, user);
    }

    @Override
    public Action actionToStoreMessage(Message message)
    {
        return new StoreMessageAction(messageRepo);
    }

    @Override
    public Action actionToStoreInInbox(User user)
    {
        return new StoreInInboxAction(inboxRepo, user);
    }

    @Override
    public Action actionToSendEmail(ActionSendEmail sendEmail)
    {
        return new DoNothingAction();
    }

}
