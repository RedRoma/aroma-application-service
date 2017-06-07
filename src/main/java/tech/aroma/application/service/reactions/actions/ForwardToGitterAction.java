/*
 * Copyright 2017 RedRoma.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.reactions.ActionForwardToGitter;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.annotations.objects.Pojo;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.http.HttpResponse;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.aroma.thrift.Urgency.HIGH;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 *
 * @author SirWellington
 */
@Internal
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class ForwardToGitterAction implements Action
{

    private final static Logger LOG = LoggerFactory.getLogger(ForwardToGitterAction.class);

    private final AlchemyHttp http;
    private final ActionForwardToGitter gitter;

    ForwardToGitterAction(AlchemyHttp http, ActionForwardToGitter gitter)
    {
        checkThat(http, gitter)
            .are(notNull());

        checkThat(gitter.gitterWebhookUrl)
            .is(nonEmptyString())
            .is(validURL());

        this.http = http;
        this.gitter = gitter;
    }

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        Action.checkMessage(message);

        URL url;

        try
        {
            url = new URL(gitter.gitterWebhookUrl);
        }
        catch (MalformedURLException ex)
        {
            LOG.warn("Failed to convert Gitter Webhook to URL", ex);
            throw new InvalidArgumentException("Gitter URL Invalid: " + gitter.gitterWebhookUrl);
        }

        GitterMessage gitterMessage = GitterMessage.createFrom(message, gitter);

        http.go()
            .post()
            .body(gitterMessage)
            .onSuccess(this::onSuccess)
            .onFailure(this::onFailure)
            .at(url);

        return Lists.emptyList();
    }

    private void onSuccess(HttpResponse response)
    {
        LOG.debug("Successfully posted message to Gitter Webhook");
    }

    private void onFailure(AlchemyHttpException ex)
    {
        LOG.error("Failed to post to Gitter", ex);
    }

    @Override
    public String toString()
    {
        return "ForwardToGitterAction{" + "http=" + http + ", gitter=" + gitter + '}';
    }

    @Pojo
    @Internal
    static class GitterMessage
    {

        static final String GITTER_LEVEL_INFO = "info";
        static final String GITTER_LEVEL_ERROR = "error";

        private String message;
        private String level = GITTER_LEVEL_INFO;

        private GitterMessage()
        {
        }

        private static GitterMessage createFrom(Message message, ActionForwardToGitter gitter)
        {
            GitterMessage gitterMessage = new GitterMessage();

            if (message.urgency == HIGH)
            {
                gitterMessage.level = GitterMessage.GITTER_LEVEL_ERROR;
            }

            gitterMessage.message = String.format("**%s** - *via Aroma*\n**%s**\n\nFrom Device: %s", message.applicationName,
                                                  message.title,
                                                  message.hostname);

            if (gitter.includeBody)
            {
                gitterMessage.message += String.format("\n\n%s", message.body);
            }

            return gitterMessage;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.message);
            hash = 29 * hash + Objects.hashCode(this.level);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final GitterMessage other = (GitterMessage) obj;
            if (!Objects.equals(this.message, other.message))
            {
                return false;
            }
            if (!Objects.equals(this.level, other.level))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "GitterMessage{" + "message=" + message + ", level=" + level + '}';
        }

    }

}
