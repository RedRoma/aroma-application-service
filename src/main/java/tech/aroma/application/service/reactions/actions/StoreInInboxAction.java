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

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.InboxRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.aroma.thrift.service.AromaServiceConstants;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This Action actually save a message in a User's Inbox.
 *
 * @author SirWellington
 */
@Internal
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class StoreInInboxAction implements Action
{

    private final static Logger LOG = LoggerFactory.getLogger(StoreInInboxAction.class);

    private final InboxRepository inboxRepo;
    private final User user;

    StoreInInboxAction(InboxRepository inboxRepo, User user)
    {
        checkThat(inboxRepo, user)
            .are(notNull());

        this.inboxRepo = inboxRepo;
        this.user = user;
    }

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        Action.checkMessage(message);

        inboxRepo.saveMessageForUser(user, message, AromaServiceConstants.DEFAULT_INBOX_LIFETIME);
        LOG.debug("Saved Message {}/{} in Inbox of User {}", message.applicationId, message.messageId, user);

        return Lists.emptyList();
    }

    @Override
    public String toString()
    {
        return "StoreInInboxAction{" + "inboxRepo=" + inboxRepo + ", user=" + user + '}';
    }

}
