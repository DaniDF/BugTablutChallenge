# Bug Tablut
_Bug Tablut_ is an artificial intelligence for the [Tablut Game Competition](http://ai.unibo.it/games/boardgamecompetition/tablut) organized by the _Foundations of Artificial Intelligence M AA 2021-2022_ course at the Unibo.
The project is written in Kotlin. The project realizes a player for an ancient Nordic strategy board game named "Tablut". 

The competition required us to stick to Ashton rules ([learn more here]((https://www.heroicage.org/issues/13/ashton.php) )) and Andrea Galassi' s server for communication with players and maintaining the game state. The server is available in this [repository](https://github.com/AGalassi/TablutCompetition).


<p align="center">
  <img src="https://github.com/DaniDF/TablutChallenge/blob/master/tablut.png" alt="tablut"/>
</p>

## Tablut
Tablut is an ancient viking board game. The game board is grid of 9x9 squares where two players alternate in moving their pawns:
+ attackers (black soldiers);
+ defenders (white soldiers and one king).

The rules of the game follows the work of [Ashton](https://www.heroicage.org/issues/13/ashton.php) 





## Download
Download the zip file from github or clone it from the command line:

```
git clone https://github.com/DaniDF/TablutChallenge.git
```

## Usage
To run one player from the VM, in /tablut run:

```
./runmyplayer.sh <role> <time> <ip>
<role> "white" or "black" 
<time> time in seconds (60 recommended, timer <59 recommended)
<ip> ip of the server
```

Let's do the same for the other player, but in a different shell.

```
./runmyplayer.sh white 60 localhost
./runmyplayer.sh black 60 localhost
```

## Authors

+ Daniele Foschi, _@[DaniDF](https://github.com/DaniDF)_;
+ Virginia Negri, _@[VIRGINIANEGRI25](https://github.com/VIRGINIANEGRI25)_;
+ Filippo Veronesi, _@[filippoveronesi](https://github.com/filippoveronesi)_.
