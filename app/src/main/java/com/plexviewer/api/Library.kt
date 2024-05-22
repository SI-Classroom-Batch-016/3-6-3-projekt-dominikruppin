import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "MediaContainer", strict = false)
data class LibraryResponse(
    @field:ElementList(entry = "Directory", inline = true)
    var directories: List<Directory>? = null
)

@Root(name = "Directory", strict = false)
data class Directory(
    /**@field:Attribute(name = "allowSync")
    var allowSync: Int = 0,
    @field:Attribute(name = "art")
    var art: String = "",
    @field:Attribute(name = "filters")
    var filters: Int = 0,
    @field:Attribute(name = "refreshing")
    var refreshing: Int = 0,
    @field:Attribute(name = "thumb")
    var thumb: String = "",*/
    @field:Attribute(name = "key")
    var key: String = "",
    @field:Attribute(name = "type")
    var type: String = "",
    @field:Attribute(name = "title")
    var title: String = "",
    /**@field:Attribute(name = "agent")
    var agent: String = "",
    @field:Attribute(name = "scanner")
    var scanner: String = "",
    @field:Attribute(name = "language")
    var language: String = "",
    @field:Attribute(name = "uuid")
    var uuid: String = "",
    @field:Attribute(name = "updatedAt")
    var updatedAt: Long = 0,
    @field:Attribute(name = "createdAt")
    var createdAt: Long = 0,
    @field:ElementList(entry = "Location", inline = true)
    var locations: List<Location>? = null*/
)

@Root(name = "Location", strict = false)
data class Location(
    @field:Attribute(name = "id")
    var id: Int = 0,
    @field:Attribute(name = "path")
    var path: String = ""
)
