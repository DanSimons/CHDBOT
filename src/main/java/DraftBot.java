import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class DraftBot extends ListenerAdapter{

    private final Dotenv config;

    DraftBot(){

        config = Dotenv.configure().load();
        String token = config.get("TOKEN");


        JDA jda = JDABuilder.createDefault(token).addEventListeners(this).build();


    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();



        event.reply("done").queue();

        }



    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        System.out.println("Connectd to " + event.getGuild().getName());

        this.setupDraftCategory(event);


        List<CommandData> commandData = new ArrayList<>();

        commandData.add(Commands.slash("welcome", "welcomes user"));






        event.getGuild().updateCommands().addCommands(commandData).queue();

    }


    // sets up the draft category
    private void setupDraftCategory(@NotNull GuildReadyEvent event){
        List<Category> categories = event.getGuild().getCategories();

        boolean hasDraftStuff = false;
        Category draftStuff;


        // check for draft stuff category
        for(int i = 0; i < categories.toArray().length; i++){
            Category cat = (Category) categories.toArray()[i];
            if(cat.getName().equals("Draft Stuff")){
                hasDraftStuff = true;
            }
        }

        // create category if not existent
        if(!hasDraftStuff){
            event.getGuild().createCategory("Draft Stuff").queue();
        }



        draftStuff = (Category) event.getGuild().getCategoriesByName("Draft Stuff", false).toArray()[0];








    }



    public Dotenv getConfig() {
        return config;
    }
}
