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

import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.reactions.ActionForwardToSlackChannel;
import tech.aroma.thrift.reactions.ActionForwardToSlackUser;
import tech.aroma.thrift.reactions.ActionSendEmail;
import tech.sirwellington.alchemy.annotations.arguments.Required;

/**
 *
 * @author SirWellington
 */
public interface ActionFactory
{

    Action actionToDoNothing();

    Action actionToSendToSlackChannel(@Required Message message, @Required ActionForwardToSlackChannel slack);

    Action actionToSendToSlackUser(@Required Message message, @Required ActionForwardToSlackUser slack);

    Action actionToSendNotifications(@Required Message message);

    Action actionToRunThroughFollowerInboxes(@Required Message message);

    Action actionToRunThroughInbox(@Required Message message, @Required User user);

    Action actionToStoreMessage(@Required Message message);

    Action actionToStoreInInbox(@Required Message message, @Required User user);

    Action actionToSendEmail(@Required Message message, @Required ActionSendEmail sendEmail);
}
