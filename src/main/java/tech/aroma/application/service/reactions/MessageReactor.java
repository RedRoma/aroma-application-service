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

package tech.aroma.application.service.reactions;

import com.google.inject.ImplementedBy;
import org.apache.thrift.TException;
import tech.aroma.application.service.reactions.actions.ActionFactory;
import tech.aroma.application.service.reactions.actions.ActionRunner;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.ReactionRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.application.service.SendMessageResponse;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;


/**
 * The {@link MessageReactor} reacts to every valid message sent to the Application Service.
 * 
 * @author SirWellington
 */
@ImplementedBy(MessageReactorImpl.class)
@ThreadSafe
public interface MessageReactor
{
    SendMessageResponse reactToMessage(@Required Message message) throws TException;

    static MessageReactor newInstance(@Required ActionFactory actionFactory, 
                                      @Required ActionRunner actionRunner,
                                      @Required MatchAlgorithm matchAlgorithm,
                                      @Required ReactionRepository reactionRepo) throws IllegalArgumentException
    {
        return new MessageReactorImpl(actionRunner, actionFactory, matchAlgorithm, reactionRepo);
    }
}
