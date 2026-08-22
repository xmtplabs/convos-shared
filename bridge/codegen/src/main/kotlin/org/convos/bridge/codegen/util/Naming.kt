package org.convos.bridge.codegen.util

/** `MembershipPlugin` -> `membership`; `AgentToolsPlugin` -> `agentTools`. */
fun pluginWireName(interfaceName: String): String =
    interfaceName.removeSuffix("Plugin").replaceFirstChar { it.lowercaseChar() }
