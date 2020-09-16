package music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class MusicHandler {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicHandler() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadAndPlay(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                sendMessage(channel, "", "Added to queue: " + track.getInfo().title);

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                sendMessage(channel, "", "Added to queue: " + firstTrack.getInfo().title + " (first track of playlist: " + playlist.getName() + ")");

                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                sendMessage(channel, "Error", "Nothing found by: " + trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sendMessage(channel, "Error", "Could not play: " + exception.getMessage());
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    public void skipTrack(TextChannel channel) {
        skip(channel);
        sendMessage(channel, "", "Skipped to the next track");
    }

    public void skip(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();
    }

    private void connectToVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                if (!voiceChannel.getMembers().isEmpty()) {
                    audioManager.openAudioConnection(voiceChannel);
                    break;
                }
            }
        }
    }

    public void leaveChannel(TextChannel textChannel) {
        AudioManager audioManager = textChannel.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            return;
        }

        audioManager.closeAudioConnection();
        sendMessage(textChannel, "", "* EpicGamerBot drops mic *");
    }

    public void emptyQeue(TextChannel textChannel) {
        getGuildAudioPlayer(textChannel.getGuild()).emptyQeue();
    }

    private void sendMessage(TextChannel textChannel, String title, String message) {
        EmbedBuilder embed = new EmbedBuilder();

        if (title.isEmpty()) {
            embed.setDescription(message);
            textChannel.sendMessage(embed.build()).queue();
            return;
        }

        embed.setTitle(title);
        embed.setDescription(message);
        textChannel.sendMessage(embed.build()).queue();
    }
}
