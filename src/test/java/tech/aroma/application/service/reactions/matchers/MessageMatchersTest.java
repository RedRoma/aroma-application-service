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

package tech.aroma.application.service.reactions.matchers;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Urgency;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MessageMatchersTest
{

    @GenerateString(UUID)
    private String appId;
        
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
        
        message.applicationId = appId;
    }

    @Test
    public void testMatchesAll()
    {
        MessageMatcher matcher = MessageMatchers.matchesAll();
        assertThat(matcher, notNullValue());

        assertMatchIs(matcher, true);
        assertMatchersMatchesNullOrEmpty(matcher);
    }

    @Test
    public void testMatchesNone()
    {
        MessageMatcher matcher = MessageMatchers.matchesNone();

        assertMatchIs(matcher, false);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    @Test
    public void testTitleContainsWhenMatch()
    {
        String substring = halfOf(message.title);

        MessageMatcher matcher = MessageMatchers.titleContains(substring);
        assertMatchIs(matcher, true);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    @Test
    public void testTitleContainsWhenNoMatch()
    {
        MessageMatcher matcher = MessageMatchers.titleContains(randomString);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testTitleContainsWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.titleContains(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBodyContainsWhenMatch()
    {
        String substring = halfOf(message.body);

        MessageMatcher matcher = MessageMatchers.bodyContains(substring);
        assertMatchIs(matcher, true);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    @Test
    public void testBodyContainsWhenNoMatch()
    {
        MessageMatcher matcher = MessageMatchers.bodyContains(randomString);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testBodyContainsWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.bodyContains(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBodyIsWhenMatch()
    {
        String expected = message.body;

        MessageMatcher matcher = MessageMatchers.bodyIs(expected);
        assertMatchIs(matcher, true);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    @Test
    public void testBodyIsWhenNoMatch()
    {
        MessageMatcher matcher = MessageMatchers.bodyIs(randomString);

        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testBodyIsWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.bodyIs(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testHostnameIsWhenMatch()
    {
        String expected = message.hostname;

        MessageMatcher matcher = MessageMatchers.hostnameIs(expected);
        assertMatchIs(matcher, true);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    @Test
    public void testHostnameIsWhenNoMatch()
    {
        MessageMatcher matcher = MessageMatchers.hostnameIs(randomString);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testHostnameIsWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.hostnameIs(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testUrgencyIsWhenMatch()
    {
        Urgency expected = message.urgency;

        MessageMatcher matcher = MessageMatchers.urgencyIs(expected);
        assertMatchIs(matcher, true);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
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

        MessageMatcher matcher = MessageMatchers.urgencyIs(urgency);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testUrgencyIsWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.urgencyIs(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testNotWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.not(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNotWhenMatch()
    {
        MessageMatcher neverMatch = MessageMatchers.matchesNone();
        MessageMatcher result = MessageMatchers.not(neverMatch);

        assertMatchIs(result, true);
        assertMatchersMatchesNullOrEmpty(result);
    }

    @Test
    public void testNotWhenNoMatch()
    {
        MessageMatcher alwaysMatch = MessageMatchers.matchesAll();
        MessageMatcher result = MessageMatchers.not(alwaysMatch);
        assertMatchIs(result, false);
    }

    @Test
    public void testNot()
    {
        MessageMatcher fakeMatcher = mock(MessageMatcher.class);

        boolean value = one(booleans());
        when(fakeMatcher.matches(message)).thenReturn(value);

        MessageMatcher matcher = MessageMatchers.not(fakeMatcher);
        assertThat(matcher, notNullValue());

        assertThat(matcher.matches(message), is(!value));
        verify(fakeMatcher).matches(message);
    }

    @Test
    public void testTitleIsWhenMatch()
    {
        String expected = message.title;

        MessageMatcher matcher = MessageMatchers.titleIs(expected);
        assertMatchIs(matcher, true);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    @Test
    public void testTitleIsWhenNoMatch()
    {
        MessageMatcher matcher = MessageMatchers.titleIs(randomString);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testTitleIsWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.titleIs(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testTitleIsNotWhenMatch()
    {
        message.title = randomString;
        String anotherRandomString = one(hexadecimalString(10));

        MessageMatcher matcher = MessageMatchers.titleIsNot(anotherRandomString);
        assertMatchIs(matcher, true);
        assertMatchersMatchesNullOrEmpty(matcher);
    }

    @Test
    public void testTitleIsNotWhenNoMatch()
    {
        message.title = randomString;
        MessageMatcher matcher = MessageMatchers.titleIsNot(randomString);
        assertMatchIs(matcher, false);
    }

    @DontRepeat
    @Test
    public void testTitleIsNotWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.titleIsNot(""));
    }

    @Test
    public void testBodyDoesNotContainWhenMatch()
    {
        MessageMatcher matcher = MessageMatchers.bodyDoesNotContain(randomString);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testBodyDoesNotContainWhenNoMatch()
    {
        String substring = halfOf(message.body);

        MessageMatcher matcher = MessageMatchers.bodyDoesNotContain(substring);
        assertMatchIs(matcher, false);
    }

    @Test
    public void testHostnameContainsWhenMatch()
    {
        String substring = halfOf(message.hostname);

        MessageMatcher matcher = MessageMatchers.hostnameContains(substring);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testHostnameContainsWhenNoMatch()
    {
        MessageMatcher matcher = MessageMatchers.hostnameContains(randomString);
        assertMatchIs(matcher, false);
    }
    
    @DontRepeat
    @Test
    public void testHostnameContainsWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.hostnameContains(""));
    }

    @Test
    public void testHostnameDoesNotContainWhenMatch()
    {
        MessageMatcher matcher = MessageMatchers.hostnameDoesNotContain(randomString);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testHostnameDoesNotContainWhenNoMatch()
    {
        String substring = halfOf(message.hostname);
        MessageMatcher matcher = MessageMatchers.hostnameDoesNotContain(substring);
        assertMatchIs(matcher, false);
    }

    @Test
    public void testUrgencyIsOneOfWhenMatch()
    {
        AlchemyGenerator<Urgency> generator = enumValueOf(Urgency.class);
        
        Set<Urgency> urgencies = Sets.createFrom(generator.get(), generator.get());
        message.urgency = Sets.oneOf(urgencies);
        
        MessageMatcher matcher = MessageMatchers.urgencyIsOneOf(urgencies);
        assertMatchIs(matcher, true);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    public void testUrgencyIsOneOfWhenNoMatch()
    {
        AlchemyGenerator<Urgency> generator = enumValueOf(Urgency.class);
        Set<Urgency> urgencies = Sets.createFrom(generator.get(), generator.get());
        
        while (urgencies.contains(message.urgency))
        {
            message.urgency = generator.get();
        }
        
        MessageMatcher matcher = MessageMatchers.urgencyIsOneOf(urgencies);
        assertMatchIs(matcher, false);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }
    
    @DontRepeat
    @Test
    public void testUrgencyIsOneOfWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.urgencyIsOneOf(null));
    }

    private void assertMatchIs(MessageMatcher matcher, boolean expectedValue)
    {
        assertThat(matcher, notNullValue());
        assertThat(matcher.matches(message), is(expectedValue));
    }

    private void assertMatchersDoesNotMatchNullOrEmpty(MessageMatcher matcher)
    {
        assertThat(matcher.matches(emptyMessage), is(false));
        assertThat(matcher.matches(null), is(false));
    }

    private void assertMatchersMatchesNullOrEmpty(MessageMatcher matcher)
    {
        assertThat(matcher.matches(emptyMessage), is(true));
        assertThat(matcher.matches(null), is(true));
    }

    private String halfOf(String string)
    {
        return string.substring(string.length() / 2);
    }

    @Test
    public void testApplicationIsWhenMatch()
    {
        MessageMatcher matcher = MessageMatchers.applicationIs(appId);
        assertMatchIs(matcher, true);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    @Test
    public void testApplicationIsWhenNoMatch()
    {
        String otherId = one(uuids);
        MessageMatcher matcher = MessageMatchers.applicationIs(otherId);
        assertMatchIs(matcher, false);
        assertMatchersDoesNotMatchNullOrEmpty(matcher);
    }

    @Test
    public void testApplicationIsWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.applicationIs(null));
        assertThrows(() -> MessageMatchers.applicationIs(""));
        assertThrows(() -> MessageMatchers.applicationIs(randomString));
    }

    @Test
    public void testApplicationIsNotWhenMatch()
    {
        String otherId = one(uuids);
        
        MessageMatcher matcher = MessageMatchers.applicationIsNot(otherId);
        assertMatchIs(matcher, true);
    }

    @Test
    public void testApplicationIsNotWhenNoMatch()
    {
        MessageMatcher matcher = MessageMatchers.applicationIsNot(appId);
        assertMatchIs(matcher, false);
    }

    @Test
    public void testApplicationIsNotWithBadArgs()
    {
        assertThrows(() -> MessageMatchers.applicationIs(null));
        assertThrows(() -> MessageMatchers.applicationIs(""));
        assertThrows(() -> MessageMatchers.applicationIs(randomString));
    }
}
