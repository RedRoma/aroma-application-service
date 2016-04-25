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
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 *
 * @author SirWellington
 */
@Internal
final class ActionFactoryImpl implements ActionFactory
{

    private final static Logger LOG = LoggerFactory.getLogger(ActionFactoryImpl.class);

    private NotificationService.Iface notificationService;

    private ActionMapper actionMapper;
    private FollowerRepository followerRepo;
    private InboxRepository inboxRepo;
    private MatchAlgorithm matchAlgorithm;
    private MessageRepository messageRepo;
    private ReactionRepository reactionRepo;

    @Override
    public Action actionToDoNothing()
    {
        return new DoNothingAction();
    }

    @Override
    public Action actionToSendToSlackChannel(ActionForwardToSlackChannel slack)
    {
        return new ForwardToSlackAction(false, slack.slackChannel, slack.domainName, slack.slackToken);
    }

    @Override
    public Action actionToSendToSlackUser(ActionForwardToSlackUser slack)
    {
        return new ForwardToSlackAction(true, slack.slackUsername, slack.domainName, slack.slackToken);
    }

    @Override
    public Action actionToSendNotifications(Message message)
    {
        return new SendNotificationAction(notificationService);
    }

    @Override
    public Action actionToRunThroughFollowerInboxes(Message message)
    {
        return new RunThroughFollowerInboxesActions(this, followerRepo);
    }

    @Override
    public Action actionToRunThroughInbox(User user)
    {
        return new RunThroughInboxAction(this, actionMapper, matchAlgorithm, reactionRepo, user);
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
