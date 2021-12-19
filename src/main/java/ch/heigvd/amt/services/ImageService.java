package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateHandler;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.utils.ResourceLoader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@ApplicationScoped
public class ImageService {

  public static final int IMAGE_WIDTH = 250;
  public static final int IMAGE_HEIGTH = 300;
  private final Jdbi jdbi;
  private final UpdateHandler updateHandler;

  private static final Logger logger = Logger.getLogger(ImageService.class);

  @Inject
  public ImageService(Jdbi jdbi, UpdateHandler updateHandler) {
    this.jdbi = jdbi;
    this.updateHandler = updateHandler;
  }

  /**
   * Get an image
   *
   * @param imageId the id of the image
   * @return the image
   */
  public Optional<Image> getImage(int imageId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(ResourceLoader.loadResource("sql/image/get.sql"))
                .bind("id", imageId)
                .mapTo(Image.class)
                .findOne());
  }

  /**
   * Add an image to the database
   *
   * @param data the data of the image
   * @return The generated id set
   */
  private int addImageToDB(byte[] data) {
    try {
      return jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/image/add.sql"))
                  .bind("data", data)
                  .executeAndReturnGeneratedKeys()
                  .mapTo(int.class)
                  .one());
    } catch (UnableToExecuteStatementException e) {
      updateHandler.handleUpdateError(e);
    }
    throw new RuntimeException("Cannot be reached");
  }

  /**
   * Modify the image data
   *
   * @param data a byte array of the image data
   * @param id the id of the image
   */
  private void updateImageToDB(byte[] data, int id) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/image/update.sql"))
                  .bind("data", data)
                  .bind("id", id)
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      updateHandler.handleUpdateError(e);
    }
  }

  /**
   * @param imageData an array with the image data
   * @param id the id of the image to update
   */
  public void updateImage(byte[] imageData, int id) throws IOException {
    updateImageToDB(rescaleImage(imageData), id);
  }

  /**
   * @param imageData an array with the image data
   * @return the id of the new image
   * @throws IOException if an IO Exception occurs
   */
  public int addImage(byte[] imageData) throws IOException {
    return addImageToDB(rescaleImage(imageData));
  }

  /**
   * @param input the data of the image
   * @return the data of the image rescaled
   * @throws IOException if an IO Exception occurs
   */
  private byte[] rescaleImage(byte[] input) throws IOException {
    java.awt.Image image =
        ImageIO.read(new ByteArrayInputStream(input))
            .getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGTH, java.awt.Image.SCALE_DEFAULT);
    BufferedImage rescaledImage =
        new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGTH, BufferedImage.TYPE_INT_RGB);
    rescaledImage.getGraphics().drawImage(image, 0, 0, null);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(rescaledImage, "png", output);
    return output.toByteArray();
  }
}
