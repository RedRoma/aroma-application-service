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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ActionRunnerTest 
{

    @Before
    public void setUp() throws Exception
    {
        
    }

    @Test
    public void testNewSynchronousRunner() throws Exception
    {
        ActionRunner instance = ActionRunner.newSynchronousRunner();
        assertThat(instance, notNullValue());
        testWithOnlyOneRoundOfAction(instance);
    }

    @Test
    public void testNewAsynchronousRunner() throws Exception
    {
        ActionRunner instance = ActionRunner.newAsynchronousRunner();
        assertThat(instance, notNullValue());
        testWithOnlyOneRoundOfAction(instance);
    }

    static void testActionRunner(ActionRunner runner) throws Exception
    {
        checkThat(runner).is(notNull());
        testWithOnlyOneRoundOfAction(runner);
        testWithMultipleRounds(runner);
        testWhenAnActionFails(runner);
    }

    private static void testWithOnlyOneRoundOfAction(ActionRunner runner) throws Exception
    {
        Message message = one(messages());
        List<Action> actions = listOf(() -> mock(Action.class), 20);

        runner.runThroughActions(message, actions);

        for (Action action : actions)
        {
            verify(action).actOnMessage(message);
        }

    }
    
    private static void testWithMultipleRounds(ActionRunner runner) throws Exception
    {
        Message message = one(messages());
        
        int rounds = one(integers(2, 5));

        List<Action> firstActions = listOf(() -> mock(Action.class), 20);

        List<Action> currentRoundOfActions = Lists.copy(firstActions);
        List<Action> nextRoundOfActions = Lists.create();
        
        for (int i = 0; i < rounds; ++i)
        {
            for (Action action : currentRoundOfActions)
            {
                List<Action> newActions = listOf(() -> mock(Action.class), 5);
                when(action.actOnMessage(message)).thenReturn(newActions);
                nextRoundOfActions.addAll(newActions);
            }
            
            currentRoundOfActions = Lists.copy(nextRoundOfActions);
            nextRoundOfActions.clear();
        }
    }
    
    private static void testWhenAnActionFails(ActionRunner runner) throws Exception
    {
        Message message = one(messages());
        
        List<Action> actions = listOf(() -> mock(Action.class), 20);
        Action failingAction = Lists.oneOf(actions);
        when(failingAction.actOnMessage(message))
            .thenThrow(new RuntimeException());
        
        runner.runThroughActions(message, actions);
        
        for (Action action : actions)
        {
            verify(action).actOnMessage(message);
        }
    }

}
