package fr.atesab.ares;

import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.reactivestreams.Publisher;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import fr.atesab.ares.data.BotReading;
import fr.atesab.ares.data.ChannelReading;
import fr.atesab.ares.data.MessageReading;
import fr.atesab.ares.data.ServerReading;
import lombok.Getter;
import reactor.core.publisher.Mono;

public class AresBot {
    private static final String CONFIG_FILE = "ares.json";

    public static void main(String[] args) {
        var options = new Options()
                // bot token
                .addOption("t", "token", true, "Bot token");

        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        var token = cmd.getOptionValue("t");

        if (token == null) {
            token = System.getenv("ARES_BOT_TOKEN");
            if (token == null) {
                System.err.println("-t --token or ARES_BOT_TOKEN env can't both be null");
                return;
            }
        }
        var bot = new AresBot(token);
        bot.startClient();
    }

    @Getter
    private DiscordClient client;
    @Getter
    private AresBotConfig config;

    public AresBot(String token) {
        client = DiscordClient.create(token);
        config = AresBotConfig.readFile(CONFIG_FILE);
    }

    /**
     * Start the bot
     */
    public void startClient() {
        var gateway = client.login().block();
        var reading = new BotReading();
        for (var serverId : config.getServerIds()) {
            gateway.getGuildById(Snowflake.of(serverId)).subscribe(server -> {
                var serverReading = new ServerReading(server.getName());
                reading.addServer(serverReading);
                server.getChannels().filter(c -> c instanceof GuildMessageChannel).map(c -> (GuildMessageChannel) c)
                        .subscribe(channel -> {
                            var channelReading = new ChannelReading(channel.getName());
                            serverReading.addChannel(channelReading);
                            channel.getMessagesBefore(channel.getLastMessage().block().getChannelId())
                                    .subscribe(msg -> {
                                        var messageReading = new MessageReading(msg.getChannelId());
                                        messageReading.readReaction(msg.getReactions());
                                        channelReading.addMessage(messageReading);
                                    });
                        });
            });
        }
    }
}
