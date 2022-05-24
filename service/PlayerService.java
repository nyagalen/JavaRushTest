package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.NotFoundException;

import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepo) {
        this.playerRepository = playerRepo;
    }


    public Player createPlayer (Player player) {
        return playerRepository.save(player);
    }

    @Transactional
    public Player saveCheckedPlayer (Player player) {
        if (player.getName() == null
                || player.getTitle() == null
                || player.getRace() == null
                || player.getBirthday() == null
                || player.getProfession() == null
                || player.getExperience() == null)
        {
            throw new BadRequestException("Please fill in all required fields");
        }
        checkParams(player);
        if (player.getBanned() == null) {
            player.setBanned(false);
        }
        Integer level = calculateLevel(player);
        player.setLevel(level);
        Integer untilNextLevel =  calculateUntilNextLevel(player);
        player.setUntilNextLevel(untilNextLevel);
        return playerRepository.save(player);
    }

    public List<Player> getAllPlayers(Specification<Player> specification) {
        return playerRepository.findAll(specification);
    }

    public List<Player> getAll () {
        return playerRepository.findAll();
    }

    public Page<Player> getAllPlayers(Specification<Player>  specification, Pageable pageable) {
        return playerRepository.findAll(specification, pageable);
    }

    @Transactional
    public Optional<Player> getById(long id) {
        if (!playerRepository.existsById(id)) {
            throw new NotFoundException("Player ", "ID", id);
        }else {
            return playerRepository.findById(id);
        }
    }

    @Transactional
    public Player updatePlayer (long id, Player player) {
        checkParams(player);
        if (!playerRepository.existsById(id)) throw new NotFoundException("Player", "ID", String.valueOf(id));

        Player updatedPlayer = playerRepository.findById(id).get();

        if (player.getName() != null)
            updatedPlayer.setName(player.getName());

        if (player.getTitle() != null)
            updatedPlayer.setTitle(player.getTitle());

        if (player.getBirthday() != null)
            updatedPlayer.setBirthday(player.getBirthday());

        if (player.getLevel() != null)
            updatedPlayer.setLevel(player.getLevel());

        if (player.getProfession() != null)
            updatedPlayer.setProfession(player.getProfession());

        if (player.getRace() != null)
            updatedPlayer.setRace(player.getRace());

        if (player.getBanned() != null)
            updatedPlayer.setBanned(player.getBanned());

        if (player.getExperience() != null)
            updatedPlayer.setExperience(player.getExperience());


        Integer level = calculateLevel(updatedPlayer);
        updatedPlayer.setLevel(level);
        Integer untilNextLevel =  calculateUntilNextLevel(updatedPlayer);
        updatedPlayer.setUntilNextLevel(untilNextLevel);
        return playerRepository.save(updatedPlayer);
    }

    @Transactional
    public void deletePlayer(long id) {
        if (!playerRepository.existsById(id)) throw new NotFoundException("Player", "ID", String.valueOf(id));
        playerRepository.deleteById(id);
    }

    public Specification<Player> nameLike (String name) {
        return (root, query, criteriaBuilder) -> name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }
    public Specification<Player> titleLike(String title) {
        return (root, query, criteriaBuilder) -> title == null ? null : criteriaBuilder.like(root.get("title"), "%" + title + "%");
    }

    public Specification<Player> professionLike(Profession profession) {
        return (root, query, criteriaBuilder) -> profession == null ? null : criteriaBuilder.equal(root.get("profession"), profession);
    }
    public Specification<Player> raceLike(Race race) {
        return (root, query, criteriaBuilder) -> race == null ? null : criteriaBuilder.equal(root.get("race"), race);
    }

    public Specification<Player> bannedFilter(Boolean isBanned) {
        return (root, query, criteriaBuilder) -> {
            if (isBanned == null) {
                return null;
            }
            if (isBanned) {
                return criteriaBuilder.isTrue(root.get("banned"));
            } else {
                return criteriaBuilder.isFalse(root.get("banned"));
            }
        };
    }

    public Specification<Player> levelFilter(Double min, Double max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("level"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), min);
            }
            return criteriaBuilder.between(root.get("level"), min, max);
        };
    }

    public Specification<Player> experienceFilter(Double min, Double max) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), max);
            }
            if (max == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), min);
            }
            return criteriaBuilder.between(root.get("experience"), min, max);
        };
    }

    public Specification<Player> dateFilter(Long after, Long before) {
        return (root, query, criteriaBuilder) -> {
            if (after == null && before == null) {
                return null;
            }
            if (after == null) {
                Date before1 = new Date(before);
                return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), before1);
            }
            if (before == null) {
                Date after1 = new Date(after);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), after1);
            }
            Date before1 = new Date(before - 3600001);
            Date after1 = new Date(after);
            return criteriaBuilder.between(root.get("birthday"), after1, before1);
        };
    }

    public long idCheck(String id) {
        if (id == null || id.equals("0") || id.equals("")) {
            throw new BadRequestException("Id value is not valid. ID = " + id );
        }
        try {
            Long checkedID = Long.parseLong(id);
            return checkedID;
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID is not a number");
        }
    }

    private void checkParams(Player player) {

        if (player.getName() != null && (player.getName().length() < 1 || player.getName().length() > 12)) {
            throw new BadRequestException("Name length is incorrect");
        }

        if (player.getTitle() != null && player.getTitle().length() > 30) {
            throw new BadRequestException("The title is incorrect");}

        if (player.getExperience() != null && (player.getExperience() < 0 || player.getExperience() > 10000000)) {
            throw new BadRequestException("The Experience size is incorrect");
        }

        if (player.getBirthday() != null) {
            Calendar date = Calendar.getInstance();
            date.setTime(player.getBirthday());
            if (date.get(Calendar.YEAR) < 2000 || date.get(Calendar.YEAR) > 3000) {
                throw new BadRequestException("The date of player Birthday is incorrect");
            }
        }
    }

    private Integer calculateLevel(Player playerRequired) {
        BigDecimal level = new BigDecimal((Math.sqrt(2500+200*playerRequired.getExperience())-50) / 100);
        return level.intValue();
    }

    private Integer calculateUntilNextLevel(Player playerRequired) {
        BigDecimal untilNextLevel = new BigDecimal(50 * (playerRequired.getLevel() + 1) * (playerRequired.getLevel()  + 2) - playerRequired.getExperience());
        return untilNextLevel.intValue();
    }

}
