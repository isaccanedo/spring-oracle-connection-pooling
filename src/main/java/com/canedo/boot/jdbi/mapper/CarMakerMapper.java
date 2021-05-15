package com.canedo.boot.jdbi.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

import com.canedo.boot.jdbi.domain.CarMaker;
import com.canedo.boot.jdbi.domain.CarModel;

@Component
public class CarMakerMapper implements RowMapper<CarMaker> {

    @Override
    public CarMaker map(ResultSet rs, StatementContext ctx) throws SQLException {
        CarMaker maker = CarMaker.builder()
          .id(rs.getLong("id"))
          .name(rs.getString("name"))
          .models(new ArrayList<CarModel>())
          .build();

        return maker;
    }
}
