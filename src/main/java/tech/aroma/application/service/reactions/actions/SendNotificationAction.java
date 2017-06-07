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

import java.time.Instant;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.events.*;
import tech.aroma.thrift.notification.service.NotificationService;
import tech.aroma.thrift.notification.service.SendNotificationRequest;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class SendNotificationAction implements Action
{

    private final static Logger LOG = LoggerFactory.getLogger(SendNotificationAction.class);

    private final NotificationService.Iface notificationService;

    SendNotificationAction(NotificationService.Iface notificationService)
    {
        checkThat(notificationService)
            .is(notNull());
        
        this.notificationService = notificationService;
    }

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        Action.checkMessage(message);

        SendNotificationRequest request = createNotificationRequestFor(message);

        tryToSendNotification(request);

        return Lists.emptyList();
    }

    private SendNotificationRequest createNotificationRequestFor(Message message)
    {
        ApplicationSentMessage applicationSentMessage = new ApplicationSentMessage()
            .setMessage(message.messageId)
            .setMessage(message.body);

        EventType eventType = new EventType();
        eventType.setApplicationSentMessage(applicationSentMessage);

        String appId = message.applicationId;
        Application app = new Application().setApplicationId(appId);

        Event event = new Event()
            .setApplication(app)
            .setApplicationId(appId)
            .setTimestamp(Instant.now().toEpochMilli())
            .setEventId("")
            .setEventType(eventType);

        return new SendNotificationRequest().setEvent(event);
    }

    private void tryToSendNotification(SendNotificationRequest sendNotificationRequest)
    {
        try
        {
            notificationService.sendNotification(sendNotificationRequest);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to send Notification request: {}", sendNotificationRequest, ex);
        }
    }

    @Override
    public String toString()
    {
        return "SendNotificationAction{" + "notificationService=" + notificationService + '}';
    }

}
