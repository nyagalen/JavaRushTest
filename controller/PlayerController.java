package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    public PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("{id}")
    @ResponseBody
    public Player getPlayerById(@PathVariable("id") String id) {
        Long checkedId = playerService.idCheck(id);
        return playerService.getById(checkedId).get();
    }

    @PostMapping()
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {


        return new ResponseEntity<Player>(playerService.saveCheckedPlayer(player), HttpStatus.OK);


    }

    @PostMapping("{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable String id, @RequestBody Player player) {

        long checkedId = playerService.idCheck(id);
        playerService.deletePlayer(checkedId);
        return new ResponseEntity<Player>(playerService.updatePlayer(checkedId, player), HttpStatus.OK);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deletePlayer(@PathVariable String id) {
        long checkedId = playerService.idCheck(id);
        playerService.deletePlayer(checkedId);
        return new ResponseEntity<String>("Player deleted", HttpStatus.OK);


    }

    @GetMapping("count")
    @ResponseBody
    public long getCountOfSuitablePlayers(@RequestParam(defaultValue = "0") int pageNumber,
                                             @RequestParam(defaultValue = "3") int pageSize,
                                             @RequestParam(value = "name", required = false) String name,
                                             @RequestParam(value = "title", required = false) String title,
                                             @RequestParam(value = "maxLevel", required = false) Double maxLevel,
                                             @RequestParam(value = "minLevel", required = false) Double minLevel,
                                             @RequestParam(value = "banned", required = false) Boolean banned,
                                             @RequestParam(value = "after", required = false) Long after,
                                             @RequestParam(value = "before", required = false) Long before,
                                             @RequestParam(value = "profession", required = false) Profession profession,
                                             @RequestParam(value = "race", required = false) Race race,
                                             @RequestParam(value = "minExperience", required = false) Double minExperience,
                                             @RequestParam(value = "maxExperience", required = false) Double maxExperience) {

        Specification<Player> specification = getPlayerSpecification(name, title, maxLevel, minLevel, banned, after, before, profession, race, minExperience, maxExperience);
        return playerService.getAllPlayers(specification).size();

    }

    private Specification<Player> getPlayerSpecification(String name, String title, Double maxLevel, Double minLevel, Boolean banned,
                                                         Long after, Long before,
                                                         Profession profession,
                                                         Race race, Double minExperience, Double maxExperience) {
        Specification<Player> specification = Specification.where(playerService.nameLike(name))
                .and(playerService.titleLike(title))
                .and(playerService.bannedFilter(banned))
                .and(playerService.levelFilter(minLevel, maxLevel))
                .and(playerService.dateFilter(after, before))
                .and(playerService.professionLike(profession))
                .and(playerService.raceLike(race))
                .and(playerService.experienceFilter(minExperience, maxExperience));
        return specification;
    }

    @GetMapping()
    public ResponseEntity<List<Player>> getAllPlayers(@RequestParam(defaultValue = "0") int pageNumber,
                                                      @RequestParam(defaultValue = "3") int pageSize,
                                                      @RequestParam(value = "title", required = false) String title,
                                                      @RequestParam(value = "name", required = false) String name,
                                                      @RequestParam(value = "minLevel", required = false) Double minLevel,
                                                      @RequestParam(value = "maxLevel", required = false) Double maxLevel,
                                                      @RequestParam(value = "banned", required = false) Boolean banned,
                                                      @RequestParam(value = "after", required = false) Long after,
                                                      @RequestParam(value = "before", required = false) Long before,
                                                      @RequestParam(value = "profession", required = false) Profession profession,
                                                      @RequestParam(value = "race", required = false) Race race,
                                                      @RequestParam(value = "minExperience", required = false) Double minExperience,
                                                      @RequestParam(value = "maxExperience", required = false) Double maxExperience) {


        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Specification<Player> specification = getPlayerSpecification(name, title, maxLevel, minLevel, banned, after, before, profession, race, minExperience,
                maxExperience);
        return new ResponseEntity<List<Player>>(playerService.getAllPlayers(specification, pageable).getContent(), HttpStatus.OK);


    }


}
