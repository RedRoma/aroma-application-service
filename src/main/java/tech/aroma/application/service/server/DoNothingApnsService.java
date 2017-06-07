/*
 * Copyright 2017 RedRoma, Inc..
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

 
package tech.aroma.application.service.server;


import java.util.*;

import com.notnoop.apns.*;
import com.notnoop.exceptions.NetworkIOException;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 *
 * @author SirWellington
 */
@Internal
final class DoNothingApnsService implements ApnsService 
{

    public DoNothingApnsService()
    {
    }

    @Override
    public ApnsNotification push(String deviceToken, String payload) throws NetworkIOException
    {
        return null;
    }

    @Override
    public EnhancedApnsNotification push(String deviceToken, String payload, Date expiry) throws NetworkIOException
    {
        return null;
    }

    @Override
    public ApnsNotification push(byte[] deviceToken, byte[] payload) throws NetworkIOException
    {
        return null;
    }

    @Override
    public EnhancedApnsNotification push(byte[] deviceToken, byte[] payload, int expiry) throws NetworkIOException
    {
        return null;
    }

    @Override
    public Collection<? extends ApnsNotification> push(Collection<String> deviceTokens, String payload) throws NetworkIOException
    {
        return Sets.emptySet();
    }

    @Override
    public Collection<? extends EnhancedApnsNotification> push(Collection<String> deviceTokens, String payload, Date expiry) throws
                                                                                                                                    NetworkIOException
    {
        return Sets.emptySet();
    }

    @Override
    public Collection<? extends ApnsNotification> push(Collection<byte[]> deviceTokens, byte[] payload) throws NetworkIOException
    {
        return Sets.emptySet();
    }

    @Override
    public Collection<? extends EnhancedApnsNotification> push(Collection<byte[]> deviceTokens, byte[] payload, int expiry) throws
                                                                                                                                   NetworkIOException
    {
        return Sets.emptySet();
    }

    @Override
    public void push(ApnsNotification message) throws NetworkIOException
    {
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }

    @Override
    public Map<String, Date> getInactiveDevices() throws NetworkIOException
    {
        return Maps.create();
    }

    @Override
    public void testConnection() throws NetworkIOException
    {
    }

}
