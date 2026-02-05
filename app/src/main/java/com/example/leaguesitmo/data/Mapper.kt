package com.example.leaguesitmo.data

import com.example.leaguesitmo.model.Item

fun LeaguesDto.toItem(): Item {
    return Item(
        title = name,
        value = country.name
    )
}