import Reading.Companion.path
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.toPath

fun HTML.index() {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"Hello from Ktor"
        }
    }
}

@ExperimentalPathApi
fun main() {
    println(path("Nezikin", "Bava Kamma", 1))
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        routing {
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            get("questions/{seder}/{masechet}/{perek}") {
                val params = call.parameters
                val entered = listOf(params["seder"], params["masechet"], params["perek"])
                if (entered.any(String?::isNullOrBlank)) {
                    call.respond(HttpStatusCode.BadRequest, "Either seder, masechet or perek was null")
                } else {
                    if (entered[2]?.toIntOrNull() == null) {
                        call.respond(HttpStatusCode.BadRequest, "perek = ${entered[2]} isn't a number")
                    } else {
                        val (seder, masechet, perek) = entered as List<String>
                        val path = path(seder, masechet, perek.toInt())
                        if(path==null){
                            call.respond(HttpStatusCode.NotFound, "File $seder:$masechet:$perek doesn't exist")
                        }else{
                            call.respondFile(path.toFile())
                        }
                    }

                }

            }
        }
    }.start(wait = true)
}

class Reading{
    companion object{
        @ExperimentalPathApi
        fun path(seder: String, masechet: String, perek: Int = 1): Path? =
        this::class.java.getResource("$seder/$masechet/$perek.txt")?.toURI()?.toPath()
    }
}
