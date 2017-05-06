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

import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.FactoryPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;


/**
 *
 * @author SirWellington
 */
@FactoryPattern(role = PRODUCT)
@StrategyPattern(role = INTERFACE)
public interface MessageMatcher
{
    boolean matches(Message message);
}
