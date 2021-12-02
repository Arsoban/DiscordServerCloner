package com.arsoban

import com.arsoban.commands.CloneCommand
import com.arsoban.commands.CloneCommandTerminal
import io.github.cdimascio.dotenv.Dotenv
import org.javacord.api.AccountType
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder

class Bot {

    companion object{
        lateinit var api: DiscordApi;

        const val version: String = "2.0";
    }

    fun createBot(){
        println("Launching DiscordServerCloner v${version}")

        var dotenv = Dotenv.load();

        api = DiscordApiBuilder()
            .setAccountType(AccountType.CLIENT)
            .setToken(dotenv.get("TOKEN"))
            .login().join();

        api.addListener(CloneCommand())

        println("Selfbot ${api.yourself.discriminatedName} launched!")

        terminalCommands()
    }

    private fun terminalCommands(){
        while (true) {
            readLine().also {
                if (it!!.startsWith("/clone")){
                    var command = it.split(Regex(" +"));

                    if (command.size == 2){
                        var cloneCommandTerminal = CloneCommandTerminal()

                        cloneCommandTerminal.clone(command[1].toLong())
                    } else{
                        println("Error found in this command!\nSyntax: /clone <Server ID>")
                    }
                } else {
                    println("Command with that name not found!\nCommand list:\n/clone <Server ID>")
                }
            }
        }
    }

}

fun main() {
    val bot = Bot();

    bot.createBot();
}