package com.game.controller;

import com.game.entity.Player;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    private PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService){
        this.playerService = playerService;
    }

    @GetMapping
    public List<Player> playerList(@RequestParam Map<String, String> categories){
        return playerService.getPlayers(categories);
    }

    @GetMapping("/count")
    public Integer playersCount(@RequestParam Map<String, String> categories){
        return playerService.getPlayersCount(categories);
    }

    @PostMapping
    public ResponseEntity<Player> createNewPlayer(@RequestBody Map<String,String> inputsMap){
        try {
            Player createdPlayer = playerService.createNewPlayer(inputsMap);
            return new ResponseEntity<>(createdPlayer, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id){
        Player player;

        try{
            player = playerService.getPlayerById(id);
            if(player == null){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestBody Map<String, String> inputsMap){
        Player updatedPlayer;

        try {
            if(inputsMap.isEmpty()){
                updatedPlayer = playerService.getPlayerById(id);
            }
            else {
                updatedPlayer = playerService.updatePlayer(id, inputsMap);
                if(updatedPlayer == null){
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            }
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(updatedPlayer, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlayerById(@PathVariable Long id){
        try {
            if (!playerService.deletePlayerById(id)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
