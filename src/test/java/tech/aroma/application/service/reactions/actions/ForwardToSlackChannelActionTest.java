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

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.thrift.reactions.ActionForwardToSlackChannel;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class ForwardToSlackChannelActionTest
{

    @Mock
    private AlchemyHttp http;

    @GeneratePojo
    private ActionForwardToSlackChannel slack;

    private ForwardToSlackChannelAction instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new ForwardToSlackChannelAction(slack, http);
        verifyZeroInteractions(http);
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {

    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new ForwardToSlackChannelAction(null, http));
        assertThrows(() -> new ForwardToSlackChannelAction(slack, null));
    }

    @Test
    public void testActOnMessage() throws Exception
    {
    }

    @DontRepeat
    @Test
    public void testWithBadArgs()
    {
        assertThrows(() -> instance.actOnMessage(null))
            .isInstanceOf(TException.class);
    }

    @Test
    public void testToString()
    {
        assertThat(instance.toString(), not(isEmptyOrNullString()));
    }

}