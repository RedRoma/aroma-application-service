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

import com.google.inject.ImplementedBy;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
@ThreadSafe
@ImplementedBy(ActionRunnerAsynchronous.class)
public interface ActionRunner
{
    
    /**
     * Runs through all of the Actions supplied, and all subsequently created actions until none are left.
     * 
     * @param message The message to process.
     * @param actions The Actions to perform based on the message. Can be empty but not null.
     * 
     * @return The total number of actions executed.
     */
    int runThroughActions(@Required Message message, @Required List<Action> actions);
    
    static ActionRunner newSynchronousRunner()
    {
        return new ActionRunnerSynchronous();
    }
    
    static ActionRunner newAsynchronousRunner()
    {
        return new ActionRunnerAsynchronous();
    }

}
