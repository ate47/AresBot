package fr.atesab.ares;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import fr.atesab.ares.data.BotReading;
import fr.atesab.ares.data.ChannelReading;
import fr.atesab.ares.data.ServerReading;
import lombok.Getter;
import reactor.core.publisher.Flux;
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
        var messageCount = Flux.fromIterable(config.getServerIds()).flatMap(serverId -> {
            return gateway.getGuildById(Snowflake.of(serverId));
        }).flatMap(server -> {
            var serverReading = new ServerReading(server.getName());
            reading.addServer(serverReading);

            return server.getChannels().ofType(GuildMessageChannel.class).zipWith(infiniteFlux(serverReading));
        }).flatMap(tuple -> {
            var channel = tuple.getT1();
            var serverReading = tuple.getT2();

            var channelReading = new ChannelReading(channel.getName());
            serverReading.addChannel(channelReading);
            return channel.getLastMessage().doOnError(c -> {
            }).onErrorResume(t -> Mono.empty()).zipWith(Mono.just(channelReading));
        }).flatMap(tuple -> {
            var message = tuple.getT1();
            var channelReading = tuple.getT2();
            System.out.println(channelReading.getName());
            // var messages = channel.getMessagesBefore(msg.getChannelId());
            // messages.subscribe(msg -> {
            // var messageReading = new MessageReading(msg.getChannelId());
            // messageReading.readReaction(msg.getReactions());
            // channelReading.addMessage(messageReading);
            // });
            return Mono.just(1);
        }).collect(Collectors.summingInt(e -> e)).block();

        System.out.println(messageCount + " message(s)");
    }

    private static <T> Flux<T> infiniteFlux(T t) {
        return Flux.<T>create(c -> {
            while (true) {
                c.next(t);
            }
        });
    }
}
