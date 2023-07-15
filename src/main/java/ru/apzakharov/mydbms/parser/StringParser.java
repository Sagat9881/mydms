package ru.apzakharov.mydbms.parser;

import lombok.extern.log4j.Log4j2;
import ru.apzakharov.mydbms.exception.ParserException;
import ru.apzakharov.mydbms.query.*;
import ru.apzakharov.mydbms.utils.PredicateUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Log4j2
//TODO: реализовать парсинги
public class StringParser implements QueryParser<String> {


    final Function<String, Map<String, Object>> tokenValueArrToMap = tokenValue -> {
        Map<String, Object> row = new HashMap<>();
        final String[] split = tokenValue.split("=");
        if (split.length != 2) {
            throw ParserException.notAcceptedPattern(tokenValue);
        }
        row.put(split[0], split[1]);
        return row;
    };

    /**
     * Метод осуществляет сопоставленеие входной строки с конкретными объектами-запросами.
     *
     * @param input - объект, который нуно распарсить для  получения объекта-запроса
     * @return
     * @param <Q> - тип запроса
     */
    @Override
    public <Q extends Query> Q parseQuery(String input) {
        final String[] tokens = input.split(" ");
        final String startToken = tokens[0].toUpperCase();

        switch (startToken) {
            case "DELETE":
                return (Q) parseDelete(input);
            case "INSERT":
                return (Q) parseInsert(input);
            case "UPDATE":
                return (Q) parseUpdate(input);
            case "SELECT":
                return (Q) parseSelect(input);
            default:
                throw ParserException.notAcceptedStartToken(startToken);
        }
    }

    private SelectQuery parseSelect(String input) {
        return new SelectQuery();
    }

    private UpdateQuery parseUpdate(String input) {
        return new UpdateQuery();
    }

    private InsertQuery parseInsert(String input) {
        final String values = input.toLowerCase().split("values ")[1];
        final Map<String, Object> tokens = Arrays.stream(values.split(", "))
                .map(tokenValueArrToMap)
                .reduce((map, map1) -> {
                    map.putAll(map1);
                    return map;
                })
                .orElseThrow(() -> new ParserException("Не удалось распарсить Insert;\nЗапрос: [ " + input + "]"));

        return InsertQuery.builder().rows(Collections.singletonList(tokens)).build();
    }

    private DeleteQuery parseDelete(String input) {
        final String stringPredicate = input.toLowerCase().split("where ")[1];
        final Predicate<Map<String, Object>> predicate = PredicateUtils.buildPredicateFromString(stringPredicate);

        return DeleteQuery.builder().predicate(predicate).build();

    }
}
