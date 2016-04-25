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

import java.util.List;
import java.util.Objects;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.reactions.AromaMatcher;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static java.util.stream.Collectors.toList;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
public interface MatchAlgorithm
{

    boolean matches(@Required Message message, @NonEmpty List<AromaMatcher> matchers);
    

    static class AndImpl implements MatchAlgorithm
    {

        private MatcherFactory matcherFactory;

        @Override
        public boolean matches(Message message, List<AromaMatcher> matchers)
        {
            if (Lists.isEmpty(matchers))
            {
                return false;
            }

            List<ReactionMatcher> reactionMatchers = matchers
                .stream()
                .map(matcherFactory::matcherFor)
                .filter(Objects::nonNull)
                .collect(toList());

            long totalMatches = reactionMatchers.stream()
                .filter(matcher -> matcher.matches(message))
                .count();

            return totalMatches == matchers.size();
        }

    }

    static class OrImpl implements MatchAlgorithm
    {

        private MatcherFactory factory;

        @Override
        public boolean matches(Message message, List<AromaMatcher> matchers)
        {
            if (Lists.isEmpty(matchers))
            {
                return false;
            }

            long totalMatches = matchers.stream()
                .map(factory::matcherFor)
                .filter(Objects::nonNull)
                .filter(matcher -> matcher.matches(message))
                .count();

            return totalMatches > 0;
        }
    }

}
