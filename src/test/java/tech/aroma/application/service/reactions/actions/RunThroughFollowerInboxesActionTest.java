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
import org.mockito.Mock;
import tech.aroma.data.FollowerRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.User;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.MessageGenerators.messages;
import static tech.aroma.thrift.generators.UserGenerators.users;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class RunThroughFollowerInboxesActionTest
{

    @Mock
    private ActionFactory factory;

    @Mock
    private FollowerRepository followerRepo;
    
    @Mock
    private Action genericAction;
    
    @Mock
    private Action actionToRunThroughInbox;
    
    private RunThroughFollowerInboxesAction instance;

    private Message message;
    private String appId;
    
    private List<User> followers;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
        
        instance = new RunThroughFollowerInboxesAction(factory, followerRepo);
    }

    private void setupData() throws Exception
    {
        message = one(messages());
        
        followers = listOf(users());
        appId = message.applicationId;
    }

    private void setupMocks() throws Exception
    {
        when(followerRepo.getApplicationFollowers(appId))
            .thenReturn(followers);

        when(factory.actionFor(any())).thenReturn(genericAction);
        when(factory.actionToRunThroughInbox(any())).thenReturn(actionToRunThroughInbox);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new RunThroughFollowerInboxesAction(null, followerRepo));
        assertThrows(() -> new RunThroughFollowerInboxesAction(factory, null));
    }
    
    @Test
    public void testActOnMessage() throws Exception
    {
        List<Action> actions = instance.actOnMessage(message);
        assertThat(actions, notNullValue());
        assertThat(actions, not(empty()));
        
        followers.forEach(follower -> verify(factory).actionToRunThroughInbox(follower));
    }

    @Test
    public void testToString()
    {
        assertThat(instance.toString(), not(isEmptyOrNullString()));
    }

}
