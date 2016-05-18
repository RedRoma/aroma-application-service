/*
 * Copyright 2016 RedRoma, Inc..
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


import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.PayloadBuilder;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.channels.IOSDevice;
import tech.aroma.thrift.channels.MobileDevice;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static java.lang.String.format;
import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

import tech.aroma.data.UserPreferencesRepository;

import static java.lang.String.format;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

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
    private final UserPreferencesRepository deviceRepo;
    private final String userId;

    @Inject
    SendPushNotificationAction(ApnsService apns, UserPreferencesRepository deviceRepo, String userId)
    {
        checkThat(apns, deviceRepo)
            .are(notNull());
        checkThat(userId).is(validUserId());
        
        this.apns = apns;
        this.deviceRepo = deviceRepo;
        this.userId = userId;
    }

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        
       deviceRepo.getMobileDevices(userId)
            .stream()
            .filter(MobileDevice::isSetIosDevice)
            .map(MobileDevice::getIosDevice)
            .filter(Objects::nonNull)
            .forEach(device -> this.sendNotification(message, device));

       return Lists.emptyList();
    }

    private void sendNotification(Message message, IOSDevice device)
    {
        String deviceToken = device.deviceToken;

        if (isNullOrEmpty(deviceToken))
        {
            //Ignore it
            return;
        }

        String payload = "";
        try
        {
            payload = createNotificationFromMessage(message);
            apns.push(deviceToken, payload);
        }
        catch (Exception ex)
        {
            LOG.warn("Failed to send Push Notification: {}", payload, ex);
        }

    }
    
    private String createNotificationFromMessage(Message message) throws TException
    {
        String title = format("%s - %s", message.applicationName, message.title);

        String serializedMessage = ThriftObjects.toJson(message);

        PayloadBuilder builder = APNS.newPayload()
            .alertTitle(title)
            .customField("message", serializedMessage);

        if (!builder.isTooLong())
        
        {
            LOG.debug("Apple PNS Payload too long. Shortening: {}", builder.toString());
            return builder.build();
        }
        else
        {
            return APNS.newPayload()
                .alertTitle(title)
                .build();
        }
    }

}
