package ch.heigvd.amt.service;

import ch.heigvd.amt.database.PostgisResource;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.services.ImageService;
import com.google.common.io.Resources;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgisResource.class)
class ImageServiceTest {

  @Inject DataSource dataSource;

  @Inject ImageService imageService;

  @BeforeEach
  void setupEach() {
    PostgisResource.runQuery(dataSource, "sql/reset_db.sql");
  }

  @Test
  void addAndGet() throws IOException {
    byte[] image = Resources.toByteArray(Resources.getResource("test.png"));

    var res = imageService.addImage(image);

    // Test if resize is valid
    Image imageResult = imageService.getImage(res).orElseThrow();
    BufferedImage data = ImageIO.read(new ByteArrayInputStream(imageResult.getData()));
    Assertions.assertEquals(ImageService.IMAGE_WIDTH, data.getWidth());
    Assertions.assertEquals(ImageService.IMAGE_HEIGTH, data.getHeight());
  }

  @Test
  void updateImage() throws IOException {
    byte[] image = Resources.toByteArray(Resources.getResource("test.png"));
    var defaultImage = imageService.getImage(Image.DEFAULT_IMAGE_ID).orElseThrow();
    Assertions.assertEquals(0, defaultImage.getData().length);

    Image finalDefaultImage = defaultImage;
    Assertions.assertDoesNotThrow(() -> imageService.updateImage(image, finalDefaultImage.getId()));
    defaultImage = imageService.getImage(Image.DEFAULT_IMAGE_ID).orElseThrow();
    Assertions.assertNotEquals(0, defaultImage.getData().length);
  }
}
