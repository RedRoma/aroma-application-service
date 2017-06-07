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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Urgency;
import tech.aroma.thrift.reactions.*;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class MatcherFactoryImplTest
{

    private AromaMatcher matcher;

    @GenerateString(UUID)
    private String appId;

    @GeneratePojo
    private Message message;

    @GenerateString(HEXADECIMAL)
    private String randomString;

    private MatcherFactoryImpl instance;

    @Before
    public void setUp() throws Exception
    {
        setupData();

        instance = new MatcherFactoryImpl();
    }

    private void setupData() throws Exception
    {
        message.applicationId = appId;

        matcher = new AromaMatcher();
    }

    @Test
    public void testNull() throws Exception
    {
        MessageMatcher result = instance.matcherFor(null);
        assertThat(result, notNullValue());

        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testEmpty()
    {
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());

        assertThat(result.matches(message), is(false));
    }

    @Test
    public void testAll()
    {
        matcher.setAll(new MatcherAll());

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());

        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testApplicationIsWhenMatches()
    {
        matcher.setApplicationIs(new MatcherApplicationIs(appId));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testApplicationIsWhenNoMatch()
    {
        String anotherId = one(uuids);
        matcher.setApplicationIs(new MatcherApplicationIs(anotherId));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testApplicationIsWithBadArgs()
    {
        matcher.setApplicationIs(new MatcherApplicationIs(randomString));
        assertThrows(() -> instance.matcherFor(matcher));

        matcher.setApplicationIs(new MatcherApplicationIs());
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testApplicationIsNotWhenMatch()
    {
        String anotherId = one(uuids);
        matcher.setApplicationIsNot(new MatcherApplicationIsNot(anotherId));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testApplicationIsNotWhenNoMatch()
    {
        matcher.setApplicationIsNot(new MatcherApplicationIsNot(appId));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testApplicationIsNotWithBadArgs()
    {
        matcher.setApplicationIsNot(new MatcherApplicationIsNot());
        assertThrows(() -> instance.matcherFor(matcher));

        matcher.setApplicationIsNot(new MatcherApplicationIsNot(randomString));
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testBodyContainsWhenMatches()
    {
        String substring = halfOf(message.body);
        matcher.setBodyContains(new MatcherBodyContains(substring));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testBodyContainsWhenNoMatch()
    {
        matcher.setBodyContains(new MatcherBodyContains(randomString));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testBodyContainsWithBadArgs()
    {
        matcher.setBodyContains(new MatcherBodyContains());
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testBodyDoesNotContainWhenMatches()
    {
        matcher.setBodyDoesNotContain(new MatcherBodyDoesNotContain(randomString));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testBodyDoesNotContainWhenNoMatch()
    {
        String substring = halfOf(message.body);
        matcher.setBodyDoesNotContain(new MatcherBodyDoesNotContain(substring));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testBodyDoesNotContainWithBadArgs()
    {
        matcher.setBodyDoesNotContain(new MatcherBodyDoesNotContain());
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testBodyIsWhenMatches()
    {
        String expected = message.body;
        matcher.setBodyIs(new MatcherBodyIs(expected));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testBodyIsWhenNoMatch()
    {
        matcher.setBodyIs(new MatcherBodyIs(randomString));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }

    @Test
    public void testTitleIsWhenMatch()
    {
        String expected = message.title;
        matcher.setTitleIs(new MatcherTitleIs(expected));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testTitleIsWhenNoMatch()
    {
        matcher.setTitleIs(new MatcherTitleIs(randomString));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testTitleIsWithBadArgs()
    {
        matcher.setTitleIs(new MatcherTitleIs());
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testTitleContainsWhenMatch()
    {
        String substring = halfOf(message.title);
        matcher.setTitleContains(new MatcherTitleContains(substring));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testTitleContainsWhenNoMatch()
    {
        matcher.setTitleContains(new MatcherTitleContains(randomString));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testTitleContainsWithBadArgs()
    {
        matcher.setTitleContains(new MatcherTitleContains());
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testTitleDoesNotContainWhenMatch()
    {
        matcher.setTitleDoesNotContain(new MatcherTitleDoesNotContain(randomString));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testTitleDoesNotContainWhenNoMatch()
    {
        String substring = halfOf(message.title);
        matcher.setTitleDoesNotContain(new MatcherTitleDoesNotContain(substring));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testTitleDoesNotContainWithBadArgs()
    {
        matcher.setTitleDoesNotContain(new MatcherTitleDoesNotContain());
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testHostnameIsWhenMatch()
    {
        String expected = message.hostname;
        matcher.setHostnameIs(new MatcherHostnameIs(expected));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testHostnameIsWhenNoMatch()
    {
        matcher.setHostnameIs(new MatcherHostnameIs(randomString));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testHostnameIsWithBadArgs()
    {
        matcher.setHostnameIs(new MatcherHostnameIs());
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testHostnameContainsWhenMatch()
    {
        String substring = halfOf(message.hostname);
        matcher.setHostnameContains(new MatcherHostnameContains(substring));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testHostnameContainsWhenNoMatch()
    {
        matcher.setHostnameContains(new MatcherHostnameContains(randomString));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result.matches(message), is(false));
    }

    @DontRepeat
    @Test
    public void testHostnameContainsWithBadArgs()
    {
        matcher.setHostnameContains(new MatcherHostnameContains());
        assertThrows(() -> instance.matcherFor(matcher));
    }
    
    @Test
    public void testHostnameDoesNotContainWhenMatch()
    {
        matcher.setHostnameDoesNotContain(new MatcherHostnameDoesNotContain(randomString));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result.matches(message), is(true));
    }
    
    @Test
    public void testHostnameDoesNotContainWhenNoMatch()
    {
        String substring = halfOf(message.hostname);
        matcher.setHostnameDoesNotContain(new MatcherHostnameDoesNotContain(substring));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result.matches(message), is(false));
    }
    
    @DontRepeat
    @Test
    public void testHostnameDoesNotContainWithBadArgs()
    {
        matcher.setHostnameDoesNotContain(new MatcherHostnameDoesNotContain());
        assertThrows(() -> instance.matcherFor(matcher));
    }

    @Test
    public void testUrgencyIsWhenMatch()
    {
        Urgency expected = message.urgency;
        matcher.setUrgencyEquals(new MatcherUrgencyIs(Sets.createFrom(expected)));

        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(true));
    }

    @Test
    public void testUrgencyIsWhenNoMatch()
    {
        AlchemyGenerator<Urgency> urgencies = enumValueOf(Urgency.class);
        Urgency expected = one(urgencies);

        while (expected == message.urgency)
        {
            expected = one(urgencies);
        }

        matcher.setUrgencyEquals(new MatcherUrgencyIs(Sets.createFrom(expected)));
        MessageMatcher result = instance.matcherFor(matcher);
        assertThat(result, notNullValue());
        assertThat(result.matches(message), is(false));
    }
    
    @DontRepeat
    @Test
    public void testUrgencyIsWithBadArgs()
    {
        matcher.setUrgencyEquals(new MatcherUrgencyIs(Sets.emptySet()));
        assertThrows(() -> instance.matcherFor(matcher));
    }

    private String halfOf(String string)
    {
        return string.substring(0, string.length() / 2);
    }
}
