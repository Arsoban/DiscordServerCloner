package com.arsoban

import com.arsoban.commands.CloneCommand
import io.github.cdimascio.dotenv.Dotenv
import org.javacord.api.AccountType
import org.javacord.api.DiscordApi
import org.javacord.api.DiscordApiBuilder

class Bot {

    companion object{
        @JvmStatic
        lateinit var api: DiscordApi;
    }

    fun createBot(){
        var dotenv = Dotenv.load();

        api = DiscordApiBuilder()
            .setAccountType(AccountType.CLIENT)
            .setToken(dotenv.get("TOKEN"))
            .login().join();

        api.addListener(CloneCommand())

        println("Бот запущен!")
    }

}

fun main() {
    var bot = Bot();

    bot.createBot();
}