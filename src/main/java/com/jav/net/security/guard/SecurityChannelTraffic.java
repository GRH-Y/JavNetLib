package com.jav.net.security.guard;

import com.jav.net.security.channel.base.ChannelTrafficInfo;

import java.util.*;

public class SecurityChannelTraffic {

    private SecurityChannelTraffic() {
    }

    private static class InnerCore {
        public static final SecurityChannelTraffic sMater = new SecurityChannelTraffic();
    }

    public static SecurityChannelTraffic getInstance() {
        return SecurityChannelTraffic.InnerCore.sMater;
    }

    private final Map<String, ChannelTrafficInfo> mChannelCache = new HashMap<>();

    public void monitorTraffic(String machineId, long inTraffic, long outTraffic) {
        if (machineId == null) {
            return;
        }
        ChannelTrafficInfo trafficInfo = mChannelCache.get(machineId);
        if (trafficInfo == null) {
            trafficInfo = new ChannelTrafficInfo(machineId);
            mChannelCache.put(machineId, trafficInfo);
        }
        trafficInfo.updateTraffic(inTraffic, outTraffic);
    }


    public ChannelTrafficInfo getChannelTrafficInfo(String channelId) {
        if (channelId == null) {
            return null;
        }
        return mChannelCache.get(channelId);
    }


    public List<ChannelTrafficInfo> getAllChannelTrafficInfo() {
        if (mChannelCache.isEmpty()) {
            return null;
        }
        Collection<ChannelTrafficInfo> collection = mChannelCache.values();
        ArrayList copy = new ArrayList<>(collection);
        for (ChannelTrafficInfo value : collection) {
            copy.add(value);
        }
        return copy;
    }

}
