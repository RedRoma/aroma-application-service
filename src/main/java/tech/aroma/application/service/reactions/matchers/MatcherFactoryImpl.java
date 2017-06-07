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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.Urgency;
import tech.aroma.thrift.reactions.AromaMatcher;
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern;

import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern.Role.FACTORY;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptySet;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
@FactoryPattern(role = FACTORY)
final class MatcherFactoryImpl implements MatcherFactory
{

    private final static Logger LOG = LoggerFactory.getLogger(MatcherFactoryImpl.class);

    @Override
    public MessageMatcher matcherFor(AromaMatcher matcher)
    {
        if (matcher == null || matcher.isSetAll())
        {
            return MessageMatchers.matchesAll();
        }

        if (matcher.isSetApplicationIs())
        {
            String appId = matcher.getApplicationIs().getAppId();
            checkThat(appId)
                .is(validApplicationId());

            return MessageMatchers.applicationIs(appId);
        }

        if (matcher.isSetApplicationIsNot())
        {
            String appId = matcher.getApplicationIsNot().getAppId();
            checkThat(appId).is(validApplicationId());

            return MessageMatchers.applicationIsNot(appId);
        }

        if (matcher.isSetBodyContains())
        {
            String substring = matcher.getBodyContains().getSubstring();

            checkThat(substring)
                .usingMessage("Matcher substring cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.bodyContains(substring);
        }

        if (matcher.isSetBodyDoesNotContain())
        {
            String substring = matcher.getBodyDoesNotContain().getSubstring();
            checkThat(substring)
                .usingMessage("Matcher substring cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.bodyDoesNotContain(substring);
        }

        if (matcher.isSetBodyIs())
        {
            String expectedBody = matcher.getBodyIs().getExpectedBody();

            checkThat(expectedBody)
                .usingMessage("Expected Body cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.bodyIs(expectedBody);
        }

        if (matcher.isSetTitleContains())
        {
            String substring = matcher.getTitleContains().getSubstring();

            checkThat(substring)
                .usingMessage("Matcher substring cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.titleContains(substring);
        }

        if (matcher.isSetTitleDoesNotContain())
        {
            String substring = matcher.getTitleDoesNotContain().getSubstring();
            checkThat(substring)
                .usingMessage("Matcher substring cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.not(MessageMatchers.titleContains(substring));
        }

        if (matcher.isSetTitleIs())
        {
            String expectedTitle = matcher.getTitleIs().getExpectedTitle();

            checkThat(expectedTitle)
                .usingMessage("Expected Message Title cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.titleIs(expectedTitle);
        }

        if (matcher.isSetTitleIsNot())
        {
            String title = matcher.getTitleIsNot().getTitle();
            checkThat(title)
                .usingMessage("Message Title cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.titleIsNot(title);
        }

        if (matcher.isSetUrgencyEquals())
        {
            Set<Urgency> expectedUrgencies = Sets.nullToEmpty(matcher.getUrgencyEquals().getPossibleUrgencies());

            checkThat(expectedUrgencies)
                .usingMessage("Expected Urgency cannot be empty")
                .is(nonEmptySet());

            return MessageMatchers.urgencyIsOneOf(expectedUrgencies);
        }

        if (matcher.isSetHostnameIs())
        {
            String expectedHostname = matcher.getHostnameIs().getExpectedHostname();

            checkThat(expectedHostname)
                .usingMessage("Expected Hostname cannote be empty")
                .is(nonEmptyString());

            return MessageMatchers.hostnameIs(expectedHostname);
        }

        if (matcher.isSetHostnameContains())
        {
            String substring = matcher.getHostnameContains().getSubstring();
            checkThat(substring)
                .usingMessage("Hostname substring cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.hostnameContains(substring);
        }

        if (matcher.isSetHostnameDoesNotContain())
        {
            String substring = matcher.getHostnameDoesNotContain().getSubstring();
            checkThat(substring)
                .usingMessage("Hostname substring cannot be empty")
                .is(nonEmptyString());

            return MessageMatchers.hostnameDoesNotContain(substring);
        }

        return MessageMatchers.matchesNone();
    }

}
