package fr.atesab.ares.data;

import java.util.ArrayList;
import java.util.List;

import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji.Unicode;
import lombok.Data;
import lombok.NonNull;

@Data
public class MessageReading {
    @NonNull
    private Snowflake author;
    private List<String> reactions = new ArrayList<>();

    public synchronized void readReaction(List<Reaction> reactions) {
        reactions.stream().map(r -> r.getEmoji()).filter(r -> r instanceof Unicode).map(r -> ((Unicode) r).getRaw())
                .forEach(this.reactions::add);
    }
}
