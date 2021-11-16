package fr.atesab.ares;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class AresBotConfig {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * read bot config or create a new one
     * 
     * @param file the config file to read
     * @return the config
     */
    public static AresBotConfig readFile(String file) {
        AresBotConfig config;
        try (var w = new FileInputStream(file)) {
            config = OBJECT_MAPPER.readValue(w, AresBotConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            config = new AresBotConfig();
        }
        config.saveFile(file);
        return config;
    }

    private List<Long> serverIds = new ArrayList<>();

    /**
     * save the config file
     * 
     * @param file the file to write
     */
    public void saveFile(String file) {
        try {
            OBJECT_MAPPER.writeValue(new File(file), this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
