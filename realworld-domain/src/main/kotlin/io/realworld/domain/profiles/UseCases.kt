package io.realworld.domain.profiles

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import arrow.core.toOption
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.extensions
import arrow.effects.fix
import arrow.typeclasses.binding
import io.realworld.domain.users.User

data class GetProfileCommand(val username: String, val current: Option<User>)
data class FollowCommand(val username: String, val current: User)

interface GetProfileUseCase {
  val getUser: GetUserByUsername
  val hasFollower: HasFollower

  fun GetProfileCommand.runUseCase(): IO<Option<Profile>> {
    val cmd = this
    return ForIO extensions {
      binding {
        getUser(cmd.username).bind().fold(
          { none<Profile>() },
          {
            Profile(
              username = it.username,
              bio = it.bio.toOption(),
              image = it.image.toOption(),
              following = current.fold(
                { none<Boolean>() },
                { follower -> hasFollower(cmd.username, follower.username).bind().some() }
              )
            ).some()
          }
        )
      }.fix()
    }
  }
}

interface FollowUseCase {
  val getUser: GetUserByUsername
  val addFollower: AddFollower

  fun FollowCommand.runUseCase(): IO<Option<Profile>> {
    val cmd = this
    return ForIO extensions {
      binding {
        getUser(cmd.username).bind().fold(
          { none<Profile>() },
          {
            addFollower(it.username, current.username).bind()
            Profile(
              username = it.username,
              bio = it.bio.toOption(),
              image = it.image.toOption(),
              following = true.toOption()
            ).some()
          }
        )
      }.fix()
    }
  }
}
