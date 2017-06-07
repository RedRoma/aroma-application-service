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
import javax.inject.Inject;

import com.notnoop.apns.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.UserPreferencesRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.channels.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static java.lang.String.format;
import static tech.aroma.data.assertions.RequestAssertions.validMessage;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class SendPushNotificationAction implements Action
{
    private final static Logger LOG = LoggerFactory.getLogger(SendPushNotificationAction.class);

    private final ApnsService apns;
    private final UserPreferencesRepository userPreferencesRepo;
    private final String userId;

    @Inject
    SendPushNotificationAction(ApnsService apns, UserPreferencesRepository userPreferencesRepo, String userId)
    {
        checkThat(apns, userPreferencesRepo)
            .are(notNull());
        
        checkThat(userId).is(validUserId());
        
        this.apns = apns;
        this.userPreferencesRepo = userPreferencesRepo;
        this.userId = userId;
    }

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        checkThat(message)
            .throwing(InvalidArgumentException.class)
            .is(validMessage());
        
       userPreferencesRepo.getMobileDevices(userId)
            .stream()
            .filter(MobileDevice::isSetIosDevice)
            .map(MobileDevice::getIosDevice)
            .filter(Objects::nonNull)
            .forEach(device -> this.sendNotification(message, device));

       return Lists.emptyList();
    }

    private void sendNotification(Message message, IOSDevice device)
    {
        byte[] deviceToken = device.getDeviceToken();

        if (deviceToken == null || deviceToken.length == 0)
        {
            //Ignore it
            return;
        }

        byte[] payload = null;
        try
        {
            payload = createNotificationFromMessage(message);
            ApnsNotification response = apns.push(deviceToken, payload);
            LOG.debug("Successfully sent Notification to Device: {}", device);
        }
        catch (Exception ex)
        {
            LOG.warn("Failed to send Push Notification for: {}", message.messageId, ex);
        }

    }
    
    private byte[] createNotificationFromMessage(Message message) throws TException
    {
        String alertTitle = message.applicationName;
        String alertBody = format("%s - %s", message.applicationName, message.title);
        
        PushNotificationPayload payload = new PushNotificationPayload()
            .setMessageId(message.messageId)
            .setApplicationId(message.applicationId);
        
        byte[] serializedPayload = ThriftObjects.toBinary(payload);

        PayloadBuilder builder = APNS.newPayload()
            .instantDeliveryOrSilentNotification()
            .alertTitle(alertTitle)
            .alertBody(alertBody)
            .customField(ChannelsConstants.PUSH_NOTIFICATION_KEY_FOR_PAYLOAD, serializedPayload);

        if (notTooLong(builder))
        {
            return builder.buildBytes();
        }
        
        LOG.debug("Apple PNS Payload too long. Shortening: {}", builder.toString());

        return APNS.newPayload()
            .instantDeliveryOrSilentNotification()
            .alertTitle(alertTitle)
            .alertBody(alertBody)
            .buildBytes();
    }

    private boolean notTooLong(PayloadBuilder builder)
    {
        return !builder.isTooLong();
    }
    
}
