# Say hello to Fitzwilliam!
*Insert backstory here.*

Fitzwilliam is a two player online arena style shooter where the main objective
is to eliminate your opponent. To help you accomplish this goal you have a
jetpack, a laser gun and grenades that ricochet.

*Insert cool pic here*
![Fitzwilliam in action](/images/logo.png)

# Built to be online
Fitzwilliam is build using the Java game development framework
[Libgdx](https://libgdx.badlogicgames.com) which has the benefit of being
cross-platform. The game uses Java's DatagramSockets to send the network data
via UDP. This ensures a low latency gaming experience. A peer to
peer architecture is used where one of the players at the start of the game
decides that he/she wants to host the session. This means that you don't have to
find a dedicated server to run the game.

# Our experiences developing the game
Being a group of gamers ourselves it was fun to try out developing our own game
for the first time. We had previous experience with Java so the net code was
quite easy and straight forward to implement. Especially since the actual
messages that are sent are quite simple (jump, shoot, move etc.). Having not
much previous experience with game development and more specifically Libgdx this
took quite a lot of time. This was however a fun learning experience and once
you got a hang of how Libgdx worked it was quite easy to work with if you have
previous experience with Java.
