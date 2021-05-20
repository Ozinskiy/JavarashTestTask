package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import static org.springframework.data.jpa.domain.Specification.where;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PlayerService {

    private PlayerRepository playerRepository;

    @Autowired
    public PlayerService (PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }

    public List<Player> getPlayers(Map<String, String> categories){

        int pageSize = categories.containsKey("pageSize") ? Integer.parseInt(categories.remove("pageSize")) : 3;
        int pageNumber = categories.containsKey("pageNumber") ? Integer.parseInt(categories.remove("pageNumber")) : 0;
        String order = categories.containsKey("order") ? categories.remove("order") : "ID";
        PlayerOrder playerOrder = PlayerOrder.valueOf(order);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(playerOrder.getFieldName()));

        Page<Player> playerPage;

        if(!categories.isEmpty()){
            List<Filter> filters = filtersFromCategories(categories);
            Specification<Player> specification = getSpecificationFromFilters(filters);
            playerPage = playerRepository.findAll(specification, pageable);
        }
        else {
            playerPage = playerRepository.findAll(pageable);
        }

        if(playerPage.hasContent()){
            return playerPage.getContent();
        }
        else {
            return new ArrayList<>();
        }
    }

    public Integer getPlayersCount(Map<String, String> categories) {
        long playersCount;

        categories.remove("pageNumber");
        categories.remove("pageSize");
        categories.remove("order");

        if(!categories.isEmpty()) {
            List<Filter> filters = filtersFromCategories(categories);
            Specification<Player> spec = getSpecificationFromFilters(filters);
            playersCount = playerRepository.count(spec);
        } else {
            playersCount = playerRepository.count();
        }
        return (int) playersCount;
    }

    public Player createNewPlayer(Map<String, String> inputsMap) throws RuntimeException{
        if(inputsMap.isEmpty()){
            throw new RuntimeException();
        }

        nullFieldsValidation(inputsMap);
        inputFieldsValidation(inputsMap);

        Player createdPlayer = setPlayerData(new Player(), inputsMap);
        return playerRepository.save(createdPlayer);
    }

    public Player getPlayerById(Long id){
        if(id < 1){
            throw new RuntimeException();
        }

        if(playerRepository.existsById(id)){
            return playerRepository.findById(id).get();
        }
        else {
            return null;
        }
    }

    public boolean deletePlayerById(Long id){
        if(id < 1){
            throw new RuntimeException();
        }

        if(playerRepository.existsById(id)){
            playerRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }

    public Player updatePlayer(Long id, Map<String, String> inputsMap) throws RuntimeException{
        if(id <= 0){
            throw new RuntimeException();
        }
        Player player;
        Player updatedPlayer;
        if(playerRepository.existsById(id)){
            player = playerRepository.findById(id).get();
            inputFieldsValidation(inputsMap);
            updatedPlayer = setPlayerData(player, inputsMap);
            playerRepository.save(updatedPlayer);
            return updatedPlayer;
        }
        else {
            return null;
        }
    }



    private Player setPlayerData(Player player, Map<String, String> inputsMap){
        if(inputsMap.containsKey("name") && !inputsMap.get("name").equals("null")) {
            player.setName(inputsMap.get("name"));
        }

        if(inputsMap.containsKey("title") && !inputsMap.get("title").equals("null")) {
            player.setTitle(inputsMap.get("title"));
        }

        if(inputsMap.containsKey("race") && !inputsMap.get("race").equals("null")) {
            player.setRace(Race.valueOf(inputsMap.get("race")));
        }

        if(inputsMap.containsKey("profession") && !inputsMap.get("profession").equals("null")) {
            player.setProfession(Profession.valueOf(inputsMap.get("profession")));
        }

        if(inputsMap.containsKey("birthday") && !inputsMap.get("birthday").equals("null")) {
            player.setBirthday(new Date(Long.parseLong(inputsMap.get("birthday"))));
        }

        Boolean banned = inputsMap.get("banned") != null && (inputsMap.get("banned").equals("true"));
        player.setBanned(banned);

        if(inputsMap.containsKey("experience") && !inputsMap.get("experience").equals("null")) {
            Integer exp = Integer.parseInt(inputsMap.get("experience"));
            player.setExperience(exp);

            Integer lvl =(int) ((Math.sqrt(2500 + 200 * exp) - 50) / 100);
            player.setLevel(lvl);

            Integer untilNextLvl = 50 * (lvl + 1) * (lvl + 2) - exp;
            player.setUntilNextLevel(untilNextLvl);
        }

        return player;
    }

    private void nullFieldsValidation(Map<String, String> inputsMap) throws RuntimeException{
        for(Map.Entry<String, String> item : inputsMap.entrySet()){
            if(item.getValue() == null){
                if(!item.getKey().equals("banned")){
                    throw new RuntimeException();
                }
            }
        }
    }

    private void inputFieldsValidation(Map<String, String> inputsMap) throws RuntimeException{
        if (inputsMap.containsKey("name") && !inputsMap.get("name").equals("null")) {
            String name = inputsMap.get("name");
            if (name.trim().isEmpty() || name.length() > 12) {
                throw new RuntimeException("Incorrect name");
            }
        }

        if (inputsMap.containsKey("title") && !inputsMap.get("title").equals("null")) {
            String title = inputsMap.get("title");
            if (title.trim().isEmpty() || title.length() > 30) {
                throw new RuntimeException("Incorrect title");
            }
        }

        if (inputsMap.containsKey("birthday") && !inputsMap.get("birthday").equals("null")) {
            Long birthday = Long.parseLong(inputsMap.get("birthday"));
            if (birthday < 0) {
                throw new RuntimeException("Incorrect birthday");
            }
        }

        if (inputsMap.containsKey("experience") && !inputsMap.get("experience").equals("null")) {
            Integer experience = Integer.parseInt(inputsMap.get("experience"));
            if (experience < 0 || experience > 10000000) {
                throw new RuntimeException("Incorrect experience");
            }
        }
    }

    private List<Filter> filtersFromCategories(Map<String, String> categories) throws RuntimeException{
        List<Filter> filters = new ArrayList<>();
        Filter filter;

        for (Map.Entry<String,String> category : categories.entrySet()){
            String fieldName = category.getKey();
            String fieldValue = category.getValue();

            if (!fieldValue.equals("null")) {
                filter = new Filter();
                filter.setField(fieldName);
                filter.setValue(fieldValue);

            switch (fieldName) {
                case "name":
                case "title":
                    filter.setParameter(RequestParameter.LIKE);
                    break;
                case "race":
                case "profession":
                case "banned":
                    filter.setParameter(RequestParameter.EQUALS);
                    break;
                case "after":
                    filter.setField("birthday");
                    filter.setParameter(RequestParameter.AFTER);
                    break;
                case "before":
                    filter.setField("birthday");
                    filter.setParameter(RequestParameter.BEFORE);
                    break;
                case "minExperience":
                    filter.setField("experience");
                    filter.setParameter(RequestParameter.BIGGER_THAN);
                    break;
                case "maxExperience":
                    filter.setField("experience");
                    filter.setParameter(RequestParameter.LESS_THAN);
                    break;
                case "minLevel":
                    filter.setField("level");
                    filter.setParameter(RequestParameter.BIGGER_THAN);
                    break;
                case "maxLevel":
                    filter.setField("level");
                    filter.setParameter(RequestParameter.LESS_THAN);
                    break;
                default:
                    throw new RuntimeException("It's impossible to create a filter for the field " + fieldName);
            }

            filters.add(filter);
        }
    }
        return filters;
    }

    private Specification<Player> getSpecificationFromFilters(List<Filter> filters) throws RuntimeException{
        Specification<Player> specification =
                where (createSpecification(filters.remove(0))) ;
        for (Filter filter : filters) {
            specification = specification.and(createSpecification(filter));
        }
        return specification;
    }

    private Object castToRequiredType(Class fieldType, String value) {
        if(fieldType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if(fieldType.isAssignableFrom(Boolean.class)){
            return Boolean.parseBoolean(value);
        } else if(Enum.class.isAssignableFrom(fieldType)){
            return Enum.valueOf(fieldType, value);
        }
        return value;
    }

    private Specification<Player> createSpecification(Filter input) throws RuntimeException{
        switch (input.getParameter()){
            case EQUALS:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get(input.getField()),
                                castToRequiredType(root.get(input.getField()).getJavaType(), input.getValue()));
            case LIKE:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.like(root.get(input.getField()), "%"+input.getValue()+"%");
            case AFTER:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.greaterThan(root.get(input.getField()), new Date(Long.parseLong(input.getValue())));
            case BEFORE:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.lessThan(root.get(input.getField()), new Date(Long.parseLong(input.getValue())));
            case BIGGER_THAN:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.ge(root.get(input.getField()),
                                (Number) castToRequiredType(root.get(input.getField()).getJavaType(), input.getValue()));
            case LESS_THAN:
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.le(root.get(input.getField()),
                                (Number) castToRequiredType(root.get(input.getField()).getJavaType(), input.getValue()));
            default:
                throw new RuntimeException();
        }
    }






}
