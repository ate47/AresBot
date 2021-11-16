package fr.atesab.ares.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BotReading {
    private List<ServerReading> servers = new ArrayList<>();

    public void addServer(ServerReading reading) {
        servers.add(reading);
    }
}
