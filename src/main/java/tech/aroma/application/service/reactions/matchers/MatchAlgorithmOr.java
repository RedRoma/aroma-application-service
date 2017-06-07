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

import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.reactions.AromaMatcher;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static java.util.stream.Collectors.toList;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class MatchAlgorithmOr implements MatchAlgorithm
{

    private final static Logger LOG = LoggerFactory.getLogger(MatchAlgorithmOr.class);
    private final MatcherFactory matcherFactory;

    @Inject
    MatchAlgorithmOr(MatcherFactory matcherFactory)
    {
        checkThat(matcherFactory)
            .is(notNull());

        this.matcherFactory = matcherFactory;
    }

    @Override
    public boolean matches(Message message, List<AromaMatcher> matchers)
    {
        if (Lists.isEmpty(matchers))
        {
            return false;
        }

        List<MessageMatcher> reactionMatchers = matchers
            .stream()
            .map(matcherFactory::matcherFor)
            .filter(Objects::nonNull)
            .collect(toList());

        long totalMatches = reactionMatchers.stream()
            .filter(matcher -> matcher.matches(message))
            .count();

        return totalMatches >= 1;
    }

    @Override
    public String toString()
    {
        return "OrMatchAlgorithm{" + "matcherFactory=" + matcherFactory + '}';
    }

}
