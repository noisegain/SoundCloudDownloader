import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onContentMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.utils.RiskFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus

@OptIn(RiskFeature::class)
suspend fun main() {
    val bot = telegramBot(TG_TOKEN)

    bot.buildBehaviourWithLongPolling(defaultExceptionsHandler = {
        println("Error:")
        println(it.message)
        println(it.cause)
        it.printStackTrace()
        println("____________________")
    }, scope = CoroutineScope(Dispatchers.IO) + SupervisorJob()) {
        println(getMe())

        onContentMessage { message ->
            execute(
                SendTextMessage(
                    message.chat.id, message.text.orEmpty()
                )
            )
            println("$message")
            if (message.text?.startsWith("/") != false) return@onContentMessage
        }

        onCommand("start") {
            suspend fun getUserName() = "@" + waitText(
                SendTextMessage(
                    it.chat.id,
                    "Не удалось увидеть ваш username. Пожалуйста, введите его:",
                    replyMarkup = ReplyKeyboardMarkup()
                )
            ).first().text.removePrefix("@")

            execute(
                SendTextMessage(
                    it.chat.id, "Привет, ${getUserName()} как дела?"
                )
            )
        }
    }.join()
}
