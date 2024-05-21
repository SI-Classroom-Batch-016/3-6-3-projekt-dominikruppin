package com.plexviewer.api

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "MediaContainer", strict = false)
data class ServerList(
    @field:ElementList(inline = true)
    var devices: List<Device>? = null
)

@Root(name = "Device", strict = false)
data class Device(
    @field:Element(name = "name", required = false)
    var name: String? = null,

    @field:ElementList(inline = true, required = false)
    var connections: List<Connection>? = null
)

@Root(name = "Connection", strict = false)
data class Connection(
    @field:Element(name = "protocol", required = false)
    var protocol: String? = null,

    @field:Element(name = "address", required = false)
    var address: String? = null,

    @field:Element(name = "port", required = false)
    var port: String? = null,

    @field:Element(name = "uri", required = false)
    var uri: String? = null,

    @field:Element(name = "local", required = false)
    var local: String? = null
)

