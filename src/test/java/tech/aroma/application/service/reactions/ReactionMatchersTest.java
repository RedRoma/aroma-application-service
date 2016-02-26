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

package tech.aroma.application.service.reactions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Urgency;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class ReactionMatchersTest
{

    @GeneratePojo
    private Message message;

    private Message emptyMessage;

    @GenerateString(HEXADECIMAL)
    private String randomString;

    @Before
    public void setUp() throws Exception
    {
        setupData();
    }

    private void setupData() throws Exception
    {
        emptyMessage = new Message();
        emptyMessage.unsetUrgency();
    }

    @Test
    public void testMatchesAll()
    {
        ReactionMatcher matcher = ReactionMatchers.matchesAll();
        assertThat(matcher, notNullValue());

        assertThat(matcher.matches(null), is(true));
        assertThat(matcher.matches(emptyMessage), is(true));
        assertThat(matcher.matches(message), is(true));
    }

    @Test
    public void testMatchesNone()
    {
        ReactionMatcher matcher = ReactionMatchers.matchesNone();

        assertMatchIs(matcher, false);
        assertThat(matcher.matches(null), is(false));
    }

    @Test
    public void testTitleContainsWhenMatch()
    {
        String substring = message.title.substring(message.title.length() / 2);

        ReactionMatcher matcher = ReactionMatchers.titleContains(substring);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testTitleContainsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.titleContains(randomString);
        assertMatchIs(matcher, false);
    }

    @Test
    public void testTitleEqualsWhenMatch()
    {
        String expected = message.title;

        ReactionMatcher matcher = ReactionMatchers.titleEquals(expected);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testTitleEqualsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.titleEquals(randomString);
        assertMatchIs(matcher, false);
    }

    @Test
    public void testBodyContainsWhenMatch()
    {
        String substring = message.body.substring(message.body.length() / 2);

        ReactionMatcher matcher = ReactionMatchers.bodyContains(substring);
        assertMatchIs(matcher, true);
    }
    
    @Test
    public void testBodyContainsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.bodyContains(randomString);
        assertMatchIs(matcher, false);
    }

    @Test
    public void testBodyEqualsWhenMatch()
    {
        String expected = message.body;
        
        ReactionMatcher matcher = ReactionMatchers.bodyEquals(expected);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testBodyEqualsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.bodyEquals(randomString);
        
        assertMatchIs(matcher, false);
    }

    @Test
    public void testHostnameEqualsWhenMatch()
    {
        String expected = message.hostname;
        
        ReactionMatcher matcher = ReactionMatchers.hostnameEquals(expected);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testHostnameEqualsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.hostnameEquals(randomString);
        assertMatchIs(matcher, false);
    }

    @Test
    public void testUrgencyEqualsWhenMatch()
    {
        Urgency expected = message.urgency;
        
        ReactionMatcher matcher = ReactionMatchers.urgencyEquals(expected);
        assertMatchIs(matcher, true);
    }
    
    @Test
    public void testUrgencyEqualsWhenNoMatch()
    {
        AlchemyGenerator<Urgency> urgencies = enumValueOf(Urgency.class);
        
        Urgency urgency = one(urgencies);
        
        while(urgency == message.urgency)
        {
            urgency = one(urgencies);
        }
        
        ReactionMatcher matcher = ReactionMatchers.urgencyEquals(urgency);
        assertMatchIs(matcher, false);
    }

    private void assertMatchIs(ReactionMatcher matcher, boolean expectedValue)
    {
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(expectedValue));
        assertThat(matcher.matches(emptyMessage), is(false));
    }
}
