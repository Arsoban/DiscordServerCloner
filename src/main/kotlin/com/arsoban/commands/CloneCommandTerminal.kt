package com.arsoban.commands

import com.arsoban.Bot
import org.javacord.api.entity.Icon
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.channel.ChannelType
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class CloneCommandTerminal {

    fun clone(id: Long){
        var server = Bot.api.getServerById(id);

        println("Creating server... | Создаю сервер...")

        var newServerIcon: Icon? = try {
            server.get().icon.get();
        } catch (exc: NoSuchElementException){
            null;
        }

        var newServerId: Long;

        try {
            newServerId = Bot.api.createServerBuilder().apply {
                setName(server.get().name)
                newServerIcon?.let {
                    setIcon(it)
                }
            }.create().join();
        } catch (exc: Exception){
            println("Error was found when creating server! Maybe you have 100 servers limit! | Произошла ошибка при создании сервера! Скорее всего у тебя уже лимит 100 серверов!")
            exitProcess(0)
        }

        Thread.sleep(3000);

        var newServer = Bot.api.getServerById(newServerId).get();

        println("Created server \"${newServer.name}\" | Создал сервер \"${newServer.name}\"")

        println("Creating roles... (only this process is running to avoid bugs) | Создаю роли... (сейчас идёт только этот процесс чтобы избежать баги)")

        var rolesThread = thread {
            server.get().roles.reversed().forEach { role ->
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
                    println("Created role \"${it.name}\" | Создал роль \"${it.name}\"")
                }
            }
        }

        rolesThread.join()

        println("Deleting default channels... | Удаляю стандартные каналы...")

        var deleteDefaultChannelsThread = thread {
            newServer.channels.forEach { channel ->
                channel.delete().join();
            }
        }

        println("Creating channels... | Создаю каналы...")

        var currentCategory: ChannelCategory? = null;

        var createChannelsThread = thread {
            server.get().channels.forEach { channel ->
                when (channel.type){
                    ChannelType.CHANNEL_CATEGORY -> {
                        var channelCategory = newServer.createChannelCategoryBuilder().apply {
                            setName(channel.name)
                        }.create().join().also {
                            println("Created category \"${it.name}\" | Создал категорию \"${it.name}\"")
                            currentCategory = it;
                        }

                        println("Editing permissions for category \"${channelCategory.name}\" | Настраиваю права для категории \"${channelCategory.name}\"")

                        channel.overwrittenRolePermissions.forEach { rolePermissions ->
                            channelCategory.createUpdater().addPermissionOverwrite(newServer.getRolesByName(Bot.api.getRoleById(rolePermissions.key).get().name)[0], rolePermissions.value).update().join()
                        }

                    }
                    ChannelType.SERVER_TEXT_CHANNEL -> {
                        var textChannel = newServer.createTextChannelBuilder().apply {
                            setName(channel.name)
                            currentCategory?.let {
                                setCategory(it)
                            }
                        }.create().join().also {
                            println("Created text channel \"${it.name}\" | Создал текстовой канал \"${it.name}\"")
                        }

                        println("Editing permissions for text channel \"${textChannel.name}\" | Настраиваю права для текстового канала \"${textChannel.name}\"")

                        channel.overwrittenRolePermissions.forEach { rolePermissions ->
                            textChannel.createUpdater().addPermissionOverwrite(newServer.getRolesByName(Bot.api.getRoleById(rolePermissions.key).get().name)[0], rolePermissions.value).update().join()
                        }

                    }
                    ChannelType.SERVER_VOICE_CHANNEL -> {
                        var voiceChannel = newServer.createVoiceChannelBuilder().apply {
                            setName(channel.name)
                            currentCategory?.let {
                                setCategory(it)
                            }
                        }.create().join().also {
                            println("Created voice channel \"${it.name}\" | Создал голосовой канал \"${it.name}\"")
                        }

                        println("Editing permissions for voice channel \"${voiceChannel.name}\" | Настраиваю права для голосового канала \"${voiceChannel.name}\"")

                        channel.overwrittenRolePermissions.forEach { rolePermissions ->
                            voiceChannel.createUpdater().addPermissionOverwrite(newServer.getRolesByName(Bot.api.getRoleById(rolePermissions.key).get().name)[0], rolePermissions.value).update().join()
                        }
                    }
                    ChannelType.SERVER_STAGE_VOICE_CHANNEL -> {
                        println("Could not created stage channel :( | Не смог создать stage канал :(")
                    }
                    else -> {}
                }
            }
        }

        println("Creating emojis... | Создаю эмодзи...")

        var createEmojisThread = thread {
            server.get().customEmojis.forEach { emoji ->
                newServer.createCustomEmojiBuilder().apply {
                    setName(emoji.name)
                    setImage(emoji.image)
                }.create().join().also {
                    println("Created emoji \"${it.name}\" | Создал эмодзи \"${it.name}\"")
                }
            }
        }

        deleteDefaultChannelsThread.join();
        createChannelsThread.join();
        createEmojisThread.join();


        println("Server cloned :) | Сервер склонирован :)")
    }

}