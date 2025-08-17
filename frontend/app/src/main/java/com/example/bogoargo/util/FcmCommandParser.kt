package com.example.bogoargo.util

object FcmKeys {
    const val COMMAND = "command"
    const val TEAM = "challenge_team"
}

sealed class FcmCommand(val message: String, val teamName: String?) {
    class ChallengeReceived(team: String)  : FcmCommand("대전 요청 받았습니다", team)
    class ChallengeAccepted(team: String)  : FcmCommand("대전 수락 되었습니다", team)
    class ChallengeCancelled(team: String) : FcmCommand("대전 취소 되었습니다", team)
    object Unknown                         : FcmCommand("알 수 없는 명령", null)
}

object FcmCommandParser {
    fun parse(data: Map<String, String>): FcmCommand {
        val cmd  = (data[FcmKeys.COMMAND] ?: "").trim()
        val team = (data[FcmKeys.TEAM] ?: "").trim()
        if (cmd.isEmpty() || team.isEmpty()) return FcmCommand.Unknown

        return when (cmd) {
            "challenge_received"  -> FcmCommand.ChallengeReceived(team)
            "challenge_accepted"  -> FcmCommand.ChallengeAccepted(team)
            "challenge_cancelled" -> FcmCommand.ChallengeCancelled(team)
            else -> FcmCommand.Unknown
        }
    }
}
