package fr.atesab.ares.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;

@Data
public class ChannelReading {
    @NonNull
    private String name;
    private List<MessageReading> servers = new ArrayList<>();

    public void addMessage(MessageReading messageReading) {
        servers.add(messageReading);
    }
}
