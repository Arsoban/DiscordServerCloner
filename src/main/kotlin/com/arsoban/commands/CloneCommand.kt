package com.arsoban.commands

import com.arsoban.Bot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.javacord.api.entity.Icon
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.channel.ChannelType
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import kotlin.system.exitProcess

class CloneCommand : MessageCreateListener {

    override fun onMessageCreate(event: MessageCreateEvent?) {
        if (event!!.messageContent.lowercase() == "/clone" && event.message.author.asUser().get() == Bot.api.yourself){
            event.message.delete().join();

            println("Creating server...")

            var newServerIcon: Icon? = try {
                event.server.get().icon.get();
            } catch (exc: NoSuchElementException){
                null;
            }

            var newServerId: Long;

            try {
                newServerId = Bot.api.createServerBuilder().apply {
                    setName(event.server.get().name)
                    newServerIcon?.let {
                        setIcon(it)
                    }
                }.create().join();
            } catch (exc: Exception){
                println("Error was found when creating server! Maybe you have 100 servers limit!")
                exitProcess(0)
            }

            Thread.sleep(3000);

            var newServer = Bot.api.getServerById(newServerId).get();

            println("Created server \"${newServer.name}\"")

            println("Creating roles... (only this process is running to avoid bugs)")

            var rolesThread = runBlocking {
                event.server.get().roles.reversed().forEach { role ->
                    launch {
                        newServer.createRoleBuilder().apply {
                            setName(role.name)
                            try {
                                setColor(role.color.get())
                            } catch (exc: NoSuchElementException){
                                ;
                            }
                            setPermissions(role.permissions)
                            setMentionable(role.isMentionable)
                            setDisplaySeparately(role.isDisplayedSeparately)
                        }.create().join().also {
                            println("Created role \"${it.name}\"")
                        }
                    }
                }
            }

            println("Deleting default channels...")

            var deleteDefaultChannelsThread = GlobalScope.launch {
                newServer.channels.forEach { channel ->
                    launch {
                        channel.delete().join();
                    }
                }
            }

            println("Creating channels...")

            var createCategories = runBlocking {
                event.server.get().channelCategories.forEach { channel ->
                    launch {
                        val channelCategory = newServer.createChannelCategoryBuilder().apply {
                            setName(channel.name)
                        }.create().join().also {
                            println("Created category \"${it.name}\"")
                        }

                        println("Editing permissions for category \"${channelCategory.name}\"")

                        channel.overwrittenRolePermissions.forEach { rolePermissions ->
                            channelCategory.createUpdater().addPermissionOverwrite(newServer.getRolesByName(Bot.api.getRoleById(rolePermissions.key).get().name)[0], rolePermissions.value).update().join()
                        }
                    }
                }
            }

            var currentCategory: ChannelCategory? = null;

            var createChannelsThread = GlobalScope.launch {

                event.server.get().channels.forEach { channel ->
                    launch {
                        when (channel.type){
                            ChannelType.CHANNEL_CATEGORY -> {
                                ;
                            }
                            ChannelType.SERVER_TEXT_CHANNEL -> {
                                var textChannel = newServer.createTextChannelBuilder().apply {
                                    setName(channel.name)
                                    setCategory(newServer.getChannelCategoriesByNameIgnoreCase(channel.asServerTextChannel().get().category.get().name)[0])
                                }.create().join().also {
                                    println("Created text channel \"${it.name}\"")
                                }

                                println("Editing permissions for text channel \"${textChannel.name}\"")

                                channel.overwrittenRolePermissions.forEach { rolePermissions ->
                                    textChannel.createUpdater().addPermissionOverwrite(newServer.getRolesByName(Bot.api.getRoleById(rolePermissions.key).get().name)[0], rolePermissions.value).update().join()
                                }

                            }
                            ChannelType.SERVER_VOICE_CHANNEL -> {
                                var voiceChannel = newServer.createVoiceChannelBuilder().apply {
                                    setName(channel.name)
                                    setCategory(newServer.getChannelCategoriesByNameIgnoreCase(channel.asServerVoiceChannel().get().category.get().name)[0])
                                }.create().join().also {
                                    println("Created voice channel \"${it.name}\"")
                                }

                                println("Editing permissions for voice channel \"${voiceChannel.name}\"")

                                channel.overwrittenRolePermissions.forEach { rolePermissions ->
                                    voiceChannel.createUpdater().addPermissionOverwrite(newServer.getRolesByName(Bot.api.getRoleById(rolePermissions.key).get().name)[0], rolePermissions.value).update().join()
                                }
                            }
                            ChannelType.SERVER_STAGE_VOICE_CHANNEL -> {
                                println("Could not created stage channel :(")
                            }
                            else -> {}
                        }
                    }
                }
            }

            println("Creating emojis...")

            var createEmojisThread = GlobalScope.launch {
                event.server.get().customEmojis.forEach { emoji ->
                    launch {
                        newServer.createCustomEmojiBuilder().apply {
                            setName(emoji.name)
                            setImage(emoji.image)
                        }.create().join().also {
                            println("Created emoji \"${it.name}\"")
                        }
                    }
                }
            }
        }
    }

}