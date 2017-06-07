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

import com.google.inject.ImplementedBy;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.reactions.*;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;

/**
 *
 * @author SirWellington
 */
@ThreadSafe
@ImplementedBy(ActionFactoryImpl.class)
public interface ActionFactory
{

    Action actionFor(AromaAction action);

    Action actionToDoNothing();

    Action actionToSendToGitter(@Required ActionForwardToGitter gitter);

    Action actionToSendToSlackChannel(@Required ActionForwardToSlackChannel slack);

    Action actionToSendToSlackUser(@Required ActionForwardToSlackUser slack);

    Action actionToSendNotifications(@Required Message message);

    Action actionToRunThroughFollowerInboxes(@Required Message message);

    Action actionToRunThroughInbox(@Required User user);

    Action actionToStoreMessage(@Required Message message);

    Action actionToStoreInInbox(@Required User user);

    Action actionToSendPushNotification(@NonEmpty String userId);

    Action actionToSendEmail(@Required ActionSendEmail sendEmail);
}
