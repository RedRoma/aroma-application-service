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

import javax.inject.Inject;

import com.notnoop.apns.ApnsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.*;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.notification.service.NotificationService;
import tech.aroma.thrift.reactions.*;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern;
import tech.sirwellington.alchemy.http.AlchemyHttp;

import static tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern.Role.FACTORY;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;

/**
 *
 * @author SirWellington
 */
@Internal
@FactoryPattern(role = FACTORY)
final class ActionFactoryImpl implements ActionFactory
{

    private final static Logger LOG = LoggerFactory.getLogger(ActionFactoryImpl.class);
    
    private final AlchemyHttp http;
    private final ApnsService apns;
    
    private final FollowerRepository followerRepo;
    private final InboxRepository inboxRepo;
    private final MatchAlgorithm matchAlgorithm;
    private final MessageRepository messageRepo;
    private final ReactionRepository reactionRepo;
    private final UserPreferencesRepository userPreferencesRepo;
    
    private final NotificationService.Iface notificationService;

    @Inject
    ActionFactoryImpl(AlchemyHttp http,
                      ApnsService apns,
                      FollowerRepository followerRepo,
                      InboxRepository inboxRepo,
                      MatchAlgorithm matchAlgorithm,
                      MessageRepository messageRepo,
                      ReactionRepository reactionRepo,
                      UserPreferencesRepository userPreferencesRepo,
                      NotificationService.Iface notificationService)
    {
        checkThat(http, apns, followerRepo, inboxRepo, matchAlgorithm, messageRepo, reactionRepo, userPreferencesRepo, notificationService)
            .are(notNull());
        
        this.http = http;
        this.apns = apns;
        this.followerRepo = followerRepo;
        this.inboxRepo = inboxRepo;
        this.matchAlgorithm = matchAlgorithm;
        this.messageRepo = messageRepo;
        this.reactionRepo = reactionRepo;
        this.userPreferencesRepo = userPreferencesRepo;
        this.notificationService = notificationService;
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
        
        if (action.isSetForwardToGitter())
        {
            return this.actionToSendToGitter(action.getForwardToGitter());
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
    public Action actionToSendToGitter(ActionForwardToGitter gitter)
    {
        checkThat(gitter).is(notNull());
        checkThat(gitter.gitterWebhookUrl)
            .usingMessage("Gitter Webhook not a valid URL: " + gitter.gitterWebhookUrl)
            .is(validURL());

        return new ForwardToGitterAction(http, gitter);
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

    @Override
    public Action actionToSendPushNotification(String userId)
    {
        return new SendPushNotificationAction(apns, userPreferencesRepo, userId);
    }

}
