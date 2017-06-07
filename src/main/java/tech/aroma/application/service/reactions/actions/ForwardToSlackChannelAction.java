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

package tech.aroma.application.service.reactions.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Strings;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.reactions.ActionForwardToSlackChannel;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.annotations.objects.Pojo;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.http.HttpResponse;
import tech.sirwellington.alchemy.http.exceptions.AlchemyHttpException;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;

/**
 *
 * @author SirWellington
 */
@Internal
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class ForwardToSlackChannelAction implements Action
{

    private final static Logger LOG = LoggerFactory.getLogger(ForwardToSlackChannelAction.class);

    private final ActionForwardToSlackChannel slack;
    private final AlchemyHttp http;

    ForwardToSlackChannelAction(ActionForwardToSlackChannel slack, AlchemyHttp http)
    {
        checkThat(slack, http)
            .are(notNull());

        this.slack = slack;
        this.http = http;
    }

    @Override
    public List<Action> actOnMessage(Message message) throws TException
    {
        Action.checkMessage(message);

        checkThat(slack.webhookUrl)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Slack Webhook URL is not a valid URL: " + slack.webhookUrl)
            .is(validURL());

        Payload payload = createPayloadFor(message);

        URL webhookUrl;

        try
        {
            webhookUrl = new URL(slack.webhookUrl);
        }
        catch (MalformedURLException ex)
        {
            LOG.error("Failed to convert Slack Webhook URL: {}", slack.webhookUrl, ex);
            throw new OperationFailedException("Could not convert URL: " + ex.getMessage());
        }

        LOG.debug("Sending Message Payload to {} for Message {}", webhookUrl, message.title);

        http.go()
            .post()
            .body(payload)
            .accept("application/json", "text/plain", "text/javascript")
            .onSuccess(this::onSuccess)
            .onFailure(this::onFailure)
            .at(webhookUrl);

        return Lists.emptyList();
    }

    private void onSuccess(HttpResponse response)
    {
        LOG.debug("Successfully sent Slack Message | {}", response);
    }

    private void onFailure(AlchemyHttpException ex)
    {
        LOG.error("Failed to post Slack Message to {}", slack, ex);
    }

    private Payload createPayloadFor(Message message)
    {

        Field titleField = new Field();
        titleField.title = message.title;
        titleField.value = message.body;

        Field deviceField = new Field();
        deviceField.title = "From Device";
        deviceField.value = message.hostname;

        Attachment attachment = new Attachment();

        switch (message.urgency)
        {
            case HIGH:
                attachment.color = Attachment.COLOR_HIGH;
                break;
            case MEDIUM:
                attachment.color = Attachment.COLOR_MEDIUM;
                break;
            default:
                attachment.color = Attachment.COLOR_LOW;
                break;
        }

        attachment.fields = Lists.createFrom(titleField, deviceField);

        Payload payload = new Payload();
        payload.attachments.add(attachment);
        payload.text = String.format("*%s* - %s", message.applicationName, message.title);

        if (!Strings.isNullOrEmpty(slack.slackChannel))
        {
            payload.channel = slack.slackChannel;
        }

        return payload;
    }

    @Override
    public String toString()
    {
        return "ForwardToSlackChannelAction{" + "slack=" + slack + ", http=" + http + '}';
    }

    @Pojo
    @Internal
    static class Payload
    {

        private String icon_url = "https://raw.githubusercontent.com/RedRoma/Aroma/develop/Graphics/Logo.png";
        private String username = "Aroma";
        private String text = null;
        private String channel;
        private Boolean mrkdwn = true;

        private List<Attachment> attachments = Lists.create();

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.icon_url);
            hash = 17 * hash + Objects.hashCode(this.username);
            hash = 17 * hash + Objects.hashCode(this.text);
            hash = 17 * hash + Objects.hashCode(this.attachments);
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
            final Payload other = (Payload) obj;
            if (!Objects.equals(this.icon_url, other.icon_url))
            {
                return false;
            }
            if (!Objects.equals(this.username, other.username))
            {
                return false;
            }
            if (!Objects.equals(this.text, other.text))
            {
                return false;
            }
            if (!Objects.equals(this.attachments, other.attachments))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Payload{" + "icon_url=" + icon_url + ", username=" + username + ", text=" + text + ", attachments=" + attachments + '}';
        }

    }

    @Pojo
    @Internal
    static class Attachment
    {

        private static final String COLOR_LOW = "#037AFF";
        private static final String COLOR_MEDIUM = "#F8E71C";
        private static final String COLOR_HIGH = "#FB3E3C";

        private String fallback;
        private String pretext;
        private String color;
        private List<Field> fields = Lists.create();

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 71 * hash + Objects.hashCode(this.fallback);
            hash = 71 * hash + Objects.hashCode(this.pretext);
            hash = 71 * hash + Objects.hashCode(this.color);
            hash = 71 * hash + Objects.hashCode(this.fields);
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
            final Attachment other = (Attachment) obj;
            if (!Objects.equals(this.fallback, other.fallback))
            {
                return false;
            }
            if (!Objects.equals(this.pretext, other.pretext))
            {
                return false;
            }
            if (!Objects.equals(this.color, other.color))
            {
                return false;
            }
            if (!Objects.equals(this.fields, other.fields))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Attachment{" + "fallback=" + fallback + ", pretext=" + pretext + ", color=" + color + ", fields=" + fields + '}';
        }

    }

    @Pojo
    @Internal
    static class Field
    {

        private String title;
        private String value;
        private boolean isShort = false;

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.title);
            hash = 97 * hash + Objects.hashCode(this.value);
            hash = 97 * hash + (this.isShort ? 1 : 0);
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
            final Field other = (Field) obj;
            if (this.isShort != other.isShort)
            {
                return false;
            }
            if (!Objects.equals(this.title, other.title))
            {
                return false;
            }
            if (!Objects.equals(this.value, other.value))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Field{" + "title=" + title + ", value=" + value + ", isShort=" + isShort + '}';
        }

    }

}
