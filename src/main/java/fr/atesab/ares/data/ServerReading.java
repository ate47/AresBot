package fr.atesab.ares.data;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
public class ServerReading {
    @NonNull
    private String name;
    private List<ChannelReading> channels = new ArrayList<>();

    public void addChannel(ChannelReading reading) {
        this.channels.add(reading);
    }
}
