package com.cmc.launcher.terracotta.profile

import com.google.gson.annotations.SerializedName
import com.cmc.launcher.R

enum class ProfileKind(val textRes: Int) {
    @SerializedName("HOST")
    HOST(R.string.terracotta_player_kind_host),

    @SerializedName("LOCAL")
    LOCAL(R.string.terracotta_player_kind_local),

    @SerializedName("GUEST")
    GUEST(R.string.terracotta_player_kind_guest)
}
