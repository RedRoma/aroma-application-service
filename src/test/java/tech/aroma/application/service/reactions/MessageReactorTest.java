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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.application.service.reactions.actions.ActionFactory;
import tech.aroma.application.service.reactions.actions.ActionRunner;
import tech.aroma.application.service.reactions.matchers.MatchAlgorithm;
import tech.aroma.data.ReactionRepository;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MessageReactorTest 
{

    @Mock
    private ActionFactory actionFactory;
    
    @Mock
    private ActionRunner actionRunner;
    
    @Mock
    private MatchAlgorithm matchAlgorithm;
    
    @Mock
    private ReactionRepository reactionRepo;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
    }


    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {
        
    }


    @Test
    public void testNewInstance()
    {
        MessageReactor instance = MessageReactor.newInstance(actionFactory, actionRunner, matchAlgorithm, reactionRepo);
        assertThat(instance, notNullValue());
    }

    @Test
    public void testNewInstanceWithBadArgs()
    {
        assertThrows(() -> MessageReactor.newInstance(null, actionRunner, matchAlgorithm, reactionRepo));
        assertThrows(() -> MessageReactor.newInstance(actionFactory, null, matchAlgorithm, reactionRepo));
        assertThrows(() -> MessageReactor.newInstance(actionFactory, actionRunner, null, reactionRepo));
        assertThrows(() -> MessageReactor.newInstance(actionFactory, actionRunner, matchAlgorithm, null));
    }
}