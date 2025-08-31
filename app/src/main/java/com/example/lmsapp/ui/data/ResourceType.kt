import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ResourceType(val backendType: String, val mimeType: String) {
    @SerialName("document")
    DOCUMENT("document", "application/pdf"),

    @SerialName("video")
    VIDEO("video", "video/*"),

    @SerialName("link")
    LINK("link", "text/plain"),

    @SerialName("note")
    NOTE("note", "text/plain"),

    @SerialName("other")
    OTHER("other", "*/*")
}
