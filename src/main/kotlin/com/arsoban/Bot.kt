package com.arsoban

import com.arsoban.commands.CloneCommand
import com.arsoban.commands.CloneCommandTerminal
import io.github.cdimascio.dotenv.Dotenv
import org.javacord.api.AccountType
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder

class Bot {

    companion object{
        @JvmStatic
        lateinit var api: DiscordApi;

        const val version: String = "1.1";
    }

    fun createBot(){
        println("Starting DiscordServerCloner v${version} | Запускается DiscordServerCloner v${version}");

        var dotenv = Dotenv.load();

        api = DiscordApiBuilder()
            .setAccountType(AccountType.CLIENT)
            .setToken(dotenv.get("TOKEN"))
            .login().join();

        api.addListener(CloneCommand())

        println("Селфбот ${api.yourself.discriminatedName} запущен!")

        terminalCommands();
    }

    private fun terminalCommands(){
        readLine().also {
            if (it!!.startsWith("/clone")){
                var command = it.split(Regex(" +"));

                if (command.size == 2){
                    var cloneCommandTerminal = CloneCommandTerminal()

                    cloneCommandTerminal.clone(command[1].toLong())
                } else{
                    println("Неверное использование команды!\nИспользование: /clone <ID Сервера>")
                }
            }
        }

        terminalCommands();
    }

}

fun main() {
    var bot = Bot();

    bot.createBot();
}