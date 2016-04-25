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
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
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

    @DontRepeat
    @Test
    public void testTitleContainsWithBadArgs()
    {
        assertThrows(() -> ReactionMatchers.titleContains(""))
            .isInstanceOf(IllegalArgumentException.class);
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

    @DontRepeat
    @Test
    public void testBodyContainsWithBadArgs()
    {
        assertThrows(() -> ReactionMatchers.bodyContains(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBodyIsWhenMatch()
    {
        String expected = message.body;

        ReactionMatcher matcher = ReactionMatchers.bodyIs(expected);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testBodyIsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.bodyIs(randomString);

        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testBodyIsWithBadArgs()
    {
        assertThrows(() -> ReactionMatchers.bodyIs(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testHostnameIsWhenMatch()
    {
        String expected = message.hostname;

        ReactionMatcher matcher = ReactionMatchers.hostnameIs(expected);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testHostnameIsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.hostnameIs(randomString);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testHostnameIsWithBadArgs()
    {
        assertThrows(() -> ReactionMatchers.hostnameIs(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testUrgencyIsWhenMatch()
    {
        Urgency expected = message.urgency;

        ReactionMatcher matcher = ReactionMatchers.urgencyIs(expected);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testUrgencyIsWhenNoMatch()
    {
        AlchemyGenerator<Urgency> urgencies = enumValueOf(Urgency.class);

        Urgency urgency = one(urgencies);

        while (urgency == message.urgency)
        {
            urgency = one(urgencies);
        }

        ReactionMatcher matcher = ReactionMatchers.urgencyIs(urgency);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testUrgencyIsWithBadArgs()
    {
        assertThrows(() -> ReactionMatchers.urgencyIs(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testNotWithBadArgs()
    {
        assertThrows(() -> ReactionMatchers.not(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNotWhenMatch()
    {
        ReactionMatcher neverMatch = ReactionMatchers.matchesNone();
        ReactionMatcher result = ReactionMatchers.not(neverMatch);

        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
        assertThat(result.matches(emptyMessage), is(true));

    }

    @Test
    public void testNotWhenNoMatch()
    {
        ReactionMatcher alwaysMatch = ReactionMatchers.matchesAll();
        ReactionMatcher result = ReactionMatchers.not(alwaysMatch);
        assertMatchIs(result, false);
    }

    private void assertMatchIs(ReactionMatcher matcher, boolean expectedValue)
    {
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(expectedValue));
        assertThat(matcher.matches(emptyMessage), is(false));
        assertThat(matcher.matches(null), is(false));
    }

    @Test
    public void testNot()
    {
        ReactionMatcher fakeMatcher = mock(ReactionMatcher.class);
        
        boolean value = one(booleans());
        when(fakeMatcher.matches(message)).thenReturn(value);
        
        ReactionMatcher matcher = ReactionMatchers.not(fakeMatcher);
        assertThat(matcher, notNullValue());
        
        assertThat(matcher.matches(message), is(!value));
        verify(fakeMatcher).matches(message);
    }

    @Test
    public void testTitleContains()
    {
        String substring = randomString.substring(randomString.length() / 2);
        message.title = randomString;
        
        ReactionMatcher matcher = ReactionMatchers.titleContains(substring);
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(true));
    }
    

    @Test
    public void testTitleIs()
    {
        message.title = randomString;
        
        ReactionMatcher matcher = ReactionMatchers.titleIs(randomString);
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(true));
    }
    
    
    @Test
    public void testTitleIsWhenMatch()
    {
        String expected = message.title;

        ReactionMatcher matcher = ReactionMatchers.titleIs(expected);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testTitleIsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.titleIs(randomString);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testTitleIsWithBadArgs()
    {
        assertThrows(() -> ReactionMatchers.titleIs(""))
            .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    public void testTitleIsNotWhenMatch()
    {
        message.title = randomString;
        String anotherRandomString = one(hexadecimalString(10));
        
        ReactionMatcher matcher = ReactionMatchers.titleIsNot(anotherRandomString);
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(true));
        
    }
    
    @Test
    public void testTitleIsNotWhenNoMatch()
    {
        message.title = randomString;
        ReactionMatcher matcher = ReactionMatchers.titleIsNot(randomString);
        assertThat(matcher.matches(message), is(false));
    }
    
    @Test
    public void testBodyDoesNotContainWhenMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.bodyDoesNotContain(randomString);
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(true));
    }
    
    @Test
    public void testBodyDoesNotContainWhenNoMatch()
    {
        String substring = message.body.substring(message.body.length() / 2);
        
        ReactionMatcher matcher = ReactionMatchers.bodyDoesNotContain(substring);
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(false));
    }
    
    @Test
    public void testHostnameContainsWhenMatch()
    {
        String substring = message.hostname.substring(message.hostname.length() / 2);
        
        ReactionMatcher matcher = ReactionMatchers.hostnameContains(substring);
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(true));
    }
    
    @Test
    public void testHostnameContainsWhenNoMatch()
    {
        ReactionMatcher matcher = ReactionMatchers.hostnameContains(randomString);
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(false));
    }

    @Test
    public void testHostnameDoesNotContain()
    {
    }

    @Test
    public void testUrgencyIs()
    {
    }

    @Test
    public void testUrgencyIsOneOf()
    {
    }

}
