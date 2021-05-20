package com.game.repository;

import com.game.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PlayerRepository extends JpaSpecificationExecutor<Player>,
                                            PagingAndSortingRepository<Player, Long> {
}
