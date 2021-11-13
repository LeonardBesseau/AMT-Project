package ch.heigvd.amt.service;

import ch.heigvd.amt.database.PostgisResource;
import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.services.CategoryService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgisResource.class)
class CategoryServiceTest {

  @Inject DataSource dataSource;

  @Inject CategoryService categoryService;

  @BeforeEach
  void setupEach() {
    PostgisResource.runQuery(
        dataSource, "sql/init_db.sql", "sql/reset_db.sql", "sql/insert_product.sql");
  }

  @Test
  void getElementDoesNotExist() {
    Assertions.assertTrue(categoryService.getCategory("Z").isEmpty());
  }

  @Test
  void getElementExist() {
    Optional<Category> result1 = categoryService.getCategory("A");
    Assertions.assertTrue(result1.isPresent());
    Category p1 = result1.get();
    Assertions.assertEquals("A", p1.getName());
  }

  @Test
  void getAllWithDataInTable() {
    List<Category> result1 = categoryService.getAllCategory();
    Assertions.assertEquals(2, result1.size());
  }

  @Test
  void getAllWithNoDataInTable() {
    PostgisResource.runQuery(dataSource, "sql/reset_db.sql");
    List<Category> result2 = categoryService.getAllCategory();
    Assertions.assertTrue(result2.isEmpty());
  }

  @Test
  void delete() {
    List<Category> result1 = categoryService.getAllCategory();
    Assertions.assertEquals(2, result1.size());
    categoryService.deleteCategory("A");
    result1 = categoryService.getAllCategory();
    Assertions.assertEquals(1, result1.size());
  }

  @Test
  void add() {
    List<Category> result1 = categoryService.getAllCategory();
    Assertions.assertEquals(2, result1.size());

    Assertions.assertEquals(UpdateResult.DUPLICATE, categoryService.addCategory(new Category("A")));

    Assertions.assertEquals(UpdateResult.SUCCESS, categoryService.addCategory(new Category("C")));
    List<Category> result2 = categoryService.getAllCategory();
    Assertions.assertEquals(3, result2.size());
  }
}
