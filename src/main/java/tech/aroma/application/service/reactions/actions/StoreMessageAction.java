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
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.*;
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
final class StoreMessageAction implements Action
{

    private final static Logger LOG = LoggerFactory.getLogger(StoreMessageAction.class);

    private final MessageRepository messageRepo;

    StoreMessageAction(MessageRepository messageRepo)
    {
        checkThat(messageRepo)
            .is(notNull());

        this.messageRepo = messageRepo;
    }

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        Action.checkMessage(message);

        messageRepo.saveMessage(message, new LengthOfTime(TimeUnit.HOURS, 18));
        LOG.debug("Saved message in Message Repo: {}/{}", message.applicationId, message.messageId);

        return Lists.emptyList();
    }

    @Override
    public String toString()
    {
        return "StoreMessageAction{" + "messageRepo=" + messageRepo + '}';
    }

}
