import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IThreadContainer;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;











public class DraftBot extends ListenerAdapter{

    private final Dotenv config;
    private TextChannel draftChannel = null;
    private Category draftStuff;
    private final String CATEGORY_NAME = "custom draft stuff";
    private final String CHANNEL_NAME = "draft";
    private final JDA jda;


    private static class Draft extends Thread{

        private class DraftListener extends ListenerAdapter{

            Draft draft;

            DraftListener(Draft draft){
                this.draft = draft;
            }



            private boolean checkPickCommand(String command, @NotNull SlashCommandInteractionEvent event) {
                return command.equalsIgnoreCase("draft") && event.getChannel().equals(this.draft.channel);
            }

            private void commandPick(@NotNull SlashCommandInteractionEvent event){
                boolean inTurn = true;

                // TODO add auto predict
                String hero = event.getOption("hero").getAsString();




                if(this.draft.format.charAt(this.draft.frame.panel.modeIndex) ==  'L'){
                    if(event.getMember().getId().equals(left.getId())){
                        this.draft.frame.pickHero(0, hero);
                    } else{
                        inTurn = false;
                    }
                } else if(this.draft.format.charAt(this.draft.frame.panel.modeIndex) ==  'R'){
                    if(event.getMember().getId().equals(right.getId())){
                        this.draft.frame.pickHero(1, hero);
                    } else{
                        inTurn = false;
                    }
                } else if(this.draft.format.charAt(this.draft.frame.panel.modeIndex) == 'l'){
                    if(event.getMember().getId().equals(left.getId())){
                        this.draft.frame.banHero(0, hero);
                    } else{
                        inTurn = false;
                    }
                } else if(this.draft.format.charAt(this.draft.frame.panel.modeIndex) ==  'r'){
                    if(event.getMember().getId().equals(right.getId())){
                        this.draft.frame.banHero(1, hero);
                    } else{
                        inTurn = false;
                    }
                }
                if(inTurn){
                    event.replyFiles(FileUpload.fromData(new File(this.draft.frame.scPath))).complete();
                    if(this.draft.frame.panel.modeIndex >= this.draft.frame.panel.mode.length()) {
                        this.draft.frame.dispose();
                        this.draft.jda.removeEventListener(this);
                        Thread.currentThread().interrupt();
                } else{
                        event.reply("it's not your turn").setEphemeral(true).queue();
                    }
                }
            }


            @Override
            public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
                String command = event.getName();

                if(checkPickCommand(command, event)){
                    commandPick(event);
                }
            }

        }



        public ThreadChannel channel;
        private final String format;
        private final JDA jda;
        private final Member left;
        private final Member right;
        private final MyFrame frame;


        Draft(ThreadChannel channel, String format, Member left, Member right, JDA jda){
            this.channel = channel;
            this.format = format;
            this.jda = jda;
            this.left = left;
            this.right = right;
            this.frame = new MyFrame(format);
        }



        public void run(){
            DraftListener listener = new DraftListener(this);

            jda.addEventListener(listener);
        }

    }



    DraftBot(){

        config = Dotenv.configure().load();
        String token = config.get("TOKEN");


        this.jda = JDABuilder.createDefault(token).addEventListeners(this).build();


    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();



        if(command.equals("start")){
            commandStart(event);
        }
        if(command.equals("draft")){
            if(!event.getChannel().getType().equals(ChannelType.GUILD_PUBLIC_THREAD)) {
                event.reply("not available in this channel").setEphemeral(true).queue();
            }
        }

    }


    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        System.out.println("Connectd to " + event.getGuild().getName());

        this.setupDraftCategory(event);
        this.addCommands(event);

    }

    private void addCommands(@NotNull GuildReadyEvent event){

        List<CommandData> commandData = new ArrayList<>();

        commandData.add(Commands.slash("start", "starts a new draft")
                .addOption(OptionType.USER, "left", "The drafter for the left team", true)
                .addOption(OptionType.USER, "right", "The drafter for the right team", true)
                .addOption(OptionType.STRING, "format", "The format code used to run the draft (if left blank will be 'lrlrLRRLLrlRRLLR')", false)
                // TODO add whisper cube draft
                // TODO add blind pick/bans
        );

        commandData.add(Commands.slash("draft", "picks/bans a hero")
                .addOption(OptionType.STRING, "hero", "the name of the hero", true)
        );



        event.getGuild().updateCommands().addCommands(commandData).queue();
    }


    private void commandStart(@NotNull SlashCommandInteractionEvent event){

        if(event.getChannel().equals(this.draftChannel)) {
            IThreadContainer threadContainer = (IThreadContainer) event.getChannel();
            Member left = event.getOption("left").getAsMember();
            Member right = event.getOption("right").getAsMember();

            String format;
            try {
                format = event.getOption("format").getAsString();
            } catch (Exception e){
                format = "lrlrLRRLLrlRRLLR";
            }

            // check that draft format contain 5 left picks and 5 right picks
            int countL = 0;
            int countR = 0;
            for(int i = 0; i < format.length(); i++) {
                if (format.charAt(i) == 'L') {
                    countL++;
                } else if (format.charAt(i) == 'R') {
                    countR++;
                }
            }
            if (countL == 5 & countR == 5) {


                String threadName = left.getEffectiveName().replace(' ', '-') + "-vs-" + right.getEffectiveName().replace(' ', '-');
                ThreadChannel channel = threadContainer.createThreadChannel(threadName).complete();
                channel.addThreadMember(left).complete();
                channel.addThreadMember(right).complete();

                Draft draft = new Draft(channel, format, left, right, this.jda);
                draft.frame.scPath = "src/main/resources/.sc/" + threadName + ".png";
                draft.start();
                event.reply("Starting new draft " + threadName + " using format " + format).queue();
            } else{
                event.reply("not a valid draft format").setEphemeral(true).queue();
            }
        }
    }


    // sets up the draft category
    private void setupDraftCategory(@NotNull GuildReadyEvent event){

        // find category, create if not found
        List<Category> categories = event.getGuild().getCategoriesByName(this.CATEGORY_NAME, true);
        if(categories.size() == 0){
            this.draftStuff = event.getGuild().createCategory(this.CATEGORY_NAME).complete();
        }else{
            this.draftStuff = categories.get(0);
        }


        // find draft channel, create if not found
        List<TextChannel> channels = event.getGuild().getTextChannelsByName(this.CHANNEL_NAME, true);
        if(channels.size() == 0){
            this.draftChannel = this.draftStuff.createTextChannel(this.CHANNEL_NAME).complete();
        } else{
            this.draftChannel = channels.get(0);
        }

        // clear the category of all extra text channels
        for(int i = 0; i < channels.size(); i++){
            if(!channels.get(i).getName().equalsIgnoreCase(this.CHANNEL_NAME)){
                channels.get(i).delete().queue();
            }
        }

        // delete all threads in draft channel
        List<ThreadChannel> threads =  this.draftChannel.getThreadChannels();
        for(int i = 0; i < threads.size(); i++){
            threads.get(i).delete().queue();
        }




    }

}
