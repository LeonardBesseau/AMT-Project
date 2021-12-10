package ch.heigvd.amt.services.mapper;

import ch.heigvd.amt.models.Category;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class CategoryColumnMapper implements ColumnMapper<Category> {
  @Override
  public Category map(ResultSet rs, int col, StatementContext ctx) throws SQLException {
    return new Category(rs.getString(col));
  }
}
