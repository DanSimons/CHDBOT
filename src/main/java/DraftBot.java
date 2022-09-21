import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.commons.collections4.functors.TruePredicate;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import javax.swing.text.html.StyleSheet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;











public class DraftBot extends ListenerAdapter{

    private final Dotenv config;
    private TextChannel draftChannel = null;
    private Category draftStuff;
    private final String CATEGORY_NAME = "custom draft stuff";
    private final String CHANNEL_NAME = "bot-commands";
    private JDA jda;


    private class Draft extends Thread{

        private class DraftListener extends ListenerAdapter{

            Draft draft;

            DraftListener(Draft draft){
                this.draft = draft;
            }

            private boolean checkCommand(String command, @NotNull SlashCommandInteractionEvent event){


                return true;
            }


            @Override
            public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
                String command = event.getName();

            }

        }



        public TextChannel c;
        private String f;
        private JDA jda;
        private IMentionable left;
        private IMentionable right;


        Draft(TextChannel channel, String format, IMentionable left, IMentionable right, JDA jda){
            this.c = channel;
            this.f = format;
            this.jda = jda;
            this.left = left;
            this.right = right;
        }



        public void run(){
            DraftListener listener = new DraftListener(this);

            List<CommandData> commandData = new ArrayList<>();

            commandData.add(Commands.slash("pick", "picks a hero")
                    .addOption(OptionType.STRING, "hero", "the name of the hero", true));
            this.c.getGuild().updateCommands().addCommands(commandData).queue();;
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



        if(command.equals("draft")){
            commandDraft(event);
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

        commandData.add(Commands.slash("draft", "starts a new draft")
                .addOption(OptionType.MENTIONABLE, "left", "The team/player drafting for the left team", true)
                .addOption(OptionType.MENTIONABLE, "right", "The team/player drafting for the right team", true)
        );


        event.getGuild().updateCommands().addCommands(commandData).queue();
    }




 /*
    create new draft
    process

        - create new text channel in draft stuff category
        - set perms to only allow left and right mentions to view and message
        - start new draft
        - ping left & right in new draft channel

 */

    private void commandDraft(@NotNull SlashCommandInteractionEvent event){

        IMentionable left = event.getOption("left").getAsMentionable();
        IMentionable right = event.getOption("right").getAsMentionable();

        String leftName;
        String rightName;


        // try to get mentions as a member, if not a member, get as a role
        try {
            leftName = event.getGuild().getMemberById(left.getId()).getEffectiveName();
        } catch (Exception e){
            leftName = event.getGuild().getRoleById(left.getId()).getName();
        }

        try {
            rightName = event.getGuild().getMemberById(right.getId()).getEffectiveName();
        } catch (Exception e){
            rightName = event.getGuild().getRoleById(right.getId()).getName();
        }


        // create draft channel & give view perms to left & right
        String channelName = leftName.replace(' ', '-') + "-vs-" + rightName.replace(' ', '-');
        this.draftStuff.createTextChannel(channelName)
                .addPermissionOverride(event.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride((IPermissionHolder) left, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride((IPermissionHolder) right, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .complete();


        TextChannel channel = event.getGuild().getTextChannelsByName(channelName, true).get(0);



        // start new draft as a thread
        Draft myDraft = new Draft(channel, "lrlrLRRLLrlRRLLR", left, right, this.jda);
        myDraft.start();


        // reply with link to new channel
        event.reply(channel.getAsMention()).queue();



    }


    // sets up the draft category
    private void setupDraftCategory(@NotNull GuildReadyEvent event){
        List<Category> categories = event.getGuild().getCategories();

        boolean hasDraftStuff = false;


        // check for draft stuff category
        for(int i = 0; i < categories.toArray().length; i++){
            Category cat = (Category) categories.toArray()[i];
            if(cat.getName().equals(this.CATEGORY_NAME)){
                hasDraftStuff = true;
            }
        }

        // create category if non-existent
        if(!hasDraftStuff){
            event.getGuild().createCategory(this.CATEGORY_NAME).complete();
        }


        this.draftStuff = (Category) event.getGuild().getCategoriesByName(this.CATEGORY_NAME, true).toArray()[0];


        // check if category has draft channel in it
        List<TextChannel> channels = this.draftStuff.getTextChannels();

        for(int i = 0; i < channels.size(); i++){
            if(channels.get(i).getName().equalsIgnoreCase(this.CHANNEL_NAME)){
                this.draftChannel = channels.get(i);
            }
        }

        // create if non-existent
        if(this.draftChannel == null){
            this.draftStuff.createTextChannel(this.CHANNEL_NAME).complete();
        }

        // clear the category of all extra text channels
        for(int i = 0; i < channels.size(); i++){
            if(!channels.get(i).getName().equals(this.CHANNEL_NAME)){
                channels.get(i).delete().queue();
            }
        }

    }

    public Dotenv getConfig() {
        return config;
    }
}
