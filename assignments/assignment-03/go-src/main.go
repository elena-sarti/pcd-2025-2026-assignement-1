package main

import (
    "fmt"
    "math"
    "math/rand"
)

type HeadsOrTails struct {
    msg string
}


func Player(msg_ch chan HeadsOrTails){
    var msg string
    if rand.Intn(2) == 0 {
        msg = "heads"
    } else {
        msg = "tails"
    }
    msg_ch <- HeadsOrTails{msg: msg}
}

func Match(match_ch chan int, players []chan int, winner_ch chan int){
    match_number := <- match_ch
    player_1 := <- players[match_number]
    player_2 := <- players[match_number + 1]
    msg_ch := make (chan HeadsOrTails)
    go Player(msg_ch)
    var winning_msg string
    if rand.Intn(2) == 0 {
      winning_msg = "heads"
    } else {
      winning_msg = "tails"
    }
    first_player_msg := <- msg_ch
    var match_winner int
    if first_player_msg.msg == winning_msg {
        match_winner = player_1
    } else {
        match_winner = player_2
    }
    winner_ch <- match_winner
    fmt.Printf("Match between players %d and %d won by player %d! \n", player_1, player_2, match_winner)
}

func Round(n_rounds int, players []chan int){
    round_winners := make([]chan int, int(math.Pow(2, float64(n_rounds-1))))
    n_players := int(math.Pow(2, float64(n_rounds)))
    for i := 0; i < n_players; i += 2 {
        match_ch := make(chan int)
        winner_ch := make(chan int)
        go Match(match_ch, players, winner_ch)
        match_ch <- i
        round_winners[i / 2] = make(chan int, 1) //Un invio su un canale non bufferizzato (senza la dimensione specificata) si blocca finché non c'è qualcuno pronto a ricevere in quel preciso istante
        round_winners[i / 2] <- (<- winner_ch)
    }
    if n_rounds == 1 {
       winner := <- round_winners[0]
       fmt.Printf("Game winner: player %d!\n", winner)
    } else {
        fmt.Printf("New round started! \n")
        go Round(n_rounds - 1, round_winners)
    }
}

func main() {
    fmt.Println("Tournament started!")

    n_rounds := 5
    n_players := int(math.Pow(2, float64(n_rounds)))

    players := make([]chan int, n_players)

    for i := 0 ; i < n_players; i++ {
        players[i] = make(chan int, 1)
        players[i] <- i
    }

    go Round(n_rounds, players)

    for {}
}