package com.arsoban.commands

import com.arsoban.Bot
import org.javacord.api.entity.Icon
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.channel.ChannelType
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class CloneCommand : MessageCreateListener {

    override fun onMessageCreate(event: MessageCreateEvent?) {
        if (event!!.messageContent.lowercase() == "/clone" && event.message.author.asUser().get() == Bot.api.yourself){
            event.message.delete().join();

            println("Создаю сервер...")

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
                println("Произошла ошибка при создании сервера! Скорее всего у тебя уже лимит 100 серверов!")
                exitProcess(0)
            }

            Thread.sleep(3000);

            var newServer = Bot.api.getServerById(newServerId).get();

            println("Создал сервер \"${newServer.name}\"")

            println("Создаю роли... (сейчас идёт только этот процесс чтобы избежать баги)")

            var rolesThread = thread {
                event.server.get().roles.reversed().forEach { role ->
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
                        println("Создал роль \"${it.name}\"")
                    }
                }
            }

            rolesThread.join()

            println("Удаляю стандартные каналы...")

            var deleteDefaultChannelsThread = thread {
                newServer.channels.forEach { channel ->
                    channel.delete().join();
                }
            }

            println("Создаю каналы...")

            var currentCategory: ChannelCategory? = null;

            var createChannelsThread = thread {
                event.server.get().channels.forEach { channel ->
                    when (channel.type){
                        ChannelType.CHANNEL_CATEGORY -> {
                            var channelCategory = newServer.createChannelCategoryBuilder().apply {
                                setName(channel.name)
                            }.create().join().also {
                                println("Создал категорию \"${it.name}\"")
                                currentCategory = it;
                            }

                            println("Настраиваю права для категории ${channelCategory.name}")

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
                                println("Создал текстовой канал \"${it.name}\"")
                            }

                            println("Настраиваю права для текстового канала ${textChannel.name}")

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
                                println("Создал голосовой канал \"${it.name}\"")
                            }

                            println("Настраиваю права для голосового канала ${voiceChannel.name}")

                            channel.overwrittenRolePermissions.forEach { rolePermissions ->
                                voiceChannel.createUpdater().addPermissionOverwrite(newServer.getRolesByName(Bot.api.getRoleById(rolePermissions.key).get().name)[0], rolePermissions.value).update().join()
                            }
                        }
                        ChannelType.SERVER_STAGE_VOICE_CHANNEL -> {
                            println("Не смог создать stage канал :(")
                        }
                        else -> {}
                    }
                }
            }

            println("Создаю эмодзи...")

            var createEmojisThread = thread {
                event.server.get().customEmojis.forEach { emoji ->
                    newServer.createCustomEmojiBuilder().apply {
                        setName(emoji.name)
                        setImage(emoji.image)
                    }.create().join().also {
                        println("Создал эмодзи \"${it.name}\"")
                    }
                }
            }

            deleteDefaultChannelsThread.join();
            createChannelsThread.join();
            createEmojisThread.join();



            println("Сервер склонирован :)")
        }
    }

}